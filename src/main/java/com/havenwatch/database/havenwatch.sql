-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 02, 2025 at 08:52 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `havenwatch`
--

-- --------------------------------------------------------

--
-- Table structure for table `alerts`
--

CREATE TABLE `alerts` (
  `alert_id` int(11) NOT NULL,
  `resident_id` int(11) DEFAULT NULL,
  `alert_type` enum('HEALTH','ENVIRONMENT','SYSTEM') NOT NULL,
  `severity` enum('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL,
  `message` text NOT NULL,
  `status` enum('ACTIVE','ACKNOWLEDGED','RESOLVED') DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `resolved_at` timestamp NULL DEFAULT NULL,
  `resolved_by` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `alerts`
--

INSERT INTO `alerts` (`alert_id`, `resident_id`, `alert_type`, `severity`, `message`, `status`, `created_at`, `resolved_at`, `resolved_by`) VALUES
(1, 1, 'HEALTH', 'MEDIUM', 'Blood pressure elevated above normal threshold', 'ACTIVE', '2025-04-08 22:43:45', NULL, NULL),
(2, 2, 'ENVIRONMENT', 'LOW', 'Room temperature below comfort threshold', 'RESOLVED', '2025-04-08 22:43:45', NULL, NULL),
(3, 3, 'HEALTH', 'HIGH', 'Missed medication schedule detected', 'ACKNOWLEDGED', '2025-04-08 22:43:45', NULL, NULL),
(4, 1, 'HEALTH', 'HIGH', 'Low heart rate detected for John Doe: 49 bpm', 'ACTIVE', '2025-04-08 22:47:11', NULL, NULL),
(5, 1, 'HEALTH', 'MEDIUM', 'Low body temperature detected for John Doe: 35.2°C', 'ACTIVE', '2025-04-08 22:47:11', NULL, NULL),
(6, 1, 'HEALTH', 'CRITICAL', 'Low blood oxygen detected for John Doe: 85%', 'ACTIVE', '2025-04-08 22:47:11', NULL, NULL),
(7, 1, 'ENVIRONMENT', 'MEDIUM', 'High room temperature detected for John Doe: 34.6°C', 'ACTIVE', '2025-04-08 22:48:11', NULL, NULL),
(8, 1, 'ENVIRONMENT', 'LOW', 'Low humidity detected for John Doe: 21%', 'ACTIVE', '2025-04-08 22:48:11', NULL, NULL),
(9, 1, 'ENVIRONMENT', 'CRITICAL', 'High gas level detected for John Doe: 66%', 'ACTIVE', '2025-04-08 22:48:11', NULL, NULL),
(10, 3, 'HEALTH', 'HIGH', 'Low heart rate detected for Robert Smith: 48 bpm', 'ACTIVE', '2025-07-01 19:00:04', NULL, NULL),
(11, 3, 'HEALTH', 'HIGH', 'High body temperature detected for Robert Smith: 38.5°C', 'ACTIVE', '2025-07-01 19:00:04', NULL, NULL),
(12, 1, 'ENVIRONMENT', 'MEDIUM', 'High room temperature detected for John Doe: 30.7°C', 'ACTIVE', '2025-07-01 19:02:04', NULL, NULL),
(13, 3, 'HEALTH', 'HIGH', 'Low heart rate detected for Robert Smith: 46 bpm', 'ACTIVE', '2025-07-01 19:02:04', NULL, NULL),
(14, 3, 'HEALTH', 'HIGH', 'High body temperature detected for Robert Smith: 38.1°C', 'ACTIVE', '2025-07-01 19:02:04', NULL, NULL),
(15, 3, 'HEALTH', 'CRITICAL', 'Low blood oxygen detected for Robert Smith: 90%', 'ACTIVE', '2025-07-01 19:02:04', NULL, NULL),
(16, 3, 'HEALTH', 'HIGH', 'High heart rate detected for Robert Smith: 112 bpm', 'ACTIVE', '2025-07-01 19:03:04', NULL, NULL),
(17, 3, 'HEALTH', 'MEDIUM', 'Low body temperature detected for Robert Smith: 35.2°C', 'ACTIVE', '2025-07-01 19:03:04', NULL, NULL),
(18, 3, 'HEALTH', 'MEDIUM', 'High glucose level detected for Robert Smith: 208 mg/dL', 'ACTIVE', '2025-07-01 19:03:04', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `environment_data`
--

CREATE TABLE `environment_data` (
  `environment_id` int(11) NOT NULL,
  `resident_id` int(11) DEFAULT NULL,
  `room_temperature` decimal(4,1) DEFAULT NULL,
  `humidity` int(11) DEFAULT NULL,
  `air_quality` int(11) DEFAULT NULL,
  `gas_level` int(11) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `environment_data`
--

INSERT INTO `environment_data` (`environment_id`, `resident_id`, `room_temperature`, `humidity`, `air_quality`, `gas_level`, `timestamp`) VALUES
(1, 1, 22.5, 45, 85, 5, '2025-04-08 22:43:45'),
(2, 1, 22.8, 47, 82, 4, '2025-04-08 22:43:45'),
(3, 1, 23.0, 46, 84, 6, '2025-04-08 22:43:45'),
(4, 1, 22.6, 44, 94, 5, '2025-04-08 22:44:11'),
(5, 2, 20.5, 60, 80, 8, '2025-04-08 22:44:11'),
(6, 3, 20.8, 45, 86, 17, '2025-04-08 22:44:11'),
(7, 1, 22.7, 42, 80, 19, '2025-04-08 22:45:11'),
(8, 2, 20.6, 42, 72, 19, '2025-04-08 22:45:11'),
(9, 3, 20.7, 45, 91, 8, '2025-04-08 22:45:11'),
(10, 1, 22.3, 43, 76, 14, '2025-04-08 22:46:11'),
(11, 2, 20.2, 49, 71, 15, '2025-04-08 22:46:11'),
(12, 3, 20.5, 57, 94, 18, '2025-04-08 22:46:11'),
(13, 1, 22.4, 55, 94, 19, '2025-04-08 22:47:11'),
(14, 2, 19.9, 52, 92, 1, '2025-04-08 22:47:11'),
(15, 3, 20.0, 43, 87, 16, '2025-04-08 22:47:11'),
(16, 1, 34.6, 21, 81, 66, '2025-04-08 22:48:11'),
(17, 2, 19.9, 54, 89, 12, '2025-04-08 22:48:11'),
(18, 3, 19.9, 43, 76, 8, '2025-04-08 22:48:11'),
(19, 1, 25.0, 46, 79, 15, '2025-07-01 18:57:04'),
(20, 2, 20.2, 54, 70, 6, '2025-07-01 18:57:04'),
(21, 3, 20.3, 55, 77, 16, '2025-07-01 18:57:04'),
(22, 1, 25.0, 57, 79, 3, '2025-07-01 18:58:04'),
(23, 2, 20.5, 55, 91, 3, '2025-07-01 18:58:04'),
(24, 3, 20.5, 47, 91, 11, '2025-07-01 18:58:04'),
(25, 1, 25.0, 52, 74, 1, '2025-07-01 18:59:04'),
(26, 2, 20.5, 54, 86, 20, '2025-07-01 18:59:04'),
(27, 3, 20.2, 49, 83, 16, '2025-07-01 18:59:04'),
(28, 1, 24.8, 43, 94, 2, '2025-07-01 19:00:04'),
(29, 2, 21.0, 47, 78, 18, '2025-07-01 19:00:04'),
(30, 3, 20.6, 57, 86, 18, '2025-07-01 19:00:04'),
(31, 1, 25.0, 43, 77, 11, '2025-07-01 19:01:04'),
(32, 2, 21.2, 60, 77, 7, '2025-07-01 19:01:04'),
(33, 3, 21.0, 48, 86, 17, '2025-07-01 19:01:04'),
(34, 1, 30.7, 46, 70, 14, '2025-07-01 19:02:04'),
(35, 2, 21.0, 60, 80, 7, '2025-07-01 19:02:04'),
(36, 3, 21.2, 57, 71, 6, '2025-07-01 19:02:04'),
(37, 1, 25.0, 50, 77, 1, '2025-07-01 19:03:04'),
(38, 2, 21.4, 45, 91, 1, '2025-07-01 19:03:04'),
(39, 3, 20.8, 58, 90, 0, '2025-07-01 19:03:04'),
(40, 1, 25.0, 44, 73, 5, '2025-07-01 19:04:04'),
(41, 2, 21.4, 52, 81, 17, '2025-07-01 19:04:04'),
(42, 3, 20.4, 54, 73, 19, '2025-07-01 19:04:04');

-- --------------------------------------------------------

--
-- Table structure for table `health_data`
--

CREATE TABLE `health_data` (
  `health_id` int(11) NOT NULL,
  `resident_id` int(11) DEFAULT NULL,
  `heart_rate` int(11) DEFAULT NULL,
  `blood_pressure` varchar(20) DEFAULT NULL,
  `blood_oxygen` int(11) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `health_data`
--

INSERT INTO `health_data` (`health_id`, `resident_id`, `heart_rate`, `blood_pressure`, `blood_oxygen`, `timestamp`) VALUES
(1, 1, 75, '130/85', 97, '2025-04-08 22:43:45'),
(2, 1, 72, '128/82', 98, '2025-04-08 22:43:45'),
(3, 1, 78, '135/88', 96, '2025-04-08 22:43:45'),
(4, 1, 82, '125/72', 97, '2025-04-08 22:44:11'),
(5, 2, 62, '124/87', 94, '2025-04-08 22:44:11'),
(6, 3, 88, '119/79', 99, '2025-04-08 22:44:11'),
(7, 1, 83, '126/87', 99, '2025-04-08 22:45:11'),
(8, 2, 60, '112/79', 97, '2025-04-08 22:45:11'),
(9, 3, 86, '137/77', 94, '2025-04-08 22:45:11'),
(10, 1, 80, '113/89', 95, '2025-04-08 22:46:11'),
(11, 2, 60, '117/75', 98, '2025-04-08 22:46:11'),
(12, 3, 88, '130/83', 98, '2025-04-08 22:46:11'),
(13, 1, 49, '148/107', 85, '2025-04-08 22:47:11'),
(14, 2, 62, '136/83', 94, '2025-04-08 22:47:11'),
(15, 3, 89, '135/87', 99, '2025-04-08 22:47:11'),
(16, 1, 60, '114/76', 94, '2025-04-08 22:48:11'),
(17, 2, 64, '125/70', 94, '2025-04-08 22:48:11'),
(18, 3, 87, '113/78', 95, '2025-04-08 22:48:11'),
(19, 1, 61, '140/72', 98, '2025-07-01 18:57:04'),
(20, 2, 64, '114/80', 99, '2025-07-01 18:57:04'),
(21, 3, 89, '128/90', 99, '2025-07-01 18:57:04'),
(22, 1, 60, '132/80', 99, '2025-07-01 18:58:04'),
(23, 2, 60, '129/81', 99, '2025-07-01 18:58:04'),
(24, 3, 86, '116/90', 95, '2025-07-01 18:58:04'),
(25, 1, 63, '111/90', 94, '2025-07-01 18:59:04'),
(26, 2, 60, '128/78', 99, '2025-07-01 18:59:04'),
(27, 3, 83, '111/75', 94, '2025-07-01 18:59:04'),
(28, 1, 60, '127/71', 96, '2025-07-01 19:00:04'),
(29, 2, 60, '137/84', 95, '2025-07-01 19:00:04'),
(30, 3, 48, '128/81', 94, '2025-07-01 19:00:04'),
(31, 1, 63, '114/73', 96, '2025-07-01 19:01:04'),
(32, 2, 65, '111/73', 97, '2025-07-01 19:01:04'),
(33, 3, 60, '114/84', 94, '2025-07-01 19:01:04'),
(34, 1, 60, '137/80', 97, '2025-07-01 19:02:04'),
(35, 2, 61, '123/79', 97, '2025-07-01 19:02:04'),
(36, 3, 46, '172/105', 90, '2025-07-01 19:02:04'),
(37, 1, 60, '117/90', 96, '2025-07-01 19:03:04'),
(38, 2, 63, '126/81', 98, '2025-07-01 19:03:04'),
(39, 3, 112, '125/74', 97, '2025-07-01 19:03:04'),
(40, 1, 60, '137/85', 99, '2025-07-01 19:04:04'),
(41, 2, 68, '117/75', 96, '2025-07-01 19:04:04'),
(42, 3, 100, '138/90', 97, '2025-07-01 19:04:04');

-- --------------------------------------------------------

--
-- Table structure for table `residents`
--

CREATE TABLE `residents` (
  `resident_id` int(11) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `date_of_birth` date NOT NULL,
  `gender` enum('MALE','FEMALE','OTHER') NOT NULL,
  `address` varchar(200) DEFAULT NULL,
  `emergency_contact` varchar(100) DEFAULT NULL,
  `emergency_phone` varchar(20) DEFAULT NULL,
  `medical_conditions` text DEFAULT NULL,
  `medications` text DEFAULT NULL,
  `allergies` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `residents`
--

INSERT INTO `residents` (`resident_id`, `first_name`, `last_name`, `date_of_birth`, `gender`, `address`, `emergency_contact`, `emergency_phone`, `medical_conditions`, `medications`, `allergies`, `created_at`) VALUES
(1, 'John', 'Doe', '1945-05-15', 'MALE', '123 Main St, Anytown', 'Jane Doe (Daughter)', '555-2001', 'Hypertension, Diabetes Type 2', 'Metformin 500mg, Lisinopril 10mg', 'Penicillin', '2025-04-08 22:43:45'),
(2, 'Martha', 'Johnson', '1940-08-22', 'FEMALE', '456 Oak Ave, Anytown', 'Robert Johnson (Son)', '555-2002', 'Arthritis, Heart Disease', 'Aspirin 81mg, Atorvastatin 20mg', 'Sulfa drugs', '2025-04-08 22:43:45'),
(3, 'Robert', 'Smith', '1938-11-10', 'MALE', '789 Pine Rd, Anytown', 'Susan Smith (Wife)', '555-2003', 'COPD, Glaucoma', 'Albuterol, Timolol eye drops', 'None', '2025-04-08 22:43:45');

-- --------------------------------------------------------

--
-- Table structure for table `resident_notes`
--

CREATE TABLE `resident_notes` (
  `note_id` int(11) NOT NULL,
  `resident_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `note` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `resident_notes`
--

INSERT INTO `resident_notes` (`note_id`, `resident_id`, `user_id`, `note`, `created_at`) VALUES
(1, 1, 2, 'John was feeling tired today. Recommended more rest and fluids.', '2025-04-08 22:43:45'),
(2, 2, 3, 'Adjusted Martha\'s medication schedule. Follow up in one week.', '2025-04-08 22:43:45'),
(3, 3, 2, 'Robert had a good day today. No issues reported.', '2025-04-08 22:43:45');

-- --------------------------------------------------------

--
-- Table structure for table `settings`
--

CREATE TABLE `settings` (
  `setting_id` int(11) NOT NULL,
  `setting_name` varchar(50) NOT NULL,
  `setting_value` text NOT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `settings`
--

INSERT INTO `settings` (`setting_id`, `setting_name`, `setting_value`, `user_id`) VALUES
(1, 'ALERT_FREQUENCY', '15', NULL),
(2, 'DASHBOARD_REFRESH', '30', NULL),
(3, 'NOTIFICATION_EMAIL', 'yes', 2),
(4, 'NOTIFICATION_SMS', 'no', 2);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `role` enum('ADMIN','CAREGIVER','FAMILY','HEALTHCARE') NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `role`, `full_name`, `email`, `phone`, `created_at`) VALUES
(1, 'admin', 'admin123', 'ADMIN', 'Admin User', 'admin@havenwatch.com', '555-1000', '2025-04-08 22:43:45'),
(2, 'caregiver', 'caregiver123', 'CAREGIVER', 'Care Giver', 'caregiver@havenwatch.com', '555-1001', '2025-04-08 22:43:45'),
(3, 'doctor', 'doctor123', 'HEALTHCARE', 'Dr. Smith', 'doctor@havenwatch.com', '555-1002', '2025-04-08 22:43:45'),
(4, 'family', 'family123', 'FAMILY', 'Family Member', 'family@havenwatch.com', '555-1003', '2025-04-08 22:43:45');

-- --------------------------------------------------------

--
-- Table structure for table `user_resident_access`
--

CREATE TABLE `user_resident_access` (
  `user_id` int(11) NOT NULL,
  `resident_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_resident_access`
--

INSERT INTO `user_resident_access` (`user_id`, `resident_id`) VALUES
(1, 1),
(1, 2),
(1, 3),
(2, 1),
(2, 2),
(3, 1),
(3, 3),
(4, 2);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `alerts`
--
ALTER TABLE `alerts`
  ADD PRIMARY KEY (`alert_id`),
  ADD KEY `resident_id` (`resident_id`),
  ADD KEY `resolved_by` (`resolved_by`);

--
-- Indexes for table `environment_data`
--
ALTER TABLE `environment_data`
  ADD PRIMARY KEY (`environment_id`),
  ADD KEY `resident_id` (`resident_id`);

--
-- Indexes for table `health_data`
--
ALTER TABLE `health_data`
  ADD PRIMARY KEY (`health_id`),
  ADD KEY `resident_id` (`resident_id`);

--
-- Indexes for table `residents`
--
ALTER TABLE `residents`
  ADD PRIMARY KEY (`resident_id`);

--
-- Indexes for table `resident_notes`
--
ALTER TABLE `resident_notes`
  ADD PRIMARY KEY (`note_id`),
  ADD KEY `resident_id` (`resident_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `settings`
--
ALTER TABLE `settings`
  ADD PRIMARY KEY (`setting_id`),
  ADD UNIQUE KEY `setting_name` (`setting_name`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `user_resident_access`
--
ALTER TABLE `user_resident_access`
  ADD PRIMARY KEY (`user_id`,`resident_id`),
  ADD KEY `resident_id` (`resident_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `alerts`
--
ALTER TABLE `alerts`
  MODIFY `alert_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `environment_data`
--
ALTER TABLE `environment_data`
  MODIFY `environment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

--
-- AUTO_INCREMENT for table `health_data`
--
ALTER TABLE `health_data`
  MODIFY `health_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

--
-- AUTO_INCREMENT for table `residents`
--
ALTER TABLE `residents`
  MODIFY `resident_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `resident_notes`
--
ALTER TABLE `resident_notes`
  MODIFY `note_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `settings`
--
ALTER TABLE `settings`
  MODIFY `setting_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `alerts`
--
ALTER TABLE `alerts`
  ADD CONSTRAINT `alerts_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`resident_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `alerts_ibfk_2` FOREIGN KEY (`resolved_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `environment_data`
--
ALTER TABLE `environment_data`
  ADD CONSTRAINT `environment_data_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`resident_id`) ON DELETE CASCADE;

--
-- Constraints for table `health_data`
--
ALTER TABLE `health_data`
  ADD CONSTRAINT `health_data_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`resident_id`) ON DELETE CASCADE;

--
-- Constraints for table `resident_notes`
--
ALTER TABLE `resident_notes`
  ADD CONSTRAINT `resident_notes_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`resident_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `resident_notes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `settings`
--
ALTER TABLE `settings`
  ADD CONSTRAINT `settings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `user_resident_access`
--
ALTER TABLE `user_resident_access`
  ADD CONSTRAINT `user_resident_access_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `user_resident_access_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`resident_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
