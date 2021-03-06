-- 3.1 (plpgsql) Creating a new Profile -------------------
DECLARE
account_id INTEGER := -1;

BEGIN

INSERT INTO "Account"
    (email, phonenumber, isprivate)
VALUES
       (Email, PhoneNumber, FALSE)
    RETURNING AccountId INTO account_id;

INSERT INTO "Profile"
    (accountid, firstname, lastname, username, Password)
VALUES
       (account_id, FirstName, LastName, Username, Password);

RETURN account_id;

END
------------------------------------------------------------

-- 3.2 (plpgsql) Creating a new Page -----------------------
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
------------------------------------------------------------

-- 3.3 (JDBC) Create a new Post ----------------------------
INSERT INTO "Post"
    (posterid, wallid, postmessage, postmediaurl,
     pollquestion, pollendtime, parentpostid)
VALUES (?, ?, ?, ?, ?, ?, ?)
    RETURNING postid;
------------------------------------------------------------

-- 3.4 (JDBC) Show page details ----------------------------
SELECT * FROM "Account" a
LEFT JOIN "Profile" prof ON PROF.accountid = a.accountid
LEFT JOIN "Page" page ON page.accountid = a.accountid
WHERE a.accountid = ?;
------------------------------------------------------------

-- 3.5 (JDBC) Show a profile's activity --------------------
SELECT * FROM "Post" p
WHERE p.posterid = ?
ORDER BY p.createdtime DESC;
------------------------------------------------------------

-- 3.6 (JDBC) Count posts on a given wall on a date -------
SELECT COUNT(postid) FROM "Post"
WHERE wallid = ? AND createdtime::date = ?
------------------------------------------------------------

-- 3.7a (JDBC) Update account information ------------------
UPDATE "Account"
SET email = ?, phonenumber = ?, profileimageurl = ?,
    headerimageurl = ?, isprivate = ?, isactive = ?
WHERE accountid = ?;
------------------------------------------------------------

-- 3.7b (JDBC) Update profile information ------------------
UPDATE "Profile"
SET firstname = ?, lastname = ?, username = ?, password = ?
WHERE accountid = ?;
------------------------------------------------------------

-- 3.7c (JDBC) Update page information ---------------------
UPDATE "Page"
SET pagename = ?, pagedesc = ?
WHERE accountid = ?;
------------------------------------------------------------

-- 3.8 (JDBC) Deactivate a profile -------------------------
-- NOTE: neither ACCOUNTs nor their POSTs may be deleted
--   from this system. There is an explanation in the design
--   decisions document.
UPDATE "Account"
SET isactive = ?
WHERE accountid = ?;
------------------------------------------------------------
