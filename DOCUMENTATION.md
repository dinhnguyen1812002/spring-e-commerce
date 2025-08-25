
# Tài liệu Luồng Giỏ hàng, Thanh toán và Đơn hàng

Tài liệu này mô tả chi tiết về các luồng xử lý chính liên quan đến giỏ hàng, thanh toán và quản lý đơn hàng trong hệ thống e-commerce.

## Các Thành phần Chính

- **Controllers**: `CartController`, `OrderController`
- **Entities**: `User`, `Product`, `Cart`, `CartItem`, `Order`, `OrderItem`, `Coupon`
- **Services**: `CartService`, `OrderService`, `UserService`, `ProductService`, `CouponService`
- **DTOs**: `CheckoutRequestDTO`
- **Templates**: `cart/view.html`, `cart/checkout.html`, `cart/orderHistory.html`

---

## 1. Luồng Quản lý Giỏ hàng (Cart)

Luồng này xử lý tất cả các thao tác liên quan đến giỏ hàng của người dùng, bao gồm cả khách (guest) và người dùng đã đăng nhập.

### Sơ đồ tóm tắt
```
Người dùng -> Thêm/Sửa/Xoá sản phẩm -> CartController -> CartService -> Cập nhật Cart Entity -> Database
                                         ^
                                         |
                                    (Session/User)
```

### Chi tiết các bước:

1.  **Khởi tạo Giỏ hàng**:
    *   Khi người dùng (đã đăng nhập hoặc khách) thực hiện thao tác đầu tiên với giỏ hàng (ví dụ: thêm sản phẩm), `CartService` sẽ được gọi.
    *   `cartService.getOrCreateCart(user, session)`:
        *   **Nếu là khách**: Một `Cart` mới được tạo và lưu vào `HttpSession`. `isGuestCart` được set là `true`.
        *   **Nếu đã đăng nhập**: Hệ thống sẽ tìm `Cart` của `User` trong database. Nếu không có, một giỏ hàng mới sẽ được tạo và liên kết với `User` đó.
        *   **Khi đăng nhập**: Nếu có giỏ hàng trong session của khách, nó sẽ được hợp nhất (`mergeCart`) vào giỏ hàng của người dùng.

2.  **Các thao tác chính (`CartController`)**:
    *   `POST /cart/add`: Thêm một `Product` với `quantity` vào giỏ hàng. `CartService` sẽ tạo mới một `CartItem` hoặc cập nhật số lượng nếu sản phẩm đã tồn tại.
    *   `POST /cart/update`: Cập nhật `quantity` của một `CartItem` đã có trong giỏ.
    *   `POST /cart/remove`: Xóa một `CartItem` khỏi giỏ hàng.
    *   `POST /cart/clear`: Xóa tất cả `CartItem` khỏi giỏ hàng.
    *   `GET /cart`: Hiển thị trang giỏ hàng (`cart/view.html`), truyền vào đối tượng `Cart` để render ra view.

3.  **Áp dụng Mã giảm giá (Coupon)**:
    *   `POST /cart/apply-coupon`: Người dùng nhập một `couponCode`. `CartService` sẽ kiểm tra tính hợp lệ của mã và áp dụng giảm giá vào `Cart` (cập nhật `discountAmount` và `couponCode`).
    *   `POST /cart/remove-coupon`: Xóa mã giảm giá đã áp dụng.

4.  **Cấu trúc Entity**:
    *   `Cart`: Lưu thông tin tổng quan về giỏ hàng, liên kết với `User` (có thể null nếu là khách), chứa một tập hợp các `CartItem`. Nó cũng tự tính toán `subtotal`, `tax`, `shippingCost`, và `totalPrice`.
    *   `CartItem`: Đại diện cho một sản phẩm trong giỏ, chứa thông tin về `Product`, `Cart` và `quantity`.

---

## 2. Luồng Thanh toán (Checkout)

Luồng này bắt đầu khi người dùng quyết định đặt hàng từ giỏ hàng của họ.

