package com.imsi_main;

import com.imsi_main.service.FileProcessorService;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEncryptableProperties
public class FileProcessorApplication implements CommandLineRunner {

    @Autowired
    FileProcessorService fileProcessorService;
    public static void main(String[] args) {
        SpringApplication.run(FileProcessorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        fileProcessorService.processFiles();
    }
}
