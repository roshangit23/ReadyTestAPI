package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DatabaseHelper {
    private static final Logger logger = LogManager.getLogger(DatabaseHelper.class);
    private Connection connection;

    public DatabaseHelper(String url, String username, String password) {
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            logger.error("Database connection failed: " + url, e);
            Assert.fail("Database connection failed: " + url);
        }
    }

    public ResultSet executeQuery(String query) {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            logger.error("Error executing query: " + query, e);
            Assert.fail("Error executing query: " + query);
            return null;
        }
    }

    public boolean executeUpdate(String query) {
        try {
            Statement statement = connection.createStatement();
            int result = statement.executeUpdate(query);
            return result > 0;
        } catch (SQLException e) {
            logger.error("Error executing update: " + query, e);
            Assert.fail("Error executing update: " + query);
            return false;
        }
    }

    public void startTransaction() {
        try {
            if (connection != null) {
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            logger.error("Error starting a transaction.", e);
            Assert.fail("Error starting a transaction.");
        }
    }

    public void commitTransaction() {
        try {
            if (connection != null) {
                connection.commit();
            }
        } catch (SQLException e) {
            logger.error("Error committing transaction.", e);
            Assert.fail("Error committing transaction.");
        }
    }

    public void rollbackTransaction() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e) {
            logger.error("Error rolling back transaction.", e);
            Assert.fail("Error rolling back transaction.");
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Error closing the database connection.", e);
            Assert.fail("Error closing the database connection.");
        }
    }

    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            logger.error("Error checking if connection is valid.", e);
            Assert.fail("Error checking if connection is valid.");
            return false;
        }
    }

    public List<Object[]> executeQueryAndGetResults(String query) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();
            int columns = metaData.getColumnCount();
            List<Object[]> results = new ArrayList<>();

            while (rs.next()) {
                Object[] row = new Object[columns];
                for (int i = 1; i <= columns; ++i) {
                    row[i - 1] = rs.getObject(i);
                }
                results.add(row);
            }
            return results;
        } catch (SQLException e) {
            logger.error("Error executing query and retrieving results: " + query, e);
            Assert.fail("Error executing query and retrieving results: " + query);
            return null;
        }
    }

    public boolean checkIfRowExists(String query) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            return rs.next();
        } catch (SQLException e) {
            logger.error("Error checking if row exists for query: " + query, e);
            Assert.fail("Error checking if row exists for query: " + query);
            return false;
        }
    }

    public int getRowCount(String query) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
            }
            return rowCount;
        } catch (SQLException e) {
            logger.error("Error getting row count for query: " + query, e);
            Assert.fail("Error getting row count for query: " + query);
            return -1;
        }
    }

    public boolean executeBatchUpdate(List<String> queries) {
        try {
            Statement statement = connection.createStatement();
            for (String query : queries) {
                statement.addBatch(query);
            }
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            logger.error("Error executing batch update", e);
            Assert.fail("Error executing batch update");
            return false;
        }
    }

    public boolean verifyColumnValue(String query, String columnName, Object expectedValue) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                Object actualValue = rs.getObject(columnName);
                return expectedValue.equals(actualValue);
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error verifying column value for query: " + query, e);
            Assert.fail("Error verifying column value for query: " + query);
            return false;
        }
    }

    // Method to check if a specific table exists in the database
    public boolean verifyTableExistence(String tableName) {
        String query = "SELECT * FROM information_schema.tables WHERE table_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tableName);
            ResultSet rs = preparedStatement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.error("Error verifying table existence: " + tableName, e);
            Assert.fail("Error verifying table existence: " + tableName);
            return false;
        }
    }

    // Method to retrieve the data type of specific column in a table
    public String getColumnDataType(String tableName, String columnName) {
        String query = "SELECT DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tableName);
            preparedStatement.setString(2, columnName);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getString("DATA_TYPE");
            } else {
                return null;
            }
        } catch (SQLException e) {
            logger.error("Error getting column data type for " + columnName + " in " + tableName, e);
            Assert.fail("Error getting column data type for " + columnName + " in " + tableName);
            return null;
        }
    }

    // Method to check if a specified column is a primary key
    public boolean verifyPrimaryKey(String tableName, String columnName) {
        String query = "SELECT * FROM information_schema.table_constraints tc INNER JOIN " +
                "information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name " +
                "WHERE tc.table_name = ? AND kcu.column_name = ? AND tc.constraint_type = 'PRIMARY KEY'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tableName);
            preparedStatement.setString(2, columnName);
            ResultSet rs = preparedStatement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.error("Error verifying primary key for " + columnName + " in " + tableName, e);
            Assert.fail("Error verifying primary key for " + columnName + " in " + tableName);
            return false;
        }
    }

    // Method to list all columns in a table that have unique constraints
    public List<String> getUniqueConstraintColumns(String tableName) {
        String query = "SELECT column_name FROM information_schema.table_constraints tc INNER JOIN " +
                "information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name " +
                "WHERE tc.table_name = ? AND tc.constraint_type = 'UNIQUE'";
        List<String> columns = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tableName);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                columns.add(rs.getString("column_name"));
            }
        } catch (SQLException e) {
            logger.error("Error getting unique constraint columns for table: " + tableName, e);
            Assert.fail("Error getting unique constraint columns for table: " + tableName);
        }
        return columns;
    }

    public List<Object[]> executePreparedQuery(String query, Object... parameters) {
        List<Object[]> results = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            ResultSet rs = preparedStatement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columns = metaData.getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[columns];
                for (int i = 1; i <= columns; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                results.add(row);
            }
        } catch (SQLException e) {
            logger.error("Error executing prepared query: " + query, e);
            Assert.fail("Error executing prepared query: " + query);
        }
        return results;
    }

    public boolean executeCallableStatement(String procedureCall, Object... parameters) {
        try (CallableStatement callableStatement = connection.prepareCall(procedureCall)) {
            for (int i = 0; i < parameters.length; i++) {
                callableStatement.setObject(i + 1, parameters[i]);
            }
            return callableStatement.execute();
        } catch (SQLException e) {
            logger.error("Error executing callable statement: " + procedureCall, e);
            Assert.fail("Error executing callable statement: " + procedureCall);
            return false;
        }
    }

    public DatabaseMetaData getDatabaseMetaData() {
        try {
            return connection.getMetaData();
        } catch (SQLException e) {
            logger.error("Error retrieving database metadata", e);
            Assert.fail("Error retrieving database metadata");
            return null;
        }
    }

    public ResultSetMetaData getTableMetaData(String tableName) {
        String query = "SELECT * FROM " + tableName + " WHERE 1 = 0"; // No data is actually queried
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            return rs.getMetaData();
        } catch (SQLException e) {
            logger.error("Error retrieving metadata for table: " + tableName, e);
            Assert.fail("Error retrieving metadata for table: " + tableName);
            return null;
        }
    }

    public List<String> listAllTables() {
        List<String> tables = new ArrayList<>();
        try (ResultSet rs = getDatabaseMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            logger.error("Error listing all tables", e);
            Assert.fail("Error listing all tables");
        }
        return tables;
    }

    public long getExecutionTimeForQuery(String query) {
        long startTime = System.currentTimeMillis();
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            logger.error("Error executing query for performance testing: " + query, e);
            Assert.fail("Error executing query for performance testing: " + query);
        }
        return System.currentTimeMillis() - startTime;
    }

    public void runLoadTest(String query, int numberOfExecutions) {
        try (Statement statement = connection.createStatement()) {
            for (int i = 0; i < numberOfExecutions; i++) {
                statement.execute(query);
            }
        } catch (SQLException e) {
            logger.error("Error running load test with query: " + query, e);
            Assert.fail("Error running load test with query: " + query);
        }
    }

    public void truncateTable(String tableName) {
        String query = "TRUNCATE TABLE " + tableName;
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            logger.error("Error truncating table: " + tableName, e);
            Assert.fail("Error truncating table: " + tableName);
        }
    }

    public void insertTestData(String tableName, Map<String, Object> data) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (String key : data.keySet()) {
            columns.append(key).append(",");
            values.append("?").append(",");
        }
        columns.setLength(columns.length() - 1); // Remove last comma
        values.setLength(values.length() - 1);   // Remove last comma
        String query = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int index = 1;
            for (Object value : data.values()) {
                preparedStatement.setObject(index++, value);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error inserting test data into table: " + tableName, e);
            Assert.fail("Error inserting test data into table: " + tableName);
        }
    }

    public void deleteTestData(String tableName, String condition) {
        String query = "DELETE FROM " + tableName + " WHERE " + condition;
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            logger.error("Error deleting test data from table: " + tableName, e);
            Assert.fail("Error deleting test data from table: " + tableName);
        }
    }

    public boolean compareResultSets(ResultSet rs1, ResultSet rs2) throws SQLException {
        ResultSetMetaData rsmd1 = rs1.getMetaData();
        ResultSetMetaData rsmd2 = rs2.getMetaData();
        int columnCount = rsmd1.getColumnCount();
        if (columnCount != rsmd2.getColumnCount()) {
            return false;
        }

        while (rs1.next()) {
            if (!rs2.next()) {
                return false;
            }
            for (int i = 1; i <= columnCount; i++) {
                if (!Objects.equals(rs1.getObject(i), rs2.getObject(i))) {
                    return false;
                }
            }
        }
        return !rs2.next();
    }

    public boolean compareTableData(String table1, String table2) {
        String query1 = "SELECT * FROM " + table1;
        String query2 = "SELECT * FROM " + table2;

        try (Statement statement = connection.createStatement();
             ResultSet rs1 = statement.executeQuery(query1);
             ResultSet rs2 = statement.executeQuery(query2)) {
            return compareResultSets(rs1, rs2);
        } catch (SQLException e) {
            logger.error("Error comparing table data between " + table1 + " and " + table2, e);
            Assert.fail("Error comparing table data between " + table1 + " and " + table2);
            return false;
        }
    }
}