### Sơ đồ tóm tắt
```
GET /orders/checkout -> OrderController -> Hiển thị trang checkout.html -> Người dùng điền thông tin -> POST /orders/checkout
```

### Chi tiết các bước:

1.  **Hiển thị trang Checkout**:
    *   Người dùng nhấn nút "Thanh toán" từ trang giỏ hàng, trình duyệt gửi yêu cầu `GET /orders/checkout`.
    *   `OrderController` xử lý yêu cầu này.
    *   Nó lấy `Cart` hiện tại từ `CartService`. Nếu giỏ hàng rỗng, người dùng sẽ được chuyển hướng ngược lại trang giỏ hàng.
    *   Controller truyền đối tượng `Cart` và danh sách các `PaymentMethod` vào model.
    *   Render ra view `cart/checkout.html`, hiển thị một form để người dùng nhập thông tin giao hàng và chọn phương thức thanh toán.

---

## 3. Luồng Xử lý Đơn hàng (Order Placement)

Đây là bước cuối cùng, nơi thông tin từ giỏ hàng và form checkout được chuyển thành một đơn hàng chính thức.

### Sơ đồ tóm tắt
```
POST /orders/checkout -> OrderController -> OrderService.checkoutCart() -> Tạo Order & OrderItem -> Xóa Cart -> Database -> Redirect trang thành công
```

### Chi tiết các bước:

1.  **Gửi thông tin Đơn hàng**:
    *   Người dùng điền đầy đủ thông tin vào form checkout và nhấn "Đặt hàng". Trình duyệt gửi một yêu cầu `POST /orders/checkout`.
    *   Các thông tin như `fullName`, `phone`, `address`, `note`, `paymentMethod` được gửi lên.

2.  **Xử lý tại `OrderController`**:
    *   `OrderController` nhận thông tin.
    *   Nó gọi phương thức `orderService.checkoutCart(...)`.

3.  **Logic tại `OrderService.checkoutCart()`**:
    *   **Lấy `User` và `Cart`**: Dịch vụ lấy thông tin người dùng đang đăng nhập và giỏ hàng của họ.
    *   **Tạo `Order` mới**: Một thực thể `Order` mới được tạo. ID của đơn hàng được tạo tự động bằng `ULID` (`prePersist`).
    *   **Sao chép thông tin**:
        *   Thông tin người nhận (`fullName`, `phone`, `address`...) được sao chép từ `CheckoutRequestDTO` (hoặc các `RequestParam`) vào `Order`.
        *   Thông tin về giá (`subtotal`, `tax`, `shippingCost`, `discountAmount`, `totalAmount`) được sao chép từ `Cart` sang `Order`.
        *   `orderStatus` được thiết lập về trạng thái ban đầu (ví dụ: `PENDING`).
    *   **Tạo `OrderItem`**: Dịch vụ lặp qua từng `CartItem` trong `Cart`:
        *   Với mỗi `CartItem`, một `OrderItem` tương ứng được tạo ra.
        *   Thông tin `Product`, `quantity`, và `price` (giá tại thời điểm đặt hàng) được sao chép vào `OrderItem`.
        *   `OrderItem` được liên kết với `Order` vừa tạo.
    *   **Lưu vào Database**: `Order` và tất cả `OrderItem` của nó được lưu vào cơ sở dữ liệu trong cùng một transaction.
    *   **Xóa Giỏ hàng**: Sau khi đơn hàng được tạo thành công, `CartService` sẽ được gọi để xóa sạch giỏ hàng (`clearCart`).
    *   **Trả về `Order`**: Dịch vụ trả về đối tượng `Order` vừa được tạo.

4.  **Hoàn tất và Chuyển hướng**:
    *   `OrderController` nhận lại đối tượng `Order`.
    *   Nó chuyển hướng người dùng đến trang chi tiết đơn hàng (`GET /orders/{orderId}`) hoặc một trang xác nhận đặt hàng thành công.

5.  **Xem lại Lịch sử**:
    *   Người dùng có thể xem lại tất cả các đơn hàng của mình tại `GET /orders`, trang này sẽ hiển thị `cart/orderHistory.html`.
