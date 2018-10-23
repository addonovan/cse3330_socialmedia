-- Drop in reverse order of creation
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA "public" FROM "application";

DROP TABLE DirectMessage;
DROP TABLE "Group";
DROP TABLE PollAnswers;
DROP TABLE PollPost;
DROP TABLE PostReaction;
DROP TABLE RefEmotion;
DROP TABLE Post;
DROP TABLE EventInterest;
DROP TABLE Event;
DROP TABLE PageInvite;
DROP TABLE PageAdmin;
DROP TABLE PageLike;
DROP TABLE Page;
DROP TABLE Profile;
