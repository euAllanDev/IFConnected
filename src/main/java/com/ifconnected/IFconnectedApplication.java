package com.ifconnected;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
// teste    
@SpringBootApplication
@EnableCaching // Habilita o Redis Cache
public class IFconnectedApplication {

	public static void main(String[] args) {
		SpringApplication.run(IFconnectedApplication.class, args);
	}

}
