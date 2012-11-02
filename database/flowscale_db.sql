-- MySQL dump 10.13  Distrib 5.5.24, for debian-linux-gnu (i686)
--
-- Host: localhost    Database: flowscale_db
-- ------------------------------------------------------
-- Server version	5.5.24-0ubuntu0.12.04.1

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
  `network_protocol` int(11) DEFAULT NULL,
  `transport_direction` int(11) DEFAULT NULL,
  PRIMARY KEY (`group_id`),
  KEY `input_switch` (`input_switch`),
  KEY `group_id` (`group_id`)
) ENGINE=MyISAM AUTO_INCREMENT=468 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flow_group`
--

LOCK TABLES `flow_group` WRITE;
/*!40000 ALTER TABLE `flow_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `flow_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flow_stats`
--

DROP TABLE IF EXISTS `flow_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flow_stats` (
  `datapath_id` bigint(30) NOT NULL DEFAULT '0',
  `timestamp` bigint(20) NOT NULL DEFAULT '0',
  `match_string` varchar(100) NOT NULL DEFAULT '',
  `action` varchar(30) DEFAULT NULL,
  `packet_count` int(11) DEFAULT NULL,
  `priority` int(5) DEFAULT NULL,
  PRIMARY KEY (`datapath_id`,`timestamp`,`match_string`),
  KEY `timestamp` (`timestamp`) USING HASH
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flow_stats`
--

LOCK TABLES `flow_stats` WRITE;
/*!40000 ALTER TABLE `flow_stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `flow_stats` ENABLE KEYS */;
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
INSERT INTO `group_type` VALUES (1,'IP Address'),(2,'Transport Layer'),(3,'Ethernet Type');
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
/*!40000 ALTER TABLE `group_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `port_mirrors`
--

DROP TABLE IF EXISTS `port_mirrors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `port_mirrors` (
  `datapath_id` varchar(30) NOT NULL,
  `port` int(10) DEFAULT NULL,
  `mirror_port` int(10) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `port_mirrors`
--

LOCK TABLES `port_mirrors` WRITE;
/*!40000 ALTER TABLE `port_mirrors` DISABLE KEYS */;
/*!40000 ALTER TABLE `port_mirrors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `port_stats`
--

DROP TABLE IF EXISTS `port_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `port_stats` (
  `datapath_id` bigint(30) NOT NULL DEFAULT '0',
  `timestamp` bigint(20) NOT NULL DEFAULT '0',
  `port` int(4) NOT NULL DEFAULT '0',
  `packets_received` bigint(20) DEFAULT NULL,
  `packets_transmitted` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`datapath_id`,`timestamp`,`port`),
  KEY `timestamp_index` (`timestamp`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `port_stats`
--

LOCK TABLES `port_stats` WRITE;
/*!40000 ALTER TABLE `port_stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `port_stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `port_status`
--

DROP TABLE IF EXISTS `port_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `port_status` (
  `datapath_id` varchar(30) DEFAULT NULL,
  `timestamp` bigint(20) NOT NULL,
  `pord_id` int(4) NOT NULL,
  `port_address` varchar(20) NOT NULL,
  `port_status` int(1) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `port_status`
--

LOCK TABLES `port_status` WRITE;
/*!40000 ALTER TABLE `port_status` DISABLE KEYS */;
INSERT INTO `port_status` VALUES ('1',1351833791439,7,'a6:c9:61:51:50:f0',0),('1',1351833791439,4,'c2:ad:c1:da:ff:d9',0),('1',1351833791439,1,'2a:58:e1:e5:49:ed',0),('1',1351833791439,3,'0e:23:73:cb:0a:e4',0),('1',1351833791439,10,'2a:fd:0b:15:b9:52',0),('1',1351833791439,8,'96:68:b4:f3:a4:15',0),('1',1351833791439,2,'c6:2d:0a:4b:18:63',0),('1',1351833791439,14,'b2:db:9a:c7:d3:65',0),('1',1351833791439,5,'f6:27:e6:54:6a:2b',0),('1',1351833791439,11,'3e:5b:1c:4c:da:c5',0),('1',1351833791439,16,'f2:a6:1f:b3:9b:09',0),('1',1351833791439,15,'a2:6e:b1:e2:77:2f',0),('1',1351833791439,12,'22:bc:4c:23:9f:ee',0),('1',1351833791439,13,'96:53:54:ec:d5:53',0),('1',1351833791439,6,'e6:a2:d5:42:e2:f5',0),('1',1351833791439,9,'32:d7:97:07:57:ea',0),('1',1351833821454,7,'a6:c9:61:51:50:f0',0),('1',1351833821454,4,'c2:ad:c1:da:ff:d9',0),('1',1351833821454,1,'2a:58:e1:e5:49:ed',0),('1',1351833821454,3,'0e:23:73:cb:0a:e4',0),('1',1351833821454,10,'2a:fd:0b:15:b9:52',0),('1',1351833821454,8,'96:68:b4:f3:a4:15',0),('1',1351833821454,2,'c6:2d:0a:4b:18:63',0),('1',1351833821454,14,'b2:db:9a:c7:d3:65',0),('1',1351833821454,5,'f6:27:e6:54:6a:2b',0),('1',1351833821454,11,'3e:5b:1c:4c:da:c5',0),('1',1351833821454,16,'f2:a6:1f:b3:9b:09',0),('1',1351833821454,15,'a2:6e:b1:e2:77:2f',0),('1',1351833821454,12,'22:bc:4c:23:9f:ee',0),('1',1351833821454,13,'96:53:54:ec:d5:53',0),('1',1351833821454,6,'e6:a2:d5:42:e2:f5',0),('1',1351833821454,9,'32:d7:97:07:57:ea',0),('1',1351833851466,7,'a6:c9:61:51:50:f0',0),('1',1351833851466,4,'c2:ad:c1:da:ff:d9',0),('1',1351833851466,1,'2a:58:e1:e5:49:ed',0),('1',1351833851466,3,'0e:23:73:cb:0a:e4',0),('1',1351833851466,10,'2a:fd:0b:15:b9:52',0),('1',1351833851466,8,'96:68:b4:f3:a4:15',0),('1',1351833851466,2,'c6:2d:0a:4b:18:63',0),('1',1351833851466,14,'b2:db:9a:c7:d3:65',0),('1',1351833851466,5,'f6:27:e6:54:6a:2b',0),('1',1351833851466,11,'3e:5b:1c:4c:da:c5',0),('1',1351833851466,16,'f2:a6:1f:b3:9b:09',0),('1',1351833851466,15,'a2:6e:b1:e2:77:2f',0),('1',1351833851466,12,'22:bc:4c:23:9f:ee',0),('1',1351833851466,13,'96:53:54:ec:d5:53',0),('1',1351833851466,6,'e6:a2:d5:42:e2:f5',0),('1',1351833851466,9,'32:d7:97:07:57:ea',0),('1',1351833881475,7,'a6:c9:61:51:50:f0',0),('1',1351833881475,4,'c2:ad:c1:da:ff:d9',0),('1',1351833881475,1,'2a:58:e1:e5:49:ed',0),('1',1351833881475,3,'0e:23:73:cb:0a:e4',0),('1',1351833881475,10,'2a:fd:0b:15:b9:52',0),('1',1351833881475,8,'96:68:b4:f3:a4:15',0),('1',1351833881475,2,'c6:2d:0a:4b:18:63',0),('1',1351833881475,14,'b2:db:9a:c7:d3:65',0),('1',1351833881475,5,'f6:27:e6:54:6a:2b',0),('1',1351833881475,11,'3e:5b:1c:4c:da:c5',0),('1',1351833881475,16,'f2:a6:1f:b3:9b:09',0),('1',1351833881475,15,'a2:6e:b1:e2:77:2f',0),('1',1351833881475,12,'22:bc:4c:23:9f:ee',0),('1',1351833881475,13,'96:53:54:ec:d5:53',0),('1',1351833881475,6,'e6:a2:d5:42:e2:f5',0),('1',1351833881475,9,'32:d7:97:07:57:ea',0),('1',1351833911487,7,'a6:c9:61:51:50:f0',0),('1',1351833911487,4,'c2:ad:c1:da:ff:d9',0),('1',1351833911487,1,'2a:58:e1:e5:49:ed',0),('1',1351833911487,3,'0e:23:73:cb:0a:e4',0),('1',1351833911487,10,'2a:fd:0b:15:b9:52',0),('1',1351833911487,8,'96:68:b4:f3:a4:15',0),('1',1351833911487,2,'c6:2d:0a:4b:18:63',0),('1',1351833911487,14,'b2:db:9a:c7:d3:65',0),('1',1351833911487,5,'f6:27:e6:54:6a:2b',0),('1',1351833911487,11,'3e:5b:1c:4c:da:c5',0),('1',1351833911487,16,'f2:a6:1f:b3:9b:09',0),('1',1351833911487,15,'a2:6e:b1:e2:77:2f',0),('1',1351833911487,12,'22:bc:4c:23:9f:ee',0),('1',1351833911487,13,'96:53:54:ec:d5:53',0),('1',1351833911487,6,'e6:a2:d5:42:e2:f5',0),('1',1351833911487,9,'32:d7:97:07:57:ea',0),('1',1351833941501,7,'a6:c9:61:51:50:f0',0),('1',1351833941501,4,'c2:ad:c1:da:ff:d9',0),('1',1351833941501,1,'2a:58:e1:e5:49:ed',0),('1',1351833941501,3,'0e:23:73:cb:0a:e4',0),('1',1351833941501,10,'2a:fd:0b:15:b9:52',0),('1',1351833941501,8,'96:68:b4:f3:a4:15',0),('1',1351833941501,2,'c6:2d:0a:4b:18:63',0),('1',1351833941501,14,'b2:db:9a:c7:d3:65',0),('1',1351833941501,5,'f6:27:e6:54:6a:2b',0),('1',1351833941501,11,'3e:5b:1c:4c:da:c5',0),('1',1351833941501,16,'f2:a6:1f:b3:9b:09',0),('1',1351833941501,15,'a2:6e:b1:e2:77:2f',0),('1',1351833941501,12,'22:bc:4c:23:9f:ee',0),('1',1351833941501,13,'96:53:54:ec:d5:53',0),('1',1351833941501,6,'e6:a2:d5:42:e2:f5',0),('1',1351833941501,9,'32:d7:97:07:57:ea',0),('1',1351833971513,7,'a6:c9:61:51:50:f0',0),('1',1351833971513,4,'c2:ad:c1:da:ff:d9',0),('1',1351833971513,1,'2a:58:e1:e5:49:ed',0),('1',1351833971513,3,'0e:23:73:cb:0a:e4',0),('1',1351833971513,10,'2a:fd:0b:15:b9:52',0),('1',1351833971513,8,'96:68:b4:f3:a4:15',0),('1',1351833971513,2,'c6:2d:0a:4b:18:63',0),('1',1351833971513,14,'b2:db:9a:c7:d3:65',0),('1',1351833971513,5,'f6:27:e6:54:6a:2b',0),('1',1351833971513,11,'3e:5b:1c:4c:da:c5',0),('1',1351833971513,16,'f2:a6:1f:b3:9b:09',0),('1',1351833971513,15,'a2:6e:b1:e2:77:2f',0),('1',1351833971513,12,'22:bc:4c:23:9f:ee',0),('1',1351833971513,13,'96:53:54:ec:d5:53',0),('1',1351833971513,6,'e6:a2:d5:42:e2:f5',0),('1',1351833971513,9,'32:d7:97:07:57:ea',0),('1',1351834001525,7,'a6:c9:61:51:50:f0',0),('1',1351834001525,4,'c2:ad:c1:da:ff:d9',0),('1',1351834001525,1,'2a:58:e1:e5:49:ed',0),('1',1351834001525,3,'0e:23:73:cb:0a:e4',0),('1',1351834001525,10,'2a:fd:0b:15:b9:52',0),('1',1351834001525,8,'96:68:b4:f3:a4:15',0),('1',1351834001525,2,'c6:2d:0a:4b:18:63',0),('1',1351834001525,14,'b2:db:9a:c7:d3:65',0),('1',1351834001525,5,'f6:27:e6:54:6a:2b',0),('1',1351834001525,11,'3e:5b:1c:4c:da:c5',0),('1',1351834001525,16,'f2:a6:1f:b3:9b:09',0),('1',1351834001525,15,'a2:6e:b1:e2:77:2f',0),('1',1351834001525,12,'22:bc:4c:23:9f:ee',0),('1',1351834001525,13,'96:53:54:ec:d5:53',0),('1',1351834001525,6,'e6:a2:d5:42:e2:f5',0),('1',1351834001525,9,'32:d7:97:07:57:ea',0),('1',1351834031538,7,'a6:c9:61:51:50:f0',0),('1',1351834031538,4,'c2:ad:c1:da:ff:d9',0),('1',1351834031538,1,'2a:58:e1:e5:49:ed',0),('1',1351834031538,3,'0e:23:73:cb:0a:e4',0),('1',1351834031538,10,'2a:fd:0b:15:b9:52',0),('1',1351834031538,8,'96:68:b4:f3:a4:15',0),('1',1351834031538,2,'c6:2d:0a:4b:18:63',0),('1',1351834031538,14,'b2:db:9a:c7:d3:65',0),('1',1351834031538,5,'f6:27:e6:54:6a:2b',0),('1',1351834031538,11,'3e:5b:1c:4c:da:c5',0),('1',1351834031538,16,'f2:a6:1f:b3:9b:09',0),('1',1351834031538,15,'a2:6e:b1:e2:77:2f',0),('1',1351834031538,12,'22:bc:4c:23:9f:ee',0),('1',1351834031538,13,'96:53:54:ec:d5:53',0),('1',1351834031538,6,'e6:a2:d5:42:e2:f5',0),('1',1351834031538,9,'32:d7:97:07:57:ea',0),('1',1351834061551,7,'a6:c9:61:51:50:f0',0),('1',1351834061551,4,'c2:ad:c1:da:ff:d9',0),('1',1351834061551,1,'2a:58:e1:e5:49:ed',0),('1',1351834061551,3,'0e:23:73:cb:0a:e4',0),('1',1351834061551,10,'2a:fd:0b:15:b9:52',0),('1',1351834061551,8,'96:68:b4:f3:a4:15',0),('1',1351834061551,2,'c6:2d:0a:4b:18:63',0),('1',1351834061551,14,'b2:db:9a:c7:d3:65',0),('1',1351834061551,5,'f6:27:e6:54:6a:2b',0),('1',1351834061551,11,'3e:5b:1c:4c:da:c5',0),('1',1351834061551,16,'f2:a6:1f:b3:9b:09',0),('1',1351834061551,15,'a2:6e:b1:e2:77:2f',0),('1',1351834061551,12,'22:bc:4c:23:9f:ee',0),('1',1351834061551,13,'96:53:54:ec:d5:53',0),('1',1351834061551,6,'e6:a2:d5:42:e2:f5',0),('1',1351834061551,9,'32:d7:97:07:57:ea',0),('1',1351834091563,7,'a6:c9:61:51:50:f0',0),('1',1351834091563,4,'c2:ad:c1:da:ff:d9',0),('1',1351834091563,1,'2a:58:e1:e5:49:ed',0),('1',1351834091563,3,'0e:23:73:cb:0a:e4',0),('1',1351834091563,10,'2a:fd:0b:15:b9:52',0),('1',1351834091563,8,'96:68:b4:f3:a4:15',0),('1',1351834091563,2,'c6:2d:0a:4b:18:63',0),('1',1351834091563,14,'b2:db:9a:c7:d3:65',0),('1',1351834091563,5,'f6:27:e6:54:6a:2b',0),('1',1351834091563,11,'3e:5b:1c:4c:da:c5',0),('1',1351834091563,16,'f2:a6:1f:b3:9b:09',0),('1',1351834091563,15,'a2:6e:b1:e2:77:2f',0),('1',1351834091563,12,'22:bc:4c:23:9f:ee',0),('1',1351834091563,13,'96:53:54:ec:d5:53',0),('1',1351834091563,6,'e6:a2:d5:42:e2:f5',0),('1',1351834091563,9,'32:d7:97:07:57:ea',0),('1',1351834121576,7,'a6:c9:61:51:50:f0',0),('1',1351834121576,4,'c2:ad:c1:da:ff:d9',0),('1',1351834121576,1,'2a:58:e1:e5:49:ed',0),('1',1351834121576,3,'0e:23:73:cb:0a:e4',0),('1',1351834121576,10,'2a:fd:0b:15:b9:52',0),('1',1351834121576,8,'96:68:b4:f3:a4:15',0),('1',1351834121576,2,'c6:2d:0a:4b:18:63',0),('1',1351834121576,14,'b2:db:9a:c7:d3:65',0),('1',1351834121576,5,'f6:27:e6:54:6a:2b',0),('1',1351834121576,11,'3e:5b:1c:4c:da:c5',0),('1',1351834121576,16,'f2:a6:1f:b3:9b:09',0),('1',1351834121576,15,'a2:6e:b1:e2:77:2f',0),('1',1351834121576,12,'22:bc:4c:23:9f:ee',0),('1',1351834121576,13,'96:53:54:ec:d5:53',0),('1',1351834121576,6,'e6:a2:d5:42:e2:f5',0),('1',1351834121576,9,'32:d7:97:07:57:ea',0),('1',1351834151588,7,'a6:c9:61:51:50:f0',0),('1',1351834151588,4,'c2:ad:c1:da:ff:d9',0),('1',1351834151588,1,'2a:58:e1:e5:49:ed',0),('1',1351834151588,3,'0e:23:73:cb:0a:e4',0),('1',1351834151588,10,'2a:fd:0b:15:b9:52',0),('1',1351834151588,8,'96:68:b4:f3:a4:15',0),('1',1351834151588,2,'c6:2d:0a:4b:18:63',0),('1',1351834151588,14,'b2:db:9a:c7:d3:65',0),('1',1351834151588,5,'f6:27:e6:54:6a:2b',0),('1',1351834151588,11,'3e:5b:1c:4c:da:c5',0),('1',1351834151588,16,'f2:a6:1f:b3:9b:09',0),('1',1351834151588,15,'a2:6e:b1:e2:77:2f',0),('1',1351834151588,12,'22:bc:4c:23:9f:ee',0),('1',1351834151588,13,'96:53:54:ec:d5:53',0),('1',1351834151588,6,'e6:a2:d5:42:e2:f5',0),('1',1351834151588,9,'32:d7:97:07:57:ea',0),('1',1351834181600,7,'a6:c9:61:51:50:f0',0),('1',1351834181600,4,'c2:ad:c1:da:ff:d9',0),('1',1351834181600,1,'2a:58:e1:e5:49:ed',0),('1',1351834181600,3,'0e:23:73:cb:0a:e4',0),('1',1351834181600,10,'2a:fd:0b:15:b9:52',0),('1',1351834181600,8,'96:68:b4:f3:a4:15',0),('1',1351834181600,2,'c6:2d:0a:4b:18:63',0),('1',1351834181600,14,'b2:db:9a:c7:d3:65',0),('1',1351834181600,5,'f6:27:e6:54:6a:2b',0),('1',1351834181600,11,'3e:5b:1c:4c:da:c5',0),('1',1351834181600,16,'f2:a6:1f:b3:9b:09',0),('1',1351834181600,15,'a2:6e:b1:e2:77:2f',0),('1',1351834181600,12,'22:bc:4c:23:9f:ee',0),('1',1351834181600,13,'96:53:54:ec:d5:53',0),('1',1351834181600,6,'e6:a2:d5:42:e2:f5',0),('1',1351834181600,9,'32:d7:97:07:57:ea',0),('1',1351834211612,7,'a6:c9:61:51:50:f0',0),('1',1351834211612,4,'c2:ad:c1:da:ff:d9',0),('1',1351834211612,1,'2a:58:e1:e5:49:ed',0),('1',1351834211612,3,'0e:23:73:cb:0a:e4',0),('1',1351834211612,10,'2a:fd:0b:15:b9:52',0),('1',1351834211612,8,'96:68:b4:f3:a4:15',0),('1',1351834211612,2,'c6:2d:0a:4b:18:63',0),('1',1351834211612,14,'b2:db:9a:c7:d3:65',0),('1',1351834211612,5,'f6:27:e6:54:6a:2b',0),('1',1351834211612,11,'3e:5b:1c:4c:da:c5',0),('1',1351834211612,16,'f2:a6:1f:b3:9b:09',0),('1',1351834211612,15,'a2:6e:b1:e2:77:2f',0),('1',1351834211612,12,'22:bc:4c:23:9f:ee',0),('1',1351834211612,13,'96:53:54:ec:d5:53',0),('1',1351834211612,6,'e6:a2:d5:42:e2:f5',0),('1',1351834211612,9,'32:d7:97:07:57:ea',0),('1',1351834241631,7,'a6:c9:61:51:50:f0',0),('1',1351834241631,4,'c2:ad:c1:da:ff:d9',0),('1',1351834241631,1,'2a:58:e1:e5:49:ed',0),('1',1351834241631,3,'0e:23:73:cb:0a:e4',0),('1',1351834241631,10,'2a:fd:0b:15:b9:52',0),('1',1351834241631,8,'96:68:b4:f3:a4:15',0),('1',1351834241631,2,'c6:2d:0a:4b:18:63',0),('1',1351834241631,14,'b2:db:9a:c7:d3:65',0),('1',1351834241631,5,'f6:27:e6:54:6a:2b',0),('1',1351834241631,11,'3e:5b:1c:4c:da:c5',0),('1',1351834241631,16,'f2:a6:1f:b3:9b:09',0),('1',1351834241631,15,'a2:6e:b1:e2:77:2f',0),('1',1351834241631,12,'22:bc:4c:23:9f:ee',0),('1',1351834241631,13,'96:53:54:ec:d5:53',0),('1',1351834241631,6,'e6:a2:d5:42:e2:f5',0),('1',1351834241631,9,'32:d7:97:07:57:ea',0),('1',1351834271644,7,'a6:c9:61:51:50:f0',0),('1',1351834271644,4,'c2:ad:c1:da:ff:d9',0),('1',1351834271644,1,'2a:58:e1:e5:49:ed',0),('1',1351834271644,3,'0e:23:73:cb:0a:e4',0),('1',1351834271644,10,'2a:fd:0b:15:b9:52',0),('1',1351834271644,8,'96:68:b4:f3:a4:15',0),('1',1351834271644,2,'c6:2d:0a:4b:18:63',0),('1',1351834271644,14,'b2:db:9a:c7:d3:65',0),('1',1351834271644,5,'f6:27:e6:54:6a:2b',0),('1',1351834271644,11,'3e:5b:1c:4c:da:c5',0),('1',1351834271644,16,'f2:a6:1f:b3:9b:09',0),('1',1351834271644,15,'a2:6e:b1:e2:77:2f',0),('1',1351834271644,12,'22:bc:4c:23:9f:ee',0),('1',1351834271644,13,'96:53:54:ec:d5:53',0),('1',1351834271644,6,'e6:a2:d5:42:e2:f5',0),('1',1351834271644,9,'32:d7:97:07:57:ea',0),('1',1351834301655,7,'a6:c9:61:51:50:f0',0),('1',1351834301655,4,'c2:ad:c1:da:ff:d9',0),('1',1351834301655,1,'2a:58:e1:e5:49:ed',0),('1',1351834301655,3,'0e:23:73:cb:0a:e4',0),('1',1351834301655,10,'2a:fd:0b:15:b9:52',0),('1',1351834301655,8,'96:68:b4:f3:a4:15',0),('1',1351834301655,2,'c6:2d:0a:4b:18:63',0),('1',1351834301655,14,'b2:db:9a:c7:d3:65',0),('1',1351834301655,5,'f6:27:e6:54:6a:2b',0),('1',1351834301655,11,'3e:5b:1c:4c:da:c5',0),('1',1351834301655,16,'f2:a6:1f:b3:9b:09',0),('1',1351834301655,15,'a2:6e:b1:e2:77:2f',0),('1',1351834301655,12,'22:bc:4c:23:9f:ee',0),('1',1351834301655,13,'96:53:54:ec:d5:53',0),('1',1351834301655,6,'e6:a2:d5:42:e2:f5',0),('1',1351834301655,9,'32:d7:97:07:57:ea',0),('1',1351834331667,7,'a6:c9:61:51:50:f0',0),('1',1351834331667,4,'c2:ad:c1:da:ff:d9',0),('1',1351834331667,1,'2a:58:e1:e5:49:ed',0),('1',1351834331667,3,'0e:23:73:cb:0a:e4',0),('1',1351834331667,10,'2a:fd:0b:15:b9:52',0),('1',1351834331667,8,'96:68:b4:f3:a4:15',0),('1',1351834331667,2,'c6:2d:0a:4b:18:63',0),('1',1351834331667,14,'b2:db:9a:c7:d3:65',0),('1',1351834331667,5,'f6:27:e6:54:6a:2b',0),('1',1351834331667,11,'3e:5b:1c:4c:da:c5',0),('1',1351834331667,16,'f2:a6:1f:b3:9b:09',0),('1',1351834331667,15,'a2:6e:b1:e2:77:2f',0),('1',1351834331667,12,'22:bc:4c:23:9f:ee',0),('1',1351834331667,13,'96:53:54:ec:d5:53',0),('1',1351834331667,6,'e6:a2:d5:42:e2:f5',0),('1',1351834331667,9,'32:d7:97:07:57:ea',0),('1',1351834361679,7,'a6:c9:61:51:50:f0',0),('1',1351834361679,4,'c2:ad:c1:da:ff:d9',0),('1',1351834361679,1,'2a:58:e1:e5:49:ed',0),('1',1351834361679,3,'0e:23:73:cb:0a:e4',0),('1',1351834361679,10,'2a:fd:0b:15:b9:52',0),('1',1351834361679,8,'96:68:b4:f3:a4:15',0),('1',1351834361679,2,'c6:2d:0a:4b:18:63',0),('1',1351834361679,14,'b2:db:9a:c7:d3:65',0),('1',1351834361679,5,'f6:27:e6:54:6a:2b',0),('1',1351834361679,11,'3e:5b:1c:4c:da:c5',0),('1',1351834361679,16,'f2:a6:1f:b3:9b:09',0),('1',1351834361679,15,'a2:6e:b1:e2:77:2f',0),('1',1351834361679,12,'22:bc:4c:23:9f:ee',0),('1',1351834361679,13,'96:53:54:ec:d5:53',0),('1',1351834361679,6,'e6:a2:d5:42:e2:f5',0),('1',1351834361679,9,'32:d7:97:07:57:ea',0);
/*!40000 ALTER TABLE `port_status` ENABLE KEYS */;
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

-- Dump completed on 2012-11-02  8:36:37
