2. Đặc tả chức năng Thêm lịch khởi hành
1) Mô tả
Chức năng này cho phép người dùng thêm lịch khởi hành mới cho các tour.
Actor sử dụng chức năng này là: Quản trị viên.
2) Điều kiện trước
Người dùng đã đăng nhập vào hệ thống.
Người dùng truy cập vào trang Quản lý ngày khởi hành.
3) Tình huống chính
1. Hệ thống hiển thị trang Quản lý ngày khởi hành.
2. Người dùng chọn tour trong danh sách comboBox “Chọn tour”.
3. Người dùng bấm + Thêm ngày khởi hành.
4. Hệ thống mở form nhập lịch khởi hành với các trường: Ngày khởi hành, Điểm khởi hành, Giá cơ bản (người lớn), Giá trẻ em, Tổng số chỗ, Chỗ còn lại, Trạng thái.
5a. Người dùng nhập Ngày khởi hành hợp lệ.
6a. Người dùng nhập Điểm khởi hành hợp lệ.
7a. Người dùng nhập Giá cơ bản (người lớn) và Giá trẻ em hợp lệ.
8a. Người dùng nhập Tổng số chỗ hợp lệ (> 0).
9. Hệ thống tự động cập nhật Chỗ còn lại bằng Tổng số chỗ.
10. Người dùng bấm Lưu.
11. Hệ thống kiểm tra dữ liệu hợp lệ.
12. Hệ thống tạo lịch khởi hành mới.
13. Hệ thống hiển thị thông báo lưu thành công và cập nhật danh sách lịch khởi hành.
14. Kết thúc.
4) Các tình huống thay thế
a) Ngày khởi hành hoặc thông tin bắt buộc không hợp lệ
5b. Người dùng nhập Ngày khởi hành hoặc thông tin bắt buộc không hợp lệ.
(Thông tin không hợp lệ khi:
Ngày khởi hành để trống.
Không chọn tour hoặc điểm khởi hành.
Trùng lịch khởi hành theo tour + ngày khởi hành + điểm khởi hành.)
6. giống tình huống chính.
7. giống tình huống chính.
8. giống tình huống chính.
9. giống tình huống chính.
10. giống tình huống chính.
11. Hệ thống kiểm tra dữ liệu không hợp lệ.
12. Hệ thống hiển thị thông báo “Lịch khởi hành đã tồn tại cho ngày và điểm khởi hành này” hoặc thông báo dữ liệu không hợp lệ.
13. Người dùng vẫn ở form thêm lịch khởi hành.
14. Người dùng quay lại bước 5 trong tình huống chính.
b) Tổng số chỗ không hợp lệ (Tổng số chỗ ≤ 0)
8b. Người dùng nhập Tổng số chỗ không hợp lệ.
(Thông tin không hợp lệ khi:
Tổng số chỗ để trống.
Tổng số chỗ nhỏ hơn hoặc bằng 0.)
9. giống tình huống chính.
10. giống tình huống chính.
11. Hệ thống kiểm tra dữ liệu không hợp lệ.
12. Hệ thống hiển thị thông báo “Số chỗ phải lớn hơn 0”.
13. Người dùng vẫn ở form thêm lịch khởi hành.
14. Người dùng quay lại bước 8 trong tình huống chính.
c) Chỗ còn lại không hợp lệ
9b. Người dùng nhập hoặc thay đổi Chỗ còn lại không hợp lệ.
(Thông tin không hợp lệ khi:
Chỗ còn lại nhỏ hơn 0.
Chỗ còn lại lớn hơn Tổng số chỗ.)
10. giống tình huống chính.
11. Hệ thống kiểm tra dữ liệu không hợp lệ.
12. Hệ thống hiển thị thông báo “Số chỗ còn lại không hợp lệ”.
13. Người dùng vẫn ở form thêm lịch khởi hành.
14. Người dùng quay lại bước 9 trong tình huống chính.
d) Người dùng chưa chọn tour nhưng nhấn + Thêm ngày khởi hành
2b. Người dùng không chọn tour trong comboBox “Chọn tour” và nhấn + Thêm ngày khởi hành.
3. Hệ thống hiển thị thông báo “Vui lòng chọn tour trước khi thêm”.
4. Người dùng vẫn ở trang Quản lý ngày khởi hành.
5. Người dùng quay lại bước 2 trong tình huống chính.

II.
1. Đặc tả chức năng Đặt tour (Booking)
1) Mô tả
Chức năng này cho phép khách hàng đặt tour và thanh toán qua VNPAY.
Actor sử dụng chức năng này là: Khách hàng (ROLE_CUSTOMER).

2) Điều kiện trước
Khách hàng đã đăng nhập vào hệ thống.
Khách hàng đang ở trang chi tiết tour và chọn ngày khởi hành.
Hệ thống hiển thị trang Đặt tour với nút Thanh toán.

