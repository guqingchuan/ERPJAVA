package com.maksim.procurement_service.configuration;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${base.url}")
    private String BaseUrl;
    @Value("${email.url}")
    private String emailUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPurchaseOrderEmail(String to, byte[] pdfBytes, Long orderId) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            // true = multipart (za attachment i HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Purchase Order #" + orderId);

            // Linkovi za confirm i close
            String confirmUrl = emailUrl + "/purchase-orders/" + orderId + "/confirm";
            String closeUrl = emailUrl + "/purchase-orders/" + orderId + "/close";

            // HTML sadržaj email-a
            String htmlContent = """
                    <html>
                        <body>
                            <p>Hello,</p>
                            <p>Please see the attached purchase order.</p>
                            <p>
                                <a href="%s" style="display:inline-block;padding:10px 20px;background-color:#4CAF50;color:white;text-decoration:none;border-radius:5px;">
                                    Confirm
                                </a>
                                &nbsp;
                                <a href="%s" style="display:inline-block;padding:10px 20px;background-color:#f44336;color:white;text-decoration:none;border-radius:5px;">
                                    Cancel
                                </a>
                            </p>
                            <p>Best regards.</p>
                        </body>
                    </html>
                    """.formatted(confirmUrl, closeUrl);

            helper.setText(htmlContent, true);

            helper.addAttachment(
                    "purchase_order_" + orderId + ".pdf",
                    new ByteArrayResource(pdfBytes)
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}