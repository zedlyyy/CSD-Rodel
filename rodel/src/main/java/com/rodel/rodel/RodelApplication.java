package com.rodel.rodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class RodelApplication {

	public static void main(String[] args) {
		SpringApplication.run(RodelApplication.class, args); 
		new LedgerReplica();
	}

}
