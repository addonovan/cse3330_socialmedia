CREATE OR REPLACE FUNCTION FindReactionsTo(
    _PostId      INTEGER
) RETURNS TABLE (
    ProfileId   INTEGER,
    EmotionId   INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
        SELECT profileid, emotionid FROM "PostReaction"
        WHERE postid = _PostId;

END
$$;

CREATE OR REPLACE FUNCTION FindEmotionByName(
    _EmotionName "RefEmotion".emotionname%TYPE
) RETURNS SETOF "RefEmotion"
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
        SELECT * FROM "RefEmotion"
        WHERE emotionname = _EmotionName;

END
$$;

CREATE OR REPLACE FUNCTION FindRepliesToPost(
    _PostId INTEGER
) RETURNS SETOF "Post"
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
        SELECT * FROM "Post" p
        WHERE
            p.parentpostid IS NOT NULL AND
            p.parentpostid = _PostId
        ORDER BY p.createdtime DESC;

END
$$;



CREATE OR REPLACE FUNCTION FindAdminedPages(
    UserId  INTEGER
) RETURNS SETOF INTEGER
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
        SELECT pageid FROM "PageAdmin"
        WHERE profileid = UserId;

END
$$;

CREATE OR REPLACE FUNCTION UpdateProfile(
    _AccountId  INTEGER,
    _Email       "Account".Email%TYPE,
    _PhoneNumber "Account".PhoneNumber%TYPE,
    _FirstName   "Profile".FirstName%TYPE,
    _LastName    "Profile".LastName%TYPE,
    _Username    "Profile".Username%TYPE,
    _Password    "Profile".Password%TYPE
) RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN

    UPDATE "Account"
    SET email = _email, phonenumber = _phonenumber
    WHERE accountid = _AccountId;

    UPDATE "Profile"
    SET firstname = _FirstName, lastname = _LastName, username = _Username, password = _Password
    WHERE accountid = _AccountId;

END
$$;


CREATE OR REPLACE FUNCTION GetEventInterest(
    _EventId         INTEGER,
    _IsAttending     BOOLEAN
) RETURNS SETOF INTEGER
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
        SELECT ei.profileid FROM "EventInterest" ei
        WHERE ei.eventid = _eventid AND ei.isattending = _IsAttending;

END
$$;

CREATE OR REPLACE FUNCTION MarkEventInterest(
    UserId          INTEGER,
    EventId         INTEGER,
    OnlyInterested  BOOLEAN
) RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN

    INSERT INTO "EventInterest"(eventid, profileid, isattending)
    VALUES (eventid, userid, NOT OnlyInterested);

END
$$;

CREATE OR REPLACE FUNCTION DeleteEvent(
    EventId     INTEGER
) RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
    _event_id INTEGER = EventId;

BEGIN

    DELETE FROM "Event" e
        WHERE e.eventid = _event_id;

END
$$;

CREATE OR REPLACE FUNCTION FindCalendarFor(
    AccountId   INTEGER
) RETURNS SETOF "Event"
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
        SELECT * FROM "Event" e
        WHERE e.hostid = AccountId
            OR e.hostid IN (
                SELECT followeeid FROM "Follow"
                WHERE followerid = AccountId
            );

END
$$;

CREATE OR REPLACE FUNCTION CreateEvent(
    HostId      INTEGER,
    Name        "Event".EventName%TYPE,
    Description "Event".EventDesc%TYPE,
    StartTime   TIMESTAMP,
    EndTime     TIMESTAMP,
    Location    "Event".Location%TYPE
) RETURNS INTEGER
LANGUAGE plpgsql
AS $$

DECLARE
    _new_id INTEGER = 0;

BEGIN

    INSERT INTO "Event" (hostid, eventname, eventdesc, starttime, endtime, location)
    VALUES (HostId, name, description, starttime, endtime, location)
    RETURNING eventid INTO _new_id;

    RETURN _new_id;

END
$$;

CREATE OR REPLACE FUNCTION FindEvent(
    EventId     INTEGER
) RETURNS SETOF "Event"
LANGUAGE plpgsql
AS $$

DECLARE
    _event_id INTEGER = EventId;

BEGIN

    RETURN QUERY
        SELECT * FROM "Event" e
        WHERE e.eventid = _event_id;

END
$$;

CREATE OR REPLACE FUNCTION FindFollowing(
    FollowerId          INTEGER
) RETURNS TABLE (
    FolloweeId          INTEGER
)
LANGUAGE plpgsql
AS $$

DECLARE
    _follower_id INTEGER = FollowerId;

BEGIN

    RETURN QUERY
        SELECT f.FolloweeId
        FROM "Follow" f
        WHERE f.followerid = _follower_id;

END
$$;

CREATE OR REPLACE FUNCTION FindFollowers(
    FolloweeId          INTEGER,
    Requests            BOOLEAN
) RETURNS TABLE (
    FollowerId          INTEGER
)
LANGUAGE plpgsql
AS $$

DECLARE
    _followee_id INTEGER = FolloweeId;

BEGIN

    IF Requests THEN

        RETURN QUERY
            SELECT fr.FollowerId
            FROM "FollowRequest" fr
            WHERE fr.followeeid = _followee_id;

    ELSE

        RETURN QUERY
            SELECT f.FollowerId
            FROM "Follow" f
            WHERE f.followeeid = _followee_id;

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
    _follower_id INTEGER = FollowerId;
    _followee_id INTEGER = FolloweeId;

BEGIN

    -- if we need to unfollow, then we can just remove it from the tables
    IF NOT Following THEN

        DELETE FROM "Follow"
        WHERE "Follow".followerid = _follower_id
          AND "Follow".followeeid = _followee_id;

        DELETE FROM "FollowRequest"
        WHERE "FollowRequest".followerid = _follower_id
          AND "FollowRequest".followeeid = _followee_id;

    ELSE

        -- check if the followee account is private
        SELECT isprivate INTO followee_is_private
        FROM "Account" a
        WHERE a.accountid = FolloweeId;

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

CREATE OR REPLACE FUNCTION FindFeedFor(
    AccountId           INTEGER
) RETURNS SETOF "Post"
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
        SELECT * FROM "Post" p
        WHERE p.wallid = AccountId
           OR (
                p.parentpostid IS NULL
                    AND
                p.posterid IN (SELECT followeeid FROM "Follow" f WHERE f.followerid = AccountId)
           )
        ORDER BY p.createdtime DESC;

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


