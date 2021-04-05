package vf.aviation.input.controller.aircraft

import utopia.flow.util.StringExtensions._

/**
 * Utility methods concerning aircraft manufacturers
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object ManufacturerUtils
{
	/**
	 * Accepts a string containing a name followed by country name(s) in parenthesis. Separates the two.
	 * @param original Original String. E.g. "Manufacturer A (Germany/France)"
	 * @return Manufacturer name + country name(s) in a vector
	 */
	def separateNameAndCountries(original: String) =
	{
		val parenthesisStartIndex = original.optionLastIndexOf(")").flatMap { parenthesisEndIndex =>
			original.take(parenthesisEndIndex).optionLastIndexOf("(")
		}
		
		val name = parenthesisStartIndex match
		{
			case Some(index) => original.take(index)
			case None => original
		}
		val countryNames = parenthesisStartIndex.map { index => original.drop(index + 1).untilFirst(")") } match
		{
			case Some(parenthesisString) => parenthesisString.split("/").map { _.trim }.toVector
				.filterNot { _.isEmpty }
			case None => Vector()
		}
		
		name -> countryNames
	}
}
