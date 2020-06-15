package com.borysov.dev.services.impl;

import com.borysov.dev.services.MailingService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.ResourceBundle;

@Service
@Log4j
public class MailingServiceImpl implements MailingService {

    @Autowired
    private JavaMailSender sender;

    public void sendMail(String email, String text) {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setTo(email);
            helper.setText(text);
            helper.setSubject(ResourceBundle.getBundle("messages").getString("title"));
            sender.send(message);
        } catch (MessagingException | MailSendException | MailAuthenticationException e) {
            log.error(e.toString());
        }
    }

/*    public void sendCongratulationMail(User user) {
        String text = ResourceBundle.getBundle("messages").getString("mailing.congratulationMail");
        sendMail(user.getEmail(), String.format(text, user.getFirstName()));
    }*/

    @Scheduled(fixedRate =  60*1000)
    public void scheduleCheckWorksFine() {
        log.info("Try to send Scheduled message");
        sendMail("defan3171@gmail.com", "App works fine(Scheduled) " + new Date());
    }
}
