package com.pding.paymentservice.app.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeafDonationConfig {
    List<LeafItem> Basic;

    List<LeafItem> Signature;

    public static LeafDonationConfig createConfig() {
        LeafItem basicItem1 = new LeafItem(1, 3, "https://drive.google.com/file/d/1_jXMsUcZ3tUAMxZVB1PlmAfoa5a3-JqO/view", null);
        LeafItem basicItem2 = new LeafItem(2, 10, "https://drive.google.com/file/d/1jqzPpeS1BEVECTliInQHpE4pIcxf5XWE/view", null);
        LeafItem basicItem3 = new LeafItem(3, 20, "https://drive.google.com/file/d/18PDjW_q0_jekFn61GeUQT_H6cyzNfIcm/view", null);
        LeafItem basicItem4 = new LeafItem(4, 33, "https://drive.google.com/file/d/1cJqoeedc6t2VodITZela8ERqeOmV2_AH/view", null);
        LeafItem basicItem5 = new LeafItem(5, 50, "https://drive.google.com/file/d/1SJSCFAF2pvm3h24yfHpQXSCrfnnvp0ie/view", null);
        LeafItem basicItem6 = new LeafItem(6, 77, "https://drive.google.com/file/d/17bTfCzfxkE9W8iDrWw_kDyI8YYe6WEuc/view", null);

        LeafItem basicItem7 = new LeafItem(7, 100, "https://drive.google.com/file/d/1vlZe5ymQUW3izpDOdGQLrLEB6dem5iiu/view", null);
        LeafItem basicItem8 = new LeafItem(8, 1000, "https://drive.google.com/file/d/1AU6yuoBtDvzCW1wkewOK9pQNP__F_gPT/view", null);
        LeafItem basicItem9 = new LeafItem(9, 2000, "https://drive.google.com/file/d/14N6Ly6hLCB2jycHLjzG-PPOUqgbS098I/view", null);
        LeafItem basicItem10 = new LeafItem(10, 3000, "https://drive.google.com/file/d/1jeBqKHJDCJgkCR42jFkryzFSoJtxeMLo/view", null);
        LeafItem basicItem11 = new LeafItem(11, 5000, "https://drive.google.com/file/d/1ooIt0dbiS29NVM1oEWtWxwS0GQbKVSwX/view", null);
        LeafItem basicItem12 = new LeafItem(12, 10000, "https://drive.google.com/file/d/1Gg1Jhu2qAA_MtyFBtYfjgy3i0TPq6kaB/view", null);

        List<LeafItem> basic = new ArrayList<>();
        basic.add(basicItem1);
        basic.add(basicItem2);
        basic.add(basicItem3);
        basic.add(basicItem4);
        basic.add(basicItem5);
        basic.add(basicItem6);
        basic.add(basicItem7);
        basic.add(basicItem8);
        basic.add(basicItem9);
        basic.add(basicItem10);
        basic.add(basicItem11);
        basic.add(basicItem12);


        LeafItem signatureItem1 = new LeafItem(1, 10, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");
        LeafItem signatureItem2 = new LeafItem(2, 20, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");
        LeafItem signatureItem3 = new LeafItem(3, 44, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");
        LeafItem signatureItem4 = new LeafItem(4, 50, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");
        LeafItem signatureItem5 = new LeafItem(5, 100, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");
        LeafItem signatureItem6 = new LeafItem(6, 1000, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");

        List<LeafItem> signature = new ArrayList<>();
        signature.add(signatureItem1);
        signature.add(signatureItem2);
        signature.add(signatureItem3);
        signature.add(signatureItem4);
        signature.add(signatureItem5);
        signature.add(signatureItem6);

        return new LeafDonationConfig(basic, signature);
    }
}

