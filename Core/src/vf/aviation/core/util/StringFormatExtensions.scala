package vf.aviation.core.util

import utopia.flow.parse.Regex

/**
 * Extensions for formatting text
 * @author Mikko Hilpinen
 * @since 29.3.2021, v0.1
 */
object StringFormatExtensions
{
	// ATTRIBUTES   --------------------------
	
	private val parenthesisRegex = Regex.whiteSpace.noneOrOnce +
		Regex.escape('(') + Regex.any + Regex.escape(')')
	
	
	// EXTENSIONS   --------------------------
	
	implicit class FormatString(val s: String) extends AnyVal
	{
		/**
		 * @return This string without parts in parenthesis. For example "Example (string) more..." would become
		 *         "Example more..."
		 */
		def withoutParenthesisContent = parenthesisRegex.filterNot(s)
	}
}
