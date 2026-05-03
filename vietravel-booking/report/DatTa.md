II.
1. Đặc tả chức năng Đặt tour (Booking)
1) Mô tả
Chức năng này cho phép khách hàng đặt tour và thanh toán qua VNPAY.
Actor sử dụng chức năng này là: Khách hàng (ROLE_CUSTOMER).

2) Điều kiện trước
Khách hàng đã đăng nhập vào hệ thống.
Khách hàng đang ở trang chi tiet tour va chon ngay khoi hanh.
He thong hien thi trang Dat tour voi nut Thanh toan.

3) Tinh huong chinh
1. He thong mo trang Dat tour va hien thi day du thong tin tour, ngay khoi hanh, gia nguoi lon/tre em, tom tat chuyendi.
2. He thong yeu cau khach hang nhap cac truong bat buoc: Ho ten lien lac, dien thoai, email, so luong nguoi lon/tre em (toi thieu 1 nguoi lon), danh sach hanh khach tuong ung (ho ten, ngay sinh).
3a. Khach hang nhap thong tin hop le vao cac truong bat buoc.
(Thong tin hop le khi thoa man cac tieu chi:
- Khong duoc de trong.
- Ngay khoi hanh duoc chon.
- So luong hanh khach phu hop voi so luong nguoi lon/tre em.)
4. Khach hang (tuy chon) nhap cac truong khong bat buoc: Ghi chu, ma giam gia.
5. Khach hang nhan Thanh toan.
6. He thong kiem tra du lieu hop le, tao don booking voi trang thai PENDING va tinh tong tien.
7. He thong tao giao dich thanh toan (Payment) va sinh duong dan thanh toan VNPAY.
8. He thong chuyen huong khach hang sang cong thanh toan VNPAY.
9. Khach hang hoan tat thanh toan tren VNPAY.
10. He thong nhan ket qua thanh toan, cap nhat trang thai Payment.
11. He thong chuyen ve trang Dat tour va hien thi ket qua thanh toan thanh cong (kem ma booking).
12. Ket thuc.

4) Cac tinh huong thay the


C) Ma giam gia khong hop le
4b. Khach hang nhap ma giam gia sai hoac het han.
5. He thong thong bao "Khong the ap dung ma giam gia" va dat giam gia ve 0.
6. Khach hang co the nhap lai ma khac hoac bo qua.


E) Thanh toan that bai
9b. VNPAY tra ve ket qua that bai hoac chu ky khong hop le.
10. He thong cap nhat Payment thanh FAILED.
11. He thong chuyen ve trang Dat tour va hien thi ket qua thanh toan that bai (nut Thanh toan lai).
12. Khach hang nhan Thanh toan lai va quay lai buoc 5 o tinh huong chinh.
