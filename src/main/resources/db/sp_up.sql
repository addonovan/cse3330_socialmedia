
CREATE OR REPLACE FUNCTION CreatePost(
    AccountId           INTEGER,
    WallId              INTEGER,
    Message             "Post".PostMessage%TYPE,
    MediaURL            "Post".PostMediaURL%TYPE,
    PollQuestion        "Post".PollQuestion%TYPE,
    PollEndTime         "Post".PollEndTime%TYPE,
    ParentPostId        INTEGER
) RETURNS INTEGER
LANGUAGE plpgsql
AS $$

DECLARE
    post_id INTEGER;

BEGIN

    INSERT INTO "Post"
        (PosterId, WallId, PostMessage, MediaURL, PollQuestion, PollEndTime, ParentPostId)
    VALUES
        (AccountId, WallId, message, MediaURL, PollQuestion, PollEndTime, ParentPostId)
    RETURNING "Post".PostId INTO post_id;

    RETURN post_id;

END
$$;

CREATE OR REPLACE FUNCTION FindWallOverviewFor(
    AccountId           INTEGER
) RETURNS SETOF "Post"
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT * FROM "Post" p
    WHERE p.wallid = AccountId
    ORDER BY p.createdtime DESC;

END
$$;

CREATE OR REPLACE FUNCTION FindAccount(
    DesiredId       INTEGER,
    DesiredUsername VARCHAR(32)
) RETURNS TABLE (
    FirstName           "Profile".FirstName%TYPE,
    LastName            "Profile".LastName%TYPE,
    Username            "Profile".Username%TYPE,
    Password            "Profile".Password%TYPE,
    LanguageId          "Profile".LanguageId%TYPE,
    Name                "Page".PageName%TYPE,
    Description         "Page".PageDesc%TYPE,
    ViewCount           "Page".ViewCount%TYPE,
    Id                  "Account".AccountId%TYPE,
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
           prof.firstname, prof.lastname, prof.username, prof.password, prof.languageid,
           page.pagename, page.pagedesc, page.viewcount,
           a.AccountId, a.email, a.phonenumber, a.profileimageurl, a.headerimageurl,
           a.isactive, a.isprivate, a.createdtime
    FROM "Account" a
    LEFT JOIN "Profile" prof ON prof.accountid = a.AccountId
    LEFT JOIN "Page" page ON page.accountid = a.AccountId
    WHERE
        (DesiredId IS NULL OR a.AccountId = DesiredId)
        AND
        (DesiredUsername IS NULL OR prof.username = DesiredUsername);


END
$$;

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
        (accountid, name, description)
    VALUES
        (account_id, name, description);

    INSERT INTO "PageAdmin"
        (pageid, profileid)
    VALUES
        (account_id, AdminId);

    RETURN account_id;

END
$$;

CREATE OR REPLACE FUNCTION ViewPage(
    PageId          INTEGER
) RETURNS VOID
LANGUAGE plpgsql
AS $$

BEGIN
    UPDATE "Page" p
    SET viewcount = p.viewcount + 1
    WHERE p.accountid = PageId;
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


