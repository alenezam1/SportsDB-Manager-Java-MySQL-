import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class code {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/"; 

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            System.out.println("Connected to MySQL server successfully.");
            //Mohsens Original
            System.out.println(" DB:\n");
            createAndSelectDatabase(connection);
            createTables(connection);
            populateTableFromCSV(connection, "DB.csv");
            executeQueries(connection);
            //ojas code
            System.out.println("\n DB:\n");
            ojasCreateAndSelectDatabase(connection);
            ojasCreateTables(connection);
            ojasPopulateTableFromCSV(connection, "DB.csv");
            ojasExecuteQueries(connection);

            System.out.println("Database, tables setup, and data population completed successfully.");
        } catch (SQLException | IOException e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createAndSelectDatabase(Connection connection) throws SQLException {
        String dbName = "DB";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            statement.executeUpdate("USE " + dbName);
            System.out.println("Database '" + dbName + "' created and selected successfully.");
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        // DDL for Sports table
        String createSportsTable = "CREATE TABLE IF NOT EXISTS Sports (" +
                "SportID INT PRIMARY KEY," +
                "SportName VARCHAR(255) NOT NULL," +
                "WorldGoverningBody VARCHAR(255)" +
                ");";

        // DDL for Teams table
        String createTeamsTable = "CREATE TABLE IF NOT EXISTS Teams (" +
                "TeamID INT PRIMARY KEY," +
                "TeamName VARCHAR(255) NOT NULL," +
                "SportID INT," +
                "FoundedYear INT," +
                "Country VARCHAR(255)," +
                "FOREIGN KEY (SportID) REFERENCES Sports(SportID)" +
                ");";

        // DDL for Players table
        String createPlayersTable = "CREATE TABLE IF NOT EXISTS Players (" +
                "PlayerID INT PRIMARY KEY," +
                "PlayerName VARCHAR(255) NOT NULL," +
                "TeamID INT," +
                "SportID INT," +
                "Nationality VARCHAR(255)," +
                "FOREIGN KEY (TeamID) REFERENCES Teams(TeamID)," +
                "FOREIGN KEY (SportID) REFERENCES Sports(SportID)" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createSportsTable);
            statement.executeUpdate(createTeamsTable);
            statement.executeUpdate(createPlayersTable);
            System.out.println("Tables created successfully.");
        }
    }

    private static void populateTableFromCSV(Connection connection, String filePath) throws SQLException, IOException {
        String insertSports = "INSERT INTO Sports (SportID, SportName, WorldGoverningBody) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE SportName=VALUES(SportName), WorldGoverningBody=VALUES(WorldGoverningBody);";
        String insertTeams = "INSERT INTO Teams (TeamID, TeamName, SportID, FoundedYear, Country) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE TeamName=VALUES(TeamName), SportID=VALUES(SportID), FoundedYear=VALUES(FoundedYear), Country=VALUES(Country);";
        String insertPlayers = "INSERT INTO Players (PlayerID, PlayerName, TeamID, SportID, Nationality) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE PlayerName=VALUES(PlayerName), TeamID=VALUES(TeamID), SportID=VALUES(SportID), Nationality=VALUES(Nationality);";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
                PreparedStatement psSports = connection.prepareStatement(insertSports);
                PreparedStatement psTeams = connection.prepareStatement(insertTeams);
                PreparedStatement psPlayers = connection.prepareStatement(insertPlayers)) {

            String line;
            br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                // Inserting into Sports table
                psSports.setInt(1, Integer.parseInt(values[0].trim())); // SportID
                psSports.setString(2, values[1].trim()); // SportName
                psSports.setString(3, values[2].trim()); // WorldGoverningBody
                psSports.executeUpdate();

                // Inserting into Teams table
                psTeams.setInt(1, Integer.parseInt(values[3].trim())); // TeamID
                psTeams.setString(2, values[4].trim()); // TeamName
                psTeams.setInt(3, Integer.parseInt(values[0].trim())); // SportID 
                psTeams.setInt(4, Integer.parseInt(values[5].trim())); // FoundedYear
                psTeams.setString(5, values[6].trim()); // Country
                psTeams.executeUpdate();

                // Inserting into Players table
                psPlayers.setInt(1, Integer.parseInt(values[7].trim())); // PlayerID
                psPlayers.setString(2, values[8].trim()); // PlayerName
                psPlayers.setInt(3, Integer.parseInt(values[3].trim())); // TeamID 
                psPlayers.setInt(4, Integer.parseInt(values[0].trim())); // SportID 
                psPlayers.setString(5, values[9].trim()); // Nationality
                psPlayers.executeUpdate();
            }
            System.out.println("Data inserted successfully from " + filePath);
        }
    }

    private static void executeQueries(Connection connection) throws SQLException {
        // DELETE operations
        System.out.println("Executing DELETE operations...");
        executeDeleteStatement(connection, "DELETE FROM Sports WHERE SportID = 1");
        executeDeleteStatement(connection, "DELETE FROM Teams WHERE TeamID = 218");

        // SELECT queries 
        System.out.println("Executing SELECT queries...");
        executeSelectQuery(connection,
                "SELECT S.SportName, COUNT(T.TeamID) AS NumberOfTeams FROM Sports S JOIN Teams T ON S.SportID = T.SportID GROUP BY S.SportName HAVING COUNT(T.TeamID) > 3");
        executeSelectQuery(connection,
                "SELECT P.Nationality, COUNT(P.PlayerID) AS NumberOfPlayers FROM Players P GROUP BY P.Nationality HAVING COUNT(P.PlayerID) > 5");
    }

   
    private static void executeDeleteStatement(Connection connection, String deleteSql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(deleteSql);
            System.out.println("Rows affected by the delete operation: " + rowsAffected);
        } catch (SQLException e) {
            System.out.println("Error executing DELETE statement: " + e.getMessage());
        }
    }

    private static void executeSelectQuery(Connection connection, String selectSql) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(selectSql)) {
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1)
                        System.out.print(",  ");
                    String columnValue = resultSet.getString(i);
                    System.out.print(rsmd.getColumnName(i) + ": " + columnValue);
                }
                System.out.println("");
            }
        } catch (SQLException e) {
            System.out.println("Error executing SELECT query: " + e.getMessage());
        }
    }

}
