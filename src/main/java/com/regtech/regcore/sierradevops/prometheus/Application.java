package com.regtech.regcore.sierradevops.prometheus;

import com.regtech.regcore.sierradevops.prometheus.services.MetricsAggregator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@SpringBootApplication
@EnableScheduling
@Slf4j
public class Application {

	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);
	}
	@Autowired
	MetricsAggregator aggregator;


	@Scheduled(fixedDelay = 30000)  // run every 30s
	public void extractMetrics () throws Exception {
		log.debug("running executor...");
		aggregator.execute();
	}
}

