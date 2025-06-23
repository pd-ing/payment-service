package com.pding.paymentservice;

import com.stripe.Stripe;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.logging.Logger;

@SpringBootApplication
@EnableAsync
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@EnableScheduling
public class PaymentServiceApplication {

    private static final Logger logger = Logger.getLogger(PaymentServiceApplication.class.getName());

    @Value("${stripe.secret.key}")
    private String secretKey;

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

//        logger.info("AWS Access Key and Secret Key added => " + awsAccessKey + " -- " + awsSecretKey + " -- " + awsRegion);
        if (awsRegion != null && awsSecretKey != null && awsAccessKey != null) {
            System.setProperty("spring.cloud.aws.region.static", awsRegion);
            System.setProperty("spring.cloud.aws.credentials.accessKey", awsAccessKey);
            System.setProperty("spring.cloud.aws.credentials.secretKey", awsSecretKey);
            logger.info("AWS Access Key and Secret Key added!!!");
        }

        // Run the app
        SpringApplication.run(PaymentServiceApplication.class, args);

    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            logger.info("Setting Stripe API key...");
            Stripe.apiKey = secretKey;
            logger.info("Stripe API key set successfully!");
        };
    }
}
