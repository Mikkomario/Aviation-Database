package vf.aviation.core.model.partial

import utopia.genesis.util.Distance
import vf.aviation.core.model.cached.Coordinates

import java.time.LocalDate

/**
 * Contains common information about stations, like an airport or a train station
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 * @param name Name of this station
 * @param coordinates Latitude & Longitude coordinates of this station
 * @param altitude Altitude of this station (if known)
 * @param stationTypeId Id of this station's type (if known)
 * @param dotId dot system id for this station (if known)
 * @param openFlightsId Open Flights system id for this station (if known)
 * @param iataCode IATA-standard code for this airport (if known / applicable)
 * @param icaoCode ICAO-standard code for this airport (if known / applicable)
 * @param cityId Id of the city where this station resides in (if known)
 * @param started Date when this station was opened / recorded (if known)
 * @param closed Date when this station was closed (if known / applicable)
 * @param isClosed Whether this station is currently closed (default = false)
 */
case class StationData(name: String, coordinates: Coordinates, altitude: Option[Distance] = None,
                       stationTypeId: Option[Int] = None, dotId: Option[Int] = None, openFlightsId: Option[Int] = None,
                       iataCode: Option[String] = None, icaoCode: Option[String] = None, cityId: Option[Int] = None,
                       started: Option[LocalDate] = None, closed: Option[LocalDate] = None, isClosed: Boolean = false)
