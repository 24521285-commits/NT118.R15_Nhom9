# Tích hợp API: Khám phá

Đã nối `GET /home/feed`, `GET /home/search`, `GET /users/interests` và
`POST /home/swipe`. Trang Khám phá hiển thị hồ sơ/ảnh từ server, có trạng thái
đang tải, danh sách rỗng, lỗi và chạm thẻ để thử lại. Like chỉ hiện match khi
response có `data.matched = true`. Nếu request lỗi, giữ lại hồ sơ để thử lại.
Hồ sơ chi tiết và album mở từ Khám phá dùng dữ liệu thật của cùng hồ sơ.

## Ghép với phần đăng nhập do thành viên khác làm

Không thay đổi đăng nhập, OTP, đăng ký hoặc backend. Sau khi login/register
thành công, lưu **cả hai token** trước khi mở HomeActivity:

```java
new SessionStore(this).saveTokens(
    response.getJSONObject("data").getJSONObject("tokens")
);
```

Import `com.datezone.app.api.SessionStore`. `response` ở ví dụ là JSONObject
của toàn bộ response backend. Nếu dùng Retrofit/model riêng, tạo JSONObject
chứa `accessToken` và `refreshToken` từ model rồi gọi cùng phương thức.
Logout cần gọi API logout bằng refresh token rồi `new SessionStore(this).clear()`.
Phần Settings hiện tại chưa được nối vào cơ chế này.

ApiClient tự refresh khi nhận 401, lưu cặp token mới và thử lại đúng một lần.
Các lần refresh được tuần tự hóa để tránh dùng lại refresh token một lần.
Thiếu token sẽ hiện yêu cầu đăng nhập; không tự dùng tài khoản demo hay dữ liệu mẫu.
Vì luồng đăng nhập hiện tại chưa lưu token vào SessionStore, cần ghép bước trên
để dùng được trang Khám phá mới.

## Địa chỉ server

Mặc định `http://10.0.2.2:3000/api/` cho Android Emulator. Đổi cho điện thoại
thật bằng IP LAN hoặc URL server, ví dụ:

```powershell
.\gradlew.bat :app:assembleDebug -Pdatezone.apiBaseUrl=http://192.168.1.10:3000/api/
```

Có thể đặt `datezone.apiBaseUrl` trong Gradle properties trên máy phát triển.
Debug cho phép HTTP; release cần HTTPS. Server/MySQL phải đang chạy. URL ảnh
localhost/127.0.0.1 trả từ backend được đổi sang host/port cấu hình API.

## Bộ lọc và phạm vi còn lại

Các trường đã nối: giới tính, tuổi, khoảng cách, sở thích (tối đa 10, ID lấy
từ server). Áp dụng gọi search cho phiên tìm kiếm hiện tại; không ghi đè
preferences trên server. Bộ lọc được giữ khi mở lại và khi xoay màn hình.
Các điều khiển chưa có trong hợp đồng backend được vô hiệu hóa.

Danh sách lượt thích/tương hợp, chat, cuộc gọi, chỉnh sửa hồ sơ, OTP, đăng ký,
đổi mật khẩu/email và báo cáo/chặn chưa nằm trong đợt tích hợp này.
Trang hồ sơ chi tiết thật ẩn thông tin cá nhân mẫu và nút báo cáo/chặn chưa có API.

## Kiểm tra

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --offline
.\gradlew.bat :app:connectedDebugAndroidTest -Pdatezone.apiBaseUrl=http://127.0.0.1:38127/api/ -Pandroid.testInstrumentationRunnerArguments.class=com.datezone.app.api.ApiContractTest
```

ApiContractTest dùng HTTP server giả lập ngay trên thiết bị, preferences riêng,
không truy cập backend thật. Kiểm tra JSON nullable, ảnh/sở thích, thiếu session,
401 → refresh → thử lại cùng body, refresh bị thu hồi và lỗi validation.
Các test HTTP chỉ chạy khi base URL đúng địa chỉ test trên. Sau test, build lại
không có tham số test để APK trở về địa chỉ API bình thường.

Nếu Gradle chưa tải thư viện UTP để chạy connected tests, có thể build
`:app:assembleDebug :app:assembleDebugAndroidTest` với cùng base URL test,
cài hai APK bằng `adb install -r -t`, rồi chạy:

```powershell
adb shell am instrument -w -e class com.datezone.app.api.ApiContractTest com.datezone.app.test/androidx.test.runner.AndroidJUnitRunner
```

Đã kiểm tra build debug, unit test và 6 instrumentation test qua ADB. Chưa
kiểm thử với backend/MySQL thật vì bản local chưa có `.env` và `node_modules`.
