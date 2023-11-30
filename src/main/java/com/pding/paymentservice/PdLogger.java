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

    public void logException(EVENT event, Priority priority, Exception ex) {

        Sentry.configureScope(scope -> {
            if (event != null) {
                scope.setExtra("task", event.name());
            }

            scope.setLevel(SentryLevel.ERROR);
            scope.setTag("priority", priority.name()); // Using name instead of getValue()
        });

        Sentry.captureException(ex);
    }

    public void logException(EVENT event, Exception ex) {
        logException(event, event.getLevel(), ex);
    }

    public void logException(Priority priority, Exception ex) {
        logException(null, priority, ex);
    }

    public void logException(Exception ex) {
        logException(null, Priority.p3, ex);
    }

    public static enum EVENT {
        //p0

        //p1
        IMAGE_CDN_LINK(Priority.p1);

        private final Priority priority;

        private EVENT(Priority priority) {
            this.priority = priority;
        }

        public Priority getLevel() {
            return priority;
        }
    }

    public static enum Priority {
        p0, // critical, should be fixed within same day
        p1, // should be fixed within a week
        p2, // should be fixed within a month
        p3 // should be fixed or add to backlog and see when to fix
    }

}
