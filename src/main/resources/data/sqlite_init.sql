CREATE TABLE IF NOT EXISTS "config" ("key" text NOT NULL,"value" text NOT NULL,PRIMARY KEY ("key"));
INSERT OR IGNORE INTO "config" VALUES ('clientId', '');
INSERT OR IGNORE INTO "config" VALUES ('clientSecret', '');
INSERT OR IGNORE INTO "config" VALUES ('redirectUri', '');
INSERT OR IGNORE INTO "config" VALUES ('refreshTokenCron', '0 0/15 * * * ?');
INSERT OR IGNORE INTO "config" VALUES ('tokenInfo', '');
INSERT OR IGNORE INTO "config" VALUES ('herokuKeepAliveCron', '0 0/25 * * * ?');
INSERT OR IGNORE INTO "config" VALUES ('herokuKeepAliveAddress', '');
INSERT OR IGNORE INTO "config" VALUES ('siteName', 'onedrive-x');
INSERT OR IGNORE INTO "config" VALUES ('refreshCacheCron', '0 0/10 * * * ?');
INSERT OR IGNORE INTO "config" VALUES ('theme', 'nexmoe');
INSERT OR IGNORE INTO "config" VALUES ('password', 'onedrive-x');
INSERT OR IGNORE INTO "config" VALUES ('onedriveRoot', '/');
INSERT OR IGNORE INTO "config" VALUES ('onedriveHide', '');
INSERT OR IGNORE INTO "config" VALUES ('onedriveHotlink', '');
INSERT OR IGNORE INTO "config" VALUES ('cacheExpireTime', '3600');
INSERT OR IGNORE INTO "config" VALUES ('openCache', '0');
INSERT OR IGNORE INTO "config" VALUES ('uploadPath', '/app/upload')