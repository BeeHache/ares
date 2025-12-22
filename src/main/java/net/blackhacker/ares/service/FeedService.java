package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.FeedRepository;
import net.blackhacker.ares.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

@Service
public class FeedService {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private ReaderService readerService;

    void addFeed(String link) throws IOException {
        readerService.read(link);
    }

    Collection<Feed> getFeeds() throws IOException {
        return null;

    }
}
