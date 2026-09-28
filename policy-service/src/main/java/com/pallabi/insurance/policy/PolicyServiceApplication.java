package com.pallabi.insurance.policy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Policy Service.
 *
 * @SpringBootApplication combines three annotations:
 *
 * 1. @Configuration
 *    This class can define Spring "beans" (objects Spring creates and manages).
 *
 * 2. @EnableAutoConfiguration
 *    Spring Boot looks at the libraries on the classpath and configures them automatically.
 *    Example: it sees the Oracle driver + data-jpa, so it sets up a database connection pool
 *    and Hibernate for us. We only provide the URL and password.
 *
 * 3. @ComponentScan
 *    Spring scans this package (com.pallabi.insurance.policy) and all sub-packages
 *    for classes marked @RestController, @Service, @Repository, etc.,
 *    creates one instance of each, and connects them together.
 *    This is "Dependency Injection": Spring builds objects and hands them to
 *    whoever needs them, instead of us calling "new" everywhere.
 *
 * Because of ComponentScan, every other class we write MUST live in this package
 * or a sub-package, otherwise Spring won't find it.
 */
@SpringBootApplication
public class PolicyServiceApplication {

	/**
	 * Standard Java main method: the JVM starts here.
	 *
	 * SpringApplication.run(...) does all the heavy lifting:
	 *  - creates the "ApplicationContext" (Spring's container holding all beans)
	 *  - runs auto-configuration
	 *  - starts the embedded Tomcat web server (default port 8080)
	 *  - app is now ready to accept HTTP requests
	 */
	public static void main(String[] args) {
		SpringApplication.run(PolicyServiceApplication.class, args);
	}
}