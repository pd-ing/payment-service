package com.pding.paymentservice;

import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.security.LoggedInUserRecord;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.protocol.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PdLogger {

    @Value("{sentry.env}")
    String env;

    @Autowired
    private AuthHelper authHelper;

    public void logException(EVENT event, LEVEL level, Exception ex) {

        LoggedInUserRecord loggedInUserRecord = null;

        try {
            loggedInUserRecord = authHelper.getLoggedInFirebaseUser();
        } catch (Exception e) {
        }

        User user = new User();
        if (loggedInUserRecord != null) {
            user.setEmail(loggedInUserRecord.getEmail());
            user.setId(loggedInUserRecord.getUid());
        }

        Sentry.configureScope(scope -> {

            if (event != null) {
                scope.setExtra("task", event.name());
            }

            scope.setUser(user);

            scope.setLevel(SentryLevel.ERROR);

            scope.setTag("level", level.getValue());
            scope.setTag("env", env);

        });

        Sentry.captureException(ex);

    }

    public void logException(EVENT event, Exception ex) {
        logException(event, event.getLevel(), ex);
    }

    public void logException(LEVEL level, Exception ex) {
        logException(null, level, ex);
    }

    public void logException(Exception ex) {
        logException(null, LEVEL.p3, ex);
    }

    public static enum EVENT {
        //p0
        USER_SIGN_UP(LEVEL.p0.value),
        GET_USER(LEVEL.p0.value),
        UPDATE_USER(LEVEL.p0.value),
        FIREBASE_AUTH(LEVEL.p0.value),
        SERVICE_AUTH(LEVEL.p0.value),


        //p1
        IMAGE_CDN_LINK(LEVEL.p1.value),
        USER_FOLLOWING(LEVEL.p1.value),
        GET_USER_LIST(LEVEL.p1.value),
        UPLOAD_IMAGE(LEVEL.p1.value);

        private final String value;

        private EVENT(String value) {
            this.value = value;
        }

        public LEVEL getLevel() {
            return LEVEL.valueOf(value);
        }

    }

    public static enum LEVEL {
        p0("p0"), // critical, should be fixed within same day
        p1("p1"), // should be fixed within a week
        p2("p2"), // should be fixed within a month
        p3("p3"); // should be fixed or add to backlog and see when to fix

        private final String value;

        private LEVEL(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

}
