package com.example.demo;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

@RestController
class MyController {

	private static final Logger log = LoggerFactory.getLogger(MyController.class);
	private final MyUserService myUserService;

	MyController(MyUserService myUserService) {
		this.myUserService = myUserService;
	}

	@GetMapping("/user/{userId}")
	String userName(@PathVariable("userId") String userId) {
		log.info("Got a request");
		return myUserService.userName(userId);
	}
}
// end::controller[]

// tag::service[]
@Service
class MyUserService {

	private static final Logger log = LoggerFactory.getLogger(MyUserService.class);

	private final Random random = new Random();

	// Example of using an annotation to observe methods
	// <user.name> will be used as a metric name
	// <getting-user-name> will be used as a span  name
	// <userType=userType2> will be set as a tag for both metric & span
	@Observed(name = "user.name",
			contextualName = "getting-user-name",
			lowCardinalityKeyValues = {"userType", "userType2"})
	String userName(String userId) {
		log.info("Getting user name for user with id <{}>", userId);
		try {
			Thread.sleep(random.nextLong(200L)); // simulates latency
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return "foo";
	}
}
