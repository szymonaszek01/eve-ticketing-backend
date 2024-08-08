package com.eve.ticketing.app.emailnotification;

import com.eve.ticketing.app.emailnotification.dto.EmailDto;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@EnableAsync
@Component
@AllArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    @Async
    public void send(EmailDto emailDto) throws IllegalStateException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMultipart mimeMultipart = new MimeMultipart("related");
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(emailDto.getEmail(), "text/html");
            mimeMultipart.addBodyPart(mimeBodyPart);
            mimeBodyPart = new MimeBodyPart();
            DataSource dataSource = new FileDataSource("email-notification/src/main/resources/assets/logo.png");
            mimeBodyPart.setDataHandler(new DataHandler(dataSource));
            mimeBodyPart.setHeader("Content-ID", "<image>");
            mimeMultipart.addBodyPart(mimeBodyPart);

            if (emailDto.getAttachment() != null && !StringUtils.isBlank(emailDto.getAttachmentName()) && !StringUtils.isBlank(emailDto.getAttachmentType())) {
                mimeBodyPart = new MimeBodyPart();
                dataSource = new ByteArrayDataSource(new ByteArrayInputStream(emailDto.getAttachment()), emailDto.getAttachmentType());
                mimeBodyPart.setDataHandler(new DataHandler(dataSource));
                mimeBodyPart.setFileName(emailDto.getAttachmentName());
                mimeMultipart.addBodyPart(mimeBodyPart);
            }

            mimeMessage.setFrom("eve.ticketing.contact@gmail.com");
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDto.getTo()));
            mimeMessage.setSubject(emailDto.getSubject());
            mimeMessage.setContent(mimeMultipart);
            mailSender.send(mimeMessage);
        } catch (MessagingException | IOException e) {
            throw new IllegalStateException("Failed to send email");
        }
    }
}
