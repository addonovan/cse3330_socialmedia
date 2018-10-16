package edu.uta.cse3330.socialmediasite;

import edu.uta.cse3330.socialmediasite.model.Profile;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseDriver {

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
