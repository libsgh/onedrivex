CREATE TABLE IF NOT EXISTS "public"."config" ("key" text COLLATE "pg_catalog"."default" NOT NULL,"value" text COLLATE "pg_catalog"."default");
ALTER TABLE "public"."config" ADD CONSTRAINT "config_pkey" PRIMARY KEY ("key");
INSERT INTO "public"."config" VALUES ('clientId', '');
INSERT INTO "public"."config" VALUES ('clientSecret', '');
INSERT INTO "public"."config" VALUES ('redirectUri', 'http://localhost:8080/authRedirect');
INSERT INTO "public"."config" VALUES ('refreshTokenCron', '0 0/15 * * * ?');
INSERT INTO "public"."config" VALUES ('tokenInfo', '');