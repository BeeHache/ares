package net.blackhacker.ares;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum AresFeatures implements Feature {

    @Label("Public Launch")
    @EnabledByDefault
    PUBLIC_LAUNCH,


    @Label("Full Search")
    FULL_SEARCH;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
