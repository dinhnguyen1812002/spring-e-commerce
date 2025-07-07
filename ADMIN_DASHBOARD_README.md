# Admin Dashboard - E-commerce

## Tổng quan

Admin Dashboard được xây dựng với Tailwind CSS và tích hợp các trang có sẵn trong codebase. Dashboard cung cấp giao diện quản lý toàn diện cho hệ thống e-commerce.

## Tính năng

### 1. Dashboard Overview (`/admin/dashboard`)
- **Thống kê tổng quan**: Hiển thị số lượng sản phẩm, người dùng, danh mục
- **Quick Actions**: Các nút truy cập nhanh đến các chức năng chính
- **Recent Activity**: Hoạt động gần đây
- **Recent Orders**: Đơn hàng mới nhất

### 2. Products Management (`/admin/products`)
- **Danh sách sản phẩm**: Hiển thị tất cả sản phẩm với thông tin chi tiết
- **Tìm kiếm**: Tìm kiếm sản phẩm theo từ khóa
- **Thêm sản phẩm**: Link đến trang thêm sản phẩm
- **Chỉnh sửa/Xóa**: Các action để quản lý sản phẩm
- **Phân trang**: Hỗ trợ phân trang cho danh sách lớn

### 3. Categories Management (`/admin/categories`)
- **Danh sách danh mục**: Hiển thị tất cả danh mục sản phẩm
- **Thêm danh mục**: Link đến trang thêm danh mục
- **Chỉnh sửa/Xóa**: Quản lý danh mục

### 4. Users Management (`/admin/users`)
- **Danh sách người dùng**: Hiển thị thông tin người dùng
- **Avatar**: Hiển thị avatar người dùng
- **Quản lý tài khoản**: Chỉnh sửa/xóa người dùng

### 5. Traffic Analytics (`/admin/traffic`)
- **Thống kê truy cập**: Bảng thống kê theo ngày
- **Biểu đồ**: Chart.js để hiển thị xu hướng truy cập
- **Real-time updates**: WebSocket để cập nhật real-time

## Cấu trúc file

```
src/main/resources/templates/admin/
├── admin-dashboard.html          # Trang dashboard chính
├── admin-products.html           # Quản lý sản phẩm
├── admin-categories.html         # Quản lý danh mục
├── admin-users.html             # Quản lý người dùng
├── admin-traffic.html           # Analytics traffic
└── admin-layout.html            # Layout chung (tham khảo)

src/main/java/com/app/e_commerce/controller/
└── AdminController.java         # Controller xử lý admin routes
```

## Routes

| Route | Mô tả | Template |
|-------|-------|----------|
| `/admin/dashboard` | Dashboard chính | `admin-dashboard.html` |
| `/admin/products` | Quản lý sản phẩm | `admin-products.html` |
| `/admin/categories` | Quản lý danh mục | `admin-categories.html` |
| `/admin/users` | Quản lý người dùng | `admin-users.html` |
| `/admin/traffic` | Analytics traffic | `admin-traffic.html` |

## Sidebar Navigation

Sidebar cung cấp điều hướng đến tất cả các trang admin:

- **Dashboard**: Tổng quan hệ thống
- **Products**: Quản lý sản phẩm
- **Categories**: Quản lý danh mục
- **Users**: Quản lý người dùng
- **Traffic**: Analytics traffic
- **Orders**: Quản lý đơn hàng (để phát triển)
- **Revenue**: Quản lý doanh thu (để phát triển)

## Tích hợp với codebase hiện tại

Admin dashboard sử dụng lại các trang có sẵn:

- **Products**: Sử dụng lại logic từ `products.html`, `add-product.html`, `edit-product.html`
- **Categories**: Sử dụng lại logic từ `categories.html`, `category-form.html`
- **Users**: Sử dụng lại logic từ `list-user.html`
- **Traffic**: Sử dụng lại logic từ `traffic.html`

## Responsive Design

- **Desktop**: Sidebar cố định, content area linh hoạt
- **Tablet**: Sidebar có thể thu gọn
- **Mobile**: Sidebar ẩn, có thể toggle

## Styling

Sử dụng Tailwind CSS với:
- **Color scheme**: Blue primary, gray secondary
- **Icons**: Font Awesome 6.0
- **Charts**: Chart.js cho analytics
- **Real-time**: WebSocket với SockJS và STOMP

## Security

- Tất cả routes admin cần authentication
- Kiểm tra quyền admin trước khi truy cập
- Logout functionality tích hợp

## Development Notes

1. **Controller**: `AdminController.java` xử lý tất cả admin routes
2. **Services**: Đã thêm các method count cho statistics
3. **Templates**: Sử dụng Thymeleaf với data binding
4. **JavaScript**: Chart.js cho analytics, WebSocket cho real-time

## Future Enhancements

- [ ] Orders management
- [ ] Revenue analytics
- [ ] User roles and permissions
- [ ] Advanced filtering and search
- [ ] Export functionality
- [ ] Email notifications
- [ ] Audit logs 