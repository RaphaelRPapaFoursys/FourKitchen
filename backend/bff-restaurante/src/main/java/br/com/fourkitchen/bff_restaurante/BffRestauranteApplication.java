package br.com.fourkitchen.bff_restaurante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BffRestauranteApplication {

	public static void main(String[] args) {
		SpringApplication.run(BffRestauranteApplication.class, args);
	}

}
