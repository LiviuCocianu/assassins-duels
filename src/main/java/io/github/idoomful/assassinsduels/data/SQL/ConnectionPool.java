/*
package io.github.idoomful.assassinsduels.data.SQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionPool {
    private static final String DATABASE_URL = "jdbc:mysql://" +
            SettingsYML.SQL.HOST.getString() + ":" +
            SettingsYML.SQL.PORT.getInt() + "/" +
            SettingsYML.SQL.DATABASE.getString();

    private static final String USERNAME = SettingsYML.SQL.USERNAME.getString();
    private static final String PASSWORD = SettingsYML.SQL.PASSWORD.getString();
    private static final boolean USE_SSL = SettingsYML.SQL.USE_SSL.getBoolean();
    private static final int MAX_POOL = SettingsYML.SQL.MAX_POOL_SIZE.getInt();

    private static final HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;

    static {
        Logger.getLogger("com.zaxxer.hikari.HikariDataSource").setLevel(Level.OFF);

        try {
            config.setJdbcUrl(DATABASE_URL);
            config.setUsername(USERNAME);
            config.setPassword(PASSWORD);
            config.setMaximumPoolSize(MAX_POOL);
            config.setConnectionTestQuery("SELECT 1");

            config.setMinimumIdle(0);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(35000);
            config.setMaxLifetime(45000);

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useSSL", USE_SSL);
            dataSource = new HikariDataSource(config);
        } catch(Exception e) {
            DMain.getInstance().getLogger().warning(MessagesYML.Errors.SQL_FAIL.withPrefix());
        }
    }

    public boolean isConnectionValid() {
        if(dataSource == null) return false;

        try(Connection connection = dataSource.getConnection()) {
            return connection.isValid(0);
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection connect() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void disconnect() {
        dataSource.close();
    }
}
*/
