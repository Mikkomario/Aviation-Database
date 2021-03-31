package vf.aviation.core.model.partial.country

import utopia.flow.util.UncertainBoolean
import utopia.flow.util.UncertainBoolean.Undefined

import java.time.LocalDate

/**
 * Contains basic information about a country
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 * @param name                 Name of this country
 * @param worldRegionId        Id of the world region this country belongs to (if known)
 * @param isoCode              ISO-code assigned to this country (if applicable)
 * @param dafifCode            Dafif-code assigned to this country (if applicable)
 * @param capitalId            Id of the capital city of this country (if known)
 * @param sovereigntyCountryId Id of the country which is sovereign over this country (if any)
 * @param ended                Date when this country was terminated (None if not terminated or unknown)
 * @param comment              Comment concerning this country (optional)
 * @param independent          Whether this country is independent (may be unknown (default))
 */
// TODO: worldRegionId may become Int instead of Option[Int]
case class CountryData(name: String, worldRegionId: Option[Int] = None, isoCode: Option[String] = None,
                       dafifCode: Option[String] = None, capitalId: Option[Int] = None,
                       sovereigntyCountryId: Option[Int] = None, ended: Option[LocalDate] = None,
                       comment: Option[String] = None, independent: UncertainBoolean = Undefined)
