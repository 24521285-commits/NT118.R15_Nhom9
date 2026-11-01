/**
 * HỌC GU NGƯỜI DÙNG TỪ LỊCH SỬ QUẸT (Mức 1 + Mức 2 của thuật toán cá nhân hoá)
 *
 * Dữ liệu: bảng swipes (like / superlike = thích, pass = không thích).
 * Kết quả lưu ở bảng user_taste, tính lại sau mỗi RECOMPUTE_EVERY lượt quẹt.
 *
 * Mức 1 - Gu: tuổi trung bình & độ lệch, khoảng cách trung bình của người đã like,
 *         độ ưa thích từng sở thích (sở thích nào xuất hiện ở người mình like nhiều hơn mức bình thường).
 * Mức 2 - Trọng số riêng: với mỗi yếu tố (sở thích chung, khoảng cách, tuổi), so độ phù hợp trung bình
 *         của người đã like với người đã pass. Chênh lệch càng lớn -> yếu tố đó càng quan trọng với người này.
 */
const db = require('../../config/db');
const matching = require('./matching');

const RECOMPUTE_EVERY = 10;
const HISTORY_LIMIT = 500; // chỉ học từ 500 lượt quẹt gần nhất (gu có thể thay đổi)
const SMOOTHING = 5; // làm mượt cho sở thích ít dữ liệu
const FULL_CONFIDENCE_LIKES = 10; // dưới 10 like thì giảm mức tin vào độ ưa thích sở thích
const MIN_SAMPLES_FOR_WEIGHTS = 3; // cần ≥ 3 like và ≥ 3 pass mới học trọng số

const round = (x, d = 2) => (x == null ? null : Math.round(x * 10 ** d) / 10 ** d);
const mean = (a) => (a.length ? a.reduce((s, x) => s + x, 0) / a.length : null);
const std = (a) => {
  if (a.length < 2) return null;
  const m = mean(a);
  return Math.sqrt(a.reduce((s, x) => s + (x - m) ** 2, 0) / (a.length - 1));
};

/**
 * Hàm thuần (không đụng DB) - dễ test.
 * @param swipes [{ liked: bool, age, distanceKm, interestIds: number[] }]
 * @param me     { age, interestIds: number[], radiusKm }
 */
function learnTaste(swipes, me) {
  const n = swipes.length;
  const liked = swipes.filter((s) => s.liked);
  const passed = swipes.filter((s) => !s.liked);

  // ── Mức 1: gu ──
  const likedAges = liked.map((s) => s.age).filter((x) => x != null);
  const likedDist = liked.map((s) => s.distanceKm).filter((x) => x != null);

  const p = n ? liked.length / n : 0; // tỉ lệ like chung
  const stats = new Map(); // interestId -> { seen, liked }
  for (const s of swipes) {
    for (const id of s.interestIds || []) {
      const st = stats.get(id) || { seen: 0, liked: 0 };
      st.seen += 1;
      if (s.liked) st.liked += 1;
      stats.set(id, st);
    }
  }
  const affinity = [];
  const confidence = Math.min(1, liked.length / FULL_CONFIDENCE_LIKES);
  if (p > 0 && p < 1) {
    for (const [id, st] of stats) {
      if (st.seen < 3) continue;
      // Tỉ lệ like khi người kia có sở thích này (có làm mượt) so với tỉ lệ like chung, chuẩn hoá về -1..1
      const diff = (st.liked + SMOOTHING * p) / (st.seen + SMOOTHING) - p;
      const a = (diff > 0 ? diff / (1 - p) : diff / p) * confidence;
      if (Math.abs(a) >= 0.1) affinity.push([id, round(a)]);
    }
  }
  affinity.sort((x, y) => Math.abs(y[1]) - Math.abs(x[1]));

  // ── Mức 2: trọng số riêng ──
  let weights = null;
  if (liked.length >= MIN_SAMPLES_FOR_WEIGHTS && passed.length >= MIN_SAMPLES_FOR_WEIGHTS) {
    const myInterests = new Set(me.interestIds || []);
    const meCtx = { age: me.age, interestCount: myInterests.size, radiusKm: me.radiusKm };
    const fitOf = (s) =>
      matching.fits(
        {
          ...s,
          commonInterests: (s.interestIds || []).filter((id) => myInterests.has(id)).length,
          lastActiveAt: null,
          interestCount: 0,
        },
        meCtx,
      );
    const fl = liked.map(fitOf);
    const fp = passed.map(fitOf);

    const raw = {};
    let rawSum = 0;
    let defSum = 0;
    for (const k of matching.ADJUSTABLE) {
      const d = mean(fl.map((f) => f[k])) - mean(fp.map((f) => f[k])); // -1..1
      raw[k] = matching.DEFAULT_WEIGHTS[k] * Math.min(2.5, Math.max(0.3, 1 + 3 * d));
      rawSum += raw[k];
      defSum += matching.DEFAULT_WEIGHTS[k];
    }
    // Giữ tổng trọng số = 100 (chỉ phân bổ lại giữa các yếu tố có thể điều chỉnh)
    weights = { ...matching.DEFAULT_WEIGHTS };
    for (const k of matching.ADJUSTABLE) weights[k] = round((raw[k] / rawSum) * defSum, 1);
  }

  const taste = {
    swipeCount: n,
    likeCount: liked.length,
    ageMean: round(mean(likedAges)),
    ageStd: round(std(likedAges)),
    distanceMean: round(mean(likedDist)),
    interestAffinity: Object.fromEntries(affinity.slice(0, 20)),
    weights,
  };

  // Tỉ trọng 3 phần của điểm gu: phần nào phân biệt like / pass rõ nhất thì chiếm nhiều nhất
  if (weights) {
    const tl = liked.map((s) => matching.tasteFits(s, taste));
    const tp = passed.map((s) => matching.tasteFits(s, taste));
    const d = {};
    let sum = 0;
    for (const k of ['age', 'distance', 'interests']) {
      d[k] = Math.max(0.05, mean(tl.map((x) => x[k])) - mean(tp.map((x) => x[k])));
      sum += d[k];
    }
    weights.mix = Object.fromEntries(Object.entries(d).map(([k, v]) => [k, round(v / sum)]));
  }
  return taste;
}

