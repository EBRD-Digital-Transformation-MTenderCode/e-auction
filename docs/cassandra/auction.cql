CREATE KEYSPACE ocds
WITH replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 3
};

CREATE TABLE IF NOT EXISTS ocds.auction_calendar (
    country text,
    year int,
    month int,
    work_days set<int>,
    PRIMARY KEY ((country, year, month))
);

CREATE TABLE IF NOT EXISTS ocds.auction_slots (
	date date,
	country text,
	slot int,
	start_time time,
	end_time time,
	max_lines int,
	cpids SET<text>,
	PRIMARY KEY ((date, country), slot)
);

CREATE TABLE IF NOT EXISTS ocds.auction_schedule (
    cpid text,
    operation_id uuid,
    operation_date timestamp,
    auctions text,
    PRIMARY KEY (cpid, operation_id)
);

CREATE TABLE IF NOT EXISTS ocds.auction_start (
	cpid text,
    operation_id uuid,
    operation_date timestamp,
	tender text,
	auctions text,
	bidders text,
	PRIMARY KEY (cpid)
);

CREATE TABLE IF NOT EXISTS ocds.auction_end (
	cpid text,
    operation_id uuid,
    operation_date timestamp,
	tender text,
	auctions text,
	PRIMARY KEY (cpid)
);

INSERT INTO ocds.auction_calendar(country,year,month, work_days) VALUES ('MD', 2018, 10, {1,2,3,4,5,8,9,10,11,12,15,16,17,18,19,22,23,24,25,26,29,30,31}) IF NOT EXISTS;
INSERT INTO ocds.auction_calendar(country,year,month, work_days) VALUES ('MD', 2018, 11, {1,2,5,6,7,8,9,12,13,14,15,16,19,20,21,22,23,26,27,28,29,30}) IF NOT EXISTS;
INSERT INTO ocds.auction_calendar(country,year,month, work_days) VALUES ('MD', 2018, 12, {3,4,5,6,7,10,11,12,13,14,17,18,19,20,21,24,25,26,27,28}) IF NOT EXISTS;