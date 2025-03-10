package br.com.viniciusfinancas.financas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@SpringBootApplication
public class FinancasApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(FinancasApplication.class, args);
	}
}
