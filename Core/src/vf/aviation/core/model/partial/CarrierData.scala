package vf.aviation.core.model.partial

import java.time.{LocalDate, YearMonth}

/**
 * Contains basic information about a carrier / airline
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 * @param name Carrier / airline name
 * @param alias Alias name for this carrier (optional)
 * @param callSign Call sign of this carrier (if known & assigned)
 * @param dotId Id of this carrier in the bureau of transportation (dot) system (if known)
 * @param openFlightsId Id of this carrier in the open flights system (if known)
 * @param iataCode IATA standard code assigned for this carrier (if known)
 * @param icaoCode ICAO standard code assigned for this carrier (if known)
 * @param countryId Id of the country from which this carrier originates (if known)
 * @param worldAreaCode Code of the world area from which this carrier originates (if known)
 * @param sizeCategoryId Id of this carrier's size category (if known)
 * @param typeCategoryId Id of this carrier's type / style of operation (if known)
 * @param started Year + month when this carrier was added to the originating system (if known)
 * @param ended Date when this carrier's data became deprecated (if known and applicable)
 * @param isClosed Whether this carrier has ceased functioning (default = false)
 */
case class CarrierData(name: String, alias: Option[String] = None, callSign: Option[String] = None,
                       dotId: Option[Int] = None, openFlightsId: Option[Int] = None, iataCode: Option[String] = None,
                       icaoCode: Option[String] = None, countryId: Option[Int] = None,
                       worldAreaCode: Option[Int] = None, sizeCategoryId: Option[Int] = None,
                       typeCategoryId: Option[Int] = None, started: Option[YearMonth] = None,
                       ended: Option[LocalDate] = None, isClosed: Boolean = false)
