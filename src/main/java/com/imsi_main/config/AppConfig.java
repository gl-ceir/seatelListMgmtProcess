package com.imsi_main.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@Data
public class AppConfig {

    @Value("${fileProcessedPath}")
    private String processedFolderPath;

    @Value("${file.separator.parameter}")
    private String fileSeparator;

    @Value("${fileCorruptPath}")
    private String fileCorruptPath;

    @Value("${header.addFile}")
    private String addFileHeader;

    @Value("${addFilePath.msisdn}")
    private int addFileMsisdn;

    @Value("${addFilePath.imsi}")
    private int addFileImsi;

    @Value("${addFilePath.created_date}")
    private int addFileCreatedDate;

    @Value("${addFilePath}")
    String addFilePath;

    @Value("${delFilePath}")
    String delFilePath;
    @Value("${operator}")
    String operator;

    @Value("${simchangeFileDir}")
    String simchangeFileDir;

    @Value("${simchangeFileName}")
    String simchangeFileName;

    @Value("${hlrDeactivationFileDir}")
    String hlrDeactivationFileDir;

    @Value("${hlrDeactivationFileName}")
    String hlrDeactivationFileName;


    @Value("${addFilePathForHlrAddDir}")
    String addFilePathForHlrAddDir;

    @Value("${addFilePathForHlrAddName}")
    String addFilePathForHlrAddName;

    @Value("${addFilePathForHlrDelDir}")
    String addFilePathForHlrDelDir;

    @Value("${addFilePathForHlrDelName}")
    String addFilePathForHlrDelName;

    @Value("${header.addFile}")
    String headerAddFile;

    @Value("${header.delFile}")
    String headerDelFile;

    @Value("${delFilePath.msisdn}")
    int delFilePathMsisdn;

    @Value("${delFilePath.deleted_date}")
    int delFilePathDeletedDate;

    @Value("${addFileNamePrefix}")
    String addFileNamePrefix;

    @Value("${delFileNamePrefix}")
    String delFileNamePrefix;

    @Value("${spring.datasource.url}")
     String dbUrl;

    @Value("${spring.datasource.username}")
    private  String dbUsername;

    @Value("${spring.datasource.password}")
    private  String dbPassword;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${common.path}")
    String commonPath;

}
