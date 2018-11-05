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
