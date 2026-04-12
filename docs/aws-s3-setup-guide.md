# Hướng Dẫn Thiết Lập AWS S3 Từ Con Số 0 (Dành Cho Người Mới)

Là một người mới tiếp cận AWS, hệ sinh thái của nó trông sẽ rất phức tạp vì có quá nhiều dịch vụ. Tuy nhiên, để lưu trữ ảnh/video cho EduStream, bạn chỉ cần quan tâm đúng **2 dịch vụ**:
1. **S3 (Simple Storage Service):** Cái kho (bucket) để chứa file.
2. **IAM (Identity and Access Management):** Nơi cấp chìa khóa (Access Key / Secret Key) cho code Spring Boot để có quyền mở kho S3 đó.

Dưới đây là 3 bước "Cầm tay chỉ việc":

---

## BƯỚC 1: Tạo Kho Lưu Trữ (S3 Bucket)
1. Đăng nhập vào [Hệ quản trị AWS Console](https://console.aws.amazon.com/).
2. Trên thanh tìm kiếm, gõ **S3** và bấm vào đó.
3. Bấm nút màu cam **Create bucket** (Tạo kho mới).
4. Điền các thông tin sau:
   - **Bucket name:** Đặt tên gì cũng được, phải là duy nhất trên toàn thế giới chữ thường (VD: `edustream-media-storage-xyz`).
   - **AWS Region:** Chọn `ap-southeast-1` (Asia Pacific (Singapore)) cho gần Việt Nam để mạng load video nhanh nhất!
   - Kéo xuống mục **Object Ownership**: Để nguyên `ACLs disabled`.
   - Kéo xuống mục **Block Public Access settings for this bucket**: Bạn hãy **TẮT TICK (Uncheck)** cái ô ngầm định `Block all public access`. Một ô cảnh báo sẽ hiện ra, bạn tích vào `I acknowledge...` xác nhận bạn hiểu việc tắt khóa để người ngoài có thể xem được video khóa học.
   - Các thông số còn lại giữ nguyên. Kéo xuống dưới cùng và ấn **Create bucket**.

---

## BƯỚC 2: Cấp Quyền Đọc Công Khai (Bucket Policy)
Chúng ta vừa tạo khóa nhưng mặc định AWS vẫn giấu file. Ta cần khai báo luật: "Bất kỳ ai có đường link URL đều có thể XEM (đọc) được file".

1. Bấm vào cái tên Bucket bạn vừa tạo.
2. Chọn tab **Permissions**.
3. Kéo xuống phần **Bucket policy** và ấn nút **Edit**.
4. Dán đoạn JSON dưới đây vào ô trống (Nhớ thay chữ `ten-bucket-cua-ban` thành tên thật của bucket nhé):

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::ten-bucket-cua-ban/*"
        }
    ]
}
```
5. Ấn **Save changes**.

---

## BƯỚC 3: Lấy Chìa Khóa Cho Spring Boot (IAM Access Keys)
Cái kho đã xong, giờ ta cần chìa khóa để nhét vào file `.env` của Spring Boot để code có quyền Nạp file (Upload) vào kho!

1. Nhìn lên thanh tìm kiếm trên cùng của AWS gõ: **IAM** -> Bấm vào.
2. Menu bên trái, bấm vào **Users** (Người dùng).
3. Bấm nút **Create user**:
   - Tên User: Gõ `edustream-uploader` -> **Next**.
   - Quyền hạn (Permissions): Chọn hộp thứ 3 **"Attach policies directly"**.
   - Thanh Search gõ tìm `AmazonS3FullAccess` -> Tick vào cái hộp của nó -> **Next** -> **Create user**.
4. Lấy chìa khóa:
   - Bấm vào tên User `edustream-uploader` vừa tạo.
   - Chuyển sang tab **Security credentials**.
   - Kéo xuống phần **Access keys**, ấn **Create access key**.
   - Chọn **Third-party service** -> Tích xác nhận "I understand..." -> **Next** -> **Create access key**.

🔴 **RẤT QUAN TRỌNG:**
Màn hình sẽ hiển thị ra 2 chuỗi bí mật. Nếu bạn tắt màn hình này đi, nó sẽ CHẮC CHẮN BIẾN MẤT MÃI MÃI, CÓ TIỀN CŨNG KHÔNG LẤY LẠI ĐƯỢC SECRET KEY.
Hãy Copy ngay 2 mã này dán vào 1 file Notepad:
- **Access key**
- **Secret access key**

---

## BƯỚC 4: Nhúng vào .env local
Bạn mở thư mục dự án java, tạo (hoặc mở) file `.env` ở thư mục gốc (cùng cấp với pom.xml) và dán 4 dòng này vào:

```env
AWS_S3_REGION=ap-southeast-1
AWS_S3_BUCKET=ten-bucket-cua-ban
AWS_ACCESS_KEY=dán-mã-access-key-vào-đây
AWS_SECRET_KEY=dán-mã-secret-key-vào-đây
```

*(Tuyệt đối không push file .env này lên Github kẻo bị tool hacker quét ăn cắp tiền thẻ tín dụng)*.

**Xong!** Bạn vừa học xong cách xử lý 50% khối lượng kiến thức khó nhất của hạ tầng VOD rồi đấy. Để tôi lo phần code Java tích hợp nhé.
