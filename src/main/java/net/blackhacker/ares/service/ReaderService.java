package net.blackhacker.ares.service;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
public class ReaderService {


    public void read(String url) throws IOException {

        List<Item> items = new RssReader().read(url).toList();
        if  (!items.isEmpty()) {
            Channel channel = items.getFirst().getChannel();
        }

    }
}
