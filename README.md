# NightfallDebtSystem — Hệ thống Nợ

Đây là tài liệu ngắn gọn miêu tả chức năng chính, các lệnh và quyền (permissions) của plugin NightfallDebtSystem (NFS Debt System).

## Mục tiêu chính
- Cung cấp hệ thống vay/mượn giữa người chơi trên server Minecraft.
- Hỗ trợ đa tiền tệ thông qua `CoinsEngine`.
- Lưu trữ dữ liệu nợ vào cơ sở dữ liệu (SQLite hoặc MySQL).
- Áp dụng hình phạt (debuff/potion) và lãi suất tự động khi nợ quá hạn.

## Các tính năng chính
- Tạo yêu cầu nợ giữa người vay và người cho vay.
- Người cho vay có thể chấp nhận yêu cầu và thực hiện chuyển tiền qua CoinsEngine.
- Người vay trả nợ từng phần hoặc toàn bộ; trạng thái nợ và số dư còn lại được lưu.
- Scheduler kiểm tra nợ quá hạn theo cấu hình và áp dụng debuff & lãi suất tự động.
- Tùy biến thông báo via `messages.yml` và cấu hình qua `config.yml`.

## Lệnh và cách dùng

- `/debt request <player> <amount> <interest> <days>`
  - **Mô tả**: Gửi yêu cầu nợ từ người hiện tại tới `<player>`.
  - **Ví dụ**: `/debt request Notch 100 5 7`
    - Tạo khoản nợ 100 với lãi suất 5% (phần trăm theo trường `interestRate` lưu), hạn trả sau 7 ngày.
  - **Quyền cần có**: `nfsdebt.borrow`

- `/debt accept <debtID>`
  - **Mô tả**: Người cho vay chấp nhận khoản nợ đã được yêu cầu; plugin sẽ rút tiền từ người cho vay và gửi cho người vay (thông qua CoinsEngine).
  - **Ví dụ**: `/debt accept 12`
  - **Quyền cần có**: `nfsdebt.loan`

- `/debt pay <debtID> <amount>`
  - **Mô tả**: Người vay trả một phần hoặc toàn bộ nợ.
  - **Ví dụ**: `/debt pay 12 50`
  - **Quyền cần có**: `nfsdebt.borrow`

- `/debt list`
  - **Mô tả**: Liệt kê tất cả khoản nợ liên quan đến người dùng (là borrower hoặc lender).
  - **Ví dụ**: `/debt list`
  - **Quyền cần có**: `nfsdebt.view`

- `/debt reload`
  - **Mô tả**: Tải lại `config.yml` và `messages.yml` (các thông báo và cấu hình sẽ được áp dụng lại).
  - **Quyền cần có**: `nfsdebt.admin`

### Ghi chú về lệnh
- `interest` thường lưu làm hệ số/phần trăm (plugin lưu gốc như đã truyền; định nghĩa & hiển thị có thể tuỳ chỉnh trong `messages.yml`).

## Danh sách Permissions (quyền) và mô tả
- `nfsdebt.borrow`
  - **Mô tả**: Cho phép gửi yêu cầu nợ và thực hiện trả nợ (dành cho borrower).
- `nfsdebt.loan`
  - **Mô tả**: Cho phép chấp nhận khoản nợ và chuyển tiền sang borrower (dành cho lender).
- `nfsdebt.view`
  - **Mô tả**: Cho phép xem danh sách các khoản nợ liên quan (list).
- `nfsdebt.admin`
  - **Mô tả**: Quyền quản trị plugin: reload cấu hình/messages và các lệnh admin khác (nếu được thêm sau này).

## Lời khuyên vận hành
- Đảm bảo plugin `CoinsEngine` được cài (vì là hard dependency). Nếu thiếu, plugin sẽ không khởi động.
- Sao lưu file database (nếu dùng SQLite) hoặc database MySQL trước khi thay đổi cấu hình lớn.
- Tùy chỉnh `messages.yml` để dịch/định dạng thông báo theo server của bạn.

## PlaceholderAPI

Các placeholder hiện có:

- `%nfsdebt_total_debt%`
  - Mô tả: Tổng số nợ còn lại của người chơi (tổng `remainingAmount` đối với các khoản mà người chơi là borrower).
  - Ví dụ sử dụng: `Scoreboard line: You owe %nfsdebt_total_debt%`

- `%nfsdebt_total_lent%`
  - Mô tả: Tổng số tiền người chơi đã cho vay (tổng `amount` đối với các khoản mà người chơi là lender).
  - Ví dụ sử dụng: `Scoreboard line: You lent %nfsdebt_total_lent%`

- `%nfsdebt_debt_remaining_<id>%`
  - Mô tả: Số tiền còn lại của khoản nợ có ID = `<id>` (ví dụ `%nfsdebt_debt_remaining_12%` sẽ trả về số còn nợ của debtID 12).
  - Lưu ý: nếu ID không tồn tại sẽ trả về `0`.
