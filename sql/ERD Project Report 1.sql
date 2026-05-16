CREATE TABLE `Project` (
  `project_id` int PRIMARY KEY,
  `name` varchar(255),
  `sector` varchar(255),
  `material` varchar(255),
  `fee` decimal,
  `principal_id` int,
  `project_manager_id` int,
  `location_id` int,
  `owner_id` int,
  `architect_id` int
);

CREATE TABLE `Employee` (
  `employee_id` int PRIMARY KEY,
  `first_name` varchar(255),
  `last_name` varchar(255),
  `rank_id` int
);

CREATE TABLE `Owner` (
  `owner_id` int PRIMARY KEY,
  `name` varchar(255)
);

CREATE TABLE `Architect` (
  `architect_id` int PRIMARY KEY,
  `name` varchar(255)
);

CREATE TABLE `Location` (
  `location_id` int PRIMARY KEY,
  `state` varchar(255)
);

CREATE TABLE `Project_Employee` (
  `employee_id` int,
  `project_id` int,
  `role_on_project` varchar(255),
  `man_hours` int,
  PRIMARY KEY (employee_id, project_id)
);

CREATE TABLE `Principal_License` (
  `employee_id` int,
  `location_id` int,
  PRIMARY KEY (employee_id, location_id)
);

CREATE TABLE `Employee_Rank` (
  `rank_id` int PRIMARY KEY,
  `rank_cost` decimal,
  `rank_name` varchar(255)
);

ALTER TABLE `Project` ADD FOREIGN KEY (`location_id`) REFERENCES `Location` (`location_id`);

ALTER TABLE `Project` ADD FOREIGN KEY (`owner_id`) REFERENCES `Owner` (`owner_id`);

ALTER TABLE `Project` ADD FOREIGN KEY (`architect_id`) REFERENCES `Architect` (`architect_id`);

ALTER TABLE `Project` ADD FOREIGN KEY (`principal_id`) REFERENCES `Employee` (`employee_id`);

ALTER TABLE `Project` ADD FOREIGN KEY (`project_manager_id`) REFERENCES `Employee` (`employee_id`);

ALTER TABLE `Project_Employee` ADD FOREIGN KEY (`project_id`) REFERENCES `Project` (`project_id`);

ALTER TABLE `Project_Employee` ADD FOREIGN KEY (`employee_id`) REFERENCES `Employee` (`employee_id`);

ALTER TABLE `Principal_License` ADD FOREIGN KEY (`employee_id`) REFERENCES `Employee` (`employee_id`);

ALTER TABLE `Principal_License` ADD FOREIGN KEY (`location_id`) REFERENCES `Location` (`location_id`);

ALTER TABLE `Employee` ADD FOREIGN KEY (`rank_id`) REFERENCES `Employee_Rank` (`rank_id`);
