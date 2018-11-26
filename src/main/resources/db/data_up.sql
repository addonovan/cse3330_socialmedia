DELETE FROM "RefEmotion" WHERE TRUE;
INSERT INTO "RefEmotion"
    (emotionname, ImageURL)
VALUES
       ('Like', '/media/reactions/like.png'),
       ('Anger', '/media/reactions/anger.png'),
       ('Dislike', '/media/reactions/dislike.png'),
       ('Love', '/media/reactions/love.png');

DELETE FROM "RefLanguage" WHERE TRUE;
INSERT INTO "RefLanguage"
    (LocaleCode, LanguageName)
VALUES
       ('en_us', 'English (United States)'),
       ('de_de', 'Deutsch (Deutschland)'),
       ('en_tx', 'English (Texas)');

