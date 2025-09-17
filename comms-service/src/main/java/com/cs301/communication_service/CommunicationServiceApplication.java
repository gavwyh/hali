package com.cs301.communication_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.cs301.communication_service.producer.TestKafkaProducer;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class CommunicationServiceApplication implements CommandLineRunner {

	private final TestKafkaProducer testKafkaProducer;

    public CommunicationServiceApplication(TestKafkaProducer testKafkaProducer) {
        this.testKafkaProducer = testKafkaProducer;
    }

	@Override
    public void run(String... args) throws Exception {
        // Send a test message when the app starts
		System.out.println("Sending Test Create Message...");
        testKafkaProducer.sendTestCreateMessage();
		// System.out.println("Sending Test Update Message...");
        // testKafkaProducer.sendTestUpdateMessage();
		// System.out.println("Sending Test Delete Message...");
        // testKafkaProducer.sendTestDeleteMessage();
		// System.out.println("Sending Test U2C Message...");
        // testKafkaProducer.sendTestU2CMessage();
		// System.out.println("Sending Test Otp Message...");
        // testKafkaProducer.sendTestOtpMessage();
		// System.out.println("Sending Test Account Create Message...");
        // testKafkaProducer.sendTestAccountCreateMessage();
		// System.out.println("Sending Test Account Delete Message...");
        // testKafkaProducer.sendTestAccountDeleteMessage();
    }

	public static void main(String[] args) {
		SpringApplication.run(CommunicationServiceApplication.class, args);
	}

}
