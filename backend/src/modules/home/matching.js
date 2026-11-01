/**
 * THUẬT TOÁN CHẤM ĐIỂM PHÙ HỢP (matchScore 0 - 100) - CÓ CÁ NHÂN HOÁ
 *
 * Bước 1 (SQL - home.service.js): LỌC ứng viên hợp lệ
 *   giới tính 2 chiều, độ tuổi, khoảng cách, chưa quẹt, không chặn nhau.
 * Bước 2 (file này): CHẤM ĐIỂM từng ứng viên rồi sắp xếp giảm dần.
 *
 * ── Điểm cơ bản (giống nhau cho mọi người dùng mới) ─────────────────────
 *   Yếu tố              Trọng số mặc định   Độ phù hợp (0..1)
 *   Sở thích chung             35          tỉ lệ sở thích của mình mà người kia cũng có
 *   Khoảng cách                25          càng gần càng cao, về 0 ở rìa bán kính
 *   Mức độ hoạt động           20          online ≤ 1 ngày: 1, ≤ 7 ngày: 0.6, ≤ 30 ngày: 0.25
 *   Tuổi gần nhau              10          chênh 0 tuổi: 1 → chênh ≥ 10 tuổi: 0
 *   Hồ sơ đầy đủ               10          có ảnh 0.4, có bio 0.3, có sở thích 0.3
 *
 * ── Cá nhân hoá (taste.js học từ lịch sử quẹt) ──────────────────────────
 *   Mức 1 - Điểm gu (tasteScore): ứng viên giống những người mình đã like tới đâu
 *           (độ tuổi hay like, khoảng cách hay like, sở thích hay like),
 *           tỉ trọng 3 phần cũng được học riêng cho từng người.
 *   Mức 2 - Trọng số riêng: yếu tố nào thật sự ảnh hưởng tới quyết định like của
 *           người dùng thì được tăng trọng số, yếu tố không ảnh hưởng thì giảm.
 *
 *   matchScore = (1 − α) · điểm cơ bản(trọng số đã cá nhân hoá) + α · điểm gu
 *   α (mức cá nhân hoá) = 0 khi < 20 lượt quẹt (cold start), tăng dần tới 0.7 ở 50 lượt.
 *
 *   + Ưu tiên ẩn: người đã like mình được cộng LIKED_ME_BOOST khi SẮP XẾP
 *     (không cộng vào matchScore trả về, để không lộ ai đã like mình).
 */
const DEFAULT_WEIGHTS = Object.freeze({
  interests: 35,
  distance: 25,
  activity: 20,
  age: 10,
  completeness: 10,
});
/** Các yếu tố được phép cá nhân hoá trọng số (activity & completeness giữ nguyên). */
const ADJUSTABLE = ['interests', 'distance', 'age'];
const EQUAL_MIX = Object.freeze({ age: 1 / 3, distance: 1 / 3, interests: 1 / 3 });

const MIN_SWIPES = 20; // dưới mức này dùng thuật toán mặc định (chưa đủ dữ liệu)
const FULL_SWIPES = 50; // từ mức này α đạt tối đa
const MAX_ALPHA = 0.7; // luôn giữ ≥ 30% điểm cơ bản để kết quả không bị "đóng khung"
const LIKED_ME_BOOST = 8;
const DEFAULT_RADIUS_KM = 100;
const DAY = 24 * 3600 * 1000;

const clamp = (x, lo, hi) => Math.min(hi, Math.max(lo, x));

// ── Độ phù hợp từng yếu tố (0..1) ───────────────────────────────────────
function interestFit(common, myInterestCount) {
  if (!myInterestCount) return 0.5; // mình chưa chọn sở thích -> trung lập
  return Math.min(1, common / Math.min(myInterestCount, 5));
}

function distanceFit(distanceKm, radiusKm) {
  if (distanceKm == null) return 0.4; // chưa có vị trí -> thấp hơn trung bình
  return Math.max(0, 1 - distanceKm / (radiusKm || DEFAULT_RADIUS_KM));
}

function activityFit(lastActiveAt) {
  if (!lastActiveAt) return 0;
  const ago = Date.now() - new Date(lastActiveAt).getTime();
  if (ago <= DAY) return 1;
  if (ago <= 7 * DAY) return 0.6;
  if (ago <= 30 * DAY) return 0.25;
  return 0;
}

function ageFit(candidateAge, myAge) {
  if (candidateAge == null || myAge == null) return 0;
  return Math.max(0, 1 - Math.abs(candidateAge - myAge) / 10);
}

function completenessFit({ hasPhoto, hasBio, interestCount }) {
  return (hasPhoto ? 0.4 : 0) + (hasBio ? 0.3 : 0) + (interestCount > 0 ? 0.3 : 0);
}

