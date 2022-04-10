package com.opengms.maparchivebackendprj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication
// @SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class MapArchiveBackendPrjApplication {

    public static void main(String[] args) {
        SpringApplication.run(MapArchiveBackendPrjApplication.class, args);
    }

}
