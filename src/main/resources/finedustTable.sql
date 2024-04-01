-- --------------------------------------------------------
-- 호스트:                          127.0.0.1
-- 서버 버전:                        10.9.8-MariaDB - mariadb.org binary distribution
-- 서버 OS:                        Win64
-- HeidiSQL 버전:                  12.3.0.6589
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- project 데이터베이스 구조 내보내기
CREATE DATABASE IF NOT EXISTS `project` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `project`;

-- 테이블 project.alarm_issued 구조 내보내기
CREATE TABLE IF NOT EXISTS `alarm_issued` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `measurement_name` varchar(50) NOT NULL,
  `warning_level` int(11) NOT NULL,
  `time` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `message` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='경보 발령 정보테이블';

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 project.check_day 구조 내보내기
CREATE TABLE IF NOT EXISTS `check_day` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `measurement_name` varchar(50) NOT NULL,
  `measurement_code` varchar(50) NOT NULL,
  `check_time` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `message` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='측정소 점검 테이블';

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 project.fine_dust 구조 내보내기
CREATE TABLE IF NOT EXISTS `fine_dust` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` datetime DEFAULT NULL,
  `measurement_name` varchar(50) DEFAULT NULL COMMENT '측정소명',
  `measurement_code` varchar(50) DEFAULT NULL COMMENT '측정코드',
  `pm10` int(11) DEFAULT NULL,
  `pm2_5` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=372002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='다운받은 미세먼지 정보 테이블';

-- 내보낼 데이터가 선택되어 있지 않습니다.

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
