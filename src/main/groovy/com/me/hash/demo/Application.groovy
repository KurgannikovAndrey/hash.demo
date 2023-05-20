package com.me.hash.demo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class Application {

	static void main(String[] args) {
		SpringApplication app = new SpringApplication(Application)
		//println args
		def params = args[0].split(',')
		//println params
		app.setDefaultProperties([
				"server.port" : params[0] as int,
				"is.local.test" : Boolean.valueOf(params[1])
		])
		app.run(args)
	}

}
