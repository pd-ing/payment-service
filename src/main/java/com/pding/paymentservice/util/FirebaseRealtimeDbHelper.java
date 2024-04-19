package com.pding.paymentservice.util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pding.paymentservice.BaseService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class FirebaseRealtimeDbHelper extends BaseService {


    public void updateWalletBalanceInFirebase(String userId, BigDecimal leafBalance, BigDecimal treesBalance) {
        try {
            Float leaf = null;
            Float trees = null;
            if (leafBalance != null) {
                leaf = leafBalance.floatValue();
            }
            if (treesBalance != null) {
                trees = treesBalance.floatValue();
            }
            updateWalletBalanceInFirebase(userId, leaf, trees);
        } catch (Exception ex) {
            pdLogger.logException(ex);
        }
    }

    private void updateWalletBalanceInFirebase(String userId, Float leafBalance, Float treesBalance) {

        DatabaseReference walletRef = FirebaseDatabase.getInstance()
                .getReference(generatePath(userId));

        Map<String, Object> map = new HashMap<>();
        if (leafBalance != null) {
            map.put("leaf", leafBalance);
        }
        if (treesBalance != null) {
            map.put("trees", treesBalance);
        }
        walletRef.updateChildren(map, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                System.out.println("Data could not be saved " + databaseError.getMessage());
                pdLogger.logException(new Exception(databaseError.getMessage()));
                pdLogger.logInfo("update_realtime_db_wallet", "userId:" + userId + " leafBalance: " + leafBalance + " treeBalance: " + treesBalance);
            }
            // all good. data saved
        });
    }

    public String generatePath(String userId) {
        return "/pding/users/" + userId + "/wallet";
    }

}
