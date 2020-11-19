CREATE KEYSPACE auctions
WITH replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 3
};

CREATE TABLE IF NOT EXISTS auctions.calendar (
    country text,
    year int,
    month int,
    work_days set<int>,
    PRIMARY KEY ((country, year, month))
);

CREATE TABLE IF NOT EXISTS auctions.buckets (
	date date,
	country text,
	row_version int,
    api_version text,
    operation_id text,
    slots text,
    occupancy text,
	PRIMARY KEY ((date, country))
);

CREATE TABLE IF NOT EXISTS auctions.auctions (
    cpid text,
    ocid text,
    row_version int,
    api_version text,
    operation_id text,
    country text,
    status int,
    data text,
    PRIMARY KEY (cpid, ocid)
);

INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2018, 10, {1,2,3,4,5,8,9,10,11,12,15,16,17,18,19,22,23,24,25,26,29,30,31}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2018, 11, {1,2,5,6,7,8,9,12,13,14,15,16,19,20,21,22,23,26,27,28,29,30}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2018, 12, {3,4,5,6,7,10,11,12,13,14,17,18,19,20,21,24,25,26,27,28}) IF NOT EXISTS;



INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 1, {2,3,4,9,10,11,14,15,16,17,18,21,22,23,24,25,28,29,30,31}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 2, {1,4,5,6,7,8,11,12,13,14,15,18,19,20,21,22,25,26,27}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 3, {4,5,6,11,12,13,14,15,18,19,20,21,22,25,26,27,28,29}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 4, {1,2,3,4,5,8,9,10,11,12,15,16,17,18,19,22,23,24,25,26,29,30}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 5, {1,2,3,6,7,10,13,14,15,16,17,20,21,22,23,24,27,28,29,30,31}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 6, {3,4,5,6,7,10,11,12,13,14,17,18,19,20,21,24,25,26,27,28}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 7, {1,2,3,4,5,8,9,10,11,12,15,16,17,18,19,22,23,24,25,26,29,30,31}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 8, {1,2,5,6,7,8,9,12,13,14,15,16,19,20,21,22,23,28,29}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 9, {3,4,5,6,9,10,11,12,13,16,17,18,19,20,23,24,25,26,27,30}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 10, {1,2,3,4,7,8,9,10,11,14,15,16,17,18,21,22,23,24,25,28,29,30,31}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 11, {1,4,5,6,7,8,11,12,13,14,15,18,19,20,21,22,25,26,27,28,29}) IF NOT EXISTS;
INSERT INTO auctions.calendar(country,year,month, work_days) VALUES ('MD', 2019, 12, {2,3,4,5,6,9,10,11,12,13,16,17,18,19,20,23,24,25,26,27,30}) IF NOT EXISTS;

CREATE TABLE IF NOT EXISTS  auctions.history (
    operation_id text,
    command text,
    operation_date timestamp,
    json_data text,
    primary key(operation_id, command)
);