# --- !Ups
CREATE TABLE "employee"("id" INTEGER NOT NULL PRIMARY KEY ,"name" varchar(200) , "email" varchar(200)  ,"company_name" varchar,"position" varchar, "time" varchar(200));
INSERT INTO "employee" values (1,'Vikas', 'vikas@knoldus.com','Knoldus','CTO','2015-10-10');
INSERT INTO "employee" values (2,'Bhavya', 'bhavya@knoldus.com','Knoldus','Senior Director','2015-10-10');
INSERT INTO "employee" values (3,'Ayush', 'ayush@knoldus.com','Knoldus','Lead Consultant','2015-10-10');
INSERT INTO "employee" values (4,'Satendra', 'satendra@knoldus.com','Knoldus','Senior Consultant','2015-10-10');


# --- !Downs

DROP TABLE "employee";
