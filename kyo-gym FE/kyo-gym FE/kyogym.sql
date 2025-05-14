-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: kyo_gym
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `account_id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `encrypt_password` varchar(255) DEFAULT NULL,
  `is_enable` bit(1) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'admin@gmail.com','$2a$12$UvDZY3zjvw3onKj6nMJK0.WNnA4BFJxRleFFrJlepdwggAIbqMw4G',_binary '','admin'),(2,'johndoe@gmail.com','$2a$12$PCpFZSzeBUbg9AJsUTywO.W8U6T5vsP3LqsPovIK/2R1Cxq5I1gGC',_binary '','johndoe'),(3,'janesmith@gmail.com','$2a$12$PCpFZSzeBUbg9AJsUTywO.W8U6T5vsP3LqsPovIK/2R1Cxq5I1gGC',_binary '','janesmith'),(4,'alicejohnson@gmail.com','$2a$12$PCpFZSzeBUbg9AJsUTywO.W8U6T5vsP3LqsPovIK/2R1Cxq5I1gGC',_binary '','alicejohnson'),(5,'bobwilson@gmail.com','$2a$12$PCpFZSzeBUbg9AJsUTywO.W8U6T5vsP3LqsPovIK/2R1Cxq5I1gGC',_binary '','bobwilson'),(6,'emilybrown@gmail.com','$2a$12$PCpFZSzeBUbg9AJsUTywO.W8U6T5vsP3LqsPovIK/2R1Cxq5I1gGC',_binary '','emilybrown'),(7,'michaeldavis@gmail.com','$2a$12$PCpFZSzeBUbg9AJsUTywO.W8U6T5vsP3LqsPovIK/2R1Cxq5I1gGC',_binary '','michaeldavis'),(8,'lindawilson@gmail.com','$2a$12$PCpFZSzeBUbg9AJsUTywO.W8U6T5vsP3LqsPovIK/2R1Cxq5I1gGC',_binary '','lindawilson'),(9,'davidlee@gmail.com','$2a$12$PCpFZSzeBUbg9AJsUTywO.W8U6T5vsP3LqsPovIK/2R1Cxq5I1gGC',_binary '','davidlee'),(10,'susantaylor@gmail.com','$2a$12$PCpFZSzeBUbg9AJsUTywO.W8U6T5vsP3LqsPovIK/2R1Cxq5I1gGC',_binary '','susantaylor'),(11,'tomjohnson@gmail.com','$2a$12$PCpFZSzeBUbg9AJsUTywO.W8U6T5vsP3LqsPovIK/2R1Cxq5I1gGC',_binary '','tomjohnson'),(12,'vuduynambinh@gmail.com',NULL,_binary '','vuduynambinh@gmail.com'),(13,'adminduy@gmail.com','$2a$12$il4lnAGKeQSfmJCs/2gMWuQsfeV/u4m81PVU5fQEFR8OIq8.grl3W',_binary '','adminduy'),(14,'userduy@gmail.com','$2a$12$VINWEzyiWQcor/v363VyB.Bg9HwEagZDXxoR5CM5qp/CUWJssP6pK',_binary '','userduy');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_roles`
--

DROP TABLE IF EXISTS `account_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_roles` (
  `account_id` int NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`account_id`,`role_id`),
  KEY `FKi84870gssnbi37wfqfifekghb` (`role_id`),
  CONSTRAINT `FKi84870gssnbi37wfqfifekghb` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`),
  CONSTRAINT `FKtp61eta5i06bug3w1qr6286uf` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_roles`
--

