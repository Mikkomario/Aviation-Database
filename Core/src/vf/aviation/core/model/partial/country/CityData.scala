package vf.aviation.core.model.partial.country

import scala.concurrent.duration.Duration

/**
 * Contains basic information about a city
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 * @param name (Local) name of this city
 * @param countryId Id of the country this city belongs to
 * @param marketId Id of the associated city market (if known)
 * @param stateId Id of the state this city belongs to (if applicable)
 * @param worldAreaCode Code of the world area this city belongs to (if known)
 * @param timeZone Time zone in this city as difference from UTC time (if known)
 * @param timeZoneName Name of the time zone applied in this city (if known)
 * @param daylightSavingZoneCode Character representing the daylight saving zone / style applied in this city
 *                               (if applicable and known)
 */
case class CityData(name: String, countryId: Int, marketId: Option[Int] = None, stateId: Option[Int] = None,
                    worldAreaCode: Option[Int] = None, timeZone: Option[Duration] = None,
                    timeZoneName: Option[String] = None, daylightSavingZoneCode: Option[Char] = None)
