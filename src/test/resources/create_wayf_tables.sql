-- MySQL dump 10.13  Distrib 5.7.18, for Linux (x86_64)
--
-- Host: localhost    Database: wayf
-- ------------------------------------------------------
-- Server version	5.7.18-0ubuntu0.16.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `device`
--

DROP TABLE IF EXISTS `device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `device` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `global_id` varchar(36) NOT NULL,
  `status` varchar(15) NOT NULL,
  `user_agent` varchar(250) NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `global_id_UNIQUE` (`global_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `identity_provider`
--

DROP TABLE IF EXISTS `identity_provider`;
CREATE TABLE `identity_provider` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(25) NOT NULL,
  `name` varchar(75) NULL,
  `entity_id` varchar(50) NULL,
  `scope` varchar(50) NULL,
  `organization_id` varchar(50) NULL,
  `federation_id` varchar(50) NULL,
  `provider` varchar(25) NULL,
  `created_date` timestamp(4) NOT NULL DEFAULT CURRENT_TIMESTAMP(4),
  `modified_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

DROP VIEW IF EXISTS `saml_entity`;
CREATE VIEW `saml_entity` AS
SELECT `id`,
	  `type`,
	  `name`,
    `entity_id`,
    `federation_id`,
    `created_date`,
    `modified_date`
  FROM identity_provider;

DROP VIEW IF EXISTS `open_athens_entity`;
CREATE VIEW `open_athens_entity` AS
SELECT `id`,
	  `type`,
	  `name`,
    `entity_id`,
    `scope`,
    `organization_id`,
    `created_date`,
    `modified_date`
  FROM identity_provider;

DROP VIEW IF EXISTS `oauth_entity`;
CREATE VIEW `oauth_entity` AS
SELECT `id`,
	  `type`,
		`name`,
    `provider`,
    `created_date`,
    `modified_date`
  FROM identity_provider;



DROP TABLE IF EXISTS `device_idp_blacklist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `device_idp_blacklist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `device_id` varchar(36) NOT NULL,
  `idp_id` int(11) NOT NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `authorization_token`;
CREATE TABLE `authorization_token` (
  `authenticatable` VARCHAR(50) NOT NULL,
  `authenticatable_type` varchar(25) NOT NULL,
  `authenticatable_id` int(11) NOT NULL,
  `token_type` varchar(30) DEFAULT NULL,
  `token_value` varchar(50) DEFAULT NULL,
  `valid_until` timestamp NULL DEFAULT NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`authenticatable`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `email_password_credentials`;
CREATE TABLE `email_password_credentials` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `authenticatable_type` varchar(25) NOT NULL,
  `authenticatable_id` int(11) NOT NULL,
  `salt` varchar(30) NOT NULL,
  `email` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `publisher_local_id_device_xref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `publisher_local_id_device_xref` (
  `unique_publisher_key` VARCHAR (150),
  `publisher_id` int(11) NOT NULL,
  `local_id` VARCHAR(100) NOT NULL,
  `device_id` int(11) NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`unique_publisher_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;



--
-- Table structure for table `publisher`
--

DROP TABLE IF EXISTS `publisher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `publisher` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `code` varchar(50) NOT NULL,
  `widget_location` varchar(255) NULL,
  `url` varchar(255) NULL,
  `salt` varchar(30) NOT NULL,
  `status` varchar(15) NOT NULL,
  `contact_id` int(11) NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


DROP TABLE IF EXISTS `publisher_registration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `publisher_registration` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `publisher_name` varchar(45) NOT NULL,
  `url` varchar(255) NULL,
  `status` varchar(15) NOT NULL,
  `contact_id` int(11) NULL,
  `application_date` timestamp NOT NULL,
  `approval_date` timestamp NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `publisher_session`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(75) NOT NULL,
  `phone_number` varchar(25) NOT NULL,
  `created_date` timestamp(4) NOT NULL DEFAULT CURRENT_TIMESTAMP(4),
  `modified_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `device_access`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `device_access` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(30) NOT NULL,
  `device_id` int(11) NOT NULL,
  `identity_provider_id` varchar(36) DEFAULT NULL,
  `publisher_id` varchar(36) DEFAULT NULL,
  `created_date` timestamp(4) NOT NULL DEFAULT CURRENT_TIMESTAMP(4),
  `modified_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-05-04 14:13:29

DROP TABLE IF EXISTS `error_log`;

CREATE TABLE `error_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `authenticated_party` VARCHAR(45) NULL,
  `device_global_id` VARCHAR(36) NULL,
  `http_method` VARCHAR(6) NULL,
  `request_url` VARCHAR(200) NULL,
  `headers` VARCHAR(450) NULL,
  `caller_ip` VARCHAR(45) NULL,
  `server_ip` VARCHAR(20) NULL,
  `response_code` VARCHAR(45) NULL,
  `exception_type` VARCHAR(100) NULL,
  `exception_message` VARCHAR(250) NULL,
  `exception_stacktrace` TEXT NULL,
  `error_date` DATETIME NULL,
  `created_date` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
UNIQUE KEY `id_UNIQUE` (`id`)
);

DROP TABLE IF EXISTS `idp_external_id`;

CREATE TABLE `idp_external_id` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `idp_id` int (11) NOT NULL,
  `provider` VARCHAR(45) NOT NULL,
  `external_id` VARCHAR(250) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idp_external_UNIQUE` (`idp_id`, `provider`)
);


INSERT INTO wayf.user (first_name, last_name, email, phone_number, created_date)
	VALUES ('Default', 'Admin', 'test@atypon.com', '+1 (585) 555-5555', CURRENT_TIMESTAMP);

INSERT INTO wayf.email_password_credentials (authenticatable_type, authenticatable_id, salt, email, password, created_date)
	VALUES ('USER', 1, '$2a$10$s0.WBFOZzUJi8MMp8r8yWO', 'test@atypon.com', '$2a$10$s0.WBFOZzUJi8MMp8r8yWOg.IoN84sQWIjFnpns1KBugITDfe2Hw2', CURRENT_TIMESTAMP);

INSERT INTO wayf.authorization_token (authenticatable, authenticatable_type, authenticatable_id, token_type, token_value, created_date)
  VALUES ('USER-1', 'USER', 1, 'API_TOKEN', 'DEFAULT_PLEASE_CHANGE', CURRENT_TIMESTAMP);