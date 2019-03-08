-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema jdbc_project
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema jdbc_project
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `jdbc_project` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `jdbc_project` ;

-- -----------------------------------------------------
-- Table `jdbc_project`.`flight`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `jdbc_project`.`flight` (
  `fl_id` INT(11) NOT NULL AUTO_INCREMENT,
  `Flight_Name` VARCHAR(20) NOT NULL,
  `ArrivingFrom` VARCHAR(20) NOT NULL,
  `ArrivalTime` TIME NOT NULL,
  `Departure` VARCHAR(20) NULL DEFAULT NULL,
  `DepartureTime` TIME NOT NULL,
  `Fare` DECIMAL(7,2) NOT NULL,
  `Passenger_Travelling` INT(11) NOT NULL,
  `SeatsLeft` INT(11) NOT NULL,
  PRIMARY KEY (`fl_id`))
ENGINE = InnoDB
AUTO_INCREMENT = 22
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

CREATE UNIQUE INDEX `fl_id_unq` ON `jdbc_project`.`flight` (`fl_id` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `jdbc_project`.`flight_audit`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `jdbc_project`.`flight_audit` (
  `fl_id` INT(11) NOT NULL,
  `Flight_Name` VARCHAR(20) NOT NULL,
  `action_type` VARCHAR(50) NULL DEFAULT NULL,
  `action_date` DATETIME NOT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

USE `jdbc_project` ;

-- -----------------------------------------------------
-- Placeholder table for view `jdbc_project`.`flight_view`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `jdbc_project`.`flight_view` (`Flight_Name` INT, `ArrivingFrom` INT, `ArrivalTime` INT);

-- -----------------------------------------------------
-- function calculate_earning
-- -----------------------------------------------------

DELIMITER $$
USE `jdbc_project`$$
CREATE DEFINER=`root`@`localhost` FUNCTION `calculate_earning`(
  flname_param  VARCHAR(20)
) RETURNS decimal(10,0)
BEGIN
  DECLARE earn_var DECIMAL; 
	select sum(Fare*Passenger_Travelling)
	into earn_var
	from flight
	where Flight_Name = flname_param;
 RETURN earn_var;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure update_flight
-- -----------------------------------------------------

DELIMITER $$
USE `jdbc_project`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `update_flight`(
    fl_id_param INTEGER,
	Flight_Name_param VARCHAR(20),
	ArrivingFrom_param VARCHAR(20),
    ArrivalTime_param TIME(0),
	Departure_param VARCHAR(20),
	DepartureTime_param TIME(0),
	Fare_param DECIMAL(7,2),
    Passenger_Travelling_param INTEGER,
	SeatsLeft_param INTEGER
)
BEGIN
DECLARE sql_error TINYINT DEFAULT FALSE;
  
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
SET sql_error = TRUE;
    
START TRANSACTION;  
UPDATE flight
  SET Flight_Name = Flight_Name_param,
  ArrivingFrom = ArrivingFrom_param,
  ArrivalTime = ArrivalTime_param,
  Departure = Departure_param,
  DepartureTime = DepartureTime_param,
  Fare = Fare_param,
  Passenger_Travelling = Passenger_Travelling_param,
  SeatsLeft = SeatsLeft_param
  WHERE fl_id = fl_id_param;
  
IF sql_error = FALSE THEN
    COMMIT;
  ELSE
    ROLLBACK;
  END IF;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- View `jdbc_project`.`flight_view`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `jdbc_project`.`flight_view`;
USE `jdbc_project`;
CREATE  OR REPLACE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `jdbc_project`.`flight_view` AS select `jdbc_project`.`flight`.`Flight_Name` AS `Flight_Name`,`jdbc_project`.`flight`.`ArrivingFrom` AS `ArrivingFrom`,`jdbc_project`.`flight`.`ArrivalTime` AS `ArrivalTime` from `jdbc_project`.`flight`;
USE `jdbc_project`;

DELIMITER $$
USE `jdbc_project`$$
CREATE
DEFINER=`root`@`localhost`
TRIGGER `jdbc_project`.`flight_after_insert`
AFTER INSERT ON `jdbc_project`.`flight`
FOR EACH ROW
BEGIN
    INSERT INTO flight_audit VALUES
    (NEW.fl_id, NEW.Flight_Name, "INSERTED", NOW());
    
END$$

USE `jdbc_project`$$
CREATE
DEFINER=`root`@`localhost`
TRIGGER `jdbc_project`.`flight_AFTER_UPDATE`
AFTER UPDATE ON `jdbc_project`.`flight`
FOR EACH ROW
BEGIN
 INSERT INTO flight_audit VALUES
    (NEW.fl_id, NEW.Flight_Name, "UPDATED", NOW());
END$$

USE `jdbc_project`$$
CREATE
DEFINER=`root`@`localhost`
TRIGGER `jdbc_project`.`flight_AFTER_DELETE`
AFTER DELETE ON `jdbc_project`.`flight`
FOR EACH ROW
BEGIN

 INSERT INTO flight_audit VALUES
    (OLD.fl_id, OLD.Flight_Name, "DELETED", NOW());
END$$


DELIMITER ;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
