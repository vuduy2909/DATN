package com.capstone.wellnessnavigatorgym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * Gửi OTP đặt lại mật khẩu qua email
     */
    public void sendPasswordResetOTP(String toEmail, String otp) {
        try {
            logger.info("Preparing to send OTP to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("StartGym - Mã xác thực đặt lại mật khẩu");
            helper.setFrom("vuduynambinh@gmail.com");
            
            String htmlContent = createOTPEmailContent(otp);
            helper.setText(htmlContent, true);
            
            logger.info("Sending email...");
            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tạo nội dung HTML cho email OTP
     */
    private String createOTPEmailContent(String otp) {
        return "<!DOCTYPE html>" +
            "<html lang=\"vi\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<title>Mã xác thực StartGym</title>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f4f4f4; }" +
            ".container { background-color: white; padding: 40px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }" +
            ".header { text-align: center; margin-bottom: 30px; }" +
            ".logo { font-size: 32px; font-weight: bold; color: #CF4044; margin-bottom: 10px; }" +
            ".otp-code { background: linear-gradient(135deg, #CF4044 0%, #B8363C 100%); color: white; font-size: 36px; font-weight: bold; text-align: center; padding: 20px; border-radius: 8px; margin: 30px 0; letter-spacing: 8px; }" +
            ".warning { background-color: #fff3cd; border: 1px solid #ffeaa7; color: #856404; padding: 15px; border-radius: 5px; margin: 20px 0; }" +
            ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class=\"container\">" +
            "<div class=\"header\">" +
            "<div class=\"logo\">StartGym</div>" +
            "<h2>Mã xác thực đặt lại mật khẩu</h2>" +
            "</div>" +
            "<p>Xin chào,</p>" +
            "<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản StartGym của mình. Vui lòng sử dụng mã xác thực dưới đây để tiếp tục:</p>" +
            "<div class=\"otp-code\">" + otp + "</div>" +
            "<div class=\"warning\">" +
            "<strong>⚠️ Lưu ý quan trọng:</strong>" +
            "<ul>" +
            "<li>Mã xác thực này có hiệu lực trong <strong>15 phút</strong></li>" +
            "<li>Không chia sẻ mã này với bất kỳ ai</li>" +
            "<li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>" +
            "</ul>" +
            "</div>" +
            "<p>Sau khi nhập mã xác thực, bạn sẽ có thể tạo mật khẩu mới cho tài khoản của mình.</p>" +
            "<div class=\"footer\">" +
            "<p>Cảm ơn bạn đã sử dụng StartGym!</p>" +
            "<p>Email này được gửi tự động, vui lòng không trả lời.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }
    
    /**
     * Gửi email đơn giản (text) để test cấu hình
     */
    public void sendSimpleEmail(String toEmail, String subject, String content) {
        try {
            logger.info("Sending simple email to: {}", toEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            message.setFrom("vuduynambinh@gmail.com");
            
            mailSender.send(message);
            logger.info("Simple email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send simple email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
} 