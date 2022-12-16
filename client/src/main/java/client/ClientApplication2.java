package client;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@SpringBootApplication
public class ClientApplication2 {

    private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    // tag::rest-template[]
    // IMPORTANT! To instrument RestTemplate you must inject the RestTemplateBuilder
//    @Bean
//    RestTemplate restTemplate(RestTemplateBuilder builder) {
//        return builder.build();
//    }
    // end::rest-template[]

    // tag::runner[]
    @Bean
    CommandLineRunner myCommandLineRunner2(ObservationRegistry registry, RestTemplate restTemplate) {
        Random highCardinalityValues = new Random(); // Simulates potentially large number of values
        List<String> lowCardinalityValues = Arrays.asList("userType1", "userType12", "userType3"); // Simulates low number of values

        final UserIdObservationConvention userIdObservationConvention = new DefaultIdObservationConvention();
        return args -> {
            String highCardinalityUserId = String.valueOf(highCardinalityValues.nextLong(100_000));

            UserIdContext userIdContext = new UserIdContext(highCardinalityUserId, randomUserTypePicker(lowCardinalityValues));

            UserIdObservationDocumentation.SEND.observation(userIdObservationConvention, new DefaultIdObservationConvention(), () -> userIdContext,
                    registry).observe(() -> {
                log.info("Will send a request to the server"); // Since we're in an observation scope - this log line will contain tracing MDC entries ...
                String response = restTemplate.getForObject("http://localhost:7654/user/{userId}", String.class, highCardinalityUserId); // Boot's RestTemplate instrumentation creates a child span here
                log.info("Got response [{}]", response); // ... so will this line
            });
        };
    }
    // end::runner[]

    Random randomUserTypePicker = new Random();

    private String randomUserTypePicker(List<String> lowNumberOfValues) {
        return lowNumberOfValues.get(randomUserTypePicker.nextInt(2));
    }
}

