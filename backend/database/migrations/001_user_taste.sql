-- Gu của từng người dùng, học từ lịch sử quẹt (thuật toán gợi ý cá nhân hoá).
-- Idempotent: chạy lại nhiều lần không lỗi. Dùng cho DB đã có dữ liệu (VPS).
CREATE TABLE IF NOT EXISTS user_taste (
  user_id          BIGINT UNSIGNED PRIMARY KEY,
  swipe_count      INT UNSIGNED  NOT NULL DEFAULT 0,  -- số lượt quẹt lúc tính
  like_count       INT UNSIGNED  NOT NULL DEFAULT 0,
  age_mean         DECIMAL(5,2)  NULL,                -- tuổi trung bình của người đã like
  age_std          DECIMAL(5,2)  NULL,
  distance_mean    DECIMAL(7,2)  NULL,                -- khoảng cách trung bình (km) của người đã like
  interest_affinity JSON         NULL,                -- {"<interestId>": độ ưa thích -1..1}
  weights          JSON          NULL,                -- trọng số cá nhân {"interests":..,"distance":..,"age":..}
  updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_taste_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
