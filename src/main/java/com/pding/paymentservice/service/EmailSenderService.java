package com.pding.paymentservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage());
            String emailContent = getEmailContent(subject,text);
            String fromAddress = "no-reply@pd-ing.com";
            String senderName = "PD-ING LLC";
            String subjectText = "App Feature Report: " + subject;

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(fromAddress, senderName);

            helper.setSubject(subjectText);
            helper.setText(emailContent, true);
            javaMailSender.send(message);
            javaMailSender.send(messageHelper.getMimeMessage());
        } catch (MailException e) {
            e.printStackTrace();
            // Handle failure
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendEmailWithAttachment(String email, String subject, String body, File file) throws MessagingException, IOException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        String emailContent = getEmailContent(subject, body);

        String fromAddress = "no-reply@pd-ing.com";
        String senderName = "PD-ING LLC";
        String subjectText = subject;


        helper.setFrom(fromAddress, senderName);
        helper.setTo(email);
        helper.setSubject(subjectText);
        helper.setText(emailContent, true);

        MimeMultipart multipart = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(emailContent, "text/html");
        multipart.addBodyPart(textPart);

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.attachFile(file);
        multipart.addBodyPart(attachmentPart);

        mimeMessage.setContent(multipart);

        javaMailSender.send(mimeMessage);
    }

    private String getEmailContent(String title, String description) {
        String tepmplate = """
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
                        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                <html dir="ltr" xmlns="http://www.w3.org/1999/xhtml" lang="en"
                      style="font-family:arial, 'helvetica neue', helvetica, sans-serif">
                <head>
                    <meta charset="UTF-8">
                </head>
                <body>
                <p style="width: 800px; word-wrap: break-word;"><strong>Title:</strong> <span>--title--</span></p>
                <p style="width: 800px; word-wrap: break-word;"><strong>Description: </strong> <span>--description--</span></p>
                </body>
                </html>
                """;

        // Replace placeholders with actual values
        tepmplate = tepmplate.replace("--title--", title != null ? title : "Not provided");
        tepmplate = tepmplate.replace("--description--", description != null ? description : "Not provided");

        return tepmplate;
    }


}
