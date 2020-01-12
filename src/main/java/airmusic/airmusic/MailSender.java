package airmusic.airmusic;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public  class MailSender extends Thread{
    private static final String SENDER = "vasilt0shkov93@gmail.com";
    private static final String PASSWORD = "Iceman-1993";

    private String to;
    private String subject;
    private String body;
    public MailSender(String to, String subject, String body){
        this.to = to;
        this.subject=subject;
        this.body =body;
    }
    @Override
    public void run() {
        sendMail(to,subject,body);
    }

    public static void sendMail(String to, String subject, String body) {
        final String username = SENDER;
        final String password = PASSWORD;
        Properties prop = new Properties();
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "25");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS


        Session session = Session.getInstance(prop,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}