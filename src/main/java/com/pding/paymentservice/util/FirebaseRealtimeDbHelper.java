package com.pding.paymentservice.util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pding.paymentservice.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FirebaseRealtimeDbHelper extends BaseService {


    public void updateSpendingWalletBalanceInFirebase(String userId, BigDecimal leafBalance, BigDecimal treesBalance) {
//        log.info("Updating spending wallet balance in firebase for userId: {}, leafBalance: {}, treesBalance: {}", userId, leafBalance, treesBalance);
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
//            log.info("Updated spending wallet balance in firebase for userId: {}, leafBalance: {}, treesBalance: {}", userId, leafBalance, treesBalance);
        } catch (Exception ex) {
            pdLogger.logException(ex);
        }
    }


    public void updateEarningWalletBalanceInFirebase(String userId, BigDecimal leafBalance, BigDecimal treesBalance) {
//        log.info("Updating earning wallet balance in firebase for userId: {}, leafBalance: {}, treesBalance: {}", userId, leafBalance, treesBalance);
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
//            log.info("Updated earning wallet balance in firebase for userId: {}, leafBalance: {}, treesBalance: {}", userId, leafBalance, treesBalance);
        } catch (Exception ex) {
            log.error("Error updating earning wallet balance in firebase for userId: {}, leafBalance: {}, treesBalance: {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(leafBalance), LogSanitizer.sanitizeForLog(treesBalance));
            pdLogger.logException(ex);
        }
    }

    public void updateCallChargesDetailsInFirebase(String userId, String callId, BigDecimal leafDeducted, BigDecimal leafEarned, BigDecimal treeDeducted, BigDecimal treeEarned) {
        try {
            Float leafD = null;
            Float leafE = null;
            if (leafDeducted != null) {
                leafD = leafDeducted.floatValue();
            }
            if (leafEarned != null) {
                leafE = leafEarned.floatValue();
            }

            Float treeD = null;
            Float treeE = null;

            if (treeDeducted != null) {
                treeD = treeDeducted.floatValue();
            }
            if (treeEarned != null) {
                treeE = treeEarned.floatValue();
            }
            DatabaseReference callChargeDetailsRef = FirebaseDatabase.getInstance()
                    .getReference(generateCallDetailsPath(userId, callId));

            updateCallChargeDetailsInFirebase(userId, leafD, leafE, treeD, treeE, callChargeDetailsRef);
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
//                pdLogger.logInfo("update_realtime_db_wallet", "userId:" + userId + " leafBalance: " + leafBalance + " treeBalance: " + treesBalance);
            }
            // all good. data saved
        });
    }

    private void updateCallChargeDetailsInFirebase(String userId, Float leafDeducted, Float leafEarned, Float treeDeducted, Float treeEarned, DatabaseReference callRef) {
        Map<String, Object> map = new HashMap<>();
        if (leafDeducted != null) {
            map.put("leafDeducted", leafDeducted);
        }
        if (leafEarned != null) {
            map.put("leafEarned", leafEarned);
        }
        if (treeDeducted != null) {
            map.put("treeDeducted", treeDeducted);
        }

        if (treeEarned != null) {
            map.put("treeEarned", treeEarned);
        }

        callRef.updateChildren(map, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                log.error("Data could not be saved " + databaseError.getMessage());
//                pdLogger.logException(new Exception(databaseError.getMessage()));
//                pdLogger.logInfo("update_realtime_db_wallet", "userId:" + userId + " leafDeducted: " + leafDeducted + " leafEarned: " + leafEarned);
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
