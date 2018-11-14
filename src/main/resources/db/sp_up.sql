CREATE OR REPLACE FUNCTION GetFollowing(
    FollowerId          INTEGER
) RETURNS TABLE (
    FolloweeId          INTEGER
)
LANGUAGE plpgsql
AS $$

BEGIN

    SELECT FolloweeId
    FROM "Follow" f
    WHERE f.followerid = FollowerId;

END
$$;

CREATE OR REPLACE FUNCTION GetFollowers(
    FolloweeId          INTEGER,
    Requests            BOOLEAN
) RETURNS TABLE (
    FollowerId          INTEGER
)
LANGUAGE plpgsql
AS $$

BEGIN

    IF Requests THEN

        SELECT FollowerId
        FROM "FollowRequest" fr
        WHERE fr.followeeid = FolloweeId;

    ELSE

        SELECT FollowerId
        FROM "Follow" f
        WHERE f.followeeid = FolloweeId;

    END IF;

END
$$;

CREATE OR REPLACE FUNCTION UpdateFollow(
    FollowerId          INTEGER,
    FolloweeId          INTEGER,
    Following           BOOLEAN
) RETURNS VOID
LANGUAGE plpgsql
AS $$

DECLARE
    followee_is_private BOOLEAN = TRUE;

BEGIN

    -- if we need to unfollow, then we can just remove it from the tables
    IF NOT Following THEN

        DELETE FROM "Follow"
        WHERE "Follow".followerid = FollowerId
            AND "Follow".followeeid = FolloweeId;

        DELETE FROM "FollowRequest"
        WHERE "FollowRequest".followerid = FollowerId
            AND "FollowRequest".followeeid = FolloweeId;

    ELSE

        -- check if the followee account is private
        SELECT isprivate Into followee_is_private
        FROM "Account" a
        WHERE a.id = FolloweeId;

        -- if it's private, then we'll add a follow request
        IF followee_is_private THEN
            INSERT INTO "FollowRequest" (followerid, followeeid)
            VALUES (FollowerId, FolloweeId);

        -- otherwise, we'll just add the follower
        ELSE
            INSERT INTO "Follow" (followerid, followeeid)
            VALUES (FollowerId, FolloweeId);
        END IF;

    END IF;

END
$$;

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
        (PosterId, WallId, PostMessage, PostMediaURL, PollQuestion, PollEndTime, ParentPostId)
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
    PageName            "Page".PageName%TYPE,
    PageDesc            "Page".PageDesc%TYPE,
    ViewCount           "Page".ViewCount%TYPE,
    AccountId           "Account".AccountId%TYPE,
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


