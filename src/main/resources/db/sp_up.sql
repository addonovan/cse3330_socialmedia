CREATE OR REPLACE FUNCTION FindProfile(
    DesiredId       INTEGER,
    DesiredUsername VARCHAR(32)
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
          (DesiredId IS NULL OR p.accountid = DesiredId)
            AND
          (DesiredUsername IS NULL OR p.username = DesiredUsername);


END
$$;

CREATE OR REPLACE FUNCTION FindPage(
    PageId INTEGER
) RETURNS TABLE (
    Name                "Page".Name%TYPE,
    Description         "Page".Description%TYPE,
    ViewCount           "Page".ViewCount%TYPE,
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
            p.name, p.description, p.viewcount,
            a.id, a.email, a.phonenumber, a.profileimageurl, a.headerimageurl,
            a.isactive, a.isprivate, a.createdtime
    FROM "Page" p
    INNER JOIN "Account" a ON p.accountid = a.id
    WHERE
      p.accountid = PageId;


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
