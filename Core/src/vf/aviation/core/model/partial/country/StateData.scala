package vf.aviation.core.model.partial.country

/**
 * Contains basic information about a country state
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 * @param name Name of this state
 * @param countryId Id of the country this state belongs to
 * @param isoCode ISO standard code for this state
 * @param fipsCode Fips standard id for this state (optional)
 * @param comment A comment concerning this state (optional)
 */
case class StateData(name: String, countryId: Int, isoCode: String, fipsCode: Option[Int] = None,
                     comment: Option[String] = None)
