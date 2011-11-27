-- MySQL dump 10.13  Distrib 5.1.52, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: webui
-- ------------------------------------------------------
-- Server version	5.1.52

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
-- Table structure for table `flow_group`
--

DROP TABLE IF EXISTS `flow_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flow_group` (
  `group_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `input_switch` char(16) NOT NULL,
  `output_switch` char(16) NOT NULL,
  `comments` varchar(500) DEFAULT NULL,
  `priority` smallint(6) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `maximum_flows` int(10) DEFAULT NULL,
  PRIMARY KEY (`group_id`),
  KEY `input_switch` (`input_switch`),
  KEY `group_id` (`group_id`)
) ENGINE=MyISAM AUTO_INCREMENT=161 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flow_group`
--

LOCK TABLES `flow_group` WRITE;
/*!40000 ALTER TABLE `flow_group` DISABLE KEYS */;
INSERT INTO `flow_group` VALUES (160,'123456abcdef','123456abcdef','ipv4',100,3,1);
/*!40000 ALTER TABLE `flow_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_port`
--

DROP TABLE IF EXISTS `group_port`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_port` (
  `group_id` int(10) unsigned NOT NULL,
  `port_direction` tinyint(4) NOT NULL,
  `port_id` int(11) NOT NULL,
  KEY `port_id` (`port_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_port`
--

LOCK TABLES `group_port` WRITE;
/*!40000 ALTER TABLE `group_port` DISABLE KEYS */;
INSERT INTO `group_port` VALUES (160,1,4),(160,0,3);
/*!40000 ALTER TABLE `group_port` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_type`
--

DROP TABLE IF EXISTS `group_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_type` (
  `group_type_id` tinyint(4) NOT NULL,
  `group_type_desc` varchar(30) NOT NULL,
  PRIMARY KEY (`group_type_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_type`
--

LOCK TABLES `group_type` WRITE;
/*!40000 ALTER TABLE `group_type` DISABLE KEYS */;
INSERT INTO `group_type` VALUES (1,'IP Address'),(2,'TCP Port'),(3,'Ethernet Type'),(4,'ANY');
/*!40000 ALTER TABLE `group_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_values`
--

DROP TABLE IF EXISTS `group_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_values` (
  `group_id` int(11) NOT NULL,
  `value` varchar(30) NOT NULL,
  KEY `group_id` (`group_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_values`
--

LOCK TABLES `group_values` WRITE;
/*!40000 ALTER TABLE `group_values` DISABLE KEYS */;
INSERT INTO `group_values` VALUES (160,'0800');
/*!40000 ALTER TABLE `group_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `switch`
--

DROP TABLE IF EXISTS `switch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `switch` (
  `datapath_id` char(16) NOT NULL DEFAULT '',
  `mac_address` char(12) NOT NULL,
  `ip_address` char(15) NOT NULL,
  `switch_name` varchar(50) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`datapath_id`),
  UNIQUE KEY `switch_name` (`switch_name`),
  UNIQUE KEY `ip_address` (`ip_address`),
  UNIQUE KEY `mac_address` (`mac_address`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `switch`
--

LOCK TABLES `switch` WRITE;
/*!40000 ALTER TABLE `switch` DISABLE KEYS */;
INSERT INTO `switch` VALUES ('123456abcdef','E8:9A:8F:3C:','156.56.5.43','pronto3780',' ');
/*!40000 ALTER TABLE `switch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `switch_port`
--

DROP TABLE IF EXISTS `switch_port`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `switch_port` (
  `port_id` int(10) unsigned NOT NULL,
  `port_address` char(17) NOT NULL,
  `switch_id` char(16) NOT NULL,
  PRIMARY KEY (`port_id`,`switch_id`),
  KEY `switch_id` (`switch_id`),
  KEY `port_address` (`port_address`,`switch_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `switch_port`
--

LOCK TABLES `switch_port` WRITE;
/*!40000 ALTER TABLE `switch_port` DISABLE KEYS */;
INSERT INTO `switch_port` VALUES (1,'00:AA:DD','123456abcdef'),(2,'00:AA:DD','123456abcdef'),(3,'00:AA:DD','123456abcdef');
/*!40000 ALTER TABLE `switch_port` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `x_connect`
--

DROP TABLE IF EXISTS `x_connect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `x_connect` (
  `switch_1_id` char(16) NOT NULL,
  `switch_2_id` char(16) NOT NULL,
  `port_1_id` int(10) unsigned NOT NULL,
  `port_2_id` int(10) unsigned NOT NULL,
  KEY `switch_1_id` (`switch_1_id`),
  KEY `switch_2_id` (`switch_2_id`),
  KEY `port_1_id` (`port_1_id`),
  KEY `port_2_id` (`port_2_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `x_connect`
--

LOCK TABLES `x_connect` WRITE;
/*!40000 ALTER TABLE `x_connect` DISABLE KEYS */;
/*!40000 ALTER TABLE `x_connect` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-11-09 21:24:30
