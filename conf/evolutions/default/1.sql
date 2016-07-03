# --- !Ups
CREATE TABLE "account"(
    "id" INTEGER NOT NULL PRIMARY KEY ,
    "date" varchar ,
    "io" varchar(10)  ,
    "amount" REAL,
    "balance" REAL,
    "reason" varchar);

INSERT INTO "account" values (1,'1',"收入", 1,1,'吃饭');

---!Downs

---!DROP TABLE "employee";
