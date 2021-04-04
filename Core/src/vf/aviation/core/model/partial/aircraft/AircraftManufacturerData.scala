package vf.aviation.core.model.partial.aircraft

/**
 * Contains basic information about an aircraft manufacturer
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 * @param icaoCode ICAO standard code for this manufacturer (if known)
 * @param alternativeCode Alternative code used for this manufacturer (optional)
 * @param countryId Id of the origin country of this manufacturer (if known)
 */
case class AircraftManufacturerData(icaoCode: Option[String] = None, alternativeCode: Option[String] = None,
                                    countryId: Option[Int] = None)
