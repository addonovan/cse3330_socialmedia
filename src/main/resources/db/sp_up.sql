CREATE OR REPLACE FUNCTION CreatePage(
    AdminId     INTEGER,
    Email       "Account".Email%TYPE,
    PhoneNumber "Account".PhoneNumber%TYPE,
    Name        "Page".PageName%TYPE,
    Description "Page".PageDesc%TYPE
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
    RETURNING AccountId INTO account_id;

    INSERT INTO "Page"
        (accountid, pagename, pagedesc)
    VALUES
        (account_id, name, description);

    INSERT INTO "PageAdmin"
        (pageid, profileid)
    VALUES
        (account_id, AdminId);

    RETURN account_id;

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
    RETURNING AccountId INTO account_id;

    INSERT INTO "Profile"
        (accountid, firstname, lastname, username, Password, languageid)
    VALUES
        (account_id, FirstName, LastName, Username, Password, 1);

    RETURN account_id;

END
$$;


