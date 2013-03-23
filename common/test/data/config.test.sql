CREATE DATABASE IF NOT EXISTS testconfig DEFAULT CHARACTER SET utf8;

USE testconfig;

DROP TABLE IF EXISTS config;

CREATE TABLE config (
   id                     BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
   boolvalue              BOOLEAN,
   intvalue               INT,
   doublevalue            DOUBLE,
   stringvalue            VARCHAR(50),
   textvalue              TEXT
) ENGINE = InnoDB;

-- Finally, insert default table values.
INSERT INTO config (boolvalue, intvalue, doublevalue, stringvalue, textvalue) VALUES (null, null, null, null, null);
INSERT INTO config (boolvalue, intvalue, doublevalue, stringvalue, textvalue) VALUES (false, 111, 1.11, "111", "111_111");
INSERT INTO config (boolvalue, intvalue, doublevalue, stringvalue, textvalue) VALUES (true, 222, 22.2222, "222", "222_222");
