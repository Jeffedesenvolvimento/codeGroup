package br.com.codegroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients(basePackages = "br.com.codegroup.client")
@EnableJpaRepositories(basePackages = "br.com.codegroup.repository")
public class CodegroupApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodegroupApplication.class, args);
	}

}
