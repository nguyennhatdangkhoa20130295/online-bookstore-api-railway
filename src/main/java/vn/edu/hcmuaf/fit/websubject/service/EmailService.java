package vn.edu.hcmuaf.fit.websubject.service;

import org.springframework.stereotype.Component;
import vn.edu.hcmuaf.fit.websubject.entity.Mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.internet.MimeUtility;

@Component
public class EmailService {

    private Properties properties;
    private Session session;
    private String username;
    private static EmailService mailService;

    private EmailService() {

        properties = new Properties();

        properties.put(Mail.HOST, Mail.SERVER);

        properties.put(Mail.URL_PORT, Mail.PORT);

        properties.put(Mail.AUTH, Mail.AUTH_STATUS);

        properties.put(Mail.URL_TLS, Mail.TLS_STATUS);

        initializedSesstion(Mail.USERNAME, Mail.PASSWORD);

    }


    public void initializedSesstion(String username, String password) {
        session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        //  gán username cho username hiện tại để gửi email
        this.username = username;

    }

    public boolean sendEmailForgot(String recipientEmail, String otp, int type) {
        boolean test = false;
        String subject = "";
        String text = "";
        if (type == 1) {
            try {
                subject = MimeUtility.encodeText("Đặt lại mật khẩu tài khoản GoldLeaf", "UTF-8", "B");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            text = "<h1 style=\"padding: 0; font-size: 25px;color: #ff0000;font-family:sans-serif\">Đặt lại mật khẩu tài khoản GoldLeaf</h1><p style=\"padding: 0;font-size: 14px;color: #000000;font-family:sans-serif\">Để đặt lại mật khẩu cho tài khoản, hãy nhập OTP: <strong style=\"font-size: 18px; color: #ff0000; font-family:sans-serif\">" + otp + "</strong></p><p style=\"padding: 0;font-size: 14px;color: #000000;font-family:sans-serif\">Cảm ơn bạn,</p><p style=\"padding: 0;font-size: 14px;color: #00BFFF;font-family:sans-serif\">GoldLeaf.</p>";
        } else {
            try {
                subject = MimeUtility.encodeText("Tạo tài khoản GoldLeaf", "UTF-8", "B");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            text = "<h1 style=\"padding: 0; font-size: 25px;color: #ff0000;font-family:sans-serif\">Tạo tài khoản GoldLeaf</h1><p style=\"padding: 0;font-size: 14px;color: #000000;font-family:sans-serif\">Để xác nhận tạo tài khoản, hãy nhập OTP: <strong style=\"font-size: 18px; color: #ff0000; font-family:sans-serif\">" + otp + "</strong></p><p style=\"padding: 0;font-size: 14px;color: #000000;font-family:sans-serif\">Cảm ơn bạn,</p><p style=\"padding: 0;font-size: 14px;color: #00BFFF;font-family:sans-serif\">GoldLeaf.</p>";
        }

        String toEmail = recipientEmail;
        String fromEmail = Mail.USERNAME;
        String password = Mail.PASSWORD;

        try {
            // your host email smtp server details
            Properties pr = new Properties();
            pr.setProperty(Mail.HOST, Mail.SERVER);
            pr.setProperty(Mail.URL_PORT, Mail.PORT);
            pr.setProperty(Mail.AUTH, Mail.AUTH_STATUS);
            pr.setProperty(Mail.URL_TLS, Mail.TLS_STATUS);
            pr.put("mail.smtp.socketFactory.port", "587");
            pr.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            //get session to authenticate the host email address and password
            Session session = Session.getInstance(pr, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

            //set email message details
            Message mess = new MimeMessage(session);

            //set from email address
            mess.setFrom(new InternetAddress(fromEmail));
            //set to email address or destination email address
            mess.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            //set email subject
            mess.setSubject(subject);

            //set message text
            mess.setContent(text, "text/html; charset=UTF-8");
            //send the message
            Transport.send(mess);
            System.out.println("Đã gửi email!");
            test = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return test;
    }

    public boolean sendEmailContact(String recipientEmail, String title, String content) {
        boolean test = false;
        String subject = "";
        String text = "";

        try {
            subject = MimeUtility.encodeText("Trả lời liên hệ: " + title, "UTF-8", "B");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        text = "<h1 style=\"padding: 0; font-size: 25px;color: #ff0000;font-family:sans-serif\">Trả lời liên hệ GoldLeaf</h1>\n" +
                "<p style=\"padding: 0;font-size: 14px;color: #000000;font-family:sans-serif\">Xin chào,</p>\n" +
                "<p style=\"padding: 0;font-size: 14px;color: #000000;font-family:sans-serif\">Cảm ơn bạn đã liên hệ với chúng tôi. Dưới đây là câu trả lời của chúng tôi:</p>\n" +
                content+
                "<p style=\"padding: 0;font-size: 14px;color: #000000;font-family:sans-serif\">Nếu bạn có bất kỳ câu hỏi nào khác, xin đừng ngần ngại liên hệ lại với chúng tôi.</p>\n" +
                "<p style=\"padding: 0;font-size: 14px;color: #000000;font-family:sans-serif\">Cảm ơn bạn,</p>\n" +
                "<p style=\"padding: 0;font-size: 14px;color: #00BFFF;font-family:sans-serif\">GoldLeaf.</p>";

        String toEmail = recipientEmail;
        String fromEmail = Mail.USERNAME;
        String password = Mail.PASSWORD;

        try {
            // your host email smtp server details
            Properties pr = new Properties();
            pr.setProperty(Mail.HOST, Mail.SERVER);
            pr.setProperty(Mail.URL_PORT, Mail.PORT);
            pr.setProperty(Mail.AUTH, Mail.AUTH_STATUS);
            pr.setProperty(Mail.URL_TLS, Mail.TLS_STATUS);
            pr.put("mail.smtp.socketFactory.port", "587");
            pr.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            //get session to authenticate the host email address and password
            Session session = Session.getInstance(pr, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

            //set email message details
            Message mess = new MimeMessage(session);

            //set from email address
            mess.setFrom(new InternetAddress(fromEmail));
            //set to email address or destination email address
            mess.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            //set email subject
            mess.setSubject(subject);

            //set message text
            mess.setContent(text, "text/html; charset=UTF-8");
            //send the message
            Transport.send(mess);
            System.out.println("Đã gửi email!");
            test = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return test;
    }

}

