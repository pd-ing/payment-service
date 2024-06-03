package com.pding.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Logger;

@SpringBootApplication
public class PaymentServiceApplication {

    private static final Logger logger = Logger.getLogger(PaymentServiceApplication.class.getName());

    public static void main(String[] args) {
        logger.info("Starting User Management Application on the production server...");

        String awsAccessKey = null;
        String awsSecretKey = null;
        String awsRegion = null;

        for (String arg : args) {
            if (arg.startsWith("--aws.accessKey=")) {
                awsAccessKey = arg.split("=")[1];
            }
            if (arg.startsWith("--aws.secretKey=")) {
                awsSecretKey = arg.split("=")[1];
            }
            if (arg.startsWith("--aws.region=")) {
                awsRegion = arg.split("=")[1];
            }
        }

        logger.info("AWS Access Key and Secret Key added => " + awsAccessKey + " -- " + awsSecretKey + " -- " + awsRegion);
        if (awsRegion != null && awsSecretKey != null && awsAccessKey != null) {
            System.setProperty("spring.cloud.aws.region.static", awsRegion);
            System.setProperty("spring.cloud.aws.credentials.accessKey", awsAccessKey);
            System.setProperty("spring.cloud.aws.credentials.secretKey", awsSecretKey);
            logger.info("AWS Access Key and Secret Key added!!!");
        }

        // Run the app
        SpringApplication.run(PaymentServiceApplication.class, args);

    }

}
