-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: anxi
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `anxi`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `anxi` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `anxi`;

--
-- Table structure for table `assessments`
--

DROP TABLE IF EXISTS `assessments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assessments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `doctor_id` int NOT NULL,
  `anxiety_score` float NOT NULL,
  `anxiety_level` varchar(50) NOT NULL,
  `dominant_emotion` varchar(50) DEFAULT NULL,
  `notes` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `patient_id` (`patient_id`),
  KEY `doctor_id` (`doctor_id`),
  CONSTRAINT `assessments_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE,
  CONSTRAINT `assessments_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assessments`
--

LOCK TABLES `assessments` WRITE;
/*!40000 ALTER TABLE `assessments` DISABLE KEYS */;
INSERT INTO `assessments` VALUES (1,3,1,75.5,'High','fear',NULL,'2026-02-13 07:35:59'),(2,1,1,75.5,'High','fear',NULL,'2026-02-13 09:03:39'),(3,19,20,0,'Low Anxiety',NULL,NULL,'2026-02-13 09:25:11'),(4,19,20,0,'Low Anxiety',NULL,NULL,'2026-02-13 09:25:56'),(5,21,20,65,'High Anxiety',NULL,NULL,'2026-02-13 11:01:07'),(6,24,20,65,'High Anxiety',NULL,NULL,'2026-02-13 11:05:14'),(7,26,20,50,'Moderate Anxiety',NULL,NULL,'2026-02-14 03:55:15'),(8,31,1,70,'High Anxiety',NULL,NULL,'2026-02-14 04:58:44'),(9,32,1,29,'Low',NULL,NULL,'2026-02-14 05:14:39'),(10,33,1,0,'Low',NULL,NULL,'2026-02-14 05:20:02'),(11,34,20,33,'Low Anxiety',NULL,NULL,'2026-02-14 07:50:12'),(12,35,20,33,'Low Anxiety',NULL,NULL,'2026-02-14 08:20:54'),(13,37,20,49,'Moderate Anxiety',NULL,NULL,'2026-02-14 09:18:39'),(14,40,1,0,'Low Anxiety',NULL,NULL,'2026-02-17 03:47:22'),(15,63,1,3,'Low Anxiety',NULL,NULL,'2026-02-17 04:31:12'),(16,64,20,47,'Moderate Anxiety',NULL,NULL,'2026-02-18 07:22:44'),(17,65,20,5,'Low Anxiety',NULL,NULL,'2026-02-18 07:36:35'),(18,65,20,5,'Low Anxiety',NULL,NULL,'2026-02-18 07:36:58'),(19,66,20,2,'Low Anxiety',NULL,NULL,'2026-02-18 08:21:35'),(20,67,20,4,'Low Anxiety',NULL,NULL,'2026-02-18 08:31:03'),(21,67,20,4,'Low Anxiety',NULL,NULL,'2026-02-18 08:31:28'),(22,68,20,4,'Low Anxiety','neutral',NULL,'2026-02-18 08:43:28'),(23,69,20,4,'Low Anxiety','neutral',NULL,'2026-02-18 09:16:17'),(24,71,1,49,'Moderate Anxiety','fear',NULL,'2026-02-23 03:18:49'),(25,72,1,4,'Low Anxiety','neutral',NULL,'2026-02-23 04:41:57'),(26,72,1,75.5,'High','fear',NULL,'2026-02-24 02:57:37'),(27,74,20,49,'Moderate Anxiety','fear',NULL,'2026-02-24 04:01:03'),(28,76,20,49,'Moderate Anxiety','Fear',NULL,'2026-02-24 04:34:32'),(29,77,20,49,'Moderate Anxiety','Fear',NULL,'2026-02-24 04:47:12'),(30,78,20,49,'Moderate Anxiety','Fear',NULL,'2026-02-24 06:43:59'),(31,80,20,11.67,'Low','neutral',NULL,'2026-02-24 07:14:21'),(32,81,20,6.68,'Low','neutral',NULL,'2026-02-24 07:16:31'),(33,82,20,39.16,'Moderate','fear',NULL,'2026-02-24 07:31:03'),(34,82,20,9.04,'Low','neutral',NULL,'2026-02-24 07:31:35'),(35,83,20,0.56,'Low','neutral',NULL,'2026-02-24 07:42:26'),(36,84,20,23.73,'Low','angry',NULL,'2026-02-24 08:07:11'),(37,85,20,49.91,'Moderate','fear',NULL,'2026-02-24 11:11:03'),(38,86,20,5.74,'Low','neutral',NULL,'2026-02-24 13:08:39'),(39,87,20,29,'Low Anxiety','sad',NULL,'2026-02-26 06:45:02'),(40,88,20,10,'Low Anxiety','angry',NULL,'2026-02-26 08:29:54'),(41,89,20,46,'Moderate Anxiety','fear',NULL,'2026-02-27 03:40:36'),(42,94,20,29,'Low Anxiety','sad',NULL,'2026-02-27 09:06:15'),(43,95,20,10.44,'Low Anxiety','neutral',NULL,'2026-02-28 04:58:15'),(44,96,20,2,'Low Anxiety','neutral',NULL,'2026-02-28 04:59:18'),(45,98,20,2.86,'Low','neutral',NULL,'2026-03-13 07:43:55'),(46,99,20,9.46,'Low Anxiety','neutral',NULL,'2026-03-13 08:03:09'),(47,100,20,29,'Low Anxiety','sad',NULL,'2026-03-13 08:06:50'),(48,101,20,3,'Low Anxiety','neutral',NULL,'2026-03-13 08:11:16'),(49,103,20,0.18,'Low Anxiety','happy',NULL,'2026-03-16 10:14:55'),(50,105,20,40.35,'Moderate Anxiety','neutral',NULL,'2026-03-17 04:11:09'),(51,106,20,28,'Low Anxiety','neutral',NULL,'2026-03-17 04:47:56'),(52,107,20,27,'Low Anxiety','neutral',NULL,'2026-03-17 07:05:32'),(53,108,1,54.5,'Moderate Anxiety','fear',NULL,'2026-03-17 07:15:33'),(54,109,20,62.82,'Moderate Anxiety','sad',NULL,'2026-03-17 07:21:51'),(55,112,20,0.52,'Low','neutral',NULL,'2026-03-17 08:09:07');
/*!40000 ALTER TABLE `assessments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doctors`
--

DROP TABLE IF EXISTS `doctors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctors` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `email` varchar(120) NOT NULL,
  `otp` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fullname` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `specialization` varchar(255) DEFAULT NULL,
  `clinic_name` varchar(255) DEFAULT NULL,
  `profile_photo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctors`
--

LOCK TABLES `doctors` WRITE;
/*!40000 ALTER TABLE `doctors` DISABLE KEYS */;
INSERT INTO `doctors` VALUES (1,'Dr. Test','akashranga27@gmail.com',NULL,'2026-02-11 09:16:53','akash','9874563210','radio','saveetha',NULL),(20,'suman','sumanraj71718@gmail.com',NULL,'2026-02-12 09:12:49','dhoni','9344967210','dental','St. Jude Medical Center - Dental Wing','profile_20_1773720733.jpg');
/*!40000 ALTER TABLE `doctors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patients`
--

DROP TABLE IF EXISTS `patients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patients` (
  `id` int NOT NULL AUTO_INCREMENT,
  `patientid` varchar(50) DEFAULT NULL,
  `doctorid` int NOT NULL,
  `fullname` varchar(255) DEFAULT NULL,
  `age` varchar(3) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `proceduretype` varchar(255) DEFAULT NULL,
  `healthissue` varchar(255) DEFAULT NULL,
  `previousanxietyhistory` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patients`
--

LOCK TABLES `patients` WRITE;
/*!40000 ALTER TABLE `patients` DISABLE KEYS */;
INSERT INTO `patients` VALUES (1,'1',1,'aakash raj','21','male','dental','no','no'),(2,'420',1,'Aakash Raj','21','Male','Dental','No','No'),(3,'30',1,'MEENAKSHI.K','25','Female','Dental Surgery','no','no'),(4,'3',20,'mahi','44','Male','General Checkup','no','no'),(5,'3012',20,'suman','21','Male','MRI Scan','no','no'),(6,'3012',20,'suman','21','Male','MRI Scan','no','no'),(7,'3012',20,'suman','21','Male','MRI Scan','no','no'),(8,'30',20,'MEENAKSHI.K','25','Female','General Checkup','no',''),(9,'7',20,'MEENAKSHI.K','25','Female','General Checkup','no',''),(10,'7',20,'MEENAKSHI.K','25','Female','General Checkup','no',''),(11,'7',20,'MEENAKSHI.K','25','Female','General Checkup','no',''),(12,'3012',20,'vyshu','19','Female','General Checkup','',''),(13,'30',20,'vyshu','19','Female','General Checkup','no',''),(14,'30',20,'vyshu','19','Female','General Checkup','no',''),(15,'54',20,'MEENAKSHI.K','25','Female','General Checkup','',''),(16,'3',20,'suman','21','Male','General Checkup','',''),(17,'333',20,'suman','21','Male','Dental Surgery','no','no'),(18,'12',20,'sure','55','Male','General Checkup','no','no'),(19,'717',20,'Suman Raj','21','Male','Dental Surgery','nono',''),(20,'34',20,'anbu','21','Male','General Checkup','no','no'),(21,'3',20,'sai','14','Male','General Checkup','no','no'),(22,'9',20,'u','65','Female','General Checkup','no','no'),(23,'4',20,'Suman Raj','21','Male','MRI Scan','no','no'),(24,'4',20,'Suman Raj','21','Male','MRI Scan','no','no'),(25,'23',20,'priya','21','Female','General Checkup','no','no'),(26,'23',20,'priya','21','Female','General Checkup','no','no'),(27,'43',20,'MEENAKSHI.K','35','Female','General Checkup','no','no'),(28,'967775',1,'Akash Ranga','26','Male','Dental Surgery','stress ','anxiety '),(29,'967775',1,'Akash Ranga','26','Male','Dental Surgery','stress ','anxiety '),(30,'967775',1,'Akash Ranga','26','Male','Dental Surgery','stress ','anxiety '),(31,'13589',1,'xhfhg','89','Male','General Checkup','fhc','fjcgy'),(32,'7',1,'dhanush','23','Male','MRI Scan','bsbs','gdhd'),(33,'687',1,'surya','22','Male','Vaccination','vdhdh','rhdbd'),(34,'566',20,'66','25','Female','MRI Scan','nono',''),(35,'56',20,'Suman Raj','21','Male','MRI Scan','no','no'),(36,'45',20,'d','52','Male','General Checkup','no','no'),(37,'45',20,'d','52','Male','General Checkup','no','no'),(38,'45',20,'Suman Raj','99','Female','General Checkup','no','no'),(39,'57474735',1,'Akash Ranga','23','Male','MRI Scan','hdjdh','dbdbd'),(40,'57474735',1,'Akash Ranga','23','Male','MRI Scan','hdjdh','dbdbd'),(41,'74847',20,'zayn','29','Male','General Checkup','test','test'),(42,'74847',20,'zayn','29','Male','General Checkup','test','test'),(43,'74847',20,'zayn','29','Male','General Checkup','test','test'),(44,'74847',20,'zayn','29','Male','General Checkup','test','test'),(45,'987',20,'bdjdv','236','Other','Vaccination','bdhd','hshshs'),(46,'987',20,'bdjdv','236','Other','Vaccination','bdhd','hshshs'),(47,'987',20,'bdjdv','236','Other','Vaccination','bdhd','hshshs'),(48,'938747',1,'aakash','25','Male','General Checkup','hdhd','dhdbd'),(49,'938747',1,'aakash','25','Male','General Checkup','hdhd','dhdbd'),(50,'938747',1,'aakash','25','Male','General Checkup','hdhd','dhdbd'),(51,'76745',1,'jgjf','86','Female','General Checkup','fhc','fug'),(52,'76745',1,'jgjf','86','Female','General Checkup','fhc','fug'),(53,'76745',1,'jgjf','86','Female','General Checkup','fhc','fug'),(54,'76745',1,'jgjf','86','Female','General Checkup','fhc','fug'),(55,'76745',1,'jgjf','86','Female','General Checkup','fhc','fug'),(56,'76745',1,'jgjf','86','Female','General Checkup','fhc','fug'),(57,'76745',1,'jgjf','86','Female','General Checkup','fhc','fug'),(58,'76745',1,'jgjf','86','Female','General Checkup','fhc','fug'),(59,'76745',1,'jgjf','86','Female','General Checkup','fhc','fug'),(60,'76745',1,'jgjf','86','Female','General Checkup','fhc','fug'),(61,'3',1,'Akash Ranga','21','Male','General Checkup','no','no'),(62,'3',1,'Akash Ranga','21','Male','General Checkup','no','no'),(63,'3',1,'Akash Ranga','21','Male','General Checkup','no','no'),(64,'12',20,'MEENAKSHI.K','21','Female','General Checkup','no','no'),(65,'90909',20,'mmm','39','Male','Dental Surgery','healthy food ','unhealthy food '),(66,'5767',20,'aakash','26','Male','MRI Scan','gshzgs','dhdvdjd'),(67,'1451',20,'MEENAKSHI.K','69','Female','General Checkup','no','no'),(68,'1',20,'MEENAKSHI.K','99','Female','General Checkup','no','no'),(69,'100',20,'MEENAKSHI.K','99','Female','General Checkup','no','sugar'),(70,'5',20,'Suman Raj','99','Female','General Checkup','no',''),(71,'34',1,'Akash Ranga','99','Male','General Checkup','no',''),(72,'77',1,'Akash Ranga','99','Female','General Checkup','no',''),(73,'2',20,'hai','22','Male','General Surgery','no',''),(74,'9',20,'hai','22','Male','General Surgery','no',''),(76,'323',20,'jawahar','99','Male','General Surgery','no',''),(77,'21',20,'Jawahar B R','21','Male','Diagnostic Imaging','',''),(78,'323',20,'hai','22','Male','General Surgery','no',''),(79,'323',20,'hai','22','Male','General Surgery','no',''),(80,'323',20,'jawahar','32','Male','General Surgery','',''),(81,'323',20,'jawahar','32','Male','General Surgery','',''),(82,'323',20,'jawahar','32','Male','General Surgery','',''),(83,'245',20,'mjhfgh','48','Female','General Surgery','hgfcuyjh','nbhguhy'),(84,'2',20,'mahi','56','Male','General Surgery','',''),(85,'323',20,'jawahar','7','Male','General Surgery','no',''),(86,'7',20,'sarathi','21','Male','General Surgery','no',''),(87,'4',20,'se','21','Male','Dental Surgery','no',''),(88,'7',20,'meena','25','Male','General Checkup',' \nno',''),(89,'8',20,'hi','69','Male','General Checkup','no',''),(90,'8',20,'sumanraj71718@gmail.com 67','69','Male','General Checkup','',''),(91,'67',20,'Suman Raj','99','Male','General Checkup','',''),(92,'6',20,'s','25','Male','General Checkup','',''),(93,'7',20,'dh','66','Male','Dental Surgery','',''),(94,'6',20,'5','69','Male','MRI Scan','',''),(95,'TEMP-8080',20,'Quick Assessment','36','Male','Dental Surgery','',''),(96,'3',20,'Suman Raj','99','Female','General Checkup','no',''),(97,'9',20,'3ee','33','Male','General Surgery','',''),(98,'323',20,'hai','99','Male','General Surgery','',''),(99,'TEMP-2464',20,'Quick Assessment','22','Male','Dental Surgery','hshssj','jwjsj'),(100,'P01234',20,'Sathwik Tallapaka','22','Male','MRI Scan','allergy','ntg'),(101,'p1334',20,'hdhshs','22','Male','General Checkup','allergy','ntg'),(102,'7377',20,'suman','99','Male','Dental Surgery','no',''),(103,'TEMP-1561',20,'Quick Assessment','66','Female','Dental Surgery','no',''),(104,'88',20,'suman','45','Male','General Checkup','no',''),(105,'TEMP-2237',20,'suman','21','Male','Dental Surgery','no',''),(106,'55',20,'suman','21','Male','Dental Surgery','',''),(107,'2',20,'suman','21','Male','General Checkup','',''),(108,'P-001',1,'Test Quick Scan Patient','30','Male','Checkup','None','None'),(109,'TEMP-1404',20,'dhoni','46','Male','General Checkup','',''),(110,'567',20,'suman','21','Male','Dental Surgery','',''),(111,'65',20,'dhoni','46','Male','General Checkup','',''),(112,'55',20,'suman','21','Male','General Surgery','normal',''),(113,'P12345',20,'Gsgsh','16','Male','Dental Surgery','jssj','');
/*!40000 ALTER TABLE `patients` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-19  8:49:37
