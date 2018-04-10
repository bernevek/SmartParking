package com.smartparking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;

@SpringBootApplication
@EnableWebMvc
@EnableConfigurationProperties
@Configuration
@ComponentScan
public class ServerApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ServerApplication.class);
    }

    public static void main(String[] args) throws IOException {
        SpringApplication.run(ServerApplication.class, args);
        System.out.println("EXIT AND CLOSE fsdgahsjdgasjhdgf");
    }
}
