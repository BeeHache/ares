package net.blackhacker.ares.service;

import lombok.NonNull;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailSenderService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    EmailSenderService(JavaMailSender javaMailSender, TemplateEngine templateEngine){
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    /**
     *  Builds and sends email based template file in resources/templates folder
     * @param to
     * @param from
     * @param subject
     * @param template
     * @param args name and value pairs of arguments to be replaced in
     *             the template file.
     */
    public void sendEmail(@NonNull String to, @NonNull String from, @NonNull String subject,
                          @NonNull String template, String ...args ) {
        Context context = new Context();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i += 2) {
                context.setVariable(args[i], args[i + 1]);
            }
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject(subject);
        message.setText(templateEngine.process("templates/"+template+".html", context));
        javaMailSender.send(message);
    }
}
