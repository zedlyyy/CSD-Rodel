package com.rodel.rodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import bftsmart.tom.ServiceReplica;


@SpringBootApplication
public class RodelApplication {

	public static void main(String[] args) {
		SpringApplication.run(RodelApplication.class, args);
		new LedgerReplica();
	}

}
