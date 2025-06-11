package com.capstone.wellnessnavigatorgym.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to validate database connection and schema at application startup
 */
@Component
public class DatabaseConnectionValidator implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionValidator.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment env;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Validating database connection and schema...");
        validateConnection();
        checkTables();
    }

    private void validateConnection() {
        try {
            String url = env.getProperty("spring.datasource.url");
            logger.info("Database URL: {}", url);
            
            String validationQuery = "SELECT 1";
            Integer result = jdbcTemplate.queryForObject(validationQuery, Integer.class);
            
            if (result != null && result == 1) {
                logger.info("Database connection is valid");
            } else {
                logger.error("Database connection validation failed");
            }
        } catch (Exception e) {
            logger.error("Failed to validate database connection", e);
        }
    }

    private void checkTables() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Check for specific table names
            logger.info("Checking for table 'day' existence");
            ResultSet dayTable = metaData.getTables(null, null, "day", new String[] {"TABLE"});
            if (dayTable.next()) {
                logger.warn("Table 'day' exists in the database but should not");
            } else {
                logger.info("Table 'day' does not exist (as expected)");
            }
            
            logger.info("Checking for table 'days' existence");
            ResultSet daysTable = metaData.getTables(null, null, "days", new String[] {"TABLE"});
            if (daysTable.next()) {
                logger.info("Table 'days' exists in the database (as expected)");
                // Check column structure
                ResultSet columns = metaData.getColumns(null, null, "days", null);
                List<String> columnNames = new ArrayList<>();
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String dataType = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    columnNames.add(columnName);
                    logger.info("Column in 'days' table: {} (type: {}, size: {})", columnName, dataType, columnSize);
                }
                
                // Check if expected columns exist
                if (!columnNames.contains("day_id") || !columnNames.contains("day_name")) {
                    logger.error("Missing expected columns in 'days' table");
                }
            } else {
                logger.error("Table 'days' does not exist - THIS IS A PROBLEM");
            }
            
            // List all tables in the database for debugging
            logger.info("All tables in the current database:");
            ResultSet allTables = metaData.getTables(null, null, "%", new String[] {"TABLE"});
            while (allTables.next()) {
                String tableName = allTables.getString("TABLE_NAME");
                logger.info("Table: {}", tableName);
            }
        } catch (Exception e) {
            logger.error("Failed to check database tables", e);
        }
    }
} 