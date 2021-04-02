--
-- Database Structure for the project (v0.1)
--

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
-- TODO: Research whether world_region_id should be not null (only if countries.dat only contains duplicates)

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

-- SOURCE: WAC_COUNTRY_STATE
-- A world area / WAC
-- Notice that code (not id) is the primary key and is not auto-increment
-- but matches with the associated world area code
-- Data doesn't actually contain start date but only year + month. However, date is an easier format to use.
CREATE TABLE world_area
(
	code INT NOT NULL PRIMARY KEY,
	name VARCHAR(64) NOT NULL,
	country_id INT NOT NULL,
	state_id INT,
	started DATE NOT NULL,
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