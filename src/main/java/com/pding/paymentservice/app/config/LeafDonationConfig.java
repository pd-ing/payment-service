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
        LeafItem basicItem1 = new LeafItem(1, 3, "https://pdpd.b-cdn.net/public/3_leafs_dice_cube.png", null);
        LeafItem basicItem2 = new LeafItem(2, 10, "https://pdpd.b-cdn.net/public/10_leafs_robot.png", null);
        LeafItem basicItem3 = new LeafItem(3, 20, "https://pdpd.b-cdn.net/public/20_leafs_piggy_bank.png", null);
        LeafItem basicItem4 = new LeafItem(4, 33, "https://pdpd.b-cdn.net/public/33_leafs_pink_heart.png", null);
        LeafItem basicItem5 = new LeafItem(5, 50, "https://pdpd.b-cdn.net/public/50_leafs_pink_gift_box.png", null);
        LeafItem basicItem6 = new LeafItem(6, 77, "https://pdpd.b-cdn.net/public/77_leafs_lucky_cat.png", null);

        LeafItem basicItem7 = new LeafItem(7, 100, "https://pdpd.b-cdn.net/public/100_leafs_birthday_cake.png", null);
        LeafItem basicItem8 = new LeafItem(8, 1000, "https://pdpd.b-cdn.net/public/1000_leafs_diamond.png", null);
        LeafItem basicItem9 = new LeafItem(9, 2000, "https://pdpd.b-cdn.net/public/2000_leafs_envelope.png", null);
        LeafItem basicItem10 = new LeafItem(10, 3000, "https://pdpd.b-cdn.net/public/3000_leafs_key.png", null);
        LeafItem basicItem11 = new LeafItem(11, 5000, "https://pdpd.b-cdn.net/public/5000_leafs_cute_unicorn.png", null);
        LeafItem basicItem12 = new LeafItem(12, 10000, "https://pdpd.b-cdn.net/public/10000_leafs_light_bulb.png", null);

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


        LeafItem signatureItem1 = new LeafItem(1, 10, null, "https://pdpd.b-cdn.net/public/333new.gif");
        LeafItem signatureItem2 = new LeafItem(2, 20, null, "https://pdpd.b-cdn.net/public/777new.gif");

        List<LeafItem> signature = new ArrayList<>();
        signature.add(signatureItem1);
        signature.add(signatureItem2);

        return new LeafDonationConfig(basic, signature);
    }
}

