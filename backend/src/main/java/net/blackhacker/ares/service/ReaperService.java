package net.blackhacker.ares.service;

import net.blackhacker.ares.repository.jpa.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReaperService {

    final UserRepository userRepository;

    public ReaperService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }


    @Scheduled(cron = "0 0 2 * * *")
    public void reap() {
        userRepository.findCanceledUserIds().forEach(userId -> {
            userRepository.deleteById(userId);
            userRepository.flush();
        });
    }
}
