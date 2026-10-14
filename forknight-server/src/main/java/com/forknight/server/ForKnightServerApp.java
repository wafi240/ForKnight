package com.forknight.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class ForKnightServerApp {
    private static final Logger log = LoggerFactory.getLogger(ForKnightServerApp.class);

    public static void main(String[] args) {
        Environment env = SpringApplication.run(ForKnightServerApp.class, args).getEnvironment();
        log.info("ForKnight Server started on port(s): {}", env.getProperty("server.port"));
    }
}
