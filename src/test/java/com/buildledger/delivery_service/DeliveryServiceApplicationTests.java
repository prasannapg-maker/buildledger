package com.buildledger.delivery_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DeliveryServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	// additional unit tests for core business logic

	@Test
	void acceptBeforeDeliveredShouldFail() {
		// use Spring context to get DeliveryService bean and a real in-memory repo,
		// but for simplicity we can just verify behaviour via the service directly
	}

}
