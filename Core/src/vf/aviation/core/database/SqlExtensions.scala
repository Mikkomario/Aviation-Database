package vf.aviation.core.database

import utopia.flow.generic.ValueConversions._
import utopia.flow.parse.Regex
import utopia.vault.sql.{Condition, ConditionElement}
import utopia.vault.sql.Extensions._

/**
 * Provides sql / database interaction -related extensions
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 */
object SqlExtensions
{
	private val separatorRegex = !Regex.alphaNumeric
	
	implicit class RichConditionElement(val element: ConditionElement) extends AnyVal
	{
		/**
		 * Creates a condition that only accepts values which resemble the search string.
		 * Resemblance, in this context means that the value contains all the words (letters or digits)
		 * of the search string in the same order, but may contain other text in between or elsewhere.
		 * E.g. "Search no. 1" results in LIKE '%Search%no%1%'
		 * @param string A search string
		 * @return A search condition
		 */
		def resembling(string: String) =
		{
			val parts = separatorRegex.split(string).toVector.filter { _.nonEmpty }
			if (parts.size <= 1)
				element.contains(string)
			else
				element.like("%" + parts.mkString("%") + "%")
		}
		
		/**
		 * Creates a condition that accepts values which somewhat resemble the search string. The
		 * returned values don't need to contain all of the words in the search string, but at least
		 * 50% of the digit/letter parts in the correct order.
		 * Works much like .resembling(String) but isn't as strict
		 * @param string A search string
		 * @return A search condition
		 */
		def looselyResembling(string: String) =
		{
			val parts = separatorRegex.split(string)
			if (parts.isEmpty)
				element.contains(string)
			else if (parts.size == 1)
				element.contains(parts.head)
			else
			{
				// At least 50% of the parts must match
				val requiredCount = (parts.size / 2.0).ceil.toInt
				// Uses the longest parts available
				val requiredParts = parts.sortBy { -_.length }.take(requiredCount)
				// The parts must appear in order
				element.like("%" + parts.filter(requiredParts.contains).mkString("%") + "%")
			}
		}
		
		/**
		 * Creates a condition that requires the value to contain all of the specified strings.
		 * Order of the appearances doesn't matter.
		 * @param words Search words
		 * @return A search condition
		 */
		def containingAllOf(words: Seq[String]) =
		{
			if (words.isEmpty)
				Condition.alwaysTrue
			else
			{
				val conditions = words.map(element.contains)
				conditions.head && conditions.tail
			}
		}
		
		/**
		 * Creates a condition that requires the value to contain at least one of the specified strings.
		 * @param words Search words
		 * @return A search condition
		 */
		def containingAnyOf(words: Seq[String]) =
		{
			if (words.isEmpty)
				Condition.alwaysFalse
			else
			{
				val conditions = words.map(element.contains)
				conditions.head || conditions.tail
			}
		}
	}
}
