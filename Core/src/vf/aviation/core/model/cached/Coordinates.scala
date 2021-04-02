package vf.aviation.core.model.cached

import utopia.genesis.shape.shape1D.Angle

/**
 * A latitude-longitude -based set of coordinates
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 * @param latitudeNorth Latitude from the equator towards north
 * @param longitudeEast Longitude from the meridian towards east
 */
case class Coordinates(latitudeNorth: Angle = Angle.zero, longitudeEast: Angle = Angle.zero)