3) Tình huống chính
1. Hệ thống mở trang Đặt tour và hiển thị đầy đủ thông tin tour, ngày khởi hành, giá người lớn/trẻ em, tóm tắt chuyến đi.
2. Hệ thống yêu cầu khách hàng nhập các trường bắt buộc: Họ tên liên lạc, điện thoại, email, số lượng người lớn/trẻ em (tối thiểu 1 người lớn), danh sách hành khách tương ứng (họ tên, ngày sinh).
3a. Khách hàng nhập thông tin hợp lệ vào các trường bắt buộc.
(Thông tin hợp lệ khi thỏa mãn các tiêu chí:
- Không được để trống.
- Ngày khởi hành được chọn.
- Số lượng hành khách phù hợp với số lượng người lớn/trẻ em.)
4. Khách hàng (tùy chọn) nhập các trường không bắt buộc: Ghi chú, mã giảm giá.
5. Khách hàng nhấn Thanh toán.
6. Hệ thống kiểm tra dữ liệu hợp lệ, tạo đơn booking với trạng thái PENDING và tính tổng tiền.
7. Hệ thống tạo giao dịch thanh toán (Payment) và sinh đường dẫn thanh toán VNPAY.
8. Hệ thống chuyển hướng khách hàng sang cổng thanh toán VNPAY.
9. Khách hàng hoàn tất thanh toán trên VNPAY.
10. Hệ thống nhận kết quả thanh toán, cập nhật trạng thái Payment.
11. Hệ thống chuyển về trang Đặt tour và hiển thị kết quả thanh toán thành công (kèm mã booking).
12. Kết thúc.

4) Các tình huống thay thế
a) Chưa chọn ngày khởi hành
5b. Khách hàng nhấn Thanh toán khi chưa chọn ngày khởi hành.
6. Hệ thống hiển thị thông báo “Vui lòng chọn ngày khởi hành trước khi thanh toán”.
7. Khách hàng vẫn ở trang Đặt tour.
8. Khách hàng quay lại bước 1 trong tình huống chính.

b) Thiếu thông tin liên lạc
2b. Khách hàng bỏ trống Họ tên/Điện thoại/Email.
5. giống tình huống chính.
6. Hệ thống hiển thị thông báo “Vui lòng nhập đầy đủ thông tin liên lạc”.
7. Khách hàng vẫn ở trang Đặt tour.
8. Khách hàng quay lại bước 2 trong tình huống chính.

c) Không đủ chỗ còn lại
3b. Số lượng người lớn/trẻ em vượt quá số chỗ còn lại của lịch khởi hành.
5. giống tình huống chính.
6. Hệ thống hiển thị thông báo “Không đủ chỗ cho lịch khởi hành đã chọn”.
7. Khách hàng vẫn ở trang Đặt tour.
8. Khách hàng điều chỉnh số lượng hành khách và quay lại bước 3 trong tình huống chính.

d) Mã giảm giá không hợp lệ
4b. Khách hàng nhập mã giảm giá sai hoặc hết hạn.
5. Hệ thống thông báo “Không thể áp dụng mã giảm giá” và đặt giảm giá về 0.
6. Khách hàng có thể nhập lại mã khác hoặc bỏ qua.



f) Thanh toán thất bại
9b. VNPAY trả về kết quả thất bại hoặc chữ ký không hợp lệ.
10. Hệ thống cập nhật Payment thành FAILED.
11. Hệ thống chuyển về trang Đặt tour và hiển thị kết quả thanh toán thất bại (nút Thanh toán lại).
12. Khách hàng nhấn Thanh toán lại và quay lại bước 5 ở tình huống chính.

III.
1. Đặc tả chức năng Xử lý đặt tour của Staff (Xác nhận đặt tour)
1) Mô tả
Chức năng này cho phép nhân viên xác nhận đơn đặt tour.
Actor sử dụng chức năng này là: Nhân viên (ROLE_STAFF).

2) Điều kiện trước
Nhân viên đã đăng nhập vào hệ thống.
Nhân viên truy cập trang Xử lý đặt tour.
Booking có trạng thái PENDING hiển thị trong danh sách.

3) Tình huống chính
1. Hệ thống hiển thị danh sách booking với trạng thái, thông tin liên hệ, ngày khởi hành và thao tác.
2. Nhân viên sử dụng bộ lọc (từ ngày/đến ngày/trạng thái) và nhấn Lọc.
3. Hệ thống cập nhật danh sách booking theo bộ lọc.
4. Nhân viên kiểm tra thông tin booking (mã booking, khách hàng, tour, ngày khởi hành, thanh toán).
5. Nhân viên chọn booking có trạng thái PENDING và nhấn Xác nhận.
6. Hệ thống kiểm tra booking tồn tại và trạng thái hợp lệ (PENDING).
7. Hệ thống cập nhật trạng thái booking sang CONFIRMED.
8. Hệ thống ghi nhận lịch sử xử lý và gửi thông báo cho khách hàng.
9. Hệ thống hiển thị thông báo xác nhận thành công và quay lại danh sách.
10. Kết thúc.

4) Các tình huống thay thế

a) Xem chi tiết booking
1. giống tình huống chính.
2. giống tình huống chính.
3. giống tình huống chính.
4. giống tình huống chính.
5b. Nhân viên nhấn Chi tiết tại một booking trong danh sách.
6. Hệ thống hiển thị trang Chi tiết đặt tour với thông tin tour, liên hệ, thanh toán và hành khách.
7. Nhân viên xem thông tin và quay lại trang danh sách.
8. Luồng tiếp tục từ bước 2 của tình huống chính.

b) Nhân viên nhấn Hủy tour
1. giống tình huống chính.
2. giống tình huống chính.
3. giống tình huống chính.
4. giống tình huống chính.
5c. Nhân viên chọn Hủy tour tại danh sách 
6. Hệ thống yêu cầu chọn lý do hủy.
7. Nhân viên chọn lý do và xác nhận.
8. Hệ thống cập nhật trạng thái booking sang CANCELED và lưu lý do.
9. Hệ thống hiển thị thông báo hủy thành công và quay lại danh sách.
10. Kết thúc.

