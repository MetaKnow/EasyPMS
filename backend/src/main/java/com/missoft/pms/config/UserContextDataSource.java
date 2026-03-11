package com.missoft.pms.config;

import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class UserContextDataSource extends AbstractDataSource {

    private final DataSource targetDataSource;

    public UserContextDataSource(DataSource targetDataSource) {
        this.targetDataSource = targetDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = targetDataSource.getConnection();
        applyCurrentUser(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = targetDataSource.getConnection(username, password);
        applyCurrentUser(connection);
        return connection;
    }

    private void applyCurrentUser(Connection connection) throws SQLException {
        Long userId = UserContextHolder.getCurrentUserId();
        try (PreparedStatement ps = connection.prepareStatement("SET @current_user_id = ?")) {
            if (userId == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, userId);
            }
            ps.execute();
        }
    }
}
