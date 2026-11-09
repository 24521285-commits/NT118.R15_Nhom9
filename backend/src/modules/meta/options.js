/**
 * DANH MỤC LỰA CHỌN - khớp với các màn hình onboarding của app Android.
 * DB lưu MÃ (code) tiếng Anh; app hiển thị NHÃN (label) tiếng Việt.
 * API nhận cả mã lẫn nhãn (VD "never" hoặc "Không hút thuốc"), luôn trả về mã.
 * App lấy danh sách đầy đủ qua GET /api/meta/options.
 *
 * Thêm / sửa lựa chọn: chỉ sửa file này (cột DB là VARCHAR nên không cần migration).
 */
const OPTIONS = {
  gender: [
    ['male', 'Nam'],
    ['female', 'Nữ'],
    ['other', 'Phi nhị nguyên'],
  ],
  interestedIn: [
    ['male', 'Nam'],
    ['female', 'Nữ'],
    ['other', 'Người phi nhị nguyên'],
    ['everyone', 'Mọi giới tính'],
  ],
  relationshipGoal: [
    ['serious', 'Mối quan hệ nghiêm túc'],
    ['long_term', 'Tìm hiểu để tiến tới mối quan hệ lâu dài'],
    ['dating', 'Hẹn hò và tìm hiểu'],
    ['marriage', 'Kết hôn'],
    ['friendship', 'Kết bạn'],
    ['meet_new_people', 'Gặp gỡ và kết nối người mới'],
    ['casual', 'Mối quan hệ không ràng buộc'],
    ['not_sure', 'Chưa xác định, cứ tìm hiểu trước'],
  ],
  zodiac: [
    ['aries', 'Bạch Dương'],
    ['taurus', 'Kim Ngưu'],
    ['gemini', 'Song Tử'],
    ['cancer', 'Cự Giải'],
    ['leo', 'Sư Tử'],
    ['virgo', 'Xử Nữ'],
    ['libra', 'Thiên Bình'],
    ['scorpio', 'Bọ Cạp'],
    ['sagittarius', 'Nhân Mã'],
    ['capricorn', 'Ma Kết'],
    ['aquarius', 'Bảo Bình'],
    ['pisces', 'Song Ngư'],
  ],
  smoking: [
    ['never', 'Không hút thuốc'],
    ['social', 'Chỉ hút trong một số dịp'],
    ['sometimes', 'Thỉnh thoảng hút thuốc'],
    ['regularly', 'Hút thuốc thường xuyên'],
  ],
  drinking: [
    ['never', 'Không uống'],
    ['special_occasions', 'Chỉ uống trong các dịp đặc biệt'],
    ['sometimes', 'Thỉnh thoảng uống'],
    ['often', 'Uống khá thường xuyên'],
  ],
  exercise: [
    ['very_active', 'Tập luyện thường xuyên'],
    ['weekly', 'Tập vài lần mỗi tuần'],
    ['sometimes', 'Thỉnh thoảng tập luyện'],
    ['rarely', 'Hiếm khi tập luyện'],
    ['never', 'Không tập luyện'],
  ],
  education: [
    ['high_school_student', 'Đang học trung học phổ thông'],
    ['high_school', 'Tốt nghiệp trung học phổ thông'],
    ['vocational', 'Trung cấp hoặc cao đẳng nghề'],
    ['college', 'Cao đẳng'],
    ['university_student', 'Đang học đại học'],
    ['bachelor', 'Đại học'],
    ['postgraduate', 'Sau đại học / Thạc sĩ'],
    ['phd', 'Tiến sĩ'],
  ],
};

/** 20 sở thích trên màn hình Interests của app (thứ tự như app). */
const APP_INTERESTS = [
  'Du lịch', 'Âm nhạc', 'Xem phim và series', 'Đọc sách', 'Nấu ăn',
  'Chơi game', 'Thể thao', 'Tập gym', 'Cà phê', 'Ẩm thực',
  'Nhiếp ảnh', 'Nghệ thuật', 'Công nghệ', 'Thú cưng', 'Thiên nhiên và cắm trại',
  'Thời trang', 'Khiêu vũ', 'Khám phá địa điểm mới', 'Hoạt động ngoài trời', 'Tình nguyện',
];

/** Giới hạn khớp với app (SeekBar / EditText). */
const LIMITS = {
  heightCm: { min: 140, max: 200 },
  weightKg: { min: 35, max: 120 },
  maxDistanceKm: { min: 1, max: 100 },
  age: { min: 18, max: 100 },
  displayName: { min: 2, max: 100 },
  bio: { max: 300 },
  password: { min: 8, max: 72 },
  photos: { min: 2, max: 6 },
  interests: { min: 3, max: 20 },
};

// Chuẩn hoá để so nhãn không phân biệt hoa thường / khoảng trắng thừa
const norm = (s) => String(s).normalize('NFC').trim().replace(/\s+/g, ' ').toLowerCase();

const lookup = {};
for (const [key, list] of Object.entries(OPTIONS)) {
  lookup[key] = new Map();
  for (const [code, label] of list) {
    lookup[key].set(norm(code), code);
    lookup[key].set(norm(label), code);
  }
}

/** Đổi mã hoặc nhãn -> mã. Không khớp trả về undefined. */
function toCode(key, value) {
  if (value == null) return undefined;
  return lookup[key].get(norm(value));
}

function codes(key) {
  return OPTIONS[key].map(([code]) => code);
}

function labelOf(key, code) {
  const found = OPTIONS[key].find(([c]) => c === code);
  return found ? found[1] : null;
}

/** Dạng trả cho app: { key: [{ code, label }] } */
function asJson() {
  return Object.fromEntries(
    Object.entries(OPTIONS).map(([key, list]) => [key, list.map(([code, label]) => ({ code, label }))]),
  );
}

module.exports = { OPTIONS, APP_INTERESTS, LIMITS, toCode, codes, labelOf, asJson, norm };
