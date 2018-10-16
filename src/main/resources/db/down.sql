-- Drop in reverse order of creation
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA "public" FROM "application";

DROP TABLE GroupMessage;
DROP TABLE GroupMember;
DROP TABLE "Group";
DROP TABLE PostLikes;
DROP TABLE Post;
DROP TABLE PageCategory;
DROP TABLE PageAdmin;
DROP TABLE PageLike;
DROP TABLE Page;
DROP TABLE Profile;