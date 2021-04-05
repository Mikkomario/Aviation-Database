--
-- Database Structure for the project (v0.1)
--

CREATE DATABASE aviation_database
  DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;

USE aviation_database;

-- Hard-Coded   -----------------------------------------

-- SOURCE: OpenFlights
-- E.g.
-- Code: E
-- Name: Europe
-- Start Month: 3
-- Start Sunday Index: -1
-- End Month: 10
-- End Sunday Index: -1
-- Sunday index may be negative, where -1 means the last sunday, -2 the second last and so on
-- 0 means the first, 1 means the second and so on
-- Please note that code is the primary key in this context
CREATE TABLE daylight_saving_zone
(
	code CHAR NOT NULL PRIMARY KEY,
	name VARCHAR(32) NOT NULL,
	start_month INT NOT NULL,
	start_sunday_index INT NOT NULL,
	end_month INT NOT NULL,
	end_sunday_index INT NOT NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
-- Europe (E): Last sunday of March to last sunday of October
-- US/Canada (A): Second sunday of March to first sunday of November
-- South Africa (S): Third sunday of October to third sunday of March
-- Australia (O): First sunday of October to first sunday of April
-- New Zealand (Z): Last sunday of September to first sunday of April
INSERT INTO daylight_saving_zone (code, name, start_month, start_sunday_index, end_month, end_sunday_index) VALUES
	('E', 'Europe', 3, -1, 10, -1),
	('A', 'US/Canada', 3, 1, 11, 0),
	('S', 'South America', 10, 2, 3, 2),
	('O', 'Australia', 10, 0, 4, 0),
	('Z', 'New Zealand', 9, -1, 4, 0);

-- SOURCE: OpenFlights
-- Applicable in: airports.dat (extended)
CREATE TABLE station_type(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	open_flights_code VARCHAR(7) NOT NULL,
	name VARCHAR(32) NOT NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
-- Inserts possible values
INSERT INTO station_type (id, open_flights_code, name) VALUES
	(1, 'airport', 'Airport'),
	(2, 'station', 'Train Station'),
	(3, 'port', 'Ferry Terminal');

-- SOURCE: L_CARRIER_GROUP_NEW (BTS)
-- Overall airline / carrier categories
-- A set of categories based on annual revenue
CREATE TABLE carrier_size_category
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(32) NOT NULL,
	min_annual_revenue_million_dollars INT,
	max_annual_revenue_million_dollars INT

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
-- Values are based on L_CARRIER_GROUP_NEW (BTS) document and go as follows
-- 1 = Small = 4 = Medium Regional Carriers
-- 2 = Medium = 1 = Large Regional Carriers
-- 3 = Large = 2 = National Carriers
-- 4 = Major = 3 = Major Carriers
-- Other BTS values are not mapped
INSERT INTO carrier_size_category(id, name, min_annual_revenue_million_dollars, max_annual_revenue_million_dollars) VALUES
	(1, 'Small', NULL, 20),
	(2, 'Medium', 20, 100),
	(3, 'Large', 100, 1000),
	(4, 'Major', 1000, NULL);
-- SOURCE: L_CARRIER_GROUP_NEW (BTS)
-- Carrier / Airline categories based on type of delivery / transport
CREATE TABLE carrier_type(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(32) NOT NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
-- Values are based on L_CARRIER_GROUP_NEW document and go as follows
-- 1 = Commuter = Passenger transport (> 60 seats) = 6 & 9 = Commuter Carriers
-- 2 = All Cargo = No passengers transported, maximum payload over 18 000 pounds = 7 = All Cargo Carriers
-- 3 = Small Certified = <= 60 seats and <= 18 000 pounds of maximum payload = 5 = Small Certificated Carriers
INSERT INTO carrier_type (id, name) VALUES
	(1, 'Commuter'),
	(2, 'All Cargo'),
	(3, 'Small Certificated');


-- Dynamically Added    ----------------------------------

-- SOURCE: WAC_COUNTRY_STATE
-- Represents a major region within world (E.g. Europe / USA)
-- Notice that id is not auto-increment but matches the first digit in the associated 3-digit world area codes
CREATE TABLE world_region
(
	id INT NOT NULL PRIMARY KEY,
	name VARCHAR(64) NOT NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;

-- SOURCES: WAC_COUNTRY_STATE, countries.dat (OpenFlight), MASTER_CORD
-- Represents a single country (E.g. Finland, Germany or USA)
-- Capital id refers to the capital city in the city table (below)
CREATE TABLE country(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(64) NOT NULL,
	world_region_id INT,
	iso_code VARCHAR(2),
	dafif_code VARCHAR(2),
	capital_id INT,
	sovereignty_country_id INT,
	ended DATE,
	`comment` TEXT(510),
	independent BOOLEAN,

	INDEX c_country_iso_code_idx (iso_code),
	INDEX c_country_name_idx (name),

    CONSTRAINT c_wr_region_link_fk FOREIGN KEY c_wr_region_link_idx (world_region_id)
        REFERENCES world_region(id) ON DELETE SET NULL,

	CONSTRAINT c_c_sovereignty_link_fk FOREIGN KEY c_c_sovereignty_link_idx (sovereignty_country_id)
	    REFERENCES country(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;

-- SOURCES: WAC_COUNTRY_STATE, MASTER_CORD
-- Represents a state in the United States or Canada
CREATE TABLE `state`
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(64) NOT NULL,
	country_id INT NOT NULL,
	iso_code VARCHAR(2) NOT NULL,
	fips_code INT,
	`comment` TEXT(510),

	INDEX s_state_iso_code_idx (iso_code),

	CONSTRAINT s_c_country_link_fk FOREIGN KEY s_c_country_link_idx (country_id)
	    REFERENCES country(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;

-- SOURCE: WAC_COUNTRY_STATE, MASTER_CORD
-- A world area / WAC
-- Notice that code (not id) is the primary key and is not auto-increment
-- but matches with the associated world area code
-- Data doesn't actually contain start date but only year + month. However, date is an easier format to use.
CREATE TABLE world_area
(
	code INT NOT NULL PRIMARY KEY,
	country_id INT NOT NULL,
	state_id INT,
	name VARCHAR(64),
	started DATE,
	deprecated_after DATE,

	CONSTRAINT wa_c_country_link_fk FOREIGN KEY wa_c_country_link_idx (country_id)
	    REFERENCES country(id) ON DELETE CASCADE,

	CONSTRAINT wa_s_state_link_fk FOREIGN KEY wa_s_state_link_idx (state_id)
	    REFERENCES `state`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;

-- SOURCES: WAC_COUNTRY_STATE, MASTER_CORD, airports.dat (OpenFlights), MASTER.txt
-- Time zone is difference from UTC time in hours, but may be unknown
CREATE TABLE city(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(64) NOT NULL,
	market_id INT,
	country_id INT NOT NULL,
	state_id INT,
	world_area_code INT,
	time_zone DOUBLE,
	time_zone_name VARCHAR(32),
	daylight_saving_zone_code CHAR,

	CONSTRAINT c_c_country_link_fk FOREIGN KEY c_c_country_link_idx (country_id)
	    REFERENCES country(id) ON DELETE CASCADE,

	CONSTRAINT c_s_state_link_fk FOREIGN KEY c_s_state_link_idx (state_id)
	    REFERENCES `state`(id) ON DELETE SET NULL,

	CONSTRAINT c_wa_world_area_link_fk FOREIGN KEY c_wa_world_area_link_idx (world_area_code)
	    REFERENCES world_area(code) On DELETE SET NULL,

	CONSTRAINT c_dsz_daylight_saving_zone_link_fk FOREIGN KEY c_dsz_daylight_saving_zone_link_idx (daylight_saving_zone_code)
	    REFERENCES daylight_saving_zone(code) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;

-- Adds link from country to its capital city
ALTER TABLE country ADD CONSTRAINT c_c_capital_city_link_fk FOREIGN KEY c_c_capital_city_link_idx (capital_id)
    REFERENCES city(id) ON DELETE SET NULL;

-- SOURCES: MASTER_CORD, airports.dat (OpenFlights), airports.dat (RouteMapper)
-- Represents an airport, train station, ferry port etc.
CREATE TABLE station(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(80) NOT NULL,
	latitude_north DOUBLE NOT NULL,
    longitude_east DOUBLE NOT NULL,
    altitude_feet INT,
    type_id INT,
	dot_id INT,
	open_flights_id INT,
	iata_code VARCHAR(3),
	icao_code VARCHAR(4),
	city_id INT,
	started DATE,
	closed DATE,
	is_closed BOOLEAN NOT NULL DEFAULT FALSE,

	INDEX station_iata_code_idx (iata_code),
	INDEX station_icao_code_idx (icao_code),

	CONSTRAINT s_st_station_type_link_fk FOREIGN KEY s_st_station_type_link_idx (type_id)
	    REFERENCES station_type(id) ON DELETE SET NULL,

	CONSTRAINT s_c_station_city_link_fk FOREIGN KEY s_c_station_city_link_idx (city_id)
	    REFERENCES city(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;

-- SOURCES: CARRIER DECODE (BTS), airlines.dat (OpenFlights), airlines.dat (RouteMapper)
-- Lists carriers / airlines, including their IATA & ICAO codes, where applicable
CREATE TABLE carrier(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(96) NOT NULL,
	alias VARCHAR(32),
	call_sign VARCHAR(48),
	dot_id INT,
	open_flights_id INT,
	iata_code VARCHAR(2),
	icao_code VARCHAR(3),
	country_id INT,
	world_area_code INT,
	size_category_id INT,
	type_category_id INT,
	started DATE,
	ended DATE,
	is_closed BOOLEAN NOT NULL DEFAULT FALSE,

	INDEX c_carrier_iata_idx (iata_code),
	INDEX c_carrier_icao_idx (icao_code),

	CONSTRAINT c_c_carrier_country_fk FOREIGN KEY c_c_carrier_country_idx (country_id)
	    REFERENCES country(id) ON DELETE SET NULL,

	CONSTRAINT c_wa_carrier_world_area_code_fk FOREIGN KEY c_wa_carrier_world_area_code_idx (world_area_code)
	    REFERENCES world_area(code) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;

-- SOURCES: manufacturers.csv (BST), Order_3660.1D_Aircraft_Type_Designators (manufacturer part), ACFTREF
-- Lists aircraft manufacturers
-- alt_code is from ACTREF
CREATE TABLE aircraft_manufacturer(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	icao_code VARCHAR(32),
	alt_code VARCHAR(3),
	country_id INT,

	INDEX am_manufacturer_code_idx (icao_code),

	CONSTRAINT am_c_manufacturer_country_link_fk FOREIGN KEY am_c_manufacturer_country_link_idx (country_id)
	    REFERENCES country(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;

-- SOURCES: Same as manufacturers
-- Lists possibly multiple names for a single aircraft manufacturer
CREATE TABLE aircraft_manufacturer_name(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	manufacturer_id INT NOT NULL,
	name VARCHAR(134) NOT NULL,

	CONSTRAINT amn_am_name_owner_link_fk FOREIGN KEY amn_am_name_owner_link_idx (manufacturer_id)
	    REFERENCES aircraft_manufacturer(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;