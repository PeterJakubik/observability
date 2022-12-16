package com.example.demo;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.observation.aop.ObservedAspect;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpHeaders;
import java.util.Random;
import java.util.stream.StreamSupport;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
		return new ObservedAspect(observationRegistry);
	}
}

@RestController
class MyController {

	private static final Logger log = LoggerFactory.getLogger(MyController.class);
	private final MyUserService myUserService;
	private final MeterRegistry meterRegistry;

	public MyController(MyUserService myUserService, MeterRegistry meterRegistry) {
		this.myUserService = myUserService;
		this.meterRegistry = meterRegistry;
	}

	@GetMapping("/user/{userId}")
	public String userName(HttpServletRequest request, @PathVariable("userId") String userId) {
		log.info("Got a request");

		meterRegistry.counter("users.total", "userid", userId).increment();

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

@Component
class MyHandler implements ObservationHandler<Observation.Context> {

	private static final Logger log = LoggerFactory.getLogger(MyHandler.class);

	@Override
	public void onStart(Observation.Context context) {
		log.info("Before running the observation for context [{}], userType [{}]", context.getName(), getUserTypeFromContext(context));
	}

	@Override
	public void onStop(Observation.Context context) {
		log.info("After running the observation for context [{}], userType [{}]", context.getName(), getUserTypeFromContext(context));
	}

	@Override
	public boolean supportsContext(Observation.Context context) {
		return true;
	}

	private String getUserTypeFromContext(Observation.Context context) {
		return StreamSupport.stream(context.getLowCardinalityKeyValues().spliterator(), false)
				.filter(keyValue -> "userType".equals(keyValue.getKey()))
				.map(KeyValue::getValue)
				.findFirst()
				.orElse("UNKNOWN");
	}
}
