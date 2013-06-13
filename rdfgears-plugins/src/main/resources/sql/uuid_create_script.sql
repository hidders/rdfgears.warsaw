CREATE DATABASE IF NOT EXISTS imreal;

USE imreal;

CREATE TABLE IF NOT EXISTS uuid (
	id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
	uuid VARCHAR(100) NOT NULL UNIQUE,
	email VARCHAR(100)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS uuid_webid (
	uuid_id int NOT NULL,
	webid VARCHAR(100),
	provider VARCHAR(100),
	PRIMARY KEY (uuid_id, webid, provider),
	FOREIGN KEY (uuid_id) REFERENCES uuid (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS userProfile (
	id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
	uuid_id int NOT NULL,
	topic VARCHAR(100),
	dvalue TEXT,
	FOREIGN KEY (uuid_id) REFERENCES uuid (id),
	UNIQUE KEY (uuid_id, topic)
) ENGINE=InnoDB;