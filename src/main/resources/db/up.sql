--
-- Create all Tables
--

CREATE TABLE "Profile" (
  Id                  SERIAL          PRIMARY KEY,
  FirstName           VARCHAR(100)    NOT NULL,
  LastName            VARCHAR(100)    NOT NULL,
  PhoneNumber         CHAR(12)        NOT NULL,
  Email               VARCHAR(100)    NOT NULL,
  UserName            VARCHAR(100)    NOT NULL,
  Password            VARCHAR(100)    NOT NULL,
  CreatedTime         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  Active              BOOL            NOT NULL DEFAULT TRUE
);

CREATE TABLE "Page" (
  Id                  SERIAL          PRIMARY KEY,
  Name                VARCHAR(100)    NOT NULL,
  Description         VARCHAR(512)    NOT NULL,
  HeaderImageURL      VARCHAR(2083)   NOT NULL DEFAULT '//media/pages/default_header.png',
  ProfileImageURL     VARCHAR(2083)   NOT NULL DEFAULT '//media/pages/default_profile.png',
  Active              BOOL            NOT NULL DEFAULT TRUE
);

-- Profile/Page Relationships
CREATE TABLE "PageLike" (
  ProfileId           INTEGER         NOT NULL REFERENCES "Profile"(Id),
  PageId              INTEGER         NOT NULL REFERENCES "Page"(Id)
);

CREATE TABLE "PageAdmin" (
  ProfileId           INTEGER         NOT NULL REFERENCES "Profile"(Id),
  PageId              INTEGER         NOT NULL REFERENCES "Page"(Id)
);

CREATE TABLE "PageInvite" (
  ProfileId           INTEGER         NOT NULL REFERENCES "Profile"(Id),
  SenderProfileId     INTEGER         NOT NULL REFERENCES "Profile"(Id),
  PageId              INTEGER         NOT NULL REFERENCES "Page"(Id)
);

CREATE TABLE "PageCategory" (
  PageId              INTEGER         NOT NULL REFERENCES "Page"(Id),
  Category            VARCHAR(32)     NOT NULL
);

-- Event
CREATE TABLE "Event" (
  Id                  SERIAL          PRIMARY KEY,
  PageId              INTEGER         NOT NULL REFERENCES "Page"(Id),
  Name                VARCHAR(100)    NOT NULL,
  Description         VARCHAR(4096)   NOT NULL DEFAULT '',
  StartTime           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  EndTime             TIMESTAMP       NOT NULL,
  Location            VARCHAR(512)    NOT NULL
);

CREATE TABLE "EventInterest" (
  EventId             INTEGER         NOT NULL REFERENCES "Event"(Id),
  ProfileId           INTEGER         NOT NULL REFERENCES "Profile"(Id),
  Attending           BOOL            NOT NULL
);

-- Posts
CREATE TABLE "Post" (
  Id                  SERIAL          PRIMARY KEY,
  PosterId            INTEGER         REFERENCES "Profile"(Id),
  PageId              INTEGER         NOT NULL REFERENCES "Page"(Id),
  ParentPostId        INTEGER         REFERENCES "Post"(Id),
  Message             VARCHAR(4096),
  MediaURL            VARCHAR(2083)
);

CREATE TABLE "RefEmotion" (
  Id                  SERIAL          PRIMARY KEY,
  Name                VARCHAR(32)     NOT NULL,
  Image               VARCHAR(2083)   NOT NULL
);

CREATE TABLE "PostReaction" (
  PostId              INTEGER         NOT NULL REFERENCES "Post"(Id),
  ProfileId           INTEGER         NOT NULL REFERENCES "Profile"(Id),
  EmotionId           INTEGER         NOT NULL REFERENCES "RefEmotion"(Id)
);

-- Poll Posts
CREATE TABLE "PollPost" (
  PostId              INTEGER         PRIMARY KEY REFERENCES "Post"(Id),
  Question            VARCHAR(128)    NOT NULL,
  EndTime             TIMESTAMP       NOT NULL
);

CREATE TABLE "PollAnswer" (
  Id                  SERIAL          PRIMARY KEY,
  PostId              INTEGER         NOT NULL REFERENCES "PollPost"(PostId),
  AnswerText          VARCHAR(128)    NOT NULL
);

CREATE TABLE "PollVotes" (
  PollAnswerId        INTEGER         NOT NULL REFERENCES "PollAnswer"(Id),
  ProfileId           INTEGER         NOT NULL REFERENCES "Profile"(Id)
);

-- Group & GMs
CREATE TABLE "Group" (
  Id                  SERIAL          PRIMARY KEY,
  Name                VARCHAR(64)     NOT NULL,
  Description         VARCHAR(256)    NOT NULL DEFAULT '',
  PictureURL          VARCHAR(2083)   NOT NULL DEFAULT '//media/groups/default.png'
);

CREATE TABLE "GroupMember" (
  GroupId             INTEGER         NOT NULL REFERENCES "Group"(Id),
  ProfileId           INTEGER         NOT NULL REFERENCES "Profile"(Id)
);

CREATE TABLE "DirectMessage" (
  GroupId             INTEGER         NOT NULL REFERENCES "Group"(Id),
  SenderProfileId     INTEGER         NOT NULL REFERENCES "Profile"(Id),
  Message             VARCHAR(4096),
  MediaURL            VARCHAR(2083)
);

--
-- Grant permissions
--

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA "public" TO "application";

--
-- Insert Testing Data
--

INSERT INTO RefEmotion
  (Name, Image)
  VALUES
  ('Like', '//media/reactions/like.png'),
  ('Anger', '//media/reactions/anger.png'),
  ('Dislike', '//media/reactions/dislike.png'),
  ('Love', '//media/reactions/love.png');

INSERT INTO Profile
  (FirstName, LastName, PhoneNumber, Email, UserName, Password)
  VALUES
  ('Austin', 'Donovan', '8179911052', '', 'addonovan', 'password1'),
  ('Giannina', 'Pachas', '8179911052', '', 'giannapachas', 'password2'),
  ('Emily', 'Knowles', '8179911052', '', 'elknowles', 'password3'),
  ('Joseph', 'Brewster', '8179911052', '', 'jojettison', 'password4');