LOCK TABLES `account_roles` WRITE;
/*!40000 ALTER TABLE `account_roles` DISABLE KEYS */;
INSERT INTO `account_roles` VALUES (2,1),(3,1),(4,1),(5,1),(6,1),(7,1),(8,1),(9,1),(10,1),(11,1),(12,1),(14,1),(1,2),(13,2);
/*!40000 ALTER TABLE `account_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `cart_id` int NOT NULL AUTO_INCREMENT,
  `receiver_address` varchar(255) DEFAULT NULL,
  `receiver_email` varchar(255) DEFAULT NULL,
  `receiver_name` varchar(255) DEFAULT NULL,
  `receiver_phone` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`cart_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
INSERT INTO `cart` VALUES (1,'123 Main St','johndoe@gmail.com','John Doe','0951234567'),(2,'456 Elm St','janesmith@gmail.com','Jane Smith','0959876543'),(3,'789 Oak St','alicejohnson@gmail.com','Alice Johnson','0755555555'),(4,'101 Pine St','bobwilson@gmail.com','Bob Wilson','0852223333'),(5,'222 Cedar St','emilybrown@gmail.com','Emily Brown','0757778888'),(6,'333 Birch St','michaeldavis@gmail.com','Michael Davis','0654449999'),(7,'444 Walnut St','lindawilson@gmail.com','Linda Wilson','0951112222'),(8,'555 Maple St','davidlee@gmail.com','David Lee','0853336666'),(9,'666 Sycamore St','susantaylor@gmail.com','Susan Taylor','0758884444'),(10,'777 Redwood St','tomjohnson@gmail.com','Tom Johnson','0756661111');
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_detail`
--

DROP TABLE IF EXISTS `cart_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_detail` (
  `cart_detail_id` int NOT NULL AUTO_INCREMENT,
  `cart_id` int NOT NULL,
  `quantity` int NOT NULL,
  `status` bit(1) NOT NULL,
  `customer_type_id` int DEFAULT NULL,
  PRIMARY KEY (`cart_detail_id`),
  KEY `FK3366u4685f0t9bej7x74isn68` (`customer_type_id`),
  CONSTRAINT `FK3366u4685f0t9bej7x74isn68` FOREIGN KEY (`customer_type_id`) REFERENCES `customer_type` (`customer_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_detail`
--

LOCK TABLES `cart_detail` WRITE;
/*!40000 ALTER TABLE `cart_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comments` (
  `comment_id` int NOT NULL AUTO_INCREMENT,
  `comment_text` varchar(255) DEFAULT NULL,
  `rating` int DEFAULT NULL,
  `customer_id` int DEFAULT NULL,
  `exercise_id` int DEFAULT NULL,
  PRIMARY KEY (`comment_id`),
  KEY `FKiu2ob8pkb818nwfiiwvm4f0xn` (`customer_id`),
  KEY `FKlnx74csmg7mnr1jeesrl78tan` (`exercise_id`),
  CONSTRAINT `FKiu2ob8pkb818nwfiiwvm4f0xn` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `FKlnx74csmg7mnr1jeesrl78tan` FOREIGN KEY (`exercise_id`) REFERENCES `exercise` (`exercise_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comments`
--

LOCK TABLES `comments` WRITE;
/*!40000 ALTER TABLE `comments` DISABLE KEYS */;
INSERT INTO `comments` VALUES (1,'This is a good comment with some additional text. It provides valuable feedback on the topic.',5,1,1),(2,'I absolutely loved this! Best ever. It exceeded my expectations.',5,1,1),(3,'Another fantastic comment from a happy user. The quality of this content is outstanding.',5,2,1),(4,'Nice work, keep it up! Your efforts are truly appreciated.',5,2,1),(5,'Outstanding. I love it! This is exactly what I was looking for. Kudos to the creator.',5,3,1),(6,'Not bad, could use some improvements. While it\'s decent, there\'s room for growth.',4,3,1);
/*!40000 ALTER TABLE `comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `course_id` int NOT NULL AUTO_INCREMENT,
  `course_name` varchar(255) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `duration` varchar(255) DEFAULT NULL,
  `image` varchar(2000) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `course_type_id` int DEFAULT NULL,
  PRIMARY KEY (`course_id`),
  KEY `FKpbkedfmmgtj0b8bbos3lro1vw` (`course_type_id`),
  CONSTRAINT `FKpbkedfmmgtj0b8bbos3lro1vw` FOREIGN KEY (`course_type_id`) REFERENCES `course_type` (`course_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (1,'Bodyweight Squats','Bodyweight squats are a fundamental exercise targeting the lower body muscles, helping to improve strength and endurance without the need for additional weights.','12 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FBodyweight%20Squats%20and%20Lunges.jpg?alt=media&token=f6b6718c-93fb-4989-a34c-c5a0f52cde64',_binary '\0',1),(2,'Running (Cardio)','Running is a popular cardiovascular exercise that improves overall endurance and promotes heart health, making it an effective way to burn calories and stay fit.','12 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FRunning%20(Cardio).jpg?alt=media&token=7b684fc4-d1ee-4c27-9810-f2b8b469f91e',_binary '\0',2),(3,'Yoga and Pilates','This course combines the benefits of yoga and Pilates, focusing on improving flexibility, strength, and mental well-being through various poses and exercises.','12 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FYoga%20and%20Pilates.jpg?alt=media&token=c3698bef-deb8-482a-8f4d-85c5beb723bd',_binary '\0',3),(4,'Compound Exercises','Compound exercises involve multiple muscle groups simultaneously, aiding in muscle development and overall strength.','12 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FCompound%20Exercises.jpg?alt=media&token=ea06860f-ae31-4e1b-889f-98f5e9366048',_binary '\0',1),(5,'Push-Ups and Pull-Ups','These bodyweight exercises are essential for upper body strength, targeting the chest, arms, and back muscles.','12 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FPush-Ups%20and%20Pull-Ups.jpg?alt=media&token=c344eaf2-9cc0-4057-8d63-1d33922a7ea2',_binary '\0',1),(6,'Zumba or Fast Walking (Cardio)','Zumba or fast walking offers a dynamic cardiovascular workout, combining dance movements or brisk walking to elevate heart rate and improve overall endurance.','6 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2Fcardio1.jpg?alt=media&token=b55f3047-5de7-46cc-9e39-f617d9a07042',_binary '\0',2),(7,'Optimal training for muscles and a fit body','This training program is designed to optimize muscle growth and overall fitness through a balanced mix of strength training, cardio, and flexibility exercises.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FOptimal%20training%20for%20muscles%20and%20a%20fit%20body.jpg?alt=media&token=63efd115-77e1-4cdd-9475-41c4f68f4c15',_binary '\0',1),(8,'Kettlebell Exercises (Kettlebell Swings, Goblet Squats)','Kettlebell exercises incorporate dynamic movements to strengthen and tone various muscle groups, enhancing overall power and agility.','12 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FKettlebell%20Exercises%20(Kettlebell%20Swings%2C%20Goblet%20Squats).jpg?alt=media&token=33ae2b59-e54f-44e2-8337-e31eb3fd8bca',_binary '\0',1),(9,'Dumbbell Lunges and Dumbbell Shoulder Press','This course utilizes dumbbells to perform lunges and shoulder presses, targeting the lower body and shoulder muscles for increased strength and definition.','12 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FDumbbell%20Lunges%20and%20Dumbbell%20Shoulder%20Press.jpg?alt=media&token=f550df0b-0899-4cff-9f4b-612198ae903a',_binary '\0',1),(10,'Jumping Jacks and Mountain Climbers','These cardio exercises are effective in elevating heart rate and burning calories, contributing to improved cardiovascular health and endurance.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FJumping%20Jacks%20and%20Mountain%20Climbers.jpg?alt=media&token=c4f40aed-4a05-4d10-b7e8-46fae56f8c2c',_binary '\0',1),(11,'Strengthen your muscles with simple exercises','This course focuses on simple yet effective exercises to strengthen and tone muscles, providing an accessible and straightforward approach to muscle development.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FStrengthen%20your%20muscles%20with%20simple%20exercises.jfif?alt=media&token=008d7fd0-482e-479c-97b0-8af9e6aa91ba',_binary '\0',1),(12,'Deadlifts and Bench Press','Deadlifts and bench presses are essential compound exercises that target multiple muscle groups, promoting overall strength and muscle development.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FDeadlifts%20and%20Bench%20Press.jpg?alt=media&token=736cc741-00a6-4816-8663-c3394829f09d',_binary '\0',1),(13,'Bodyweight Rows and Tricep Dips','Bodyweight rows and tricep dips are effective exercises for building upper body strength and muscle definition.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FBodyweight%20Rows%20and%20Tricep%20Dips.webp?alt=media&token=34b56bff-9143-44e4-ad45-2694d4d10f49',_binary '\0',1),(14,'Cycling ','Cycling offers a low-impact cardiovascular workout that improves leg strength and endurance, making it an excellent choice for overall fitness and stamina.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FCycling.jpg?alt=media&token=23e2af05-c57e-4b20-9e57-9dcd7299868b',_binary '\0',2),(15,'Increase muscle strength and endurance','This training course is dedicated to improving muscle strength and endurance through a combination of resistance training and high-intensity cardiovascular workouts.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FIncrease%20muscle%20strength%20and%20endurance.jpg?alt=media&token=7e746712-48f3-450d-8f2d-5b5430576460',_binary '\0',4),(16,'Resistance Band Exercises and Planks','Utilizing resistance bands and planks, this course aims to improve muscle strength, stability, and flexibility, offering a versatile and effective training regimen.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FResistance%20Band%20Exercises%20and%20Planks.jpg?alt=media&token=2c566793-96af-4039-9329-6f9c4bba7f03',_binary '\0',3),(17,'Bodyweight Squats and Lunges','This course combines bodyweight squats and lunges to target and strengthen the lower body muscles, enhancing overall leg strength and stability.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FBodyweight%20Squats%20and%20Lunges.jpg?alt=media&token=f6b6718c-93fb-4989-a34c-c5a0f52cde64',_binary '\0',1),(18,'Jump Rope and Bodyweight Exercises','This training program incorporates jump rope and various bodyweight exercises for a dynamic and effective full-body workout.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FJump%20Rope%20and%20Bodyweight%20Exercises.jpg?alt=media&token=a2867428-1c39-49bf-b2c0-159e56ee57d7',_binary '\0',2),(19,'Meditation and Deep Breathing Exercises','This course focuses on relaxation techniques, such as meditation and deep breathing exercises, promoting mental well-being and stress reduction.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FMeditation%20and%20Deep%20Breathing%20Exercises.jfif?alt=media&token=1447cb97-1530-497d-bf50-f0a7de096b92',_binary '\0',5),(20,'TRX Exercises and Russian Twists','Using TRX suspension training and Russian twists, this course aims to improve core strength, stability, and overall muscle tone, providing a challenging and effective workout routine.','4 weeks','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FCourses%2FTRX%20Exercises%20and%20Russian%20Twists.jpg?alt=media&token=154c4ce2-4364-4e46-bfa8-215f1324195b',_binary '\0',6);
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_days`
--

DROP TABLE IF EXISTS `course_days`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_days` (
  `course_id` int NOT NULL,
  `day_id` int NOT NULL,
  `course_day_id` int NOT NULL,
  `status` bit(1) DEFAULT NULL,
  PRIMARY KEY (`course_id`,`day_id`),
  KEY `FKmevj7s1k7sx3pr9b460jfdpm6` (`day_id`),
  CONSTRAINT `FKmevj7s1k7sx3pr9b460jfdpm6` FOREIGN KEY (`day_id`) REFERENCES `days` (`day_id`),
  CONSTRAINT `FKprnabaq3o2n7b79taef670jnc` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_days`
--

LOCK TABLES `course_days` WRITE;
/*!40000 ALTER TABLE `course_days` DISABLE KEYS */;
INSERT INTO `course_days` VALUES (1,1,0,NULL),(1,2,0,NULL),(1,3,0,NULL),(1,4,0,NULL),(1,5,0,NULL),(1,6,0,NULL),(1,7,0,NULL),(1,8,0,NULL),(1,9,0,NULL),(1,10,0,NULL),(2,1,0,NULL),(2,2,0,NULL),(2,3,0,NULL),(2,4,0,NULL),(2,5,0,NULL),(2,6,0,NULL),(2,7,0,NULL),(2,8,0,NULL),(2,9,0,NULL),(2,10,0,NULL),(3,1,0,NULL),(3,2,0,NULL),(3,3,0,NULL),(3,4,0,NULL),(3,5,0,NULL),(3,6,0,NULL),(3,7,0,NULL),(3,8,0,NULL),(3,9,0,NULL),(3,10,0,NULL),(4,1,0,NULL),(4,2,0,NULL),(4,3,0,NULL),(4,4,0,NULL),(4,5,0,NULL),(4,6,0,NULL),(4,7,0,NULL),(4,8,0,NULL),(4,9,0,NULL),(4,10,0,NULL),(5,1,0,NULL),(5,2,0,NULL),(5,3,0,NULL),(5,4,0,NULL),(5,5,0,NULL),(5,6,0,NULL),(5,7,0,NULL),(5,8,0,NULL),(5,9,0,NULL),(5,10,0,NULL),(6,1,0,NULL),(6,2,0,NULL),(6,3,0,NULL),(6,4,0,NULL),(6,5,0,NULL),(6,6,0,NULL),(6,7,0,NULL),(6,8,0,NULL),(6,9,0,NULL),(6,10,0,NULL),(7,1,0,NULL),(7,2,0,NULL),(7,3,0,NULL),(7,4,0,NULL),(7,5,0,NULL),(7,6,0,NULL),(7,7,0,NULL),(7,8,0,NULL),(7,9,0,NULL),(7,10,0,NULL),(8,1,0,NULL),(8,2,0,NULL),(8,3,0,NULL),(8,4,0,NULL),(8,5,0,NULL),(8,6,0,NULL),(8,7,0,NULL),(8,8,0,NULL),(8,9,0,NULL),(8,10,0,NULL),(9,1,0,NULL),(9,2,0,NULL),(9,3,0,NULL),(9,4,0,NULL),(9,5,0,NULL),(9,6,0,NULL),(9,7,0,NULL),(9,8,0,NULL),(9,9,0,NULL),(9,10,0,NULL),(10,1,0,NULL),(10,2,0,NULL),(10,3,0,NULL),(10,4,0,NULL),(10,5,0,NULL),(10,6,0,NULL),(10,7,0,NULL),(10,8,0,NULL),(10,9,0,NULL),(10,10,0,NULL),(11,1,0,NULL),(11,2,0,NULL),(11,3,0,NULL),(11,4,0,NULL),(11,5,0,NULL),(11,6,0,NULL),(11,7,0,NULL),(11,8,0,NULL),(11,9,0,NULL),(11,10,0,NULL),(12,1,0,NULL),(12,2,0,NULL),(12,3,0,NULL),(12,4,0,NULL),(12,5,0,NULL),(12,6,0,NULL),(12,7,0,NULL),(12,8,0,NULL),(12,9,0,NULL),(12,10,0,NULL),(13,1,0,NULL),(13,2,0,NULL),(13,3,0,NULL),(13,4,0,NULL),(13,5,0,NULL),(13,6,0,NULL),(13,7,0,NULL),(13,8,0,NULL),(13,9,0,NULL),(13,10,0,NULL),(14,1,0,NULL),(14,2,0,NULL),(14,3,0,NULL),(14,4,0,NULL),(14,5,0,NULL),(14,6,0,NULL),(14,7,0,NULL),(14,8,0,NULL),(14,9,0,NULL),(14,10,0,NULL),(15,1,0,NULL),(15,2,0,NULL),(15,3,0,NULL),(15,4,0,NULL),(15,5,0,NULL),(15,6,0,NULL),(15,7,0,NULL),(15,8,0,NULL),(15,9,0,NULL),(15,10,0,NULL),(16,1,0,NULL),(16,2,0,NULL),(16,3,0,NULL),(16,4,0,NULL),(16,5,0,NULL),(16,6,0,NULL),(16,7,0,NULL),(16,8,0,NULL),(16,9,0,NULL),(16,10,0,NULL),(17,1,0,NULL),(17,2,0,NULL),(17,3,0,NULL),(17,4,0,NULL),(17,5,0,NULL),(17,6,0,NULL),(17,7,0,NULL),(17,8,0,NULL),(17,9,0,NULL),(17,10,0,NULL),(18,1,0,NULL),(18,2,0,NULL),(18,3,0,NULL),(18,4,0,NULL),(18,5,0,NULL),(18,6,0,NULL),(18,7,0,NULL),(18,8,0,NULL),(18,9,0,NULL),(18,10,0,NULL),(19,1,0,NULL),(19,2,0,NULL),(19,3,0,NULL),(19,4,0,NULL),(19,5,0,NULL),(19,6,0,NULL),(19,7,0,NULL),(19,8,0,NULL),(19,9,0,NULL),(19,10,0,NULL),(20,1,0,NULL),(20,2,0,NULL),(20,3,0,NULL),(20,4,0,NULL),(20,5,0,NULL),(20,6,0,NULL),(20,7,0,NULL),(20,8,0,NULL),(20,9,0,NULL),(20,10,0,NULL);
/*!40000 ALTER TABLE `course_days` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_type`
--

DROP TABLE IF EXISTS `course_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_type` (
  `course_type_id` int NOT NULL AUTO_INCREMENT,
  `course_type_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`course_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_type`
--

LOCK TABLES `course_type` WRITE;
/*!40000 ALTER TABLE `course_type` DISABLE KEYS */;
INSERT INTO `course_type` VALUES (1,'Strength Training'),(2,'Cardiovascular Training'),(3,'Flexibility and Balance'),(4,'Train strength and endurance'),(5,'Mind-Body Wellness'),(6,'Functional Training');
/*!40000 ALTER TABLE `course_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `customer_id` int NOT NULL AUTO_INCREMENT,
  `customer_address` varchar(255) DEFAULT NULL,
  `customer_code` varchar(255) DEFAULT NULL,
  `customer_email` varchar(255) DEFAULT NULL,
  `customer_gender` bit(1) DEFAULT NULL,
  `customer_img` varchar(255) DEFAULT NULL,
  `customer_name` varchar(255) DEFAULT NULL,
  `customer_phone` varchar(255) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `id_card` varchar(255) DEFAULT NULL,
  `is_enable` bit(1) DEFAULT NULL,
  `account_id` int DEFAULT NULL,
  `cart_id` int DEFAULT NULL,
  `customer_type_id` int DEFAULT NULL,
  PRIMARY KEY (`customer_id`),
  KEY `FKn9x2k8svpxj3r328iy1rpur83` (`account_id`),
  KEY `FKam4cgy6fxmjm52m8otoph84m3` (`cart_id`),
  KEY `FKn8vf9jf3m29plqn6rx45p2pl7` (`customer_type_id`),
  CONSTRAINT `FKam4cgy6fxmjm52m8otoph84m3` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`cart_id`),
  CONSTRAINT `FKn8vf9jf3m29plqn6rx45p2pl7` FOREIGN KEY (`customer_type_id`) REFERENCES `customer_type` (`customer_type_id`),
  CONSTRAINT `FKn9x2k8svpxj3r328iy1rpur83` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'123 Main St','KH-0001','johndoe@gmail.com',_binary '','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FAvatar%2Fman1.jpg?alt=media&token=d9ce0a41-7a55-42b6-ab67-b34b70d33d09','John Doe','0951234567','1990-05-15','201485694',_binary '',2,1,1),(2,'456 Elm St','KH-0002','janesmith@gmail.com',_binary '\0','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FAvatar%2Fwoman2.jpg?alt=media&token=0deeedb8-d6ea-4e29-8fb8-fbfd0ecd70d9','Jane Smith','0959876543','1985-12-10','201478569',_binary '',3,2,2),(3,'789 Oak St','KH-0003','alicejohnson@gmail.com',_binary '','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FAvatar%2Fman3.jpg?alt=media&token=254d36bb-e7c4-4772-891b-86a2dec5ca0d','Alice Johnson','0755555555','1995-03-25','369258147',_binary '',4,3,1),(4,'101 Pine St','KH-0004','bobwilson@gmail.com',_binary '\0','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FAvatar%2Fwoman3.jpg?alt=media&token=64e4dc60-6f93-443f-a544-93bb79a69a8c','Bob Wilson','0852223333','1988-09-03','789456123',_binary '',5,4,2),(5,'222 Cedar St','KH-0005','emilybrown@gmail.com',_binary '','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FAvatar%2Fman6.jpg?alt=media&token=95d30570-7656-40a4-876e-422605c8859b','Emily Brown','0757778888','1980-07-20','159369258',_binary '',6,5,3),(6,'333 Birch St','KH-0006','michaeldavis@gmail.com',_binary '','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FAvatar%2Fman7.jpg?alt=media&token=84921e64-157d-4b1c-abce-cb008f391b6b','Michael Davis','0654449999','1992-11-30','369456123',_binary '',7,6,1),(7,'444 Walnut St','KH-0007','lindawilson@gmail.com',_binary '\0','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FAvatar%2Fwoman4.jpg?alt=media&token=9c6092e6-8c98-4dc4-9896-5779a25afe4a','Linda Wilson','0951112222','1987-04-14','258789357',_binary '',8,7,2),(8,'555 Maple St','KH-0008','davidlee@gmail.com',_binary '','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FAvatar%2Fman9.jpg?alt=media&token=d21e5266-a148-4bbb-8728-73f4e33e207a','David Lee','0853336666','1998-01-05','258357147',_binary '',9,8,3),(9,'666 Sycamore St','KH-0009','susantaylor@gmail.com',_binary '\0','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FAvatar%2Fwoman5.jpg?alt=media&token=ff3aa333-878a-4b53-9d88-534a02c4d25a','Susan Taylor','0758884444','1986-08-28','369159753',_binary '',10,9,1),(10,'777 Redwood St','KH-0010','tomjohnson@gmail.com',_binary '','https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/images%2FAvatar%2Fman4.jfif?alt=media&token=66e859b5-d4ab-40ef-ad8e-aec929b9961d','Tom Johnson','0756661111','1991-12-18','258123951',_binary '',11,10,2),(11,NULL,'KH-0011','vuduynambinh@gmail.com',NULL,NULL,'Vũ Duy',NULL,NULL,NULL,_binary '',12,NULL,NULL);
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer_course`
--

DROP TABLE IF EXISTS `customer_course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_course` (
  `customer_course_id` int NOT NULL AUTO_INCREMENT,
  `recommended_status` bit(1) DEFAULT NULL,
  `course_id` int DEFAULT NULL,
  `customer_id` int DEFAULT NULL,
  PRIMARY KEY (`customer_course_id`),
  KEY `FK3q7yu0p3k2vj04soq1h9mxgng` (`course_id`),
  KEY `FKc31us2xum0u9t197c4lwgv4cd` (`customer_id`),
  CONSTRAINT `FK3q7yu0p3k2vj04soq1h9mxgng` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`),
  CONSTRAINT `FKc31us2xum0u9t197c4lwgv4cd` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_course`
--

LOCK TABLES `customer_course` WRITE;
/*!40000 ALTER TABLE `customer_course` DISABLE KEYS */;
INSERT INTO `customer_course` VALUES (1,_binary '\0',1,1),(2,_binary '\0',2,1),(3,_binary '\0',3,2),(4,_binary '\0',4,2),(5,_binary '\0',5,2),(6,_binary '\0',4,3),(7,_binary '\0',5,3),(8,_binary '\0',6,4),(9,_binary '\0',7,4),(10,_binary '\0',8,5),(11,_binary '\0',9,5),(12,_binary '\0',10,6),(13,_binary '\0',11,6),(14,_binary '\0',12,7),(15,_binary '\0',13,7),(16,_binary '\0',14,8),(17,_binary '\0',15,8),(18,_binary '\0',16,9),(19,_binary '\0',17,9),(20,_binary '\0',18,10),(21,_binary '\0',19,10),(22,_binary '\0',20,10),(23,_binary '',2,1);
/*!40000 ALTER TABLE `customer_course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer_type`
--

DROP TABLE IF EXISTS `customer_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_type` (
  `customer_type_id` int NOT NULL AUTO_INCREMENT,
  `customer_type_name` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  PRIMARY KEY (`customer_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_type`
--

LOCK TABLES `customer_type` WRITE;
/*!40000 ALTER TABLE `customer_type` DISABLE KEYS */;
INSERT INTO `customer_type` VALUES (1,'Regular',1000),(2,'Premium',2000),(3,'VIP',3000);
/*!40000 ALTER TABLE `customer_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `days`
--

DROP TABLE IF EXISTS `days`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `days` (
  `day_id` int NOT NULL AUTO_INCREMENT,
  `day_name` varchar(255) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  PRIMARY KEY (`day_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `days`
--

LOCK TABLES `days` WRITE;
/*!40000 ALTER TABLE `days` DISABLE KEYS */;
INSERT INTO `days` VALUES (1,'Day 1',NULL),(2,'Day 2',NULL),(3,'Day 3',NULL),(4,'Day 4',NULL),(5,'Day 5',NULL),(6,'Day 6',NULL),(7,'Day 7',NULL),(8,'Day 8',NULL),(9,'Day 9',NULL),(10,'Day 10',NULL);
/*!40000 ALTER TABLE `days` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exercise`
--

DROP TABLE IF EXISTS `exercise`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exercise` (
  `exercise_id` int NOT NULL AUTO_INCREMENT,
  `body_part` varchar(255) DEFAULT NULL,
  `duration` double DEFAULT NULL,
  `equipment` varchar(255) DEFAULT NULL,
  `exercise_description` varchar(2000) DEFAULT NULL,
  `exercise_name` varchar(255) DEFAULT NULL,
  `instructions` varchar(2000) DEFAULT NULL,
  `is_enable` varchar(255) DEFAULT NULL,
  `target` varchar(255) DEFAULT NULL,
  `upload_date` datetime DEFAULT NULL,
  `video_url` varchar(10000) DEFAULT NULL,
  `views` int DEFAULT NULL,
  PRIMARY KEY (`exercise_id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exercise`
--

LOCK TABLES `exercise` WRITE;
/*!40000 ALTER TABLE `exercise` DISABLE KEYS */;
INSERT INTO `exercise` VALUES (1,'Abs',NULL,'No equipment required','Steps: Lie on your back with your hands behind your head and legs lifted off the ground. Bring your right elbow to your left knee while straightening your right leg. Switch sides, bringing your left elbow to your right knee while straightening your left leg. Recommended Repetitions: 15-20 reps on each side, 2-3 sets in total.','Bycycle Crunch','The bicycle crunch primarily targets the rectus abdominis, obliques, and hip flexors. It helps improve core strength, stability, and endurance while also engaging the lower body muscles. It is effective for developing the abdominal muscles and enhancing overall core strength and stability.','1','Abs muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FBung_Abs%2FBicycle%20Crunch.webm?alt=media&token=a2a5e9e1-a1e5-4f51-bf52-32f091d3d06e&_gl=1*14t6lm7*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MTM5Ni42MC4wLjA.',NULL),(2,'Abs',NULL,'Treadmill, elliptical, stationary bike, or any other cardio equipment','Although not a specific exercise, engaging in cardiovascular activities such as running, swimming, or cycling is recommended. Aim for 150 minutes of moderate-intensity cardio or 75 minutes of high-intensity cardio per week for overall health and weight management.','Cardio','Cardio exercises, while not specifically targeting the abdominal muscles, are crucial for overall fitness and weight management. They help in burning calories and reducing overall body fat, which can contribute to the visibility of abdominal muscles.','1','Abs muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FBung_Abs%2FCardio.webm?alt=media&token=46f5caef-1abc-49b5-aa29-f98da0699e29&_gl=1*xojfhe*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MTM5OS41Ny4wLjA.',NULL),(3,'Abs',NULL,'Exercise mat, or a gym mat for comfort','Steps: Lie on your back with your knees bent and feet flat on the floor. Place your hands behind your head or across your chest. Lift your upper body towards your knees, engaging your abdominal muscles. Lower yourself back to the starting position. Recommended Repetitions: 15-20 reps, 2-3 sets in total.','Crunch','The crunch is an effective exercise for strengthening the rectus abdominis muscle, commonly known as the \"six-pack\" muscle. It helps to build core strength, increase muscle endurance, and improve overall posture and stability.','1','Abs muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FBung_Abs%2FCrunch.webm?alt=media&token=02f78d07-589c-4c5a-955c-bc32c5fbe474&_gl=1*usx2vi*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MTQwMS41NS4wLjA.',NULL),(4,'Abs',NULL,'Pull-up bar or captain\'s chair apparatus','Steps: Hang from a pull-up bar with your arms fully extended. Engage your core and raise your legs until they form a 90-degree angle with your torso. Lower your legs back down slowly. Recommended Repetitions: 10-12 reps, 2-3 sets in total.','Hanging Leg Raise','Hanging leg raises primarily target the lower abdominal muscles, including the lower rectus abdominis. This exercise helps to build strength in the lower abdominal region, contributing to improved core stability and balance.','1','Abs muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FBung_Abs%2FHanging%20Leg%20Raise.webm?alt=media&token=0e45bc9d-728f-40da-804d-8ef2f665829c&_gl=1*1u5wvxo*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MTQwMy41My4wLjA.',NULL),(5,'Abs',NULL,'No equipment required','Steps: Get into a push-up position, resting on your forearms instead of your hands. Keep your body in a straight line from head to heels, engaging your core and keeping your glutes tight. Hold this position for 30-60 seconds, or as long as you can maintain proper form. Recommended Repetitions: 3-4 sets, holding the plank for the recommended duration.','Plank','The plank is a versatile exercise that targets multiple muscle groups, including the rectus abdominis, transverse abdominis, and obliques. It is beneficial for building core strength, improving posture, and enhancing overall stability and endurance.','1','Abs muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FBung_Abs%2FPlank.webm?alt=media&token=18e817cf-a444-44fd-858f-38affc1bb84b&_gl=1*gdw13f*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MTQwNi41MC4wLjA.',NULL),(6,'Abs',NULL,'Medicine ball or dumbbell','Steps: Sit on the floor with your knees bent and feet flat. Lean back slightly and lift your feet a few inches off the ground. Hold a weight or your hands together and twist your torso from side to side. Recommended Repetitions: 15-20 reps on each side, 2-3 sets in total.','Russian Twist','The Russian twist targets the obliques and helps to strengthen the muscles on the sides of the abdomen. It also engages the rectus abdominis and the lower back muscles, contributing to improved rotational strength and stability.','1','Abs muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FBung_Abs%2FRussian%20Twist.webm?alt=media&token=3d7dd9e0-1d79-47e3-a0e1-e8a106d591b7&_gl=1*14g487d*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MTQwOC40OC4wLjA.',NULL),(7,'Abs',NULL,'No equipment required','Steps: Lie on one side with your legs extended and one forearm directly below your shoulder. Lift your hips off the ground, creating a straight line from your head to your feet. Engage your core and hold the position for 20-30 seconds on each side. Recommended Repetitions: 2-3 sets on each side.','Side Plank','The side plank primarily targets the obliques and helps to improve lateral core strength and stability. It also engages the muscles along the entire side of the body, contributing to improved balance and posture. Side planks are effective for building strength in the waist and reducing the risk of lower back pain.','1','Abs muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FBung_Abs%2FSide%20Plank.webm?alt=media&token=cef616af-fcb9-4fea-b88c-dd7dfed68fa7&_gl=1*qdjdtk*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MTQxMS40NS4wLjA.',NULL),(8,'Calves',NULL,'Calf raise machine, smith machine, or free weights (dumbbells or barbells)','Steps: Stand with your feet shoulder-width apart. Rise up onto the balls of your feet, lifting your heels off the ground. Pause briefly at the top and then lower your heels back down. Recommended Repetitions: 12-15 reps for 3 sets.','Calf Raise','Calf raises primarily target the muscles of the lower leg, specifically the gastrocnemius and soleus muscles. This exercise helps to strengthen and define the calves, improving lower leg stability and performance in activities such as running, jumping, and walking.','1','Calves muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FChan_Calves%2FCalf%20Raise.webm?alt=media&token=7041232e-6fff-43a7-a3ad-dfa3945af4fa&_gl=1*1qqd9ze*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MjU4MC40Mi4wLjA.',NULL),(9,'Calves',NULL,'Hamstring curl machine','Steps: Adjust the machine to fit your body and choose an appropriate weight. Lie face down on the machine with the pad against the back of your ankles. Curl your legs up toward your glutes by bending your knees. Slowly lower your legs back to the starting position. Recommended Repetitions: 10-12 reps for 3 sets.','Hamstring Curls','While hamstring curls primarily target the hamstring muscles, they also engage the calves to a certain extent. These exercises are effective for strengthening the muscles at the back of the thigh, contributing to better leg stability and improved athletic performance.','1','Calves muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FChan_Calves%2FHamstring%20Curls.webm?alt=media&token=8a6145dc-a941-47a8-bd4e-28ba44ba4a51&_gl=1*1u0fccr*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MjU4Mi40MC4wLjA.',NULL),(10,'Calves',NULL,'Leg extension machine','Steps: Adjust the machine to fit your body and select an appropriate weight. Sit on the machine with your knees at a 90-degree angle. Extend your legs out in front of you, lifting the weighted lever with your shins. Slowly return to the starting position. Recommended Repetitions: 10-12 reps for 3 sets.','Leg Extensions','Leg extensions mainly target the quadriceps, the muscles at the front of the thigh. While they are not directly focused on the calves, leg extensions can indirectly contribute to overall lower body strength, which can enhance stability and performance in various activities that require lower body strength.','1','Calves muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FChan_Calves%2FLeg%20Extensions.webm?alt=media&token=51cfa738-e826-401b-a4d0-abebe17a9eba&_gl=1*1wrv6rw*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MjU4NC4zOC4wLjA.',NULL),(11,'Calves',NULL,'Leg press machine','\"Steps: Adjust the seat and footplate to your comfort. Sit on the machine with your feet shoulder-width apart on the footplate. Push the footplate away from your body by straightening your legs. Slowly bend your knees to return to the starting position. Recommended Repetitions: 8-12 reps for 3 sets.','Leg Press','The leg press is an exercise that primarily targets the quadriceps, hamstrings, and glutes. While the calves are not the primary focus, they still play a supportive role in stabilizing the lower body during the exercise. Leg presses help to build overall lower body strength and muscle endurance.','1','Calves muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FChan_Calves%2FLeg%20Press.webm?alt=media&token=cab8bee5-3f92-4b64-a420-b744ac6952b9&_gl=1*18mfcot*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MjU4OC4zNC4wLjA.',NULL),(12,'Calves',NULL,'No specific equipment required, but you may use dumbbells or a barbell for added resistance','Steps: Stand with your feet hip-width apart. Step forward with one leg and lower your hips until both knees are bent at a 90-degree angle. Push back to the starting position. Recommended Repetitions: 10-12 reps for each leg, 3 sets in total.','Lunges','Lunges target several muscles, including the quadriceps, hamstrings, glutes, and calves. Lunges help improve lower body strength, stability, and balance, contributing to better overall athletic performance and functional movement.','1','Calves muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FChan_Calves%2FLunges.webm?alt=media&token=e9f2a530-d7eb-4cfc-a4af-6a65625962fd&_gl=1*w93r9y*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MjU5MC4zMi4wLjA.',NULL),(13,'Calves',NULL,'Squat rack or power rack, or you can perform bodyweight squats without any equipment','Steps: Stand with your feet slightly wider than shoulder-width apart. Lower your body by bending your knees and hips, keeping your back straight. Lower yourself until your thighs are parallel to the ground. Push through your heels to return to the starting position. Recommended Repetitions: 8-12 reps for 3 sets.','Squat','Squats are a compound exercise that primarily targets the muscles of the thighs and buttocks, including the quadriceps, hamstrings, and glutes. Squats also engage the calves to some extent, contributing to better lower body strength, stability, and muscle development. They are effective for enhancing overall lower body strength and functional movement.','1','Calves muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FChan_Calves%2FSquat.webm?alt=media&token=3d2cdd2e-4a9d-4ffe-bd88-eb33f0486f06&_gl=1*yc8imv*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MjU5Mi4zMC4wLjA.',NULL),(14,'Back',NULL,'Barbell, weight plates, and a barbell rack','Steps: Stand with your feet shoulder-width apart, knees slightly bent, and a barbell in front of you. Hinge at your hips to bend over while keeping your back straight. Grasp the barbell with an overhand grip and pull it towards your lower chest. Lower the barbell back to the starting position, keeping it close to your body throughout the movement. Recommended Repetitions: 8-12 reps for 3-4 sets.','Bent-Over Barbell Row','The bent-over barbell row is an effective compound exercise that primarily targets the muscles of the back, including the latissimus dorsi, rhomboids, and trapezius. It also engages the muscles of the arms, including the biceps and forearms. This exercise helps to build overall back strength, improve posture, and promote better upper body stability.','1','Back muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FLung_Back%2FBent-Over%20Barbell%20Row.webm?alt=media&token=ce55afac-e7ec-4ee0-8185-b544514b8f36&_gl=1*1am7p8u*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MjY1Mi41Ny4wLjA.',NULL),(15,'Back',NULL,'Barbell, weight plates, and a barbell rack','Steps: Stand with your feet hip-width apart and the barbell in front of you. Hinge at your hips and bend your knees to grip the barbell with hands slightly wider than shoulder-width apart. Keep your back straight and lift the bar by driving through your heels and straightening your hips and knees. Lower the bar back to the ground by hinging at your hips and bending your knees. Recommended Repetitions: 5-8 reps for 3-4 sets.','Deadlift','The deadlift is a fundamental compound exercise that targets multiple muscle groups, including the back, glutes, hamstrings, and core. While it primarily focuses on the posterior chain, including the lower back, it also engages the upper back muscles, promoting overall back strength and stability. Deadlifts are effective for building overall strength and improving functional movement patterns.','1','Back muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FLung_Back%2FDeadlift.webm?alt=media&token=3d5438ed-cac2-4fa8-8e82-1fb762b876ba&_gl=1*mhtyfx*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MjY1Ni41My4wLjA.',NULL),(16,'Back',NULL,'Pull-up bar or assisted pull-up/dip machine','Steps: Grip a pull-up bar with your palms facing away from you and hands slightly wider than shoulder-width apart. Hang from the bar with your arms fully extended. Pull yourself up until your chin is over the bar, focusing on using your back muscles. Lower yourself back to the starting position with control. Recommended Repetitions: 5-10 reps for 3-4 sets.\"','Pull-up','Pull-ups primarily target the upper back muscles, specifically the latissimus dorsi, as well as the biceps and forearms. They are effective for building upper body and back strength, improving grip strength, and promoting overall muscular development in the upper body. Pull-ups also help improve posture and shoulder stability.','1','Back muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FLung_Back%2FPull-up.webm?alt=media&token=140fca46-2c61-43a4-96e2-732a018c1478&_gl=1*3fmzqu*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MjY1OS41MC4wLjA.',NULL),(17,'Chest',NULL,'Barbell, weight plates, and a bench press rack','Steps: Lie on a flat bench with your feet firmly planted on the ground. Grasp the barbell with a grip slightly wider than shoulder-width apart. Lower the barbell to your chest, then press it back up to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Barbell Bench Press','The barbell bench press is a compound exercise that primarily targets the pectoralis major muscle. It also engages the anterior deltoids and triceps. This exercise is effective for building overall upper body strength and muscle mass, particularly in the chest area.','1','Chest muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FNguc_Chest%2FBarbell%20Bench%20Press.mp4?alt=media&token=01fc53eb-18e7-40a4-b02c-224c3ff018f5&_gl=1*wvznkd*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjc0Ni4yNC4wLjA.',NULL),(18,'Chest',NULL,'Barbell, weight plates, and an incline bench','Steps: Lie on an inclined bench set at a 45-degree angle with your feet flat on the ground. Grip the barbell with a slightly wider than shoulder-width grip. Lower the barbell to your upper chest, then press it back up to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Barbell Incline Bench Press','The barbell incline bench press targets the upper portion of the pectoralis major, along with the anterior deltoids and triceps. This exercise helps to develop strength and size in the upper chest, contributing to a well-rounded and proportionate chest development.','1','Chest muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FNguc_Chest%2FBarbell%20Incline%20Bench%20Press.mp4?alt=media&token=5cb4f7d5-25ef-49af-bb5a-524517c8f88a&_gl=1*1lcx4nr*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjc0OS4yMS4wLjA.',NULL),(19,'Chest',NULL,'Cable crossover machine','Steps: Stand in the center of a cable machine with the pulleys set high. Grab the handles with your palms facing forward and step forward slightly. Pull the handles down and across your body, squeezing your chest at the bottom of the movement. Recommended Repetitions: 10-15 reps for 3-4 sets.','Cable Crossover','Cable crossovers primarily target the chest muscles, emphasizing the inner portion of the pectoralis major. This exercise helps to improve chest definition, muscular endurance, and overall chest development by providing constant tension throughout the movement.','1','Chest muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FNguc_Chest%2FCable%20Crossover.webm?alt=media&token=84f5dab6-ef33-4108-9e46-16b7c8344545&_gl=1*1b908vn*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjc1MS4xOS4wLjA.',NULL),(20,'Chest',NULL,'Dip bar or dip station','Steps: Grip the dip bars with your arms fully extended. Lower your body by bending your elbows until your shoulders are below your elbows. Push yourself back up to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Chest Dips','Chest dips primarily target the lower and outer portion of the pectoralis major, as well as the triceps and anterior deltoids. This exercise is effective for building upper body strength, improving muscular endurance, and enhancing overall chest development.','1','Chest muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FNguc_Chest%2FChest%20Dips.webm?alt=media&token=a1813c14-77dd-4967-8375-b191b0631287&_gl=1*1du17be*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjc1NC4xNi4wLjA.',NULL),(21,'Chest',NULL,'Chest press machine or resistance bands','Steps: Sit on the chest press machine with your feet flat on the ground. Grasp the handles at chest height and push them away from your body. Slowly return to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Chest Press','The chest press, whether using a machine or free weights, targets the pectoralis major muscle. It is effective for building upper body strength, improving chest definition, and enhancing overall chest development.','1','Chest muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FNguc_Chest%2FChest%20Press.webm?alt=media&token=3a5e0c81-e5e7-4074-b0f9-e087430c8209&_gl=1*1g9uhfv*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjc1Ni4xNC4wLjA.',NULL),(22,'Chest',NULL,'Barbell, weight plates, and a decline bench','Steps: Lie on a decline bench with your feet secured at the top. Grip the barbell with a slightly wider than shoulder-width grip. Lower the barbell to your lower chest, then press it back up to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Decline Bench Press','The decline bench press targets the lower portion of the pectoralis major, along with the triceps and anterior deltoids. This exercise helps to develop strength and size in the lower chest, contributing to a well-rounded and proportionate chest development.','1','Chest muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FNguc_Chest%2FDecline%20Bench%20Press.webm?alt=media&token=d208411d-9d39-4038-a85b-c20aa1e75e19&_gl=1*1an5dof*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjc1OS4xMS4wLjA.',NULL),(23,'Chest',NULL,'Dumbbells and a flat bench','Steps: Lie on a flat bench with a dumbbell in each hand at chest level. Press the dumbbells up until your arms are fully extended, then lower them back down to chest level. Recommended Repetitions: 8-12 reps for 3-4 sets.','Dumbbell Bench Press','The dumbbell bench press primarily targets the pectoralis major, along with the anterior deltoids and triceps. It helps to improve chest strength, stability, and muscle balance between the two sides of the body.','1','Chest muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FNguc_Chest%2FDumbbell%20Bench%20Press.mp4?alt=media&token=e0f1d1df-3096-4e58-89d2-0559287c924e&_gl=1*1ea452c*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjc2Mi44LjAuMA..',NULL),(24,'Chest',NULL,'Dumbbells and a flat bench','Steps: Lie on a flat bench with a dumbbell in each hand above your chest. Lower the dumbbells out to your sides in an arc motion, then bring them back up to the starting position. Recommended Repetitions: 10-15 reps for 3-4 sets.','Dumbbell Flyes','Dumbbell flies primarily target the pectoralis major and also engage the anterior deltoids and biceps. This exercise helps to improve chest definition, develop the inner chest muscles, and enhance overall chest development.','1','Chest muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FNguc_Chest%2FDumbbell%20Flyes.webm?alt=media&token=76a29a10-1f1c-4969-a10b-1bd720f09d2f&_gl=1*1n8lmdp*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjc2NC42LjAuMA..',NULL),(25,'Chest',NULL,'Chest press machine or incline bench with dumbbells','Steps: Sit on an incline bench with a dumbbell in each hand at shoulder level. Press the dumbbells up until your arms are fully extended, then lower them back down to shoulder level. Recommended Repetitions: 8-12 reps for 3-4 sets.','Incline Chest Press','The incline chest press targets the upper portion of the pectoralis major, along with the anterior deltoids and triceps. This exercise is effective for building upper chest strength and size, contributing to a well-balanced and proportionate chest development.','1','Chest muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FNguc_Chest%2FIncline%20Chest%20Press.webm?alt=media&token=37b98594-709d-449a-8b3f-9f9d15134fb5&_gl=1*xv8ah6*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjc2Ny4zLjAuMA..',NULL),(26,'Chest',NULL,'Dumbbells and an incline bench','Steps: Lie on an inclined bench with a dumbbell in each hand at chest level. Press the dumbbells up until your arms are fully extended, then lower them back down to chest level. Recommended Repetitions: 8-12 reps for 3-4 sets.','Incline Dumbbell Bench Press','The incline dumbbell bench press primarily targets the upper chest muscles, along with the anterior deltoids and triceps. This exercise helps to develop strength and size in the upper chest, contributing to a well-rounded and proportionate chest development.','1','Chest muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FNguc_Chest%2FIncline%20Dumbbell%20Bench%20Press.webm?alt=media&token=aa2045af-1cc2-48e8-b0eb-378e85d2d938&_gl=1*1ab875j*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjc3MC42MC4wLjA.',NULL),(27,'Chest',NULL,'No equipment required','Steps: Start in a high plank position with your hands slightly wider than shoulder-width apart. Lower your body by bending your elbows, keeping your body in a straight line. Push yourself back up to the starting position. Recommended Repetitions: 10-15 reps for 3-4 sets.','Push Up','Push-ups target the chest muscles, primarily the pectoralis major, and also engage the triceps and anterior deltoids. They are effective for building upper body strength, improving muscular endurance, and enhancing overall chest development. Push-ups also help to improve core stability and strength.','1','Chest muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FNguc_Chest%2FPush%20Up.webm?alt=media&token=1c3cf566-79b7-444a-b614-87a0476c9367&_gl=1*1npzogl*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjc3My41Ny4wLjA.',NULL),(28,'Triceps',NULL,'Barbell, weight plates, and a barbell rack','Steps: Stand with your feet shoulder-width apart and knees slightly bent, holding a barbell with an overhand grip. Bend forward at the hips while keeping your back straight. Pull the barbell up towards your lower chest, keeping your elbows close to your body. Lower the barbell back down to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Bent-Over Barbell Row','The Bent-Over Barbell Row primarily targets the muscles of the back, including the latissimus dorsi, rhomboids, and trapezius, as well as the biceps. While it doesn\'t directly target the triceps, it can indirectly engage them to a certain extent for stabilization during the movement.','1','Triceps muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FTaysau_Triceps%2FBent-Over%20Barbell%20Row.webm?alt=media&token=3b1d3717-43d5-41cc-8c7f-5471a705e0a5&_gl=1*tgpqn1*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjk0NS4zNC4wLjA.',NULL),(29,'Triceps',NULL,'Dumbbell and a flat bench','Steps: Place one knee and hand on a flat bench, holding a dumbbell in your opposite hand. Keep your back straight and pull the dumbbell up to your hip, focusing on squeezing your back muscles. Lower the dumbbell back down to the starting position. Recommended Repetitions: 8-12 reps for each arm, 3-4 sets in total.','One-Arm Dumbbell Row','The One-Arm Dumbbell Row is another back exercise that targets the lats, rhomboids, and trapezius muscles. Like the Bent-Over Barbell Row, it does not specifically target the triceps.','1','Triceps muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FTaysau_Triceps%2FOne-Arm%20Dumbbell%20Row.webm?alt=media&token=21d63b73-f4a0-4c56-b664-1009cd9fb3f5&_gl=1*1t9w8sf*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjk0Ny4zMi4wLjA.',NULL),(30,'Triceps',NULL,'Pull-up bar or assisted pull-up/dip machine','Steps: Grip the pull-up bar with your palms facing away from you and hands slightly wider than shoulder-width apart. Hang from the bar with your arms fully extended. Pull your body up until your chin is over the bar, focusing on using your arm and back muscles. Lower yourself back to the starting position with control. Recommended Repetitions: 5-10 reps for 3-4 sets.','Pull-Up Bar','Pull-ups primarily target the back muscles, including the latissimus dorsi, rhomboids, and trapezius, as well as the biceps. Similar to the previous exercises, they do not directly target the triceps.','1','Triceps muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FTaysau_Triceps%2FPull-Up%20Bar.webm?alt=media&token=b5b00b7d-4d33-4cd5-a311-f16343a03b5a&_gl=1*eqn2ur*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjk1MS4yOC4wLjA.',NULL),(31,'Triceps',NULL,'Cable row machine','Steps: Sit at a cable row machine with your feet placed on the footrests and knees slightly bent. Grasp the cable attachment with both hands in front of you. Pull the cable towards your abdomen, squeezing your shoulder blades together. Slowly release the cable back to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Seated Cable Row','The Seated Cable Row mainly targets the back muscles, including the latissimus dorsi, rhomboids, and trapezius. As with the other exercises, it does not primarily target the triceps but can indirectly engage them for stabilization during the movement.','1','Triceps muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FTaysau_Triceps%2FSeated%20Cable%20Row.webm?alt=media&token=159ecd3c-bc73-4d0d-97b4-14eb6f3e7083&_gl=1*1wc633g*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mjk1My4yNi4wLjA.',NULL),(32,'Biceps',NULL,'Barbell and weight plates','Steps: Stand with your feet shoulder-width apart, holding a barbell with an underhand grip. Keep your elbows close to your body and curl the barbell up towards your chest. Lower the barbell back down to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Barbell Curl','The barbell curl is a classic biceps exercise that primarily targets the biceps brachii muscle. It helps to build biceps strength and size, contributing to overall arm development and strength.','1','Biceps muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FTaytruoc_Biceps%2FBarbell%20Curl.webm?alt=media&token=01077d69-bb1b-48b3-9325-73c973bf09b5&_gl=1*fglgif*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzA2OS4zNy4wLjA.',NULL),(33,'Biceps',NULL,'Dumbbells or barbell','Steps: Stand with your feet shoulder-width apart, holding a dumbbell in each hand with palms facing forward. Curl the dumbbells up towards your shoulders, keeping your elbows close to your body. Lower the dumbbells back down to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Curl','The term \"curl\" typically refers to any exercise that involves curling movements, usually targeting the biceps. This can include various equipment such as dumbbells, barbells, or resistance bands. Different variations can be used to target specific parts of the biceps.','1','Biceps muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FTaytruoc_Biceps%2FCurl.webm?alt=media&token=57bbe76f-76ca-461f-8ef5-e7aef16ff916&_gl=1*uh8b33*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzA3MS4zNS4wLjA.',NULL),(34,'Biceps',NULL,'Dumbbells','Steps: Stand with your feet shoulder-width apart, holding a dumbbell in each hand with palms facing your body. Curl the dumbbells up towards your shoulders, keeping your elbows close to your body. Lower the dumbbells back down to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Hammer Curl','The hammer curl is an exercise that targets the biceps brachii as well as the brachialis muscle. It aids in developing forearm strength and size, contributing to overall arm strength and muscle balance.','1','Biceps muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FTaytruoc_Biceps%2FHammer%20Curl.webm?alt=media&token=575080e4-b903-4816-a85b-64eb4f633ef5&_gl=1*1tny4cd*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzA3My4zMy4wLjA.',NULL),(35,'Biceps',NULL,'Dumbbell or cable machine','Steps: Sit at a preacher curl bench, holding an EZ bar with an underhand grip. Rest the back of your upper arms on the preacher bench pad and curl the barbell up towards your shoulders. Lower the barbell back down to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Preacher Curl','The preacher curl primarily targets the biceps brachii muscle and provides a more isolated movement for the biceps. It helps in building biceps strength and size, contributing to overall arm development and muscle balance.','1','Biceps muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FTaytruoc_Biceps%2FPreacher%20Curl.webm?alt=media&token=aa72c519-eaf4-4c2d-9521-7631abde3d6c&_gl=1*r3pbq1*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzI3OC4yNy4wLjA.',NULL),(36,'Biceps',NULL,'Preacher curl bench and barbell or dumbbells','Steps: Place your hands shoulder-width apart on a stable surface behind you and extend your legs out in front of you. Lower your body by bending your elbows until your upper arms are parallel to the ground. Push yourself back up to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Triceps Dip','Triceps dips primarily target the triceps brachii, along with the chest and shoulders. This bodyweight exercise helps in building triceps strength and size, contributing to overall arm development and strength.','1','Biceps muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FTaytruoc_Biceps%2FTriceps%20Dip.webm?alt=media&token=a1715a13-e89b-437b-a404-0356299fc8a3&_gl=1*1h390zk*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzMwOC42MC4wLjA.',NULL),(37,'Biceps',NULL,'Dip bar or dip station','Steps: Stand in front of a cable machine with a straight bar attached to the high pulley. Grip the bar with your palms facing down and push the bar down until your arms are fully extended. Slowly return the bar to the starting position. Recommended Repetitions: 10-15 reps for 3-4 sets.','Triceps Pushdown','The triceps pushdown is an isolation exercise that targets the triceps brachii muscle using a cable machine. It helps in building triceps strength and size, contributing to overall arm development and muscle balance.','1','Biceps muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FTaytruoc_Biceps%2FTriceps%20Pushdown.webm?alt=media&token=85773d47-3601-49a5-8453-9eb23fee8649&_gl=1*trktbq*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzMyOC40MC4wLjA.',NULL),(38,'Biceps',NULL,'Dumbbells or a barbell','Steps: Stand or sit with a dumbbell or a cable in one hand. Raise the weight overhead, keeping your upper arm close to your head. Lower the weight behind your head by bending your elbow. Extend your arm to return to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Overhead Triceps Extension','The overhead triceps extension primarily targets the triceps brachii muscle. It helps in building triceps strength and size, contributing to overall arm development and strength.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FTaytruoc_Biceps%2FOverhead%20Triceps%20Extension.webm?alt=media&token=6c3ebc1a-8f10-4188-8889-9d2897854c02&_gl=1*138t957*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzM0MC4yOC4wLjA.',NULL),(39,'Deltoids',NULL,'Cable machine with a straight or angled bar attachment','Steps: Sit on a bench with back support, holding a pair of dumbbells at shoulder height with your palms facing towards you. Press the dumbbells up and rotate your wrists as you do, so your palms face away from you at the top of the movement. Lower the dumbbells back to the starting position while reversing the wrist rotation. Recommended Repetitions: 8-12 reps for 3-4 sets.','Arnold Press','The Arnold press is a compound exercise that primarily targets the deltoid muscles, particularly the anterior and lateral heads. This exercise helps to develop overall shoulder strength and size, promoting well-rounded and proportionate shoulder development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FArnold%20Press.webm?alt=media&token=21dccf1e-349d-4c15-a07c-1e67f2e723da&_gl=1*1iuylov*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzQxMi41My4wLjA.',NULL),(40,'Deltoids',NULL,'Barbell and weight plates','Steps: Stand with your feet shoulder-width apart, holding a barbell at shoulder height with an overhand grip. Press the barbell overhead until your arms are fully extended. Lower the barbell back to shoulder height. Recommended Repetitions: 8-12 reps for 3-4 sets.','Barbell Military Press','The barbell military press is a compound exercise that primarily targets the deltoid muscles, particularly the anterior head. This exercise helps to develop overall shoulder strength and size, promoting well-rounded and proportionate shoulder development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FBarbell%20Military%20Press.webm?alt=media&token=d1f6c752-8586-4cd6-83ca-240a1ae0af4d&_gl=1*1xm150e*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzQzNi4yOS4wLjA.',NULL),(41,'Deltoids',NULL,'Battle ropes','Steps: Stand with your feet shoulder-width apart, holding the ends of the battle ropes in each hand. Move your arms up and down rapidly, creating waves in the ropes. Continue for a set amount of time or repetitions. Recommended Repetitions: 30-60 seconds for 3-4 sets.','Battle Ropes','Battle ropes exercises are a dynamic and high-intensity workout that targets multiple muscle groups, including the arms, shoulders, and core. These exercises help to improve cardiovascular health, endurance, and overall muscular strength and endurance.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FBattle%20Ropes.webm?alt=media&token=28c40451-df95-4268-bb46-dd611d855781&_gl=1*4oocmn*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzUwMS42MC4wLjA.',NULL),(42,'Deltoids',NULL,'Dumbbells','Steps:\r Bend at your hips with a slight bend in your knees, holding a pair of dumbbells in front of you with your palms facing each other.\r Raise the dumbbells out to the sides until your arms are parallel to the ground.\r Lower the dumbbells back to the starting position.\r Recommended Repetitions: 8-12 reps for 3-4 sets.','Bent-Over Dumbbell Raise','Bent-over dumbbell raises primarily target the posterior deltoids, along with the upper back muscles. This exercise helps to build overall shoulder and upper back strength, promoting well-rounded and proportionate shoulder development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FBent-Over%20Dumbbell%20Raise.webm?alt=media&token=ab0c083f-34e5-4e01-8a49-1fdcd227c548&_gl=1*3x1yxr*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzUxMS41MC4wLjA.',NULL),(43,'Deltoids',NULL,'Dumbbells','Steps: Hold a pair of dumbbells at shoulder height, palms facing in. Dip at the knees and use your legs to help press the dumbbells overhead. Lower the dumbbells back to shoulder height. Recommended Repetitions: 8-12 reps for 3-4 sets.','Dumbbell Push Press','The dumbbell push press is a compound exercise that primarily targets the deltoid muscles, particularly the anterior and lateral heads. This exercise helps to develop overall shoulder strength and size, promoting well-rounded and proportionate shoulder development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FDumbbell%20Push%20Press.webm?alt=media&token=c02b697f-9e0e-4df3-975d-6147f3eab144&_gl=1*1ou6net*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzUyNi4zNS4wLjA.',NULL),(44,'Deltoids',NULL,'Dumbbells','Steps: Sit on a bench with back support, holding a pair of dumbbells at shoulder height. Press the dumbbells overhead until your arms are fully extended. Lower the dumbbells back to shoulder height. Recommended Repetitions: 8-12 reps for 3-4 sets.','Dumbbell Shoulder Press','The dumbbell shoulder press is a compound exercise that primarily targets the deltoid muscles, particularly the anterior head. This exercise helps to develop overall shoulder strength and size, promoting well-rounded and proportionate shoulder development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FDumbbell%20Shoulder%20Press.webm?alt=media&token=4aec8aaa-22e1-40fe-a024-43745843203c&_gl=1*1sm6n8c*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzU0MS4yMC4wLjA.',NULL),(45,'Deltoids',NULL,'Dumbbells','Steps: Stand with your feet shoulder-width apart, holding a dumbbell in each hand with palms facing your thighs. Raise the dumbbells directly in front of you until they reach shoulder height. Lower the dumbbells back to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Front Dumbbell Raises','Front dumbbell raises primarily target the anterior deltoids. This exercise helps to build strength and size in the front portion of the shoulders, contributing to well-rounded and proportionate shoulder development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FFront%20Dumbbell%20Raises.webm?alt=media&token=8f8bf6d5-07c5-45a6-aca2-043b15dde151&_gl=1*9g3k62*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzU1MS4xMC4wLjA.',NULL),(46,'Deltoids',NULL,'Kettlebell','Steps: Stand with your feet shoulder-width apart, holding a kettlebell with both hands in front of you. Hinge at your hips and swing the kettlebell between your legs. Thrust your hips forward to swing the kettlebell up to shoulder height. Control the swing back down between your legs. Recommended Repetitions: 15-20 reps for 3-4 sets.','Kettlebell Swings','Kettlebell swings are a dynamic and high-intensity exercise that primarily targets the glutes, hamstrings, and core. This exercise helps to improve overall lower body and core strength, endurance, and power.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FKettlebell%20Swings.webm?alt=media&token=64eaf9e9-6602-4900-8032-37a1a7bcd227&_gl=1*8j51jp*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzU2Mi42MC4wLjA.',NULL),(47,'Deltoids',NULL,'Landmine apparatus and a barbell','Steps: Stand with the end of a barbell anchored in a landmine or a corner, holding the other end of the barbell at shoulder height. Press the barbell overhead until your arm is fully extended. Lower the barbell back to shoulder height. Recommended Repetitions: 8-12 reps for 3-4 sets.','Landmine Press','The landmine press is a compound exercise that primarily targets the deltoid muscles, particularly the anterior and lateral heads. This exercise helps to develop overall shoulder strength and size, promoting well-rounded and proportionate shoulder development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FLandmine%20Press.webm?alt=media&token=355de5a3-b3d5-4126-81ce-1925b707db15&_gl=1*1xjlahh*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzU3Mi41MC4wLjA.',NULL),(48,'Deltoids',NULL,'Dumbbells','Steps: Stand with your feet shoulder-width apart, holding a dumbbell in each hand by your sides. Raise the dumbbells out to your sides until they reach shoulder height. Lower the dumbbells back to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Lateral Raise','Lateral raises primarily target the lateral deltoids. This exercise helps to build strength and size in the middle portion of the shoulders, contributing to well-rounded and proportionate shoulder development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FLateral%20Raise.webm?alt=media&token=c450b8a0-069b-46db-bec9-dfc737ceb82a&_gl=1*m890of*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzU4My4zOS4wLjA.',NULL),(49,'Deltoids',NULL,'Dumbbells or a barbell','Steps: Stand with your feet shoulder-width apart, holding a barbell or dumbbells at shoulder height. Press the weight overhead until your arms are fully extended. Lower the weight back to shoulder height. Recommended Repetitions: 8-12 reps for 3-4 sets.','Overhead Shoulder Press','The overhead shoulder press is a compound exercise that primarily targets the deltoid muscles, particularly the anterior head. This exercise helps to develop overall shoulder strength and size, promoting well-rounded and proportionate shoulder development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FOverhead%20Shoulder%20Press.webm?alt=media&token=fcb944ea-58ae-498a-8247-f3b2c3369e64&_gl=1*3ve97h*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzYwMy4xOS4wLjA.',NULL),(50,'Deltoids',NULL,'Dumbbells and a bench','Steps: Bend forward at your hips with a slight bend in your knees, holding a dumbbell in each hand below your chest. Raise the dumbbells out to your sides until your arms are parallel to the ground. Lower the dumbbells back to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Reverse Dumbbell Flyes','Reverse dumbbell flies primarily target the posterior deltoids and the upper back muscles. This exercise helps to build overall shoulder and upper back strength, promoting well-rounded and proportionate shoulder development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FReverse%20Dumbbell%20Flyes.webm?alt=media&token=4d7116a2-1f08-47fb-9207-6688d0788ef5&_gl=1*1mqjxmg*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzYxNy41LjAuMA..',NULL),(51,'Deltoids',NULL,'Dumbbells or barbell','Steps: Stand with your feet shoulder-width apart, holding a barbell or dumbbells by your sides. Shrug your shoulders up towards your ears, then lower them back down. Recommended Repetitions: 12-15 reps for 3-4 sets.','Shrugs','Shrugs primarily target the trapezius muscles, particularly the upper portion. This exercise helps to build overall upper back and shoulder strength, promoting well-rounded and proportionate shoulder and upper back development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FShrugs.webm?alt=media&token=3a331a5e-aaa3-4c1f-80b8-e59549d85e4e&_gl=1*1u4v3rl*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzYzMC42MC4wLjA.',NULL),(52,'Deltoids',NULL,'Barbell and weight plates','Steps: Stand with your feet shoulder-width apart, holding a barbell with an overhand grip. Bend your knees and explosively pull the barbell up, flipping your wrists and catching the bar overhead in a squat position. Stand up to complete the movement. Recommended Repetitions: 5-8 reps for 3-4 sets.','Snatch','The snatch is a dynamic and explosive exercise that targets multiple muscle groups, including the shoulders, back, and legs. This exercise helps to improve overall power, strength, and athletic performance.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FSnatch.webm?alt=media&token=1f1f5624-fef1-46ae-bfb7-b102949af1dd&_gl=1*r103qs*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzY0Mi40OC4wLjA.',NULL),(53,'Deltoids',NULL,'Barbell and weight plates','Steps: Stand with your feet shoulder-width apart, holding a barbell or dumbbells in front of your thighs with an overhand grip. Pull the weight up towards your chin, keeping it close to your body. Lower the weight back down to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Upright Row','The upright row primarily targets the trapezius and deltoid muscles. This exercise helps to build overall shoulder and upper back strength, promoting well-rounded and proportionate shoulder and upper back development.','1','Deltoid muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FVai_Deltoid%2FUpright%20Row.webm?alt=media&token=c7314f74-fded-4750-9c57-94e607341b98&_gl=1*118lkuv*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0MzcyMi42MC4wLjA.',NULL),(54,'Lat',NULL,'Back extension bench or Roman chair','Steps: Lie face down on a back extension bench with your hips resting on the pad and your feet secured. Cross your arms over your chest or place your hands behind your head. Lift your upper body off the bench by extending your spine. Lower your body back down to the starting position. Recommended Repetitions: 12-15 reps for 3-4 sets.','Back Extension','Back extensions primarily target the erector spinae muscles in the lower back. This exercise helps to strengthen the muscles of the lower back and improve spinal stability, contributing to better posture and reduced risk of back injuries.','1','Lat muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FXo_Lats%2FBack%20Extension.webm?alt=media&token=94981ba9-e981-4b1e-a262-167aab078c98&_gl=1*11zoggl*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mzc0NC4zOC4wLjA.',NULL),(55,'Lat',NULL,'Barbell and weight plates','Steps: Stand with your feet shoulder-width apart, holding a barbell with an overhand grip. Bend at your hips and knees while keeping your back straight. Pull the barbell up towards your lower chest, squeezing your shoulder blades together. Lower the barbell back down to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Barbell Row','The barbell row is an effective compound exercise that primarily targets the muscles of the back, including the latissimus dorsi, rhomboids, and trapezius. It also engages the muscles of the arms, including the biceps and forearms. This exercise helps to build overall back strength and improve posture.','1','Lat muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FXo_Lats%2FBarbell%20Row.webm?alt=media&token=7e106c82-45ab-4598-8afc-0fe4108b8dd0&_gl=1*1sr0zod*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mzc0Ny4zNS4wLjA.',NULL),(56,'Lat',NULL,'No equipment required','Steps: Start in a standing position, then squat down and place your hands on the floor. Kick your feet back into a plank position, perform a push-up, and then jump your feet back to your hands. Explosively jump up, reaching your arms overhead. Recommended Repetitions: 10-15 reps for 3-4 sets.','Burpee','Burpees are a full-body exercise that combines a squat, push-up, and vertical jump. This exercise helps to improve cardiovascular endurance, overall strength, and explosive power. Burpees are effective for promoting fat loss and improving overall fitness.','1','Lat muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FXo_Lats%2FBurpee.webm?alt=media&token=50a84514-b9fb-404c-bf30-0dd81805b547&_gl=1*1vkm8pc*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mzc0OS4zMy4wLjA.',NULL),(57,'Lat',NULL,'Dumbbell and a flat bench','Steps: Lie on a bench with only your upper back and shoulders supported, holding a dumbbell with both hands above your chest. Lower the dumbbell back behind your head while keeping a slight bend in your elbows. Pull the dumbbell back up to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Dumbbell Pullover','Dumbbell pullovers primarily target the muscles of the upper back and chest, including the latissimus dorsi and pectoralis major. This exercise helps to strengthen the back and chest muscles, contributing to improved upper body strength and stability.','1','Lat muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FXo_Lats%2FDumbbell%20Pullover.webm?alt=media&token=568e4c7a-1829-4449-b905-c7bee7c990f2&_gl=1*1emf2k3*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mzc1My4yOS4wLjA.',NULL),(58,'Lat',NULL,'Dumbbells and a flat bench','Steps: Place one knee and hand on a flat bench, holding a dumbbell in your opposite hand. Keep your back straight and pull the dumbbell up to your hip, focusing on squeezing your shoulder blade. Lower the dumbbell back down to the starting position. Recommended Repetitions: 8-12 reps for each arm, 3-4 sets in total.','Dumbbell Row','Dumbbell rows primarily target the muscles of the back, including the latissimus dorsi, rhomboids, and trapezius. This exercise helps to build overall back strength and improve posture, contributing to better upper body stability and muscular development.','1','Lat muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FXo_Lats%2FDumbbell%20Row.webm?alt=media&token=462318e3-b29d-4fa9-b2bd-0c932d2dbbe7&_gl=1*r70ws*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mzc1NS4yNy4wLjA.',NULL),(59,'Lat',NULL,'Barbell and weight plates','Steps: Start with a barbell at thigh level. Pull the barbell up explosively, catching it at shoulder height in a squat position. Stand up to complete the movement. Recommended Repetitions: 5-8 reps for 3-4 sets.','Hang Clean','The hang clean is a dynamic and explosive exercise that primarily targets the muscles of the lower body, including the quadriceps, hamstrings, and glutes, as well as the shoulders and upper back. This exercise helps to improve overall power, strength, and athletic performance.','1','Lat muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FXo_Lats%2FHang%20Clean.webm?alt=media&token=e8888d25-4ace-4b30-8e1c-40cb94febb6d&_gl=1*gn15no*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mzc1OC4yNC4wLjA.',NULL),(60,'Lat',NULL,'Cable pull-down machine','Steps: Sit at a lat pull-down machine with your knees secured under the pads. Grasp the bar with an overhand grip and pull it down towards your chest. Slowly release the bar back up to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Pull-Down','The pull-down exercise primarily targets the latissimus dorsi muscles, as well as the muscles of the arms, including the biceps and forearms. This exercise helps to build overall back and arm strength, contributing to improved posture and upper body stability.','1','Lat muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FXo_Lats%2FPull-Down.webm?alt=media&token=d2f356e4-5a4d-47d6-81b9-834eb9b0bfe1&_gl=1*coac7n*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mzc2MS4yMS4wLjA.',NULL),(61,'Lat',NULL,'Pull-up bar or assisted pull-up/dip machine','Steps: Grip a pull-up bar with your palms facing away from you and hands slightly wider than shoulder-width apart. Hang from the bar with your arms fully extended. Pull your body up until your chin is over the bar. Lower yourself back to the starting position with control. Recommended Repetitions: 5-10 reps for 3-4 sets.','Pull-up','Pull-ups primarily target the upper back muscles, specifically the latissimus dorsi, as well as the biceps and forearms. They are effective for building upper body and back strength, improving grip strength, and promoting overall muscular development in the upper body.','1','Lat muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FXo_Lats%2FPull-up.webm?alt=media&token=d7feb767-ae2e-4d59-a874-6b7a603d5519&_gl=1*1j0iyzc*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mzc2NC4xOC4wLjA.',NULL),(62,'Lat',NULL,'No equipment required','Steps: Start in a high plank position with your hands slightly wider than shoulder-width apart. Lower your body by bending your elbows until your chest nearly touches the floor. Push yourself back up to the starting position. Recommended Repetitions: 10-15 reps for 3-4 sets.','Push-Up','Push-ups are a versatile bodyweight exercise that primarily target the pectoralis major and triceps. They are effective for building upper body strength and endurance, improving overall muscular definition, and promoting functional movement and stability.','1','Lat muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FXo_Lats%2FPush-Up.webm?alt=media&token=74208b79-bfa2-4379-ad1b-98ae087ddd5f&_gl=1*14b8zmx*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mzc2OC4xNC4wLjA.',NULL),(63,'Lat',NULL,'Cable row machine','Steps: Sit at a cable row machine with your feet placed on the footrests and knees slightly bent. Grasp the cable attachment with a close grip and pull it towards your abdomen, squeezing your shoulder blades together. Slowly release the cable back to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','Seated Close Grip Cable Row','The seated close grip cable row primarily targets the middle back muscles, including the rhomboids and latissimus dorsi. This exercise is effective for building overall back strength and improving posture and stability.','1','Lat muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FXo_Lats%2FSeated%20Close%20Grip%20Cable%20Row.webm?alt=media&token=bff9497b-b33b-41e7-a820-aaa9e1447e23&_gl=1*kmm4lf*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mzc3Mi4xMC4wLjA.',NULL),(64,'Lat',NULL,'T-bar row machine and handle attachment.','Steps: Straddle the T-bar machine and grip the handles with both hands. Keep your back straight and pull the handles up towards your abdomen, squeezing your shoulder blades together. Lower the handles back down to the starting position. Recommended Repetitions: 8-12 reps for 3-4 sets.','T-Bar Row','The T-bar row is a compound exercise that primarily targets the middle back muscles, including the rhomboids, latissimus dorsi, and trapezius. It also engages the biceps and forearms. This exercise helps to build overall back strength and improve posture and stability.','1','Lat muscle development',NULL,'https://firebasestorage.googleapis.com/v0/b/capstone1-80e4d.appspot.com/o/video%2FXo_Lats%2FT-Bar%20Row.webm?alt=media&token=7170869a-fee2-4d90-9272-802c9acb9c87&_gl=1*vt5qym*_ga*MTgzNDc0NTM1MC4xNjk5NDM5MDY2*_ga_CW55HF8NVT*MTY5OTQzOTA2NS4xLjEuMTY5OTQ0Mzc3NS43LjAuMA..',NULL);
/*!40000 ALTER TABLE `exercise` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exercise_day`
--

DROP TABLE IF EXISTS `exercise_day`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exercise_day` (
  `exercise_day_id` int NOT NULL AUTO_INCREMENT,
  `is_completed` bit(1) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `day_id` int DEFAULT NULL,
  `exercise_id` int DEFAULT NULL,
  PRIMARY KEY (`exercise_day_id`),
  KEY `FKrwo1ucbanvdgjo6b690cvucuc` (`day_id`),
  KEY `FKkv7l551lxrgu7e2xud9lbf6x1` (`exercise_id`),
  CONSTRAINT `FKkv7l551lxrgu7e2xud9lbf6x1` FOREIGN KEY (`exercise_id`) REFERENCES `exercise` (`exercise_id`),
  CONSTRAINT `FKrwo1ucbanvdgjo6b690cvucuc` FOREIGN KEY (`day_id`) REFERENCES `days` (`day_id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exercise_day`
--

LOCK TABLES `exercise_day` WRITE;
/*!40000 ALTER TABLE `exercise_day` DISABLE KEYS */;
INSERT INTO `exercise_day` VALUES (1,NULL,NULL,1,1),(2,NULL,NULL,1,2),(3,NULL,NULL,1,3),(4,NULL,NULL,1,4),(5,NULL,NULL,1,5),(6,NULL,NULL,1,6),(7,NULL,NULL,1,7),(8,NULL,NULL,2,8),(9,NULL,NULL,2,9),(10,NULL,NULL,2,10),(11,NULL,NULL,2,11),(12,NULL,NULL,2,12),(13,NULL,NULL,2,13),(14,NULL,NULL,3,14),(15,NULL,NULL,3,15),(16,NULL,NULL,3,16),(17,NULL,NULL,4,17),(18,NULL,NULL,4,18),(19,NULL,NULL,4,19),(20,NULL,NULL,4,20),(21,NULL,NULL,4,21),(22,NULL,NULL,4,22),(23,NULL,NULL,4,23),(24,NULL,NULL,4,24),(25,NULL,NULL,4,25),(26,NULL,NULL,4,26),(27,NULL,NULL,4,27),(28,NULL,NULL,5,28),(29,NULL,NULL,5,29),(30,NULL,NULL,5,30),(31,NULL,NULL,5,31),(32,NULL,NULL,6,32),(33,NULL,NULL,6,33),(34,NULL,NULL,6,34),(35,NULL,NULL,6,35),(36,NULL,NULL,6,36),(37,NULL,NULL,6,37),(38,NULL,NULL,6,38),(39,NULL,NULL,7,39),(40,NULL,NULL,7,40),(41,NULL,NULL,7,41),(42,NULL,NULL,7,42),(43,NULL,NULL,7,43),(44,NULL,NULL,7,44),(45,NULL,NULL,7,45),(46,NULL,NULL,7,46),(47,NULL,NULL,7,47),(48,NULL,NULL,7,48),(49,NULL,NULL,7,49),(50,NULL,NULL,7,50),(51,NULL,NULL,7,51),(52,NULL,NULL,7,52),(53,NULL,NULL,7,53),(54,NULL,NULL,8,54),(55,NULL,NULL,8,55),(56,NULL,NULL,8,56),(57,NULL,NULL,8,57),(58,NULL,NULL,8,58),(59,NULL,NULL,8,59),(60,NULL,NULL,8,60),(61,NULL,NULL,8,61),(62,NULL,NULL,8,62),(63,NULL,NULL,8,63),(64,NULL,NULL,8,64);
/*!40000 ALTER TABLE `exercise_day` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nutritional`
--

DROP TABLE IF EXISTS `nutritional`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nutritional` (
  `nutritional_id` int NOT NULL AUTO_INCREMENT,
  `calories` double DEFAULT NULL,
  `carbohydrates` double DEFAULT NULL,
  `fat` double DEFAULT NULL,
  `nutritional_description` varchar(255) DEFAULT NULL,
  `nutritional_name` varchar(255) DEFAULT NULL,
  `protein` double DEFAULT NULL,
  `serving` int DEFAULT NULL,
  `nutritional_type_id` int DEFAULT NULL,
  PRIMARY KEY (`nutritional_id`),
  KEY `FKk5ohag0gh96ivmfiql0g7ce7t` (`nutritional_type_id`),
  CONSTRAINT `FKk5ohag0gh96ivmfiql0g7ce7t` FOREIGN KEY (`nutritional_type_id`) REFERENCES `nutritional_type` (`nutritional_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nutritional`
--

LOCK TABLES `nutritional` WRITE;
/*!40000 ALTER TABLE `nutritional` DISABLE KEYS */;
/*!40000 ALTER TABLE `nutritional` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nutritional_day`
--

DROP TABLE IF EXISTS `nutritional_day`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nutritional_day` (
  `nutritional_day_id` int NOT NULL AUTO_INCREMENT,
  `day_id` int DEFAULT NULL,
  `nutritional_id` int DEFAULT NULL,
  PRIMARY KEY (`nutritional_day_id`),
  KEY `FK2pltxs3jkf64f2osnntbjs7gu` (`day_id`),
  KEY `FKlqcypwmw1hwvw9keeg2xsvg4d` (`nutritional_id`),
  CONSTRAINT `FK2pltxs3jkf64f2osnntbjs7gu` FOREIGN KEY (`day_id`) REFERENCES `days` (`day_id`),
  CONSTRAINT `FKlqcypwmw1hwvw9keeg2xsvg4d` FOREIGN KEY (`nutritional_id`) REFERENCES `nutritional` (`nutritional_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nutritional_day`
--

LOCK TABLES `nutritional_day` WRITE;
/*!40000 ALTER TABLE `nutritional_day` DISABLE KEYS */;
/*!40000 ALTER TABLE `nutritional_day` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nutritional_type`
--

DROP TABLE IF EXISTS `nutritional_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nutritional_type` (
  `nutritional_type_id` int NOT NULL AUTO_INCREMENT,
  `nutritional_type_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`nutritional_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nutritional_type`
--

LOCK TABLES `nutritional_type` WRITE;
/*!40000 ALTER TABLE `nutritional_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `nutritional_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cart_id` int DEFAULT NULL,
  `is_paid` bit(1) NOT NULL,
  `tnx_ref` varchar(255) DEFAULT NULL,
  `total_amount` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_cart_details`
--

DROP TABLE IF EXISTS `payment_cart_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_cart_details` (
  `payment_id` int NOT NULL,
  `cart_details_cart_detail_id` int NOT NULL,
  PRIMARY KEY (`payment_id`,`cart_details_cart_detail_id`),
  UNIQUE KEY `UK_lf7p7hk5s4yjun5dre9uexmhc` (`cart_details_cart_detail_id`),
  CONSTRAINT `FK5rs3ca1qneo594r1nq9u0l4ab` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`id`),
  CONSTRAINT `FK9h35nwnhgdkkyrl5cdoay13rv` FOREIGN KEY (`cart_details_cart_detail_id`) REFERENCES `cart_detail` (`cart_detail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_cart_details`
--

LOCK TABLES `payment_cart_details` WRITE;
/*!40000 ALTER TABLE `payment_cart_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `payment_cart_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'ROLE_USER'),(2,'ROLE_ADMIN');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `track_data_ai`
--

DROP TABLE IF EXISTS `track_data_ai`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `track_data_ai` (
  `track_data_ai_id` int NOT NULL AUTO_INCREMENT,
  `activity_level` varchar(255) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `bmi` double DEFAULT NULL,
  `effective` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `training_goals` varchar(255) DEFAULT NULL,
  `training_history` varchar(255) DEFAULT NULL,
  `course_id` int DEFAULT NULL,
  PRIMARY KEY (`track_data_ai_id`),
  KEY `FKi0kqupvfo74brypdqr0ow4te4` (`course_id`),
  CONSTRAINT `FKi0kqupvfo74brypdqr0ow4te4` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `track_data_ai`
--

LOCK TABLES `track_data_ai` WRITE;
/*!40000 ALTER TABLE `track_data_ai` DISABLE KEYS */;
INSERT INTO `track_data_ai` VALUES (1,'Beginner',18,22,'0','Male','Bodyweight Squats','Cardio 1x/week',19),(2,'Beginner',18,22,'1','Male','Bodyweight Squats','Cardio 1x/week',19),(3,'Beginner',18,22,'1','Male','Bodyweight Squats','Cardio 1x/week',19),(4,'Beginner',18,21,'1','Male','Bodyweight Squats','Cardio 1x/week',14),(5,'Beginner',18,21,'1','Male','Bodyweight Squats','Cardio 1x/week',14),(6,'Beginner',18,21,'1','Male','Bodyweight Squats','Cardio 1x/week',14),(7,'Beginner',18,23,'0','Male','Bodyweight Squats','Cardio 1x/week',4),(8,'Beginner',18,23,'1','Male','Bodyweight Squats','Cardio 1x/week',4),(9,'Beginner',18,23,'0','Male','Bodyweight Squats','Cardio 1x/week',4),(10,'Beginner',19,21,'1','Male','Bodyweight Squats','Cardio 1x/week',9),(11,'Beginner',18,22,'0','Male','Bodyweight Squats','Cardio 1x/week',19),(12,'Beginner',18,22,'0','Male','Bodyweight Squats','Cardio 1x/week',19),(13,'Beginner',18,22,'1','Male','Bodyweight Squats','Cardio 1x/week',19),(14,'Beginner',18,21,'1','Male','Bodyweight Squats','Cardio 1x/week',14),(15,'Beginner',18,21,'1','Male','Bodyweight Squats','Cardio 1x/week',14),(16,'Beginner',18,21,'1','Male','Bodyweight Squats','Cardio 1x/week',14),(17,'Beginner',18,23,'0','Male','Bodyweight Squats','Cardio 1x/week',4),(18,'Beginner',18,23,'1','Male','Bodyweight Squats','Cardio 1x/week',4),(19,'Beginner',18,23,'0','Male','Bodyweight Squats','Cardio 1x/week',4),(20,'Beginner',19,21,'1','Male','Bodyweight Squats','Cardio 1x/week',9),(21,'Beginner',18,22,'0','Male','Bodyweight Squats','Cardio 1x/week',19),(22,'Beginner',18,22,'0','Male','Bodyweight Squats','Cardio 1x/week',19),(23,'Beginner',18,22,'1','Male','Bodyweight Squats','Cardio 1x/week',19),(24,'Beginner',18,21,'1','Male','Bodyweight Squats','Cardio 1x/week',14),(25,'Beginner',18,21,'1','Male','Bodyweight Squats','Cardio 1x/week',14),(26,'Beginner',18,21,'1','Male','Bodyweight Squats','Cardio 1x/week',14),(27,'Beginner',18,23,'0','Male','Bodyweight Squats','Cardio 1x/week',4),(28,'Beginner',18,23,'1','Male','Bodyweight Squats','Cardio 1x/week',4),(29,'Beginner',18,23,'0','Male','Bodyweight Squats','Cardio 1x/week',4),(30,'Beginner',19,21,'1','Male','Bodyweight Squats','Cardio 1x/week',9);
/*!40000 ALTER TABLE `track_data_ai` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_data_ai`
--

DROP TABLE IF EXISTS `user_data_ai`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_data_ai` (
  `user_data_id` int NOT NULL AUTO_INCREMENT,
  `activity_level` varchar(255) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `bmi` double DEFAULT NULL,
  `effective` bit(1) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `training_goals` varchar(255) DEFAULT NULL,
  `training_history` varchar(255) DEFAULT NULL,
  `customer_id` int DEFAULT NULL,
  PRIMARY KEY (`user_data_id`),
  KEY `FKa9s6e6kkiechy5rdp4pe6pjk4` (`customer_id`),
  CONSTRAINT `FKa9s6e6kkiechy5rdp4pe6pjk4` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_data_ai`
--

LOCK TABLES `user_data_ai` WRITE;
/*!40000 ALTER TABLE `user_data_ai` DISABLE KEYS */;
INSERT INTO `user_data_ai` VALUES (1,'Beginner',18,22,_binary '','Male','Bodyweight Squats','Cardio 1x/week',1),(2,'Beginner',18,21,_binary '','Male','Bodyweight Squats','Cardio 1x/week',1),(3,'Beginner',18,21,_binary '','Male','Bodyweight Squats','Cardio 1x/week',1);
/*!40000 ALTER TABLE `user_data_ai` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-24  5:10:53
