INSERT INTO "Account"(accountid, email, phonenumber, isprivate)
VALUES
    (1, 'austin@addonovan.com', '8177299079', FALSE),
    (2, 'austin@addonovan.com', '8177299079', FALSE),
    (3, 'austin@addonovan.com', '8177299079', FALSE),
    (4, 'austin@addonovan.com', '8177299079', FALSE),
    (5, 'austin@addonovan.com', '8177299079', FALSE),
    (6, 'austin@addonovan.com', '8177299079', FALSE),
    (7, 'austin@addonovan.com', '8177299079', FALSE),
    (8, 'austin@addonovan.com', '8177299079', FALSE),
    (9, 'austin@addonovan.com', '8177299079', FALSE),
    (10, 'austin@addonovan.com', '8177299079', FALSE),
    (11, 'austin@addonovan.com', '8177299079', FALSE),
    (12, 'austin@addonovan.com', '8177299079', FALSE),
    (13, 'austin@addonovan.com', '8177299079', FALSE),
    (14, 'austin@addonovan.com', '8177299079', FALSE),
    (15, 'austin@addonovan.com', '8177299079', FALSE),
    (16, 'austin@addonovan.com', '8177299079', FALSE),
    (17, 'austin@addonovan.com', '8177299079', FALSE),
    (18, 'austin@addonovan.com', '8177299079', FALSE),
    (19, 'austin@addonovan.com', '8177299079', FALSE),
    (20, 'austin@addonovan.com', '8177299079', FALSE);

INSERT INTO "Profile"(accountid, firstname, lastname, username, password)
VALUES
    (1, 'Austin', 'Donovan', 'addonovan', ''),
    (2, 'Bustin', 'Donovan', 'bddonovan', ''),
    (3, 'Custin', 'Donovan', 'cddonovan', ''),
    (4, 'Dustin', 'Donovan', 'dddonovan', ''),
    (5, 'Eustin', 'Donovan', 'eddonovan', ''),
    (15, 'Pustin', 'Donovan', 'pddonovan', ''),
    (16, 'Qustin', 'Donovan', 'qddonovan', ''),
    (17, 'Rustin', 'Donovan', 'rddonovan', ''),
    (18, 'Tustin', 'Donovan', 'tddonovan', ''),
    (19, 'Uustin', 'Donovan', 'uddonovan', ''),
    (20, 'Vustin', 'Donovan', 'vddonovan', '');

INSERT INTO "Page"(accountid, pagename, pagedesc)
VALUES
    (6,  'Austin''s Page Emporium', 'Pages for all Ages!'),
    (7,  'Bustin''s Page Emporium', 'Pages for all Ages!'),
    (8,  'Custin''s Page Emporium', 'Pages for all Ages!'),
    (9,  'Dustin''s Page Emporium', 'Pages for all Ages!'),
    (10, 'Estin''s Page Emporium', 'Pages for all Ages!'),
    (11, 'Pustin''s Page Emporium', 'Pages for all Ages!'),
    (12, 'Qustin''s Page Emporium', 'Pages for all Ages!'),
    (13, 'Uustin''s Page Emporium', 'Pages for all Ages!'),
    (14, 'Vustin''s Page Emporium', 'Pages for all Ages!');

INSERT INTO "Follow"(followerid, followeeid)
VALUES
    (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
    (2, 1), (2, 10),
    (4, 5),
    (5, 1), (5, 2), (5, 3), (5, 4), (5, 5);

INSERT INTO "PageAdmin"(profileid, pageid)
VALUES
    (1, 6), (2, 7), (3, 8), (4, 9), (5, 10),
    (15, 11), (16, 12), (17, 13), (18, 14);

INSERT INTO "Post"(postid, posterid, wallid, postmessage, postmediaurl, pollquestion, pollendtime, parentpostid)
VALUES
    (1, 1, 1, 'Hey guys!', NULL, NULL, NULL, NULL);

INSERT INTO "PostReaction"(postid, profileid, emotionid)
VALUES
    (1, 1, 1), (1, 2, 1), (1, 3, 1), (1, 4, 1), (1, 5, 1),
    (1, 15, 1), (1, 16, 1), (1, 17, 1), (1, 18, 1), (1, 19, 1), (1, 20, 1);

INSERT INTO "Event"(hostid, eventname, eventdesc, starttime, endtime, location)
VALUES
    (1, 'Austin''s Birthday Bash', 'Let''s celebrate my 21st', '2019-05-01 00:00:00', '2019-05-01 23:59:59', NULL);

INSERT INTO "EventInterest"(eventid, profileid, isattending)
VALUES
    (1, 1, TRUE), (1, 2, TRUE), (1, 3, TRUE), (1, 4, TRUE), (1, 5, TRUE),
    (1, 15, TRUE), (1, 16, FALSE), (1, 17, TRUE), (1, 18, TRUE), (1, 19, TRUE),
    (1, 20, FALSE);
