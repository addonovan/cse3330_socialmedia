-- Create all Tables

CREATE TABLE Profile (
  ProfileId           SERIAL          PRIMARY KEY,
  FirstName           VARCHAR(100)    NOT NULL,
  LastName            VARCHAR(100)    NOT NULL,
  PhoneNumber         CHAR(12)        NOT NULL,
  Email               VARCHAR(100)    NOT NULL,
  UserName            VARCHAR(100)    NOT NULL,
  Password            VARCHAR(100)    NOT NULL,
  CreatedTime         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ProfileActive       BOOL            NOT NULL DEFAULT TRUE
);

CREATE TABLE Page (
  PageId              SERIAL          PRIMARY KEY,
  PageName            VARCHAR(100)    NOT NULL,
  PageDescription     VARCHAR(512)    NOT NULL,
  HeaderImageURL      VARCHAR(2083)   NOT NULL DEFAULT '//pages/headers/default.png',
  ProfileImageURL     VARCHAR(2083)   NOT NULL DEFAULT '//pages/pictures/default.png',
  PageActive          BOOL            NOT NULL DEFAULT TRUE
);

CREATE TABLE PageLike (
  ProfileId           INTEGER         NOT NULL REFERENCES Profile(ProfileId),
  PageId              INTEGER         NOT NULL REFERENCES Page(PageId)
);

CREATE TABLE PageAdmin (
  ProfileId           INTEGER         NOT NULL REFERENCES Profile(ProfileId),
  PageId              INTEGER         NOT NULL REFERENCES Page(PageId)
);

CREATE TABLE PageCategory (
  PageId              INTEGER         NOT NULL REFERENCES Page(PageId),
  CATEGORY            VARCHAR(20)     NOT NULL
);

CREATE TABLE Post (
  PostId              SERIAL          PRIMARY KEY,
  PageId              INTEGER         NOT NULL REFERENCES Page(PageId),
  ProfileId           INTEGER         REFERENCES Profile(ProfileId),
  Message             VARCHAR(1024),
  AttachmentURL       VARCHAR(2083),
  ParentPostId        INTEGER         REFERENCES Post(PostId),
  CreatedTime         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE PostLikes (
  ProfileId           INTEGER         NOT NULL REFERENCES Profile(ProfileId),
  PageId              INTEGER         NOT NULL REFERENCES Page(PageId)
);

CREATE TABLE "Group" (
  GroupId             SERIAL          PRIMARY KEY,
  GroupName           VARCHAR(128)    NOT NULL,
  GroupPictureURL     VARCHAR(2083)   NOT NULL DEFAULT '//groups/default.png',
  GroupDescription    VARCHAR(512)    NOT NULL DEFAULT ''
);

CREATE TABLE GroupMember (
  GroupId             INTEGER         NOT NULL REFERENCES "Group"(GroupId),
  ProfileId           INTEGER         NOT NULL REFERENCES Profile(ProfileId)
);

CREATE TABLE GroupMessage (
  SenderProfileId     INTEGER         NOT NULL REFERENCES Profile(ProfileId),
  GroupId             INTEGER         NOT NULL REFERENCES "Group"(GroupId),
  Message             VARCHAR(1024),
  AttachmentURL       VARCHAR(2083),
  CreatedTime         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA "public" TO "application";

-- Insert Testing Data

INSERT INTO Profile
  (FirstName, LastName, PhoneNumber, Email, UserName, Password)
  VALUES
  ('Austin', 'Donovan', '81772999079', 'austin@addonovan.com', 'addonovan', 'password1');
