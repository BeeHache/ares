package net.blackhacker.ares;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum AresFeatures implements Feature {

    @Label("Public Launch") @EnabledByDefault PUBLIC_LAUNCH,
    @Label("Maintenance Mode") MAINTENANCE_MODE,
    @Label("Full Search") FULL_SEARCH,

    @Label("Social Login") @EnabledByDefault SOCIAL_LOGIN {
        @Override
        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }
    },
    @Label("GitHub Login") @EnabledByDefault GITHUB_LOGIN {
        @Override
        public boolean isActive() {
            return SOCIAL_LOGIN.isActive() && FeatureContext.getFeatureManager().isActive(this);
        }
    },
    @Label("Google Login") @EnabledByDefault GOOGLE_LOGIN {
        @Override
        public boolean isActive() {
            return SOCIAL_LOGIN.isActive() && FeatureContext.getFeatureManager().isActive(this);
        }
    },
    @Label("Facebook Login") @EnabledByDefault FACEBOOK_LOGIN {
        @Override
        public boolean isActive() {
            return SOCIAL_LOGIN.isActive() && FeatureContext.getFeatureManager().isActive(this);
        }
    },
    @Label("Apple Login") @EnabledByDefault APPLE_LOGIN {
        @Override
        public boolean isActive() {
            return SOCIAL_LOGIN.isActive() && FeatureContext.getFeatureManager().isActive(this);
        }
    },
    @Label("Microsoft Login") @EnabledByDefault MICROSOFT_LOGIN {
        @Override
        public boolean isActive() {
            return SOCIAL_LOGIN.isActive() && FeatureContext.getFeatureManager().isActive(this);
        }
    };

    @Override
    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