/** Đọc lịch sử quẹt từ DB rồi học gu. */
async function computeTaste(userId) {
  const [me] = await db.query(
    `SELECT p.latitude, p.longitude, TIMESTAMPDIFF(YEAR, p.birthday, UTC_DATE()) AS age, pr.max_distance_km
       FROM profiles p LEFT JOIN preferences pr ON pr.user_id = p.user_id WHERE p.user_id = ?`,
    [userId],
  );
  if (!me) return null;
  const hasLoc = me.latitude != null && me.longitude != null;
  const distanceExpr = hasLoc
    ? `CASE WHEN p.latitude IS NULL THEN NULL
       ELSE ST_Distance_Sphere(POINT(p.longitude, p.latitude), POINT(${Number(me.longitude)}, ${Number(me.latitude)})) / 1000 END`
    : 'NULL';

  const rows = await db.query(
    `SELECT s.action,
            TIMESTAMPDIFF(YEAR, p.birthday, UTC_DATE()) AS age,
            ${distanceExpr} AS distance_km,
            (SELECT GROUP_CONCAT(ui.interest_id) FROM user_interests ui WHERE ui.user_id = s.swiped_id) AS interest_ids
       FROM swipes s JOIN profiles p ON p.user_id = s.swiped_id
      WHERE s.swiper_id = ?
      ORDER BY s.created_at DESC
      LIMIT ?`,
    [userId, HISTORY_LIMIT],
  );
  const myInterests = (await db.query('SELECT interest_id FROM user_interests WHERE user_id = ?', [userId])).map(
    (r) => Number(r.interest_id),
  );

  return learnTaste(
    rows.map((r) => ({
      liked: r.action !== 'pass',
      age: r.age == null ? null : Number(r.age),
      distanceKm: r.distance_km == null ? null : Number(r.distance_km),
      interestIds: parseIds(r.interest_ids),
    })),
    { age: Number(me.age), interestIds: myInterests, radiusKm: me.max_distance_km },
  );
}

function parseIds(csv) {
  return csv ? String(csv).split(',').map(Number).filter(Number.isInteger) : [];
}

async function saveTaste(userId, t) {
  await db.query(
    `INSERT INTO user_taste
       (user_id, swipe_count, like_count, age_mean, age_std, distance_mean, interest_affinity, weights)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?)
     ON DUPLICATE KEY UPDATE swipe_count = VALUES(swipe_count), like_count = VALUES(like_count),
       age_mean = VALUES(age_mean), age_std = VALUES(age_std), distance_mean = VALUES(distance_mean),
       interest_affinity = VALUES(interest_affinity), weights = VALUES(weights)`,
    [
      userId,
      t.swipeCount,
      t.likeCount,
      t.ageMean,
      t.ageStd,
      t.distanceMean,
      JSON.stringify(t.interestAffinity || {}),
      t.weights ? JSON.stringify(t.weights) : null,
    ],
  );
}

// mysql2 trả cột JSON dạng object (MySQL) hoặc string (MariaDB) -> xử lý cả hai
const parseJson = (v) => (v == null ? null : typeof v === 'string' ? JSON.parse(v) : v);

/** Gu đã lưu (null nếu chưa có). */
async function getTaste(userId) {
  let row;
  try {
    [row] = await db.query('SELECT * FROM user_taste WHERE user_id = ?', [userId]);
  } catch (err) {
    // Chưa chạy migration -> vẫn phục vụ bằng thuật toán mặc định thay vì lỗi 500
    if (err.code === 'ER_NO_SUCH_TABLE') {
      console.warn('[taste] Thiếu bảng user_taste - hãy chạy: npm run db:migrate');
      return null;
    }
    throw err;
  }
  if (!row) return null;
  return {
    swipeCount: Number(row.swipe_count),
    likeCount: Number(row.like_count),
    ageMean: row.age_mean == null ? null : Number(row.age_mean),
    ageStd: row.age_std == null ? null : Number(row.age_std),
    distanceMean: row.distance_mean == null ? null : Number(row.distance_mean),
    interestAffinity: parseJson(row.interest_affinity) || {},
    weights: parseJson(row.weights),
    updatedAt: row.updated_at,
  };
}

/** Gọi sau mỗi lượt quẹt: tính lại gu nếu đã có thêm ≥ RECOMPUTE_EVERY lượt quẹt. */
async function maybeUpdate(userId) {
  const [{ total }] = await db.query('SELECT COUNT(*) AS total FROM swipes WHERE swiper_id = ?', [userId]);
  const current = await getTaste(userId);
  if (Number(total) - (current?.swipeCount || 0) < RECOMPUTE_EVERY) return current;
  const t = await computeTaste(userId);
  if (t) await saveTaste(userId, t);
  return t;
}

/** Tính lại ngay và lưu (dùng cho API /home/taste và script giả lập). */
async function refresh(userId) {
  const t = await computeTaste(userId);
  if (t) await saveTaste(userId, t);
  return t;
}

module.exports = { learnTaste, computeTaste, getTaste, maybeUpdate, refresh, parseIds, RECOMPUTE_EVERY };
