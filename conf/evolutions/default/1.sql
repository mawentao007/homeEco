# --- !Ups
CREATE TABLE "account"(
    "id" INTEGER NOT NULL PRIMARY KEY ,
    "date" varchar ,
    "io" INTEGER  ,
    "amount" INTEGER,
    "balance" INTEGER,
    "reason" varchar);

INSERT INTO "account" values (1,'1',1, 1,1,'吃饭');

---!Downs

---!DROP TABLE "employee";