/** Độ phù hợp của 1 ứng viên theo từng yếu tố (dùng chung cho chấm điểm & học trọng số). */
function fits(c, me) {
  return {
    interests: interestFit(c.commonInterests, me.interestCount),
    distance: distanceFit(c.distanceKm, me.radiusKm),
    activity: activityFit(c.lastActiveAt),
    age: ageFit(c.age, me.age),
    completeness: completenessFit(c),
  };
}

// ── Cá nhân hoá ─────────────────────────────────────────────────────────
/** Mức cá nhân hoá α theo số lượt quẹt. */
function personalizationLevel(swipeCount) {
  if (!swipeCount || swipeCount < MIN_SWIPES) return 0;
  const t = (swipeCount - MIN_SWIPES) / (FULL_SWIPES - MIN_SWIPES);
  return Math.round(clamp(0.2 + t * (MAX_ALPHA - 0.2), 0.2, MAX_ALPHA) * 100) / 100;
}

/** Trộn trọng số mặc định và trọng số riêng theo α. */
function blendWeights(personal, alpha) {
  if (!personal || !alpha) return { ...DEFAULT_WEIGHTS };
  const w = {};
  for (const k of Object.keys(DEFAULT_WEIGHTS)) {
    w[k] = (1 - alpha) * DEFAULT_WEIGHTS[k] + alpha * (personal[k] ?? DEFAULT_WEIGHTS[k]);
  }
  return w;
}

/**
 * Mức 1 - Độ hợp gu theo 3 phần (0..1): ứng viên giống những người mình đã like tới đâu.
 * taste: { ageMean, ageStd, distanceMean, interestAffinity: { [interestId]: -1..1 } }
 */
function tasteFits(c, taste) {
  // Tuổi: phân phối chuẩn quanh tuổi trung bình mình hay like
  let age = 0.5;
  if (taste.ageMean != null && c.age != null) {
    const sd = Math.max(3, Number(taste.ageStd) || 0);
    age = Math.exp(-((c.age - taste.ageMean) ** 2) / (2 * sd * sd));
  }
  // Khoảng cách: trong khoảng mình hay like -> 1, xa hơn thì giảm dần
  let distance = 0.5;
  if (taste.distanceMean != null && c.distanceKm != null) {
    const m = Number(taste.distanceMean);
    distance = c.distanceKm <= m ? 1 : Math.max(0, 1 - (c.distanceKm - m) / (m + 10));
  }
  // Sở thích: cộng độ ưa thích của các sở thích ứng viên có
  let interests = 0.5;
  const aff = taste.interestAffinity || {};
  if (c.interestIds?.length) {
    const sum = c.interestIds.reduce((s, id) => s + (Number(aff[id]) || 0), 0);
    interests = (clamp(sum, -1, 1) + 1) / 2;
  }
  return { age, distance, interests };
}

/**
 * Điểm gu (0..100). Tỉ trọng 3 phần (taste.weights.mix) được học riêng cho từng người:
 * phần nào phân biệt người mình like / pass rõ nhất thì chiếm nhiều nhất.
 */
function tasteScore(c, taste) {
  const t = tasteFits(c, taste);
  const mix = taste.weights?.mix || EQUAL_MIX;
  return 100 * (mix.age * t.age + mix.distance * t.distance + mix.interests * t.interests);
}

/**
 * @param c     ứng viên: { distanceKm, commonInterests, interestIds, lastActiveAt, age,
 *                          hasPhoto, hasBio, interestCount, likedMe }
 * @param me    người xem: { age, interestCount, radiusKm }
 * @param taste gu đã học (taste.js) hoặc null
 * @returns { matchScore, rankScore }
 */
function score(c, me, taste = null) {
  const alpha = taste ? personalizationLevel(taste.swipeCount) : 0;
  const w = blendWeights(taste?.weights, alpha);
  const f = fits(c, me);

  let base = 0;
  for (const k of Object.keys(w)) base += w[k] * f[k];

  const total = alpha ? (1 - alpha) * base + alpha * tasteScore(c, taste) : base;
  const matchScore = Math.round(clamp(total, 0, 100));
  return { matchScore, rankScore: total + (c.likedMe ? LIKED_ME_BOOST : 0) };
}

module.exports = {
  score,
  fits,
  tasteScore,
  tasteFits,
  personalizationLevel,
  blendWeights,
  DEFAULT_WEIGHTS,
  ADJUSTABLE,
  MIN_SWIPES,
  LIKED_ME_BOOST,
  DEFAULT_RADIUS_KM,
};
