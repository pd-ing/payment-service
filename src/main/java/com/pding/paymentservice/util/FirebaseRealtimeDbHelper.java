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


    public void updateSpendingWalletBalanceInFirebase(String userId, BigDecimal leafBalance, BigDecimal treesBalance) {
        try {
            Float leaf = null;
            Float trees = null;
            if (leafBalance != null) {
                leaf = leafBalance.floatValue();
            }
            if (treesBalance != null) {
                trees = treesBalance.floatValue();
            }
            DatabaseReference spendingWalletRef = FirebaseDatabase.getInstance()
                    .getReference(generateSpendingWalletPath(userId));

            updateWalletBalanceInFirebase(userId, leaf, trees, spendingWalletRef);
        } catch (Exception ex) {
            pdLogger.logException(ex);
        }
    }


    public void updateEarningWalletBalanceInFirebase(String userId, BigDecimal leafBalance, BigDecimal treesBalance) {
        try {
            Float leaf = null;
            Float trees = null;
            if (leafBalance != null) {
                leaf = leafBalance.floatValue();
            }
            if (treesBalance != null) {
                trees = treesBalance.floatValue();
            }
            DatabaseReference earningWalletRef = FirebaseDatabase.getInstance()
                    .getReference(generateEarningWalletPath(userId));

            updateWalletBalanceInFirebase(userId, leaf, trees, earningWalletRef);
        } catch (Exception ex) {
            pdLogger.logException(ex);
        }
    }

    public void updateCallChargesDetailsInFirebase(String userId, String callId, BigDecimal leafDeducted, BigDecimal leafEarned) {
        try {
            Float leafD = null;
            Float leafE = null;
            if (leafDeducted != null) {
                leafD = leafDeducted.floatValue();
            }
            if (leafEarned != null) {
                leafE = leafEarned.floatValue();
            }
            DatabaseReference callChargeDetailsRef = FirebaseDatabase.getInstance()
                    .getReference(generateCallDetailsPath(userId, callId));

            updateCallChargeDetailsInFirebase(userId, leafD, leafE, callChargeDetailsRef);
        } catch (Exception ex) {
            pdLogger.logException(ex);
        }
    }


    private void updateWalletBalanceInFirebase(String userId, Float leafBalance, Float treesBalance, DatabaseReference walletRef) {
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

    private void updateCallChargeDetailsInFirebase(String userId, Float leafDeducted, Float leafEarned, DatabaseReference callRef) {
        Map<String, Object> map = new HashMap<>();
        if (leafDeducted != null) {
            map.put("leafDeducted", leafDeducted);
        }
        if (leafEarned != null) {
            map.put("leafEarned", leafEarned);
        }
        callRef.updateChildren(map, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                System.out.println("Data could not be saved " + databaseError.getMessage());
                pdLogger.logException(new Exception(databaseError.getMessage()));
                pdLogger.logInfo("update_realtime_db_wallet", "userId:" + userId + " leafDeducted: " + leafDeducted + " leafEarned: " + leafEarned);
            }
            // all good. data saved
        });
    }

    private String generateSpendingWalletPath(String userId) {
        return "/pding/users/" + userId + "/wallet";
    }

    private String generateEarningWalletPath(String userId) {
        return "/pding/users/" + userId + "/earning";
    }

    private String generateCallDetailsPath(String userId, String callId) {
        return "/calls/" + callId + "/" + userId;
    }
}
