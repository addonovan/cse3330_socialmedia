-- Reference Tables
CREATE TABLE "RefLanguage" (
    LanguageId      SERIAL          PRIMARY KEY,
    LocaleCode      VARCHAR(16)     NOT NULL UNIQUE,
    LanguageName    VARCHAR(64)     NOT NULL UNIQUE
);

CREATE TABLE "RefInterfaceText" (
    LanguageId      INTEGER         NOT NULL REFERENCES "RefLanguage"(languageid),
    "Key"           VARCHAR(64)     NOT NULL,
    Format          VARCHAR(2048)   NOT NULL,

    PRIMARY KEY (LanguageId, "Key")
);

CREATE TABLE "RefEmotion" (
    EmotionId       SERIAL          PRIMARY KEY,
    EmotionName     VARCHAR(64)     NOT NULL,
    ImageURL        VARCHAR(2083)   NOT NULL
);

-- Accounts
CREATE TABLE "Account" (
    AccountId       SERIAL          PRIMARY KEY,
    Email           VARCHAR(128)    NOT NULL,
    PhoneNumber     CHAR(10),
    ProfileImageURL VARCHAR(2083)   NOT NULL DEFAULT '/media/profiles/default.png',
    HeaderImageURL  VARCHAR(2083)   NOT NULL DEFAULT '/media/headers/default.png',
    IsPrivate       BOOLEAN         NOT NULL,
    IsActive        BOOLEAN         NOT NULL DEFAULT TRUE,
    CreatedTime     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "Profile" (
    AccountId       INTEGER         PRIMARY KEY REFERENCES "Account"(AccountId),
    FirstName       VARCHAR(32)     NOT NULL,
    LastName        VARCHAR(32)     NOT NULL,
    Username        VARCHAR(32)     NOT NULL UNIQUE,
    Password        VARCHAR(32)     NOT NULL,
    LanguageId      INTEGER         NOT NULL REFERENCES "RefLanguage"(languageid)
);

CREATE TABLE "Page" (
    AccountId       INTEGER         PRIMARY KEY REFERENCES "Account"(AccountId),
    PageName        VARCHAR(64)     NOT NULL,
    PageDesc        VARCHAR(512)    NOT NULL,
    ViewCount       INTEGER         NOT NULL DEFAULT 0
);

-- Content
CREATE TABLE "Event" (
    EventId         SERIAL          PRIMARY KEY,
    HostId          INTEGER         NOT NULL REFERENCES "Account"(AccountId),
    EventName       VARCHAR(128)    NOT NULL,
    EventDesc       VARCHAR(128)    NOT NULL,
    StartTime       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    EndTime         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Location        VARCHAR(512)
);

CREATE TABLE "Post" (
    PostId          SERIAL          PRIMARY KEY,
    PosterId        INTEGER         NOT NULL REFERENCES "Account"(AccountId),
    WallId          INTEGER         NOT NULL REFERENCES "Account"(AccountId),
    PostMessage     VARCHAR(4096),
    PostMediaURL    VARCHAR(2083),
    PollQuestion    VARCHAR(128),
    PollEndTime     TIMESTAMP,
    ParentPostId    INTEGER         REFERENCES "Post"(postid),
    CreatedTime     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT post_has_content CHECK (
        PostMessage IS NOT NULL
        OR PostMediaURL IS NOT NULL
        OR PollQuestion IS NOT NULL
    ),

    CONSTRAINT poll_post_has_entities CHECK (
        (PollQuestion IS NULL) = (PollEndTime IS NULL)
    )
);

CREATE TABLE "PollAnswer" (
    PollAnswerId    SERIAL          PRIMARY KEY,
    PostId          INTEGER         NOT NULL REFERENCES "Post"(postid),
    PollAnswerText  VARCHAR(32)     NOT NULL
);

-- Groups & Messaging
CREATE TABLE "Group" (
    GroupId         SERIAL          PRIMARY KEY,
    GroupName       VARCHAR(128)    NOT NULL,
    GroupDesc       VARCHAR(512),
    GroupPictureURL VARCHAR(2083)   NOT NULL DEFAULT '/media/groups/default.png'
);

CREATE TABLE "GroupMessage" (
    MessageId       SERIAL          PRIMARY KEY,
    SenderId        INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    GroupId         INTEGER         NOT NULL REFERENCES "Group"(groupid),
    MessageText     VARCHAR(4096),
    MediaURL        VARCHAR(2083),
    SendTime        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT group_message_has_content CHECK (
        MessageText IS NOT NULL
        OR MediaURL IS NOT NULL
    )
);

-- Relations
CREATE TABLE "GroupMember" (
    ProfileId       INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    GroupId         INTEGER         NOT NULL REFERENCES "Group"(GroupId)
);

CREATE TABLE "PollVote" (
    PollId          INTEGER         NOT NULL REFERENCES "Post"(PostId),
    PollAnswerId    INTEGER         NOT NULL REFERENCES "PollAnswer"(PollAnswerId),
    ProfileId       INTEGER         NOT NULL REFERENCES "Profile"(AccountId)
);

CREATE TABLE "PostReaction" (
    PostId          INTEGER         NOT NULL REFERENCES "Post"(PostId),
    ProfileId       INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    EmotionId       INTEGER         NOT NULL REFERENCES "RefEmotion"(EmotionId)
);

CREATE TABLE "EventInterest" (
    EventId         INTEGER         NOT NULL REFERENCES "Event"(EventId),
    ProfileId       INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    IsAttending     BOOLEAN         NOT NULL
);

CREATE TABLE "PageAdmin" (
    PageId          INTEGER         NOT NULL REFERENCES "Page"(AccountId),
    ProfileId       INTEGER         NOT NULL REFERENCES "Profile"(AccountId)
);

CREATE TABLE "FollowRequest" (
    FollowerId      INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    FolloweeId      INTEGER         NOT NULL REFERENCES "Account"(AccountId)
);

CREATE TABLE "Follow" (
    FollowerId      INTEGER         NOT NULL REFERENCES "Profile"(AccountId),
    FolloweeId      INTEGER         NOT NULL REFERENCES "Account"(AccountId)
);

--
-- Grant permissions
--

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA "public" TO "application";
