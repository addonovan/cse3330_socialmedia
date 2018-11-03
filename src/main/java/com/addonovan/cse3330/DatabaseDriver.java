package com.addonovan.cse3330;

import com.addonovan.cse3330.model.Profile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class DatabaseDriver {

    /** A URL used to open a connection to the SocialMedia database. */
    private static final String CONNECTION_STRING = "jdbc:postgresql://localhost/SocialMedia";

    /** The actual database connection. */
    private static final Connection CONNECTION;

    static {
        Properties props = new Properties();
        props.setProperty("user", "application");
        props.setProperty("password", "password1"); // oh boy! check that security!

        // if there's a problem connecting to the database, then we'll immediately
        // throw an exception and die off
        try {
            CONNECTION = DriverManager.getConnection(CONNECTION_STRING, props);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open database connection!", e);
        }

        // make sure the connection is (properly) closed on shutdown, not that
        // it *really* matters all that much
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                CONNECTION.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close database connection!", e);
            }
        }));
    }

    /**
     * @return A list of all active profiles.
     */
    public static List<Profile> listProfiles() {
        List<Profile> profiles = new ArrayList<>();

        try (Statement stmt = CONNECTION.createStatement()) {
            ResultSet results = stmt.executeQuery("SELECT * FROM Profile;");

            while (results.next()) {
                boolean active = results.getBoolean("ProfileActive");
                if (!active) continue;

                profiles.add(new Profile(
                        results.getInt("ProfileId"),
                        results.getString("FirstName"),
                        results.getString("LastName"),
                        results.getString("PhoneNumber"),
                        results.getString("Email"),
                        results.getString("UserName"),
                        results.getString("Password"),
                        results.getDate("CreatedTime"),
                        true
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Encountered error running query", e);
        }

        return profiles;
    }

}
