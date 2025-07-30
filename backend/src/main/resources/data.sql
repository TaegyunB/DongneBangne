USE dongnae;

LOAD DATA INFILE 'C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/SeniorCenter_Seoul.csv'
INTO TABLE senior_center
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(center_name, address);

SELECT * FROM senior_center LIMIT 5;

