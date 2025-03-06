package br.com.viniciusfinancas.financas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class FinancasApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(FinancasApplication.class, args);

		// Obtém o DataSource configurado
		DataSource dataSource = context.getBean(DataSource.class);

		// Tenta se conectar ao banco de dados
		try (Connection connection = dataSource.getConnection()) {
			System.out.println("✅ Conexão com o banco de dados realizada com sucesso!");
			System.out.println("URL: " + connection.getMetaData().getURL());
			System.out.println("Driver: " + connection.getMetaData().getDriverName());
		} catch (Exception e) {
			System.err.println("❌ Falha na conexão com o banco de dados!");
			e.printStackTrace();
		}
	}
}
