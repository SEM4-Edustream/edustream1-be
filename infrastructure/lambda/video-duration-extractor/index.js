const { S3Client, GetObjectCommand } = require("@aws-sdk/client-s3");
const { getSignedUrl } = require("@aws-sdk/s3-request-presigner");
const ffmpeg = require("fluent-ffmpeg");
const ffprobePath = require("@ffprobe-installer/ffprobe").path;
const axios = require("axios");

// Cấu hình ffprobe
ffmpeg.setFfprobePath(ffprobePath);

const s3Client = new S3Client({ region: process.env.AWS_REGION || "ap-southeast-1" });

// Các biến môi trường cần thiết cấu hình trên giao diện AWS Lambda:
// BACKEND_API_URL: https://api.edu-stream.dev/api/webhooks/video-duration
// WEBHOOK_SECRET: lamda-secret-edustream-2026

exports.handler = async (event) => {
    console.log("Received event:", JSON.stringify(event, null, 2));

    try {
        // Lấy thông tin bucket và key từ sự kiện S3
        const bucket = event.Records[0].s3.bucket.name;
        const key = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, " "));

        console.log(`Processing file: ${key} from bucket: ${bucket}`);

        // Tên file mà Frontend tạo ra có dạng: {lessonId}_{timestamp}_{randomName}.mp4
        // VD: 123e4567-e89b-12d3_1684345234_abc.mp4
        // Chú ý: Có thể key chứa cả thư mục ví dụ: videos/123e4567-e89b-12d3_1684345234_abc.mp4
        const fileName = key.split('/').pop(); 
        const lessonId = fileName.split('_')[0];

        if (!lessonId || lessonId.length < 10) {
            console.log("Could not extract lessonId from filename. Skipping.");
            return;
        }

        // Tạo Pre-signed URL tạm thời (có giá trị 15 phút) để ffprobe đọc
        const command = new GetObjectCommand({
            Bucket: bucket,
            Key: key,
        });
        const presignedUrl = await getSignedUrl(s3Client, command, { expiresIn: 900 });

        // Lấy thời lượng video bằng ffprobe
        const durationSeconds = await new Promise((resolve, reject) => {
            ffmpeg.ffprobe(presignedUrl, (err, metadata) => {
                if (err) {
                    reject(err);
                } else {
                    const duration = metadata.format.duration;
                    resolve(Math.round(duration));
                }
            });
        });

        console.log(`Extracted duration: ${durationSeconds} seconds for lesson: ${lessonId}`);

        // Gửi kết quả về Backend
        const backendUrl = process.env.BACKEND_API_URL || "http://localhost:8080/api/webhooks/video-duration";
        const webhookSecret = process.env.WEBHOOK_SECRET || "lamda-secret-edustream-2026";

        const response = await axios.put(backendUrl, {
            lessonId: lessonId,
            durationSeconds: durationSeconds,
            secretKey: webhookSecret
        });

        console.log("Successfully updated backend:", response.data);

        return {
            statusCode: 200,
            body: JSON.stringify('Successfully processed video duration.'),
        };

    } catch (error) {
        console.error("Error processing video:", error);
        throw error;
    }
};
