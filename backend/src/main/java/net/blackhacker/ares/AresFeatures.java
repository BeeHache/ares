package net.blackhacker.ares;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum AresFeatures implements Feature {

    @Label("Public Launch") @EnabledByDefault PUBLIC_LAUNCH,
    @Label("Maintenance Mode") MAINTENANCE_MODE,
    @Label("Full Search") FULL_SEARCH,
    @Label("Social Login") SOCIAL_LOGIN,
    @Label("GitHub Login") GITHUB_LOGIN,
    @Label("Google Login") GOOGLE_LOGIN,
    @Label("Facebook Login") FACEBOOK_LOGIN,
    @Label("Apple Login") APPLE_LOGIN,
    @Label("Microsoft Login") MICROSOFT_LOGIN;
}
