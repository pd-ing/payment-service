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
        LeafItem basicItem1 = new LeafItem(1, 10, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");
        LeafItem basicItem2 = new LeafItem(2, 20, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");
        LeafItem basicItem3 = new LeafItem(3, 44, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");
        LeafItem basicItem4 = new LeafItem(4, 50, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");
        LeafItem basicItem5 = new LeafItem(5, 100, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");
        LeafItem basicItem6 = new LeafItem(6, 1000, "https://picsum.photos/id/1/200/300", "https://media.giphy.com/media/z3SJjTckxqKZW3SKOd/giphy.gif");

        List<LeafItem> basic = new ArrayList<>();
        basic.add(basicItem1);
        basic.add(basicItem2);
        basic.add(basicItem3);
        basic.add(basicItem4);
        basic.add(basicItem5);
        basic.add(basicItem6);


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

