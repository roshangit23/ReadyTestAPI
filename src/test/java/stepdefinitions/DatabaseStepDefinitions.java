package stepdefinitions;

import common.Common;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.testng.Assert;
import utils.DatabaseHelper;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DatabaseStepDefinitions extends Common {

    private DatabaseHelper dbHelper = CucumberHooks.getDatabaseHelper();
    private ResultSet currentResultSet;
    private boolean updateResult;
    private List<Object[]> currentQueryResults;

    public DatabaseStepDefinitions() {
    }

    @Given("I execute the query named {string}")
    public void iExecuteTheQueryNamed(String queryName) {
        String query = getQueryFromYaml(queryName);
        currentResultSet = dbHelper.executeQuery(query);
    }

    @Then("I expect the query result to be non-empty")
    public void iExpectTheQueryResultToBeNonEmpty() {
        Assert.assertNotNull(currentResultSet, "Result set is null");
        try {
            Assert.assertTrue(currentResultSet.next(), "Result set is empty");
        } catch (Exception e) {
            throw new AssertionError("Error while asserting result set: " + e.getMessage(), e);
        }
    }

    @Given("I start a database transaction")
    public void iStartADatabaseTransaction() {
        dbHelper.startTransaction();
    }

    @Given("I commit the database transaction")
    public void iCommitTheDatabaseTransaction() {
        dbHelper.commitTransaction();
    }

    @Given("I rollback the database transaction")
    public void iRollbackTheDatabaseTransaction() {
        dbHelper.rollbackTransaction();
    }

    @Given("I execute an update named {string}")
    public void iExecuteUpdateNamed(String updateName) {
        String query = getQueryFromYaml(updateName);
        updateResult = dbHelper.executeUpdate(query);
        Assert.assertTrue(updateResult, "Expected update to be successful, but it was not.");
    }
    @Then("I expect the update to be successful")
    public void iExpectTheUpdateToBeSuccessful() {
        Assert.assertTrue(updateResult, "Update was not successful");
    }
    @Given("I execute the query named {string} and expect {int} rows")
    public void iExecuteTheQueryAndExpectRows(String queryName, int expectedRowCount) {
        String query = getQueryFromYaml(queryName);
        List<Object[]> results = dbHelper.executeQueryAndGetResults(query);
        Assert.assertEquals(results.size(), expectedRowCount, "Row count does not match expected value.");
    }

    @Given("I check if row exists for query named {string}")
    public void iCheckIfRowExistsForQuery(String queryName) {
        String query = getQueryFromYaml(queryName);
        boolean exists = dbHelper.checkIfRowExists(query);
        Assert.assertTrue(exists, "Expected row to exist, but it does not.");
    }

    @Given("I verify table {string} exists")
    public void iVerifyTableExists(String tableName) {
        boolean exists = dbHelper.verifyTableExistence(tableName);
        Assert.assertTrue(exists, "Expected table to exist, but it does not.");
    }

    @Given("I verify column {string} in table {string} has data type {string}")
    public void iVerifyColumnDataType(String columnName, String tableName, String expectedDataType) {
        String dataType = dbHelper.getColumnDataType(tableName, columnName);
        Assert.assertNotNull(dataType, "Data type is null for the specified column.");
        Assert.assertEquals(dataType, expectedDataType, "Data type does not match expected value.");
    }
    @Then("I verify that the query named {string} results in {int} rows")
    public void iVerifyRowCount(String queryName, int expectedRowCount) {
        String query = getQueryFromYaml(queryName);
        int rowCount = dbHelper.getRowCount(query);
        Assert.assertEquals(rowCount, expectedRowCount, "The number of rows returned by the query does not match the expected count.");
    }

    @Then("I verify that the query named {string} results in column {string} having value {string}")
    public void iVerifyColumnValue(String queryName, String columnName, String expectedValue) {
        String query = getQueryFromYaml(queryName);
        boolean columnValueMatches = dbHelper.verifyColumnValue(query, columnName, expectedValue);
        Assert.assertTrue(columnValueMatches, "The value of column '" + columnName + "' does not match the expected value '" + expectedValue + "'.");
    }

    @Given("I verify column {string} is a primary key in table {string}")
    public void iVerifyPrimaryKey(String columnName, String tableName) {
        boolean isPrimaryKey = dbHelper.verifyPrimaryKey(tableName, columnName);
        Assert.assertTrue(isPrimaryKey, "Column is not a primary key as expected.");
    }

    @Given("I list unique constraint columns for table {string}")
    public void iListUniqueConstraintColumns(String tableName) {
        List<String> columns = dbHelper.getUniqueConstraintColumns(tableName);
        Assert.assertNotNull(columns, "Failed to retrieve unique constraint columns.");
    }

    @Given("I execute a batch of updates from YAML named {string}")
    public void iExecuteBatchOfUpdates(String batchUpdateName) {
        List<String> queries = getBatchQueriesFromYaml(batchUpdateName);
        boolean result = dbHelper.executeBatchUpdate(queries);
        Assert.assertTrue(result, "Batch update failed to execute successfully.");
    }

    @Given("I close the database connection")
    public void iCloseDatabaseConnection() {
        dbHelper.closeConnection();
        boolean isValid = dbHelper.isConnectionValid();
        Assert.assertFalse(isValid, "Database connection did not close as expected.");
    }

    @Then("I verify the database metadata")
    public void iVerifyDatabaseMetadata() {
        DatabaseMetaData metaData = dbHelper.getDatabaseMetaData();
        Assert.assertNotNull(metaData, "Failed to retrieve database metadata.");
    }

    @Then("I list all tables and expect at least {int} tables")
    public void iListAllTablesAndExpectMinimum(int expectedTableCount) {
        List<String> tables = dbHelper.listAllTables();
        Assert.assertNotNull(tables, "Failed to retrieve list of tables.");
        Assert.assertTrue(tables.size() >= expectedTableCount, "Number of tables is less than expected.");
    }

    @Given("I execute a prepared query named {string} with parameters {string}")
    public void iExecuteAPreparedQuery(String queryName, List<String> parameters) {
        String query = getQueryFromYaml(queryName);
        List<Object[]> results = dbHelper.executePreparedQuery(query, parameters.toArray());
        Assert.assertNotNull(results, "Results should not be null.");
        // Store results in a class variable if needed for subsequent steps
        this.currentQueryResults = results;
    }

    @Then("I expect the result to have {int} rows")
    public void iExpectResultToHaveRows(int expectedRowCount) {
        Assert.assertEquals(this.currentQueryResults.size(), expectedRowCount, "Result row count does not match expected value.");
    }

    @Then("I expect row {int} in the result to contain {string}")
    public void iExpectRowInResultToContain(int rowIndex, List<String> expectedValues) {
        Assert.assertTrue(rowIndex >= 0 && rowIndex < this.currentQueryResults.size(), "Row index is out of bounds.");
        Object[] row = this.currentQueryResults.get(rowIndex);
        for (int i = 0; i < expectedValues.size(); i++) {
            Assert.assertEquals(row[i].toString(), expectedValues.get(i), "Mismatch in row " + rowIndex + " at column " + i);
        }
    }
    @Given("I execute a callable statement {string} with parameters {string}")
    public void iExecuteCallableStatement(String procedureCall, List<String> parameters) {
        boolean result = dbHelper.executeCallableStatement(procedureCall, parameters.toArray());
        Assert.assertTrue(result, "Callable statement execution should be successful.");
    }

    @Given("I retrieve table metadata for {string}")
    public void iRetrieveTableMetadata(String tableName) {
        ResultSetMetaData metaData = dbHelper.getTableMetaData(tableName);
        Assert.assertNotNull(metaData, "Table metadata should not be null.");
    }

    @Given("I execute a performance test for query {string} and expect execution time less than {int} milliseconds")
    public void iExecutePerformanceTestForQuery(String queryName, int maxExecutionTime) {
        String query = getQueryFromYaml(queryName);
        long executionTime = dbHelper.getExecutionTimeForQuery(query);
        Assert.assertTrue(executionTime < maxExecutionTime, "Query execution time should be less than " + maxExecutionTime + " milliseconds.");
    }

    @Given("I run a load test with query {string} for {int} executions")
    public void iRunLoadTest(String queryName, int numberOfExecutions) {
        String query = getQueryFromYaml(queryName);
        dbHelper.runLoadTest(query, numberOfExecutions);
    }

    @Given("I truncate table {string}")
    public void iTruncateTable(String tableName) {
        dbHelper.truncateTable(tableName);
    }

    @Given("I insert test data into table {string} with values {string}")
    public void iInsertTestData(String tableName, Map<String, Object> data) {
        dbHelper.insertTestData(tableName, data);
    }

    @Given("I delete test data from table {string} with condition {string}")
    public void iDeleteTestData(String tableName, String condition) {
        dbHelper.deleteTestData(tableName, condition);
    }

    @Then("I verify that result sets {string} and {string} are equal")
    public void iVerifyResultSetsEquality(String queryName1, String queryName2) throws SQLException {
        String query1 = getQueryFromYaml(queryName1);
        String query2 = getQueryFromYaml(queryName2);

        List<Object[]> results1 = dbHelper.executeQueryAndGetResults(query1);
        List<Object[]> results2 = dbHelper.executeQueryAndGetResults(query2);

        Assert.assertEquals(results1.size(), results2.size(), "Result sets should have the same size.");

        for (int i = 0; i < results1.size(); i++) {
            Assert.assertTrue(Arrays.equals(results1.get(i), results2.get(i)), "Mismatch found in row " + i);
        }
    }

    @Then("I verify that table data between {string} and {string} are equal")
    public void iVerifyTableDataEquality(String tableName1, String tableName2) {
        boolean isEqual = dbHelper.compareTableData(tableName1, tableName2);
        Assert.assertTrue(isEqual, "Table data should be equal.");
    }

}
