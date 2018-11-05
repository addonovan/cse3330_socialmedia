-- Drop in reverse order of creation
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA "public" FROM "application";

-- Drop all of the base tables and cascade deletions
DROP TABLE "DirectMessage";
DROP TABLE "GroupMember";
DROP TABLE "Group";
DROP TABLE "PollVotes";
DROP TABLE "PollAnswer" CASCADE;
DROP TABLE "PollPost";
DROP TABLE "PostReaction";
DROP TABLE "RefEmotion";
DROP TABLE "Post";
DROP TABLE "EventInterest";
DROP TABLE "Event";
DROP TABLE "PageCategory";
DROP TABLE "PageInvite";
DROP TABLE "PageAdmin";
DROP TABLE "PageLike";
DROP TABLE "Page";
DROP TABLE "Profile";
