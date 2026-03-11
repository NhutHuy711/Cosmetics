# Cosmetics E-Commerce Website with AI-Based Skin Type Recommendation

Website thương mại điện tử kinh doanh mỹ phẩm, tích hợp mô-đun AI nhận diện loại da từ ảnh khuôn mặt để gợi ý sản phẩm phù hợp cho người dùng.

## Giới thiệu

Dự án được xây dựng với mục tiêu phát triển một nền tảng bán mỹ phẩm trực tuyến kết hợp trí tuệ nhân tạo nhằm cá nhân hóa trải nghiệm mua sắm. Bên cạnh các chức năng thương mại điện tử thông thường như quản lý sản phẩm, giỏ hàng, đặt hàng, thanh toán và theo dõi đơn hàng, hệ thống còn hỗ trợ phân tích loại da và đề xuất sản phẩm phù hợp dựa trên kết quả nhận diện.

## Mục tiêu dự án

- Xây dựng website thương mại điện tử chuyên kinh doanh mỹ phẩm.
- Hỗ trợ cá nhân hóa trải nghiệm mua sắm bằng khuyến nghị sản phẩm theo loại da.
- Hoàn thiện các chức năng nghiệp vụ như quản lý sản phẩm, kho, đơn hàng, người dùng và tìm kiếm.
- Tích hợp mô hình AI nhận diện 5 nhóm da: Combination, Dry, Normal, Oily, Sensitive.
- Triển khai mô hình dưới dạng dịch vụ suy luận để website có thể gọi và nhận kết quả phân tích từ đó đưa ra sản phẩm cho người dùng.

## Tính năng chính

### Dành cho khách hàng
- Duyệt sản phẩm theo danh mục
- Tìm kiếm sản phẩm
- Xem chi tiết sản phẩm
- Quản lý giỏ hàng
- Đặt hàng và thanh toán
- Xem và theo dõi đơn hàng
- Đánh giá sản phẩm
- Nhận gợi ý sản phẩm theo loại da sau khi phân tích ảnh khuôn mặt

### Dành cho quản trị và nhân sự
- Dashboard quản trị
- Quản lý nhân viên
- Quản lý khách hàng
- Quản lý thương hiệu
- Quản lý danh mục
- Quản lý sản phẩm
- Quản lý tồn kho và phiếu nhập
- Quản lý khuyến mãi
- Quản lý đơn hàng
- Quản lý phí vận chuyển
- Quản lý đánh giá
- Thống kê và báo cáo

### Mô-đun AI Skin Analyzer
- Tải ảnh hoặc chụp ảnh khuôn mặt
- Phát hiện khuôn mặt
- Phân tích theo từng vùng da
- Dự đoán loại da tổng thể
- Trả kết quả để hệ thống gợi ý danh sách sản phẩm phù hợp

## Công nghệ sử dụng

### Backend Web
- Java
- Spring Boot
- JPA
- Thymeleaf

### Frontend
- Bootstrap
- JavaScript
- jQuery

### Database
- MySQL Server

### Search Engine
- Elasticsearch
- Docker

### AI Service
- Python
- FastAPI
- EfficientNet
- YOLOv8n-face
- MediaPipe FaceMesh
