-- Reference Tables
CREATE TABLE "RefLanguage" (
    Id              SERIAL          PRIMARY KEY,
    LocaleCode      VARCHAR(16)     NOT NULL UNIQUE,
    Name            VARCHAR(64)     NOT NULL UNIQUE
);

CREATE TABLE "RefInterfaceText" (
    LanguageId      INTEGER         NOT NULL REFERENCES "RefLanguage"(Id),
    "Key"           VARCHAR(64)     NOT NULL,
    Format          VARCHAR(2048)   NOT NULL,

    PRIMARY KEY (LanguageId, "Key")
);

CREATE TABLE "RefEmotion" (
    Id              SERIAL          PRIMARY KEY,
    Name            VARCHAR(64)     NOT NULL,
    ImageURL        VARCHAR(2083)   NOT NULL
);

-- Accounts
CREATE TABLE "Account" (
    Id              SERIAL          PRIMARY KEY,
    Email           VARCHAR(128)    NOT NULL,
    PhoneNumber     CHAR(10),
    ProfileImageURL VARCHAR(2083)   NOT NULL DEFAULT '/media/profiles/default.png',
    HeaderImageURL  VARCHAR(2083)   NOT NULL DEFAULT '/media/headers/default.png',
    IsPrivate       BOOLEAN         NOT NULL,
    IsActive        BOOLEAN         NOT NULL DEFAULT TRUE,
    CreatedTime     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "Profile" (
    AccountId       INTEGER         PRIMARY KEY REFERENCES "Account"(Id),
    FirstName       VARCHAR(32)     NOT NULL,
    LastName        VARCHAR(32)     NOT NULL,
    Username        VARCHAR(32)     NOT NULL UNIQUE,
    Password        VARCHAR(32)     NOT NULL,
    LanguageId      INTEGER         NOT NULL REFERENCES "RefLanguage"(Id)
);

CREATE TABLE "Page" (
    AccountId       INTEGER         PRIMARY KEY REFERENCES "Account"(Id),
    Name            VARCHAR(64)     NOT NULL,
    Description     VARCHAR(512)    NOT NULL,
    ViewCount       INTEGER         NOT NULL DEFAULT 0
);

-- Content
CREATE TABLE "Event" (
    Id              SERIAL          PRIMARY KEY,
    HostId          INTEGER         NOT NULL REFERENCES "Account"(Id),
    Name            VARCHAR(128)    NOT NULL,
    Description     VARCHAR(128)    NOT NULL,
    StartTime       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    EndTime         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Location        VARCHAR(512)
);

CREATE TABLE "Post" (
    Id              SERIAL          PRIMARY KEY,
    PosterId        INTEGER         NOT NULL REFERENCES "Account"(Id),
    WallId          INTEGER         NOT NULL REFERENCES "Account"(Id),
    Message         VARCHAR(4096),
    MediaURL        VARCHAR(2083),
    PollQuestion    VARCHAR(128),
    PollEndTime     TIMESTAMP,
    ParentPostId    INTEGER         REFERENCES "Post"(Id),
    CreateTime      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT post_has_content CHECK (
        Message IS NOT NULL
        OR MediaURL IS NOT NULL
        OR PollQuestion IS NOT NULL
    ),

    CONSTRAINT poll_post_has_entities CHECK (
        (PollQuestion IS NULL) = (PollEndTime IS NULL)
    )
);

CREATE TABLE "PollAnswer" (
    Id              SERIAL          PRIMARY KEY,
    PostId          INTEGER         NOT NULL REFERENCES "Post"(Id),
    Text            VARCHAR(32)     NOT NULL
);

-- Groups & Messaging
CREATE TABLE "Group" (
    Id              SERIAL          PRIMARY KEY,
    Name            VARCHAR(128)    NOT NULL,
    Description     VARCHAR(512),
    PictureURL      VARCHAR(2083)   NOT NULL DEFAULT '/media/groups/default.png'
);

CREATE TABLE "GroupMessage" (
    Id              SERIAL          PRIMARY KEY,
    SenderId        INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    GroupId         INTEGER         NOT NULL REFERENCES "Group"(Id),
    Message         VARCHAR(4096),
    MediaURL        VARCHAR(2083),
    SendTime        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT group_message_has_content CHECK (
        Message IS NOT NULL
        OR MediaURL IS NOT NULL
    )
);

-- Relations
CREATE TABLE "GroupMember" (
    ProfileId       INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    GroupId         INTEGER         NOT NULL REFERENCES "Group"(Id)
);

CREATE TABLE "PollVote" (
    PollId          INTEGER         NOT NULL REFERENCES "Post"(Id),
    PollAnswerId    INTEGER         NOT NULL REFERENCES "PollAnswer"(Id),
    ProfileId       INTEGER         NOT NULL REFERENCES "Profile"(AccountId)
);

CREATE TABLE "PostReaction" (
    PostId          INTEGER         NOT NULL REFERENCES "Post"(Id),
    ProfileId       INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    EmotionId       INTEGER         NOT NULL REFERENCES "RefEmotion"(Id)
);

CREATE TABLE "EventInterest" (
    EventId         INTEGER         NOT NULL REFERENCES "Event"(Id),
    ProfileId       INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    IsAttending     BOOLEAN         NOT NULL
);

CREATE TABLE "PageAdmin" (
    PageId          INTEGER         NOT NULL REFERENCES "Page"(AccountId),
    ProfileId       INTEGER         NOT NULL REFERENCES "Profile"(AccountId)
);

CREATE TABLE "FollowRequest" (
    FollowerId      INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    FolloweeId      INTEGER         NOT NULL REFERENCES "Profile"(AccountId)
);

CREATE TABLE "Follow" (
    FollowerId      INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    FolloweeId      INTEGER         NOT NULL REFERENCES "Profile"(AccountId)
);

--
-- Grant permissions
--

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA "public" TO "application";

--
-- Insert Testing Data
--

INSERT INTO "RefEmotion"
    (Name, ImageURL)
VALUES
    ('Like', '//media/reactions/like.png'),
    ('Anger', '//media/reactions/anger.png'),
    ('Dislike', '//media/reactions/dislike.png'),
    ('Love', '//media/reactions/love.png');

INSERT INTO "RefLanguage"
    (LocaleCode, Name)
VALUES
    ('en_us', 'English (United States)'),
    ('de_de', 'Deutsch (Deutschland)'),
    ('en_tx', 'English (Texas)');
