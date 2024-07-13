package com.imsi_main.service;

import com.imsi_main.builder.ModulesAuditTrailBuilder;
import com.imsi_main.config.AppConfig;
import com.imsi_main.config.DatabaseConfig;
import com.imsi_main.entity.aud.ModulesAuditTrail;
import com.imsi_main.repository.aud.ModulesAuditTrailRepository;
import com.imsi_main.validation.FileValidator;
import com.imsi_retriever.IMSI_RETRIEVER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.imsi_main.constants.Constants.seatelFileProcessFeatureName;
import static com.imsi_main.constants.Constants.seatelFileProcessModuleName;

@Service
public class FileProcessorService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    FileValidator fileValidator;

    @Autowired
    DatabaseService databaseService;

    @Autowired
    DatabaseConfig databaseConfig;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CSVReader csvReader;
    @Autowired
    ModulesAuditTrailRepository modulesAuditTrailRepository;

    @Autowired
    ModulesAuditTrailBuilder modulesAuditTrailBuilder;

    // Other properties...

    public void processFiles() {
        long startTime = System.currentTimeMillis();
        logger.info("Program Started");
        //  check entry for modules audit trail for success in the day.
        String statusCode = modulesAuditTrailRepository.getStatusCode(seatelFileProcessFeatureName,seatelFileProcessModuleName);
        if(statusCode != null && statusCode.equalsIgnoreCase("200")) {
            logger.info("Process already completed for the day.");
            return;
        }

        try {
                ModulesAuditTrail modulesAuditTrail = modulesAuditTrailBuilder.forInsert(201, "INITIAL", "NA", seatelFileProcessModuleName, seatelFileProcessFeatureName, "", "", LocalDateTime.now());
                ModulesAuditTrail entity = modulesAuditTrailRepository.save(modulesAuditTrail);
                int moduleAuditId = entity.getId();
                File addFile = getFirstFile(appConfig.getAddFilePath(), appConfig.getAddFileNamePrefix());
                File delFile = getFirstFile(appConfig.getDelFilePath(), appConfig.getDelFileNamePrefix());

                if (addFile == null || delFile == null) {
                    logger.info("No files to process.");
                    modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "The files does not exists for seatel", 0, 0, (int) (System.currentTimeMillis() - startTime), 0, moduleAuditId);
                    return;
                }


                if (!validateDate(addFile, delFile)) {
                    logger.info("Date validation failed.");
//                    updateAuditTrail(processProperties, "501", "FAIL", "Date validation failed.");
                    moveFileToCorruptFolder(addFile.getPath(), appConfig.getFileCorruptPath());
                    moveFileToCorruptFolder(delFile.getPath(), appConfig.getFileCorruptPath());
                    modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "The files are of different dates.", 0, 0, (int) (System.currentTimeMillis() - startTime), 0, moduleAuditId);
                    return;
                }
                if (!fileValidator.validateAddFile(addFile)) {
                    logger.info("Add file validation failed.");
//                    updateAuditTrail(processProperties, "501", "FAIL", "Date validation failed.");
                    moveFileToCorruptFolder(addFile.getPath(), appConfig.getFileCorruptPath());
                    moveFileToCorruptFolder(delFile.getPath(), appConfig.getFileCorruptPath());
                    modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "The add file failed validation.", 0, 0, (int) (System.currentTimeMillis() - startTime), 0, moduleAuditId);
                    return;
                }
                if (!fileValidator.validateDelFile(delFile)) {
                    logger.info("Del file validation failed.");
//                    updateAuditTrail(processProperties, "501", "FAIL", "Date validation failed.");
                    moveFileToCorruptFolder(addFile.getPath(), appConfig.getFileCorruptPath());
                    moveFileToCorruptFolder(delFile.getPath(), appConfig.getFileCorruptPath());
                    modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "The delete file failed validation.", 0, 0, (int) (System.currentTimeMillis() - startTime), 0, moduleAuditId);
                    return;
                }

                try {
                    logger.info("Processing SIM Change and HLR Deactivation");
                    processSimChangeAndHlrDeactivation(addFile, delFile);
                    moveFileToProcessedFolder(addFile.getPath(), appConfig.getProcessedFolderPath());
                    moveFileToProcessedFolder(delFile.getPath(), appConfig.getProcessedFolderPath());
                    modulesAuditTrailRepository.updateModulesAudit(200, "SUCCESS", "The process is completed.", 0, 0, (int) (System.currentTimeMillis() - startTime), 0, moduleAuditId);
                } catch (IOException e) {
                    logger.error("Failed to process files: " + e.getMessage());
                }
        } finally {
            databaseService.close();
        }
        logger.info("Program Finished");
    }

    private File getFirstFile(String directoryPath, String filePrefix) {
        logger.info("Getting the first file");
        File dir = new File(directoryPath);
        File[] files = dir.listFiles((d, name) -> name.startsWith(filePrefix));
        if (files == null || files.length == 0) {
            return null;
        }

        TreeMap<String, File> fileMap = new TreeMap<>();
        for (File file : files) {
            String dateStr = extractDateFromFilename(file.getName());
            if (dateStr != null) {
                fileMap.put(dateStr, file);
            }
        }

        return fileMap.firstEntry().getValue();
    }

    private boolean validateDate(File addFile, File delFile) {
        logger.info("Validating dates");
        String addFileName = addFile.getName();
        String delFileName = delFile.getName();

        String addFileDateStr = extractDateFromFilename(addFileName);
        String delFileDateStr = extractDateFromFilename(delFileName);

        if (addFileDateStr == null || delFileDateStr == null || !addFileDateStr.equals(delFileDateStr)) {
            logger.error("Date in file names does not match: Add File Date - " + addFileDateStr + ", Del File Date - " + delFileDateStr);
            return false;
        }

        return true;
    }

    private String extractDateFromFilename(String fileName) {
        Pattern datePattern = Pattern.compile("(\\d{8})");
        Matcher matcher = datePattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void processSimChangeAndHlrDeactivation(File addFile, File delFile) throws IOException {
        logger.info("Processing SIM and HLR Deactivation");
        logger.info("Add File: " + addFile.getPath());
        logger.info("Del File: " + delFile.getPath());

        List<String[]> addFileLines = csvReader.readCSV(addFile);
        List<String[]> delFileLines = csvReader.readCSV(delFile);

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String operator = appConfig.getOperator();

        String simChangeFilePath = appConfig.getCommonPath() + "/" +timestamp + "_" + appConfig.getSimchangeFileName();
        String hlrDeactivationFilePath = appConfig.getCommonPath() + "/" + timestamp + "_" + appConfig.getHlrDeactivationFileName();
        String addFilePathForHlrDel = appConfig.getCommonPath() + "/" + appConfig.getAddFilePathForHlrDelName() + "_" + operator + "_" + date + ".csv";
        String addFilePathForHlrAdd = appConfig.getCommonPath() + "/" + appConfig.getAddFilePathForHlrAddName() + "_" + operator + "_" + date + ".csv";

        try (BufferedWriter simChangeWriter = new BufferedWriter(new FileWriter(simChangeFilePath));
             BufferedWriter hlrDeacWriter = new BufferedWriter(new FileWriter(hlrDeactivationFilePath));
             BufferedWriter addHlrWriter = new BufferedWriter(new FileWriter(addFilePathForHlrAdd));
             BufferedWriter delHlrWriter = new BufferedWriter(new FileWriter(addFilePathForHlrDel));
             BufferedWriter errorWriter = new BufferedWriter(new FileWriter(appConfig.getFileCorruptPath()+ "/" + timestamp + "_error.csv"));
             Connection conn = databaseConfig.getConnection()) {

            // Write headers
            hlrDeacWriter.write("msisdn" +appConfig.getFileSeparator() + "imsi" + appConfig.getFileSeparator() + "deactivation_date");
            hlrDeacWriter.newLine();
            simChangeWriter.write("new_imsi"+appConfig.getFileSeparator()+"old_imsi"+appConfig.getFileSeparator()+"msisdn"+appConfig.getFileSeparator()+"sim_change_datetime");
            simChangeWriter.newLine();
            addHlrWriter.write("imsi"+appConfig.getFileSeparator()+"msisdn"+appConfig.getFileSeparator()+"activation_date");
            addHlrWriter.newLine();
            delHlrWriter.write("imsi"+appConfig.getFileSeparator()+"msisdn"+appConfig.getFileSeparator()+"activation_date");
            delHlrWriter.newLine();
            errorWriter.write(delFileLines.get(0)+ appConfig.getFileSeparator() + "Status");
            errorWriter.newLine();

            // Write addFile contents to addHlrWriter
            for (String[] line : addFileLines) {
                if (!line[0].equals("CREATED_DATE")) { // Skip header row
                    logger.info("Adding record in hlr add diff file");
                    logger.info("record is " + line[0] + line[1]);
                    addHlrWriter.write(String.join(appConfig.getFileSeparator(), line[appConfig.getAddFileImsi()], line[appConfig.getAddFileMsisdn()], line[appConfig.getAddFileCreatedDate()])); // Assuming IMSI, MSISDN, CREATED_DATE
                    addHlrWriter.newLine();
                }
            }
            logger.info("Outside");
            // Process delFile contents
            for (String[] line : delFileLines) {

                if (!line[0].equals("Customer Account ID")) { // Skip header row
                    String msisdn = line[appConfig.getDelFilePathMsisdn()];
                    String oldImsi = com.imsi_retriever.IMSI_RETRIEVER.getImsi(msisdn, conn); // Method to retrieve old IMSI from delFile
                    String newImsi = getNewImsi(msisdn, addFileLines);
                    String delDate = line[appConfig.getDelFilePathDeletedDate()]; // Delete Date Time
                    if (newImsi != null) {
                        logger.info("Adding record in sim change file");
                        simChangeWriter.write(String.join(appConfig.getFileSeparator(), newImsi, oldImsi, msisdn, delDate));
                        simChangeWriter.newLine();
                    } else {
                        logger.info("Adding record in hlr deactivation file");
                        hlrDeacWriter.write(String.join(appConfig.getFileSeparator(), oldImsi, msisdn, delDate)); // Assuming Delete Date Time, MSISDN
                        hlrDeacWriter.newLine();
                    }
                    logger.info("Old imsi is " + oldImsi);
//                    if(!oldImsi.isEmpty()) {
                        logger.info("Adding record in hlr del diff file");
                        delHlrWriter.write(String.join(appConfig.getFileSeparator(), oldImsi, msisdn, delDate));
                        delHlrWriter.newLine();
//                    } else {
//                        errorWriter.write(line + appConfig.getFileSeparator() + "not-ok");
//                        errorWriter.newLine();
//                    }
                }
            }

            moveFileToProcessedFolder(simChangeFilePath, appConfig.getSimchangeFileDir());
            moveFileToProcessedFolder(hlrDeactivationFilePath, appConfig.getHlrDeactivationFileDir());
            moveFileToProcessedFolder(addFilePathForHlrDel, appConfig.getAddFilePathForHlrDelDir());
            moveFileToProcessedFolder(addFilePathForHlrAdd, appConfig.getAddFilePathForHlrAddDir());
        } catch (IOException e) {
            logger.error("Failed to process files: " + e.getMessage());
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getImsi(String msisdn, List<String[]> addFileLines) {
        for (String[] line : addFileLines) {
            if (line.length > 1 && line[1].equals(msisdn)) { // Assuming MSISDN is at index 1
                return line[0];
            }
        }
        return databaseService.getImsi(msisdn);
    }

    private String getNewImsi(String msisdn, List<String[]> addFileLines) {
        for (String[] line : addFileLines) {
            if (line.length > 1 && line[appConfig.getAddFileMsisdn()].equals(msisdn)) { // Assuming MSISDN is at index 1
                logger.info("New imsi is" + line[4] );
                return line[appConfig.getAddFileImsi()]; // Assuming IMSI is at index 0
            }
        }
        return null;
    }

    private void moveFileToCorruptFolder(String filePath, String corruptFolderPath) {
        Path sourcePath = Paths.get(filePath);
        Path targetPath = Paths.get(corruptFolderPath, sourcePath.getFileName().toString());
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File moved to corrupt folder: " + targetPath);
        } catch (IOException e) {
            logger.error("Failed to move file to corrupt folder: " + e.getMessage());
        }
    }

    private void moveFileToProcessedFolder(String filePath, String processedFolderPath) {
        Path sourcePath = Paths.get(filePath);
        Path targetPath = Paths.get(processedFolderPath, sourcePath.getFileName().toString());
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File moved to processed folder: " + targetPath);
        } catch (IOException e) {
            logger.error("Failed to move file to processed folder: " + e.getMessage());
        }
    }

}
