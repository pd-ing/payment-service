package com.pding.paymentservice;

import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.security.LoggedInUserRecord;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
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


    public void logInfo(String code, String messageData) {
        SentryEvent event = new SentryEvent();
        Message message = new Message();
        message.setMessage(messageData);
        event.setMessage(message);

        if (code != null) {
            event.setExtra("infoCode", code);
        }
        event.setLevel(SentryLevel.INFO);

        //Sentry.captureEvent(event);
    }

    public static enum EVENT {
        //p0

        EVENTS(Priority.p0),
        DONATE(Priority.p0),

        CHARGE(Priority.p0),

        GIVE_REFERRAL_COMMISSION(Priority.p0),

        BUY_VIDEO(Priority.p0),

        IS_VIDEO_PURCHASED(Priority.p0),

        STRIPE_WEBHOOK(Priority.p0),

        //p1
        IMAGE_CDN_LINK(Priority.p1),

        DONATION_HISTORY_FOR_USER(Priority.p1),

        DONATION_HISTORY_FOR_PD(Priority.p1),

        TOP_DONOR_LIST(Priority.p1),

        VIDEO_PURCHASE_HISTORY(Priority.p1),

        WALLET(Priority.p1),

        WALLET_HISTORY(Priority.p1),

        START_WITHDRAW(Priority.p1),

        COMPLETE_WITHDRAW(Priority.p1),

        START_REFERRAL_COMMISSION(Priority.p1),

        COMPLETE_REFERRAL_COMMISSION(Priority.p1),
        
        WITHDRAW_TRANSACTION(Priority.p1),

        VIDEO_EARNING_AND_SALES(Priority.p1),

        CALL_CHARGE(Priority.p1),

        START_PAYMENT(Priority.p0),

        CALL_DETAILS_HISTORY_FOR_PD(Priority.p1),
        CALL_DETAILS_HISTORY_FOR_USER(Priority.p1),
        TOP_CALLER_LIST_FOR_PD(Priority.p1),
        TOP_CALLER_LIST(Priority.p1),

        TOP_FAN_LIST(Priority.p1),

        UPDATE_REWARD_SETTING(Priority.p1),
        GET_REWARD_SETTING(Priority.p1);


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
