package com.atoook.otsukailist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OtsukaiListApplication {

  public static void main(String[] args) {
    SpringApplication.run(OtsukaiListApplication.class, args);
  }
}
