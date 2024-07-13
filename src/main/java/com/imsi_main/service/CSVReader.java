package com.imsi_main.service;
import com.imsi_main.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class CSVReader {

    @Autowired
    AppConfig appConfig;
    private static final Logger logger = Logger.getLogger(CSVReader.class.getName());

    public List<String[]> readCSV(File file) {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            line = br.readLine(); // skipping the header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(appConfig.getFileSeparator());

                data.add(values);
            }
        } catch (IOException e) {
            logger.severe("Failed to read CSV file: " + e.getMessage());
        }
        return data;
    }


//    public  readCSV(File file) {
//        List<String[]> data = new ArrayList<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//            String line;
//            line = br.readLine(); // skipping the header
//            while ((line = br.readLine()) != null) {
//                String[] values = line.split(appConfig.getFileSeparator());
//
//                data.add(values);
//            }
//        } catch (IOException e) {
//            logger.severe("Failed to read CSV file: " + e.getMessage());
//        }
//        return data;
//    }
}
