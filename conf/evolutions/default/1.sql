# --- !Ups
CREATE TABLE "account"(
    "id" INTEGER NOT NULL PRIMARY KEY ,
    "date" varchar ,
    "user" varchar,
    "io" varchar(10)  ,
    "kind" varchar(10)  ,
    "amount" REAL,
    "balance" REAL,
    "reason" varchar,
    "whetherLatest" INTEGER);

---!INSERT INTO "account" values (1,'1',"收入", 1,1,'吃饭',1);

---!Downs

---!DROP TABLE "employee";
