--
-- Database Structure for the project (v0.1)
--

-- Hard-Coded   -----------------------------------------

-- SOURCE: OpenFlights
-- Applicable in: airports.dat (extended)
CREATE TABLE station_type(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	open_flights_code VARCHAR(7) NOT NULL,
	name VARCHAR(32) NOT NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;
-- Inserts possible values
INSERT INTO station_type (id, openflights_code, name) VALUES
	(1, 'airport', 'Airport'),
	(2, 'station', 'Train Station'),
	(3, 'port', 'Ferry Terminal');


-- Dynamically Added    ----------------------------------

-- SOURCE: WAC_COUNTRY_STATE
-- Represents a major region within world (E.g. Europe / USA)
-- Notice that id is not auto-increment but matches the first digit in the associated 3-digit world area codes
CREATE TABLE world_region
(
	id INT NOT NULL PRIMARY KEY,
	name VARCHAR(64) NOT NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

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

    CONSTRAINT c_wr_region_link_fk FOREIGN KEY c_wr_region_link_idx (world_region_id)
        REFERENCES world_region(id) ON DELETE SET NULL,

	CONSTRAINT c_c_sovereignty_link_fk FOREIGN KEY c_c_sovereignty_link_idx (sovereignty_country_id)
	    REFERENCES country(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

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

)Engine=InnoDB DEFAULT CHARSET=latin1;

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

)Engine=InnoDB DEFAULT CHARSET=latin1;

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

	CONSTRAINT c_c_country_link_fk FOREIGN KEY c_c_country_link_idx (country_id)
	    REFERENCES country(id) ON DELETE CASCADE,

	CONSTRAINT c_s_state_link_fk FOREIGN KEY c_s_state_link_idx (state_id)
	    REFERENCES `state`(id) ON DELETE SET NULL,

	CONSTRAINT c_wa_world_area_link_fk FOREIGN KEY c_wa_world_area_link_idx (world_area_code)
	    REFERENCES world_area(code) On DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Adds link from country to its capital city
ALTER TABLE country ADD CONSTRAINT c_c_capital_city_link_fk FOREIGN KEY c_c_capital_city_link_idx (capital_id)
    REFERENCES city(id) ON DELETE SET NULL;

-- SOURCES: MASTER_CORD, airports.dat (OpenFlights), airports.dat (RouteMapper)
-- Represents an airport, train station, ferry port etc.
CREATE TABLE station(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(64) NOT NULL,
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

)Engine=InnoDB DEFAULT CHARSET=latin1;