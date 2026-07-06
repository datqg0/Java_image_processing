# Java Image Processing App

Đây là một ứng dụng mã nguồn mở được phát triển bằng Java và JavaFX dành cho việc chỉnh sửa ảnh. Ứng dụng cung cấp các tính năng chỉnh sửa truyền thống kết hợp với các công cụ nâng cao như tách nền/xóa vật thể bằng trí tuệ nhân tạo (tích hợp API của Remove.bg) và xử lý hình ảnh phức tạp bằng OpenCV.

## 🌟 Tính năng chính
- **Chỉnh sửa ảnh cơ bản:** Cung cấp các công cụ thiết yếu để xử lý hình ảnh.
- **Tách nền bằng AI:** Tự động loại bỏ nền của hình ảnh một cách nhanh chóng và chính xác nhờ tích hợp API từ [Remove.bg](https://www.remove.bg/).
- **Xử lý ảnh nâng cao:** Tích hợp thư viện [OpenCV 4.10.0](https://opencv.org/) cho các tác vụ phân tích và xử lý ảnh chuyên sâu.
- **Ghép và thao tác ảnh:** Hỗ trợ tính năng ghép hình ảnh và composition.
- **Giao diện hiện đại, thân thiện:** Giao diện trực quan được xây dựng bằng JavaFX, kết hợp với các thư viện giao diện như ControlsFX, ValidatorFX, BootstrapFX, v.v.

## ⚙️ Yêu cầu hệ thống
- **Java JDK:** Phiên bản 17 trở lên.
- **Maven:** Để quản lý các thư viện và build dự án (có thể sử dụng Maven Wrapper `mvnw` đi kèm).
- **OpenCV:** Phiên bản 4.10.0 đã được tải và giải nén trên máy tính của bạn.

## 🚀 Cài đặt và Cấu hình

### 1. Tải dự án
Mở terminal hoặc command prompt và clone dự án về máy:
```bash
git clone <đường-dẫn-repo-của-bạn>
cd Java_image_processing
```

### 2. Cài đặt thư viện OpenCV
Ứng dụng sử dụng native library của OpenCV. Bạn cần:
1. Tải và giải nén [OpenCV 4.10.0](https://opencv.org/releases/) (Ví dụ giải nén ra `C:\opencv`).
2. Cập nhật đường dẫn đến thư mục chứa file `.dll` (hoặc `.so` trên Linux / `.dylib` trên Mac) trong file `pom.xml`. 
   Tìm đoạn `<jvmArg>-Djava.library.path=C:\opencv\build\java\x64</jvmArg>` trong `pom.xml` và sửa lại đúng với đường dẫn trên máy bạn (nếu khác).

### 3. Cấu hình biến môi trường (.env)
Dự án yêu cầu các biến môi trường để sử dụng các tính năng kết nối với API bên ngoài:
1. Tạo một file mới có tên `.env` tại thư mục gốc của dự án (ngang hàng với `pom.xml`).
2. Mở file `.env.example`, copy toàn bộ nội dung dán sang file `.env`.
3. Đăng ký tài khoản tại [Remove.bg](https://www.remove.bg/) để lấy API Key.
4. Điền API Key và đường dẫn OpenCV vào file `.env` của bạn:
   ```env
   REMOVE_BG_API_KEY=diền_api_key_cua_ban_vao_day
   OPENCV_LIB_PATH=C:\opencv\build\java\x64 # Đường dẫn thư viện OpenCV trên máy bạn
   ```

## 💻 Hướng dẫn chạy ứng dụng

Sử dụng Maven để biên dịch và chạy ứng dụng một cách dễ dàng:

```bash
# 1. Biên dịch dự án
mvn compile

# 2. Khởi chạy ứng dụng JavaFX
mvn javafx:run
```

## 📖 Hướng dẫn sử dụng

1. **Khởi động ứng dụng:** Sau khi chạy lệnh `mvn javafx:run`, giao diện chính của phần mềm sẽ hiện ra.
2. **Mở ảnh:** Sử dụng tính năng mở file trên thanh menu hoặc giao diện để chọn bức ảnh bạn muốn chỉnh sửa.
3. **Sử dụng công cụ tách nền AI:**
   - Chọn công cụ tách nền/Remove Background trên giao diện.
   - Phần mềm sẽ gửi ảnh lên Remove.bg thông qua API Key bạn đã cung cấp trong file `.env`.
   - Lưu ý: Máy tính của bạn cần được kết nối Internet để sử dụng tính năng này.
4. **Các tính năng xử lý ảnh khác:** Lựa chọn các công cụ để áp dụng filter, cắt ghép hoặc thay đổi các thông số của ảnh theo nhu cầu.
5. **Lưu kết quả:** Sau khi hài lòng với bức ảnh, chọn lưu file để xuất ảnh ra máy tính.

---
*Dự án sử dụng JavaFX, OpenCV, và Apache HTTP Client.*
