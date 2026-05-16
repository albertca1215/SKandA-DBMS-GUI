-- Drop existing tables if they exist (for clean reinstall)
DROP TABLE IF EXISTS Project_Employee;
DROP TABLE IF EXISTS Principal_License;
DROP TABLE IF EXISTS Project;
DROP TABLE IF EXISTS Owner;
DROP TABLE IF EXISTS Architect;
DROP TABLE IF EXISTS Employee;
DROP TABLE IF EXISTS Employee_Rank;
DROP TABLE IF EXISTS Location;

CREATE TABLE Employee_Rank (
    rank_id INT AUTO_INCREMENT PRIMARY KEY,
    rank_name VARCHAR(255) NOT NULL UNIQUE,
    rank_cost DECIMAL(10, 2) NOT NULL
);

CREATE TABLE Location (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    state VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE Employee (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    rank_id INT NOT NULL
);

CREATE TABLE Architect (
    architect_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE Owner (
    owner_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE Project (
    project_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sector VARCHAR(255) NOT NULL,
    material VARCHAR(255) NOT NULL,
    fee DECIMAL(15, 2) NOT NULL,
    location_id INT NOT NULL,
    principal_id INT NOT NULL,
    project_manager_id INT NOT NULL,
    owner_id INT NOT NULL,
    architect_id INT NOT NULL
);

CREATE TABLE Project_Employee (
    project_employee_id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    employee_id INT NOT NULL,
    man_hours DECIMAL(10, 2) NOT NULL,
    role_on_project VARCHAR(255),
    UNIQUE KEY unique_assignment (project_id, employee_id)
);

CREATE TABLE Principal_License (
    employee_id INT NOT NULL,
    location_id INT NOT NULL,
    PRIMARY KEY (employee_id, location_id)
);

ALTER TABLE Employee ADD FOREIGN KEY (rank_id) REFERENCES Employee_Rank (rank_id);

ALTER TABLE Project ADD FOREIGN KEY (location_id) REFERENCES Location (location_id);
ALTER TABLE Project ADD FOREIGN KEY (owner_id) REFERENCES Owner (owner_id);
ALTER TABLE Project ADD FOREIGN KEY (architect_id) REFERENCES Architect (architect_id);
ALTER TABLE Project ADD FOREIGN KEY (principal_id) REFERENCES Employee (employee_id);
ALTER TABLE Project ADD FOREIGN KEY (project_manager_id) REFERENCES Employee (employee_id);

ALTER TABLE Project_Employee ADD FOREIGN KEY (project_id) REFERENCES Project (project_id);
ALTER TABLE Project_Employee ADD FOREIGN KEY (employee_id) REFERENCES Employee (employee_id);

ALTER TABLE Principal_License ADD FOREIGN KEY (employee_id) REFERENCES Employee (employee_id);
ALTER TABLE Principal_License ADD FOREIGN KEY (location_id) REFERENCES Location (location_id);

INSERT INTO Employee_Rank (rank_name, rank_cost) VALUES
    ('Project Engineer 1', 75.00),
    ('Project Engineer 2', 85.00),
    ('Project Engineer 3', 95.00),
    ('Assistant Project Manager', 105.00),
    ('Project Manager', 120.00),
    ('Associate', 130.00),
    ('Principal', 150.00);

INSERT INTO Location (state) VALUES
    ('Alabama'), ('Alaska'), ('Arizona'), ('Arkansas'),
    ('California'), ('Colorado'), ('Connecticut'), ('Delaware'),
    ('District of Columbia'), ('Florida'), ('Georgia'), ('Hawaii'),
    ('Idaho'), ('Illinois'), ('Indiana'), ('Iowa'),
    ('Kansas'), ('Kentucky'), ('Louisiana'), ('Maine'),
    ('Maryland'), ('Massachusetts'), ('Michigan'), ('Minnesota'),
    ('Mississippi'), ('Missouri'), ('Montana'), ('Nebraska'),
    ('Nevada'), ('New Hampshire'), ('New Jersey'), ('New Mexico'),
    ('New York'), ('North Carolina'), ('North Dakota'), ('Ohio'),
    ('Oklahoma'), ('Oregon'), ('Pennsylvania'), ('Rhode Island'),
    ('South Carolina'), ('South Dakota'), ('Tennessee'), ('Texas'),
    ('Utah'), ('Vermont'), ('Virginia'), ('Washington'),
    ('West Virginia'), ('Wisconsin'), ('Wyoming');

-- Populate Employees, Projects, Man-Hours, and Licenses with dummy data.

INSERT INTO Architect (architect_id, name) VALUES
    (1, 'Kelly Bailey'),
    (2, 'Marcus Pisuena');

INSERT INTO Owner (owner_id, name) VALUES
    (1, 'Robert MacDonald'),
    (2, 'Loudoun County');

INSERT INTO Employee (employee_id, first_name, last_name, rank_id) VALUES
    (1, 'James', 'Borg', 7),
    (2, 'Jennifer', 'Wallace', 7),
    (3, 'Ahmad', 'Jabbar', 6),
    (4, 'Alicia', 'Zelaya', 5),
    (5, 'Franklin', 'Wong', 4),
    (6, 'Joyce', 'English', 3),
    (7, 'Ramesh', 'Narayan', 2);

INSERT INTO Project (
    project_id, name, sector, material, fee, location_id,
    principal_id, project_manager_id, owner_id, architect_id
) VALUES
    (1, 'Wellmeadows Hospital', 'Renovation', 'Steel', 25000.00, 21, 1, 3, 1, 1),
    (2, 'Pine Valley Park', 'Mixed Use', 'Concrete', 35000.00, 47, 1, 3, 2, 2);

INSERT INTO Project_Employee (project_id, employee_id, man_hours, role_on_project) VALUES
    (1, 6, 9.0, 'Engineer'),
    (1, 3, 7.0, 'PM'),
    (1, 1, 12.0, 'Principal'),
    (1, 7, 8.0, 'Engineer'),
    (2, 3, 7.0, 'PM'),
    (2, 1, 12.0, 'Principal'),
    (2, 7, 12.0, 'Engineer'),
    (2, 5, 10.0, 'Engineer');

INSERT INTO Principal_License (employee_id, location_id) VALUES
    -- James Borg
    (1, 8),
    (1, 9),
    (1, 21),
    (1, 34),
    (1, 39),
    (1, 41),
    (1, 47),
    (1, 49),
    -- Jennifer Wallace
    (2, 9),
    (2, 11),
    (2, 21),
    (2, 31),
    (2, 33),
    (2, 36),
    (2, 39),
    (2, 47);

