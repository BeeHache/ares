package net.blackhacker.ares.controller;

import net.blackhacker.ares.AresFeatures;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/features")
public class FeatureController {

    @GetMapping
    public Map<String, Boolean> getFeatures() {
        Map<String, Boolean> features = new HashMap<>();
        for (AresFeatures feature : AresFeatures.values()) {
            features.put(feature.name(), feature.isActive());
        }
        return features;
    }
}
