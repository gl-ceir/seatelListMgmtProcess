package com.imsi_main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class DatabaseService {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public String getImsi(String msisdn) {
        String sql = "SELECT imsi FROM active_msisdn_list WHERE msisdn = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{msisdn}, String.class);
    }

    public void close() {
        // Implement logic to close database connection if necessary
    }
}
