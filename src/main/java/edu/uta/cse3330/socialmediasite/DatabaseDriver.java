package edu.uta.cse3330.socialmediasite;

import edu.uta.cse3330.socialmediasite.model.Profile;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class DatabaseDriver {

    private static final String CONNECTION_STRING = "jdbc:postgresql://localhost/SocialMedia";

    private static final Connection CONNECTION;

    static {
        Properties props = new Properties();
        props.setProperty("user", "oh no");
        props.setProperty("password", "oh no");
        props.setProperty("ssl", "true");

        try {
            CONNECTION = DriverManager.getConnection(CONNECTION_STRING, props);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open database connection!", e);
        }
    }


    public static List<Profile> listProfiles() {
        List<Profile> profiles = new ArrayList<>();
        profiles.add(new Profile(
                1,
                "Austin",
                "Donovan",
                "8177299079",
                "austin@addonovan.com",
                "password1",
                Date.valueOf("2018-10-15"),
                true
        ));
        return profiles;
    }

}
