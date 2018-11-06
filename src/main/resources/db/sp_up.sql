CREATE OR REPLACE FUNCTION FindProfileById(
    DesiredId   "Account".Id%TYPE
) RETURNS TABLE (
    FirstName           "Profile".FirstName%TYPE,
    LastName            "Profile".LastName%TYPE,
    Username            "Profile".Username%TYPE,
    Password            "Profile".Password%TYPE,
    LanguageId          "Profile".LanguageId%TYPE,
    Id                  "Account".Id%TYPE,
    Email               "Account".Email%TYPE,
    PhoneNumber         "Account".PhoneNumber%TYPE,
    ProfileImageURL     "Account".ProfileImageURL%TYPE,
    HeaderImageURL      "Account".HeaderImageURL%TYPE,
    IsActive            BOOLEAN,
    IsPrivate           BOOLEAN,
    CreatedTime         TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
        SELECT
            p.firstname, p.lastname, p.username, p.password, p.languageid,
            a.id, a.email, a.phonenumber, a.profileimageurl, a.headerimageurl,
            a.isactive, a.isprivate, a.createdtime
        FROM "Profile" p
        INNER JOIN "Account" a ON p.accountid = a.id
        WHERE
            p.accountid = DesiredId
            AND
            a.isactive = TRUE;


END
$$;

CREATE OR REPLACE FUNCTION CreateProfile(
    Email       "Account".Email%TYPE,
    PhoneNumber "Account".PhoneNumber%TYPE,
    FirstName   "Profile".FirstName%TYPE,
    LastName    "Profile".LastName%TYPE,
    Username    "Profile".Username%TYPE,
    Password    "Profile".Password%TYPE
) RETURNS INTEGER
LANGUAGE plpgsql
AS $$

    DECLARE
        account_id INTEGER := -1;

    BEGIN

        INSERT INTO "Account"
          (email, phonenumber, isprivate)
        VALUES
          (Email, PhoneNumber, FALSE)
        RETURNING Id INTO account_id;

        INSERT INTO "Profile"
          (accountid, firstname, lastname, username, Password, languageid)
        VALUES
          (account_id, FirstName, LastName, Username, Password, 1);

        RETURN account_id;

    END
$$;
