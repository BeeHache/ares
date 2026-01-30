package net.blackhacker.ares.service;

import jakarta.mail.internet.MimeMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;

@Slf4j
@Service
public class EmailSenderService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;


    private final int copyrightStartYear;

    EmailSenderService(JavaMailSender javaMailSender,
                       TemplateEngine templateEngine,
                       @Value("${app.copyright.start-year}") int copyrightStartYear){
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.copyrightStartYear = copyrightStartYear;
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
        try {
            Context context = new Context();
            
            // Add copyright and current year to the context
            context.setVariable("copyrightStartYear", copyrightStartYear);
            context.setVariable("currentYear", Year.now().getValue());

            // Add other arguments
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i += 2) {
                    context.setVariable(args[i], args[i + 1]);
                }
            }

            String processedContent = templateEngine.process(template, context);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setText(processedContent, true);
            javaMailSender.send(mimeMessage);
        } catch (Exception e){
            log.error("Error sending email", e);
        }
    }
}
