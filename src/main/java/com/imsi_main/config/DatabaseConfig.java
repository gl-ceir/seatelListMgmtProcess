package com.imsi_main.config;

import com.imsi_main.validation.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;


@Service
public class DatabaseConfig {



    private static Connection connection = null;
    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getName());

    @Autowired
    AppConfig appConfig;



//    @Bean
//    public DataSource dataSource() throws ClassNotFoundException {
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        Class.forName(driverClassName);
//        dataSource.setUrl(dbUrl);
//        dataSource.setUsername(dbUsername);
//        dataSource.setPassword(dbPassword);
//        return dataSource;
//    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            synchronized (DatabaseConfig.class) {
                if (connection == null || connection.isClosed()) {
//                    String URL = System.getenv("db_url");
//                    String USER = System.getenv("dbUsername");
//                    String PASSWORD = System.getenv("dbPassword");

//                    logger.info(  ""+  en.getActiveProfiles());
//                    logger.info("credentials are {} , {} , {}", dbUrl, USER, PASSWORD);
//                    logger.info("SAmple credentials  {} , {} ", a, b);

                    connection = DriverManager.getConnection(appConfig.getDbUrl(), appConfig.getDbUsername(), appConfig.getDbPassword());
                }
            }
        }
        return connection;
    }
}
