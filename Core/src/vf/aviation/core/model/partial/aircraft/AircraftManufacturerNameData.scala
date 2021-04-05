package vf.aviation.core.model.partial.aircraft

/**
 * Contains basic information about an aircraft manufacturer name assignment
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 * @param manufacturerId Id of the associated manufacturer
 * @param name Name assigned to the manufacturer
 */
case class AircraftManufacturerNameData(manufacturerId: Int, name: String)
