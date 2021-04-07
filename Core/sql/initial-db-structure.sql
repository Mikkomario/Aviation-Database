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

-- Lists more general aircarft engine categories
CREATE TABLE generic_engine_type(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(32) NOT NULL,
	is_airbreathing BOOLEAN NOT NULL,
	is_rotation_based BOOLEAN NOT NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;

-- Generic Groups & Specific groups
--	- Rotating Airbreathing
--		- Turboprop
--		- Turboshaft
--	- Non-rotating Airbreathing
--		- Turbojet
--		- Turbofan
--		- Ramjet
--	- Piston
--		- 2-Cycle
--		- 4-Cycle
--	- Other Rotating
--		- Electric
--		- Rotary
--	- Other Non-rotating
--		- Rocket
INSERT INTO generic_engine_type (id, name, is_airbreathing, is_rotation_based) VALUES
	(1, 'Airbreathing Propelling', TRUE, TRUE),
	(2, 'Airbreathing Jet', TRUE, FALSE),
	(3, 'Piston', FALSE, TRUE),
	(4, 'Other Propelling', FALSE, TRUE),
	(5, 'Other Propulsion', FALSE; FALSE);

CREATE TABLE specific_engine_type(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	generic_type_id INT NOT NULL,
	name VARCHAR(32) NOT NULL
)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
INSERT INTO specific_engine_type (id, generic_type_id, name) VALUES
	(1, 1, 'Turboprop'),
	(2, 1, 'Turboshaft'),
	(3, 2, 'Turbojet'),
	(4, 2, 'Turbofan'),
	(5, 2, 'Ramjet'),
	(6, 3, '2-Cycle'),
	(7, 3, '4-Cycle'),
	(8, 4, 'Electric'),
	(9, 4, 'Rotary'),
	(10, 5, 'Rocket');

-- - Land/Sea distiction (mostly for fixed wings)
-- 	- LandPlane
--	- Amphibian
--	- SeaPlane
-- - Weight distinction
--	- Lighter-than-air
--	- Heavier-than-air
--	- Hybrid lift
-- - Wing type distinction (applicable to heavier-than-air and hybrid aircrafts)
--	- Fixed-wing
--		- Airplane (requires motor to maneuver)
--		- Glider (doesn't require a motor to maneuver)
--	- Rotary-wing
--		- Helicopter
--		- Gyroplane/Gyrocopter
--	- Tilt-wing
--	- Ornithopter (not present in data)
-- - Powered / non-powered distinction
--	- Powered
--	- Non-powered

-- Determines where an aircraft can land
CREATE TABLE aircraft_environment_category(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	can_land_in_ground BOOLEAN NOT NULL,
	can_land_in_water BOOLEAN NOT NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
INSERT INTO aircraft_environment_category (id, can_land_in_ground, can_land_in_water) VALUES
	(1, 'Land', TRUE, FALSE),
	(2, 'Sea', FALSE, TRUE),
	(3, 'Amphibian', TRUE, TRUE);

-- Determines the lift type of the aircraft, whether based on weight or aerodynamics
CREATE TABLE aircraft_lift_method(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(32) NOT NULL
)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
-- Aerodynamics (1) are heavier-than-air aircrafts which use other means for lift
-- Lighter-Than-Airs (2) are based on lighter-than-air gasses
-- Hybrids (3) combine these two elements
INSERT INTO aircraft_lift_method (id, name) VALUES
	(1, 'Aerodynamics'),
	(2, 'Lighter-Than-Air'),
	(3, 'Hybrid');

CREATE TABLE aircraft_wing_type(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(32) NOT NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
INSERT INTO aircraft_wing_type (id, name) VALUES
	(1, 'Fixed-Wing'),
	(2, 'Rotary-Wing'),
	(3, 'Transforming'),
	(4, 'Wingless');

-- Determines aircraft power use / function
CREATE TABLE aircraft_power_use(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(32) NOT NULL,
	is_powered BOOLEAN NOT NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
-- Fully Powered (1): E.g. Standard Airplane or a Helicopter
-- Partly Powered (2): E.g. Powered Glider or a Gyroplane
-- Non-powered (3): E.g. Glider or a balloon
INSERT INTO aicraft_power_use (id, name, is_powered) VALUES
	(1, 'Fully Powered', TRUE),
	(2, 'Partly Powered', TRUE),
	(3, 'Non-powered', FALSE);

-- Combines lift type, wing type and power type, but not environment type
CREATE TABLE aircraft_category(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(32) NOT NULL,
	lift_method_id INT NOT NULL,
	wing_type_id INT NOT NULL,
	power_use_id INT NOT NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
-- Standard Airplane (1)
-- Gliders (2, 3) based on power use
-- Rotary-Wing aircrafts (4, 5) based on whether the rotor is powered
-- Tilt-Wing/Tiltrotor (6, 7)
-- Airships (8, 9 and 10) based on aerodynamics and power use
INSERT INTO aircraft_category (id, name, lift_method_id, wing_type_id, power_use_id) VALUES
	(1, 'Airplane', 1, 1, 1),
	(2, 'Powered Glider', 1, 1, 2),
	(3, 'Non-powered Glider', 1, 1, 3),
	(4, 'Helicopter', 1, 2, 1),
	(5, 'Gyroplane', 1, 2, 2),
	(6, 'Tiltrotor', 1, 1, 1),
	(7, 'Tilt-Wing', 1, 3, 1),
	(8, 'Airship', 2, 4, 1),
	(9, 'Balloon', 2, 4, 3),
	(10, 'Hybrid Airship', 3, 1, 1);

-- A custom weight category class based on wake turbulence categories by ICAO and FAA,
-- As well as weight class system in ACREF
-- These values are meant to be used as min/max pairs
-- The maximum values are exclusive
CREATE TABLE aircraft_weight_category(
	id NOT NULL PRIMARY KEY AUTO_INCREMENT,
	code VARCHAR(2) NOT NULL,
	name VARCHAR(16) NOT NULL,
	icao_code VARCHAR(1) NOT NULL,
	faa_code VARCHAR(2) NOT NULL,
	min_take_off_weight_pounds INT,
	max_take_off_weight_pounds INT

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
-- Super / J (7) = Super class, which is above 1 000 000 pounds
-- Heavy / H (6) = ICAO Heavy class, limited by FAA Large class
-- Semi Heavy / H- (5) = Intersection between ICAO heavy and FAA large
-- Medium+ / M+ (4) = Intersection between ACREF class 3 and FAA S+ class
-- Medium / M (3) = Intersection between ICAO medium, FAA S+ and ACREF class 2
-- Light+ / L+ (2) = Intersection between ICAO light and FAA S+
-- Light / L (1) = FAA S class (also ACREF class 1)
INSERT INTO aircraft_weight_category (id, code, name, icao_code, faa_code, min_take_off_weight_pounds, max_take_off_weight_pounds) VALUES
	(1, 'L', 'Light', 'L', 'S', NULL, 12500),
	(2, 'L+', 'Light+', 'L', 'S+', 12500, 15400),
	(3, 'M', 'Medium', 'M', 'S+', 15400, 20000),
	(4, 'M+', 'Medium+', 'M', 'S+', 20000, 41000),
	(5, 'H-', 'Semi Heavy', 'M', 'L', 41000, 300000),
	(6, 'H', 'Heavy', 'H', 'H', 300000, 1000000),
	(7, 'J', 'Super', 'J', 'J', 1000000, NULL);

-- SOURCE: Order_7360.1D_Aircraft_Type_Designators (FAA)
-- Based on RECAT 1.5 Wake Categories
-- Provides hints about aircraft wing span
-- Please note that minimum values are exclusive and maximum values inclusive
CREATE TABLE aircraft_wing_span_category(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	recat_wake_category_code VARCHAR(1) NOT NULL,
	min_wing_span_feet INT NOT NULL,
	max_wing_span_feet INT

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
-- A => > 245 feet
-- B => > 175 feet & <= 245 feet
-- C => > 125 feet & <= 175 feet
-- D => > 90 feet <= 175 feet
-- E => > 65 feet <= 90 feet
-- F => <= 65 feet (although the data doesn't spell this out)
INSERT INTO aircraft_wing_span_category (id, recat_wake_category_code, min_wing_span_feet, max_wing_span_feet) VALUES
	(1, 'F', 0, 65),
	(2, 'E', 65, 90),
	(3, 'D', 90, 175),
	(4, 'C', 125, 175),
	(5, 'B', 175, 245),
	(6, 'A', 245, NULL);

-- SOURCE: https://www.skybrary.aero/index.php/Airplane_Design_Group_(ADG)
-- Matches values in FAA Aircraft Char Database
-- Design group is based on most restrictive value (wing span or tail height)
-- All maximum values are exclusive
CREATE TABLE aircraft_design_group(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	code VARCHAR(3) NOT NULL,
	max_wing_span_feet INT NOT NULL,
	max_tail_height_feet INT NOT NULL

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;
INSERT INTO aircraft_design_group (id, code, max_wing_span_feet, max_tail_height_feet) VALUES
	(1, 'I', 49, 20),
	(2, 'II', 79, 30),
	(3, 'III', 118, 45),
	(4, 'IV', 171, 60),
	(5, 'V', 214, 66),
	(6, 'VI', 262, 80);

-- Minimum values are exclusive, maximum values are inclusive
-- Code determined by FAA-standard
CREATE TABLE aircraft_approach_speed_group(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    code CHAR NOT NULL,
    min_approach_speed_knots INT NOT NULL,
    max_approach_speed_knots INT,

    INDEX apsg_code_idx (code)

))Engine=InnoDB DEFAULT CHARACTER SET utf8
                DEFAULT COLLATE utf8_general_ci;
INSERT INTO aircraft_approach_speed_group (id, code, min_approach_speed_knots, max_approach_speed_knots) VALUES
    (1, 'A', 0, 90),
    (2, 'B', 90, 120),
    (3, 'C', 120, 140),
    (4, 'D', 140, 165),
    (5, 'E', 165, NULL);


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

	INDEX amn_manufacturer_name_idx (name),

	CONSTRAINT amn_am_name_owner_link_fk FOREIGN KEY amn_am_name_owner_link_idx (manufacturer_id)
	    REFERENCES aircraft_manufacturer(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARACTER SET utf8
               DEFAULT COLLATE utf8_general_ci;

-- SOURCES: ACFTREF (and others)
-- Lists aircraft model/series groups, which are usually attached to some ICAO code
-- A model may be manufactured by multiple manufacturers, where each has their own series / variation
-- Min and max weight categories are both inclusive
CREATE TABLE aircraft_model(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	environment_id INT NOT NULL DEFAULT 1,
	min_weight_category_id INT NOT NULL DEFAULT 1,
    max_weight_category_id INT NOT NULL DEFAULT 7,
	manufacturer_code VARCHAR(3),
	model_code VARCHAR(2),
	iata_code VARCHAR(3),
	icao_code VARCHAR(4),
	category_id INT,
	wing_type_id INT,
	number_of_engines INT,
	engine_category_id INT,
	engine_type_id INT,
	airworthiness CHAR,
	taxiway_design_group_code VARCHAR(2),

    INDEX am_code_idx (manufacturer_code, model_code),
	INDEX am_iata_idx (iata_code),
	INDEX am_icao_idx (icao_code),

	CONSTRAINT am_aec_ac_category_reference_fk FOREIGN KEY am_aec_ac_category_reference_idx (environment_id)
	    REFERENCES aircraft_environment_category(id) ON DELETE CASCADE,

	CONSTRAINT am_awc_ac_min_wt_category_reference_fk FOREIGN KEY am_awc_ac_min_wt_category_reference_idx (min_weight_category_id)
	    REFERENCES aircraft_weight_category(id) ON DELETE CASCADE,

	CONSTRAINT am_awc_ac_max_wt_category_reference_fk FOREIGN KEY am_awc_ac_max_wt_category_reference_idx (max_weight_category_id)
	    REFERENCES aircraft_weight_category(id) ON DELETE CASCADE,

	CONSTRAINT am_ac_ac_category_reference_fk FOREIGN KEY am_ac_ac_category_reference_idx (category_id)
	    REFERENCES aircraft_category(id) ON DELETE SET NULL,

	CONSTRAINT am_awt_ac_wing_type_reference_fk FOREIGN KEY am_awt_ac_wing_type_reference_idx (wing_type_id)
	    REFERENCES aircraft_wing_type(id) ON DELETE SET NULL,

	CONSTRAINT am_get_ac_engine_type_generic_reference_fk FOREIGN KEY am_get_ac_engine_type_generic_reference_idx (engine_category_id)
	    REFERENCES generic_engine_type(id) ON DELETE SET NULL,

	CONSTRAINT am_set_ac_engine_type_specific_reference_fk FOREIGN KEY am_set_ac_engine_type_specific_reference_idx (engine_type_id)
	    REFERENCES specific_engine_type(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- SOURCES: ACFTREF (and others)
-- Represents a specific A/C model
-- TODO: Add name as idx (possibly with manufacturer) if there are searches based on it
CREATE TABLE aircraft_model_variant(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	model_id INT NOT NULL,
	manufacturer_id INT NOT NULL,
	name VARCHAR(16) NOT NULL,
	manufacturer_code VARCHAR(3),
	model_code VARCHAR(2),
	series_code VARCHAR(2),
	design_group_id INT,
	wing_span_feet DOUBLE,
    length_feet DOUBLE,
    tail_height_feet DOUBLE,
    wheel_base_feet DOUBLE,
    main_gear_width_feet DOUBLE,
	number_of_seats INT,
	max_take_off_weight_pounds INT,
	max_taxi_weight_pounds INT,
	approach_speed_group_id INT,
	approach_speed_knots INT,
	cruising_speed_knots DOUBLE,
	manufacture_started_year INT,
	manufacture_ended_year INT,

    INDEX amv_code_idx (manufacturer_code, model_code, series_code),

	CONSTRAINT amv_am_parent_model_link_fk FOREIGN KEY amv_am_parent_model_link_idx (model_id)
	    REFERENCES aircraft_model(id) ON DELETE CASCADE,

	CONSTRAINT amv_am_manufacturer_link_fk FOREIGN KEY amv_am_manufacturer_link_idx (manufacturer_id)
	    REFERENCES aircraft_manufacturer(id) ON DELETE CASCADE,

    CONSTRAINT amv_adg_ac_design_group_reference_fk FOREIGN KEY amv_adg_ac_design_group_reference_idx (design_group_id)
        REFERENCES aircraft_design_group(id) ON DELETE SET NULL,

    CONSTRAINT amv_aasg_ac_approach_speed_group_reference_fk FOREIGN KEY amv_aasg_ac_approach_speed_group_reference_idx (approach_speed_group_id)
        REFERENCES aircraft_approach_speed_group(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;