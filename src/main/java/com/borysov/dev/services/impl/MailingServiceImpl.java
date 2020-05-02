package com.borysov.dev.services.impl;

import com.borysov.dev.services.MailingService;
import lombok.extern.log4j.Log4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ResourceBundle;

@Service
@Transactional
@Log4j
public class MailingServiceImpl implements MailingService {

    @Autowired
    private JavaMailSender sender;

    private void sendMail(String email, String text) {
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
}
