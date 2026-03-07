package net.blackhacker.ares.controller;

import net.blackhacker.ares.AresFeatures;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;

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

    @PostMapping("/{featureName}")
    @PreAuthorize("hasRole('ADMIN')")
    public void setFeatureState(@PathVariable String featureName, @RequestParam boolean enabled) {
        AresFeatures feature = AresFeatures.valueOf(featureName);
        FeatureState state = new FeatureState(feature, enabled);
        FeatureContext.getFeatureManager().setFeatureState(state);
    }
}
