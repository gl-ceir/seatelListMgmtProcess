package com.imsi_main.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileValidator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${file.separator.parameter}")
    private String fileSeparator;

    @Value("${fileCorruptPath}")
    private String fileCorruptPath;

    @Value("${header.addFile}")
    private String addFileHeader;

    @Value("${header.delFile}")
    String headerDelFile;

    @Value("${addFilePath.msisdn}")
    private int addFileMsisdn;

    @Value("${addFilePath.imsi}")
    private int addFileImsi;

    @Value("${addFilePath.created_date}")
    private int addFileCreatedDate;

    @Value("${delFilePath.msisdn}")
    private int delFilePathMsisdn;

    @Value("${delFilePath.deleted_date}")
    private int delFilePathDeletedDate;

    public boolean validateHeaders(File file, String expectedHeader) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            logger.info("The header is validating");
            logger.info("The expected header is: " + expectedHeader);
            logger.info("The actual header is: " + header);
            if(expectedHeader.equalsIgnoreCase(header)) {
                logger.info("The header is validated");
                return true;
            }
            else {
                logger.error("The header is failed validation");
                return false;
            }
        } catch (IOException e) {
            logger.error("Failed to read file: " + file.getName() + " (" + e.getMessage() + ")");
            return false;
        }
    }

    public boolean validateContents(File file, int msisdnColumn, int imsiColumn, int dateColumn) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            String[] columns = header.split(fileSeparator);

            int msisdnIndex = msisdnColumn;
            int imsiIndex = imsiColumn;
            int dateIndex = dateColumn;
//            logger.info(String.valueOf(msisdnIndex));
//            logger.info(String.valueOf(imsiIndex));
//            logger.info(String.valueOf(dateIndex));
//            if (msisdnIndex == -1 || imsiIndex == -1 || dateIndex == -1) {
//                return false;
//            }

            Set<String> msisdnSet = new HashSet<>();
            Set<String> imsiSet = new HashSet<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(fileSeparator);

                if (values[msisdnIndex].isEmpty() || values[imsiIndex].isEmpty()) {
                    logger.info("The msisdn or imsi is empty");
                    return false;
                }

                if (!msisdnSet.add(values[msisdnIndex]) || !imsiSet.add(values[imsiIndex])) {
                    logger.info("The msisdn or imsi is duplicate");
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            logger.error("Failed to read file: " + file.getName() + " (" + e.getMessage() + ")");
            return false;
        }
    }

    public boolean validateContents(File file, int msisdnColumn, int dateColumn) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            String[] columns = header.split(fileSeparator);

            int msisdnIndex = msisdnColumn;
            int dateIndex = dateColumn;
//
//            if (msisdnIndex == -1 || dateIndex == -1) {
//                return false;
//            }

            Set<String> msisdnSet = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(fileSeparator);

                if (values[msisdnIndex].isEmpty()) {
                    return false;
                }

                 if (!msisdnSet.add(values[msisdnIndex])) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            logger.error("Failed to read file: " + file.getName() + " (" + e.getMessage() + ")");
            return false;
        }
    }

//    private int getIndex(String[] columns, int column) {
//        if(columns.length > column)
//        return -1;
//    }

    public boolean validateAddFile(File addFile) {
        return validateHeaders(addFile, addFileHeader) && validateContents(addFile, addFileMsisdn, addFileImsi, addFileCreatedDate);
    }

    public boolean validateDelFile(File delFile) {
        return validateHeaders(delFile, headerDelFile) && validateContents(delFile, delFilePathMsisdn, delFilePathDeletedDate);
    }

    public void moveFileToCorruptFolder(File file) {
        Path sourcePath = file.toPath();
        Path targetPath = Paths.get(fileCorruptPath, file.getName());
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File moved to corrupt folder: " + targetPath);
        } catch (IOException e) {
            logger.error("Failed to move file to corrupt folder: " + e.getMessage());
        }
    }

    public void moveFileToCorruptFolder(String filePath) {
        Path sourcePath = Paths.get(filePath);
        Path targetPath = Paths.get(fileCorruptPath, sourcePath.getFileName().toString());
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File moved to corrupt folder: " + targetPath);
        } catch (IOException e) {
            logger.error("Failed to move file to corrupt folder: " + e.getMessage());
        }
    }
}


