package edu.uta.cse3330.socialmediasite.model;


import java.sql.Date;

public class Profile {

    private final int profileId;

    private final String firstName;

    private final String lastName;

    private final String phoneNumber;

    private final String email;

    private final String password;

    private final Date createdTime;

    private final boolean profileActive;

    public Profile(
            int profileId,
            String firstName,
            String lastName,
            String phoneNumber,
            String email,
            String password,
            Date createdTime,
            boolean profileActive
    ) {
        this.profileId = profileId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.createdTime = createdTime;
        this.profileActive = profileActive;
    }

    public int getProfileId() {
        return profileId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public boolean isProfileActive() {
        return profileActive;
    }
}