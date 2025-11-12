# Hướng Dẫn Sử Dụng Plugin NightfallDebtSystem

NightfallDebtSystem! Plugin cho phép người chơi quản lý các khoản nợ trong game một cách dễ dàng, bao gồm yêu cầu nợ, chấp nhận nợ, thanh toán và xem danh sách nợ.

---

## 📜 Mục Lục

1.  [Giới Thiệu](#giới-thiệu)
2.  [Các Lệnh](#các-lệnh)
    *   [`/debt request`](#debt-request)
    *   [`/debt accept`](#debt-accept)
    *   [`/debt pay`](#debt-pay)
    *   [`/debt list`](#debt-list)
    *   [`/debt delete`](#debt-delete)
    *   [`/debt detail`](#debt-detail)
    *   [`/debt reload`](#debt-reload)
3.  [Ví Dụ Sử Dụng](#ví-dụ-sử-dụng)
4.  [Quyền Hạn (Permissions)](#quyền-hạn-permissions)
5.  [Khắc Phục Sự Cố](#khắc-phục-sự-cố)

---

## 🌟 Giới Thiệu

NightfallDebtSystem là một plugin quản lý nợ đơn giản cho máy chủ Minecraft của bạn. Cho phép người chơi vay và cho vay tiền, với các tính năng như lãi suất và ngày đáo hạn.

---

## 🚀 Các Lệnh

Dưới đây là danh sách các lệnh chính mà bạn có thể sử dụng trong plugin:

### `/debt request <người chơi> <số tiền> <lãi suất> <ngày>` {#debt-request}

*   **Mô tả**: Gửi yêu cầu vay tiền từ một người chơi khác.
    *   `<người chơi>`: Tên của người chơi bạn muốn vay tiền.
    *   `<số tiền>`: Số tiền bạn muốn vay.
    *   `<lãi suất>`: Tỷ lệ lãi suất (ví dụ: `0.05` cho 5%).
    *   `<ngày>`: Số ngày cho đến khi khoản nợ đáo hạn.
*   **Ví dụ**: `/debt request Kennji 1000 0.05 7`
    *   Bạn yêu cầu Kennji cho vay 1000 tiền với lãi suất 5% trong 7 ngày.
*   **Lưu ý**: Bạn không thể yêu cầu nợ từ chính mình.

### `/debt accept <ID khoản nợ>` {#debt-accept}

*   **Mô tả**: Chấp nhận một yêu cầu nợ mà bạn đã nhận được. Khi chấp nhận, số tiền sẽ được chuyển từ tài khoản của bạn sang người vay.
    *   `<ID khoản nợ>`: ID của khoản nợ bạn muốn chấp nhận (hiển thị trong tin nhắn yêu cầu).
*   **Ví dụ**: `/debt accept 123`
    *   Bạn chấp nhận khoản nợ có ID 123.
*   **Lưu ý**:
    *   Bạn phải là người cho vay của khoản nợ đó.
    *   Khoản nợ không được chấp nhận hoặc thanh toán trước đó.
    *   Bạn phải có đủ tiền để cho vay.

### `/debt pay <ID khoản nợ> <số tiền>` {#debt-pay}

*   **Mô tả**: Thanh toán một phần hoặc toàn bộ khoản nợ của bạn.
    *   `<ID khoản nợ>`: ID của khoản nợ bạn muốn thanh toán.
    *   `<số tiền>`: Số tiền bạn muốn trả.
*   **Ví dụ**: `/debt pay 123 500`
    *   Bạn thanh toán 500 tiền cho khoản nợ có ID 123.
*   **Lưu ý**:
    *   Bạn phải là người vay của khoản nợ đó.
    *   Khoản nợ phải đã được chấp nhận và chưa thanh toán hết.
    *   Bạn phải có đủ tiền để thanh toán.
*   **Tab Complete**: Khi gõ `/debt pay`, bạn sẽ thấy gợi ý các ID khoản nợ mà bạn đã chấp nhận và chưa thanh toán.

### `/debt list` {#debt-list}

*   **Mô tả**: Hiển thị danh sách tất cả các khoản nợ liên quan đến bạn (cả với tư cách người vay và người cho vay).
*   **Ví dụ**: `/debt list`

### `/debt delete <ID khoản nợ>` {#debt-delete}

*   **Mô tả**: Xóa một khoản nợ khỏi cơ sở dữ liệu. Chỉ dành cho quản trị viên.
    *   `<ID khoản nợ>`: ID của khoản nợ bạn muốn xóa.
*   **Ví dụ**: `/debt delete 123`

### `/debt detail <người chơi>` {#debt-detail}

*   **Mô tả**: Hiển thị chi tiết tất cả các khoản nợ liên quan đến một người chơi cụ thể. Chỉ dành cho quản trị viên.
    *   `<người chơi>`: Tên của người chơi bạn muốn xem chi tiết nợ.
*   **Ví dụ**: `/debt detail Kennji`

### `/debt reload` {#debt-reload}

*   **Mô tả**: Tải lại cấu hình và tin nhắn của plugin. Chỉ dành cho quản trị viên.
*   **Ví dụ**: `/debt reload`

---

## 💡 Ví Dụ Sử Dụng

1.  **Người chơi A muốn vay tiền từ Người chơi B:**
    *   Người chơi A gõ: `/debt request PlayerB 500 0.02 5`
    *   Người chơi B nhận được thông báo yêu cầu nợ với một ID cụ thể (ví dụ: ID 456).
2.  **Người chơi B chấp nhận yêu cầu nợ:**
    *   Người chơi B gõ: `/debt accept 456`
    *   500 tiền được chuyển từ PlayerB sang PlayerA.
3.  **Người chơi A thanh toán một phần khoản nợ:**
    *   Người chơi A gõ: `/debt pay 456 200`
    *   200 tiền được chuyển từ PlayerA sang PlayerB.
4.  **Người chơi A kiểm tra các khoản nợ của mình:**
    *   Người chơi A gõ: `/debt list`
    *   Hiển thị danh sách các khoản nợ, bao gồm khoản nợ ID 456 với số tiền còn lại.

---

## 🔒 Quyền Hạn (Permissions)

*   `nfsdebt.user`: Cho phép người chơi yêu cầu nợ, chấp nhận yêu cầu nợ, thanh toán nợ và xem danh sách nợ của mình.
*   `nfsdebt.admin`: Cho phép quản trị viên sử dụng các lệnh `/debt reload`, `/debt delete`, và `/debt detail`.

---

## ❓ Khắc Phục Sự Cố

*   **"Bạn không có đủ tiền."**: Đảm bảo bạn có đủ tiền trong tài khoản để thực hiện giao dịch (cho vay hoặc thanh toán).
*   **"Không tìm thấy khoản nợ với ID {id}."**: Kiểm tra lại ID khoản nợ.
*   **"Bạn không phải là người cho vay của khoản nợ này để chấp nhận."**: Bạn chỉ có thể chấp nhận các khoản nợ mà bạn là người cho vay.
*   **"Khoản nợ {id} đã được chấp nhận rồi."**: Khoản nợ này đã được chấp nhận trước đó.
*   **"Khoản nợ {id} đã được thanh toán rồi."**: Khoản nợ này đã được thanh toán hoàn toàn.
*   **"Bạn không thể yêu cầu nợ từ chính mình."**: Bạn không thể tự vay tiền từ mình.
*   **"Đã xảy ra lỗi cơ sở dữ liệu. Kiểm tra console."**: Liên hệ quản trị viên máy chủ để kiểm tra log console để biết chi tiết lỗi.

---

Cảm ơn bạn đã sử dụng NightfallDebtSystem!
