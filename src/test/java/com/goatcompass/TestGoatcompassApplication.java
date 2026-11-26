package com.goatcompass;

import org.springframework.boot.SpringApplication;

public class TestGoatcompassApplication {

	public static void main(String[] args) {
		SpringApplication.from(GoatcompassApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
