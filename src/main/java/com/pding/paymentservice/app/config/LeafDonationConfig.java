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
        LeafItem basicItem1 = new LeafItem("BASIC_1", 3, "https://pdpd.b-cdn.net/public/3_glazy-dice-cube(3).svg", null);
        LeafItem basicItem2 = new LeafItem("BASIC_2", 3, "https://pdpd.b-cdn.net/public/3_glazy-white-rose(3).svg", null);
        LeafItem basicItem3 = new LeafItem("BASIC_3", 5, "https://pdpd.b-cdn.net/public/5_glazy-yellow-rubber-duck(5).svg", null);
        LeafItem basicItem4 = new LeafItem("BASIC_4", 7, "https://pdpd.b-cdn.net/public/7_glazy-watermelon-slice(7).svg", null);
        LeafItem basicItem5 = new LeafItem("BASIC_5", 10, "https://pdpd.b-cdn.net/public/10_glazy-lucky-cat(10).svg", null);
        LeafItem basicItem6 = new LeafItem("BASIC_6", 12, "https://pdpd.b-cdn.net/public/12_glazy-school-backpack(12).svg", null);

        LeafItem basicItem7 = new LeafItem("BASIC_7", 20, "https://pdpd.b-cdn.net/public/20_glazy-heart-shaped-chocolate-candy(20).svg", null);
        LeafItem basicItem8 = new LeafItem("BASIC_8", 22, "https://pdpd.b-cdn.net/public/22_glazy-metalic-padlock(22).svg", null);
        LeafItem basicItem9 = new LeafItem("BASIC_9", 22, "https://pdpd.b-cdn.net/public/22_glazy-white-gamepad(22).svg", null);
        LeafItem basicItem10 = new LeafItem("BASIC_10", 33, "https://pdpd.b-cdn.net/public/33_glazy-funny-speech-bubbles(33).svg", null);
        LeafItem basicItem11 = new LeafItem("BASIC_11", 44, "https://pdpd.b-cdn.net/public/44_glazy-halloween-pumpkin(44).svg", null);
        LeafItem basicItem12 = new LeafItem("BASIC_12", 50, "https://pdpd.b-cdn.net/public/50_glazy-cute-friendly-robot(50).svg", null);

        LeafItem basicItem13 = new LeafItem("BASIC_13", 60, "https://pdpd.b-cdn.net/public/60_glazy-pink-book(60).svg", null);
        LeafItem basicItem14 = new LeafItem("BASIC_14", 70, "https://pdpd.b-cdn.net/public/70_glazy-piece-of-cake(70).svg", null);
        LeafItem basicItem15 = new LeafItem("BASIC_15", 90, "https://pdpd.b-cdn.net/public/90_glazy-pink-gift-box(90).svg", null);
        LeafItem basicItem16 = new LeafItem("BASIC_16", 100, "https://pdpd.b-cdn.net/public/100_glazy-cute-bird(100).svg", null);
        LeafItem basicItem17 = new LeafItem("BASIC_17", 120, "https://pdpd.b-cdn.net/public/120_glazy-pink-bus-1(120).svg", null);
        LeafItem basicItem18 = new LeafItem("BASIC_18", 150, "https://pdpd.b-cdn.net/public/150_glazy-piggy-bank(150).svg", null);
        LeafItem basicItem19 = new LeafItem("BASIC_19", 200, "https://pdpd.b-cdn.net/public/200_glazy-diamond(200).svg", null);
        LeafItem basicItem20 = new LeafItem("BASIC_20", 300, "https://pdpd.b-cdn.net/public/300_glazy-ancient-statue (300).svg", null);
        LeafItem basicItem21 = new LeafItem("BASIC_21", 333, "https://pdpd.b-cdn.net/public/333_glazy-treasure-chest(333).svg", null);
        LeafItem basicItem22 = new LeafItem("BASIC_22", 400, "https://pdpd.b-cdn.net/public/400_glazy-dove(400).svg", null);
        LeafItem basicItem23 = new LeafItem("BASIC_23", 500, "https://pdpd.b-cdn.net/public/500_glazy-ice-cream(500).svg", null);
        LeafItem basicItem24 = new LeafItem("BASIC_24", 666, "https://pdpd.b-cdn.net/public/666_glazy-brown-cute-puppy(666).svg", null);
        LeafItem basicItem25 = new LeafItem("BASIC_25", 777, "https://pdpd.b-cdn.net/public/777_glazy-birthday-cake(777).svg", null);
        LeafItem basicItem26 = new LeafItem("BASIC_26", 1000, "https://pdpd.b-cdn.net/public/1000_glazy-money-bag(1000).svg", null);
        LeafItem basicItem27 = new LeafItem("BASIC_27", 2000, "https://pdpd.b-cdn.net/public/2000_glazy-robot(2000).svg", null);
        LeafItem basicItem28 = new LeafItem("BASIC_28", 2200, "https://pdpd.b-cdn.net/public/2200_glazy-teddy-bear(2200).svg", null);
        LeafItem basicItem29 = new LeafItem("BASIC_29", 3000, "https://pdpd.b-cdn.net/public/3000_glazy-pink-heart(3000).svg", null);
        LeafItem basicItem30 = new LeafItem("BASIC_30", 4000, "https://pdpd.b-cdn.net/public/4000_glazy-sakura-tree(4000).svg", null);
        LeafItem basicItem31 = new LeafItem("BASIC_31", 5000, "https://pdpd.b-cdn.net/public/5000_glazy-bottle-of-champagne(5000).svg", null);
        LeafItem basicItem32 = new LeafItem("BASIC_32", 6000, "https://pdpd.b-cdn.net/public/6000_glazy-silver-coin-with-a-star(6000).svg", null);
        LeafItem basicItem33 = new LeafItem("BASIC_33", 7777, "https://pdpd.b-cdn.net/public/7777_glazy-cute-unicorn(7777).svg", null);
        LeafItem basicItem34 = new LeafItem("BASIC_34", 10000, "https://pdpd.b-cdn.net/public/10000_glazy-bouquet-of-white-roses(10,000).svg", null);


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

        basic.add(basicItem13);
        basic.add(basicItem14);
        basic.add(basicItem15);
        basic.add(basicItem16);
        basic.add(basicItem17);
        basic.add(basicItem18);
        basic.add(basicItem19);
        basic.add(basicItem20);
        basic.add(basicItem21);
        basic.add(basicItem22);
        basic.add(basicItem23);
        basic.add(basicItem24);

        basic.add(basicItem25);
        basic.add(basicItem26);
        basic.add(basicItem27);
        basic.add(basicItem28);
        basic.add(basicItem29);
        basic.add(basicItem30);
        basic.add(basicItem31);
        basic.add(basicItem32);
        basic.add(basicItem33);
        basic.add(basicItem34);


        // LeafItem signatureItem1 = new LeafItem(1, 10, null, "https://pdpd.b-cdn.net/public/333new.gif");
        //LeafItem signatureItem2 = new LeafItem(2, 20, null, "https://pdpd.b-cdn.net/public/777new.gif");

        List<LeafItem> signature = new ArrayList<>();
        //signature.add(signatureItem1);
        //signature.add(signatureItem2);

        return new LeafDonationConfig(basic, signature);
    }
}

