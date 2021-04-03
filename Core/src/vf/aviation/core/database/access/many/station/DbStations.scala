package vf.aviation.core.database.access.many.station

import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyRowModelAccess
import utopia.vault.sql.{Update, Where}
import vf.aviation.core.database.factory.station.StationFactory
import vf.aviation.core.database.model.StationModel
import vf.aviation.core.model.stored.station.Station

/**
 * Used for accessing multiple stations at a time
 * @author Mikko Hilpinen
 * @since 3.4.2021, v0.1
 */
object DbStations extends ManyRowModelAccess[Station]
{
	// COMPUTED ---------------------------
	
	private def model = StationModel
	
	/**
	 * @return An access point to those stations that haven't yet been assigned any type
	 */
	def withoutType = DbStationsWithoutType
	
	
	// IMPLEMENTED  -----------------------
	
	override def factory = StationFactory
	
	override def globalCondition = None
	
	
	// NESTED   ---------------------------
	
	object DbStationsWithoutType extends ManyRowModelAccess[Station]
	{
		// ATTRIBUTES   -------------------
		
		private val condition = model.typeIdColumn.isNull
		
		
		// IMPLEMENTED  -------------------
		
		override def factory = DbStations.factory
		
		override def globalCondition = Some(DbStations.mergeCondition(condition))
		
		
		// OTHER    -----------------------
		
		/**
		 * Specifies type for some undefined stations based on the station name
		 * @param typeId Assigned station type's id
		 * @param nameWords Words in station names that can be assigned as this type
		 * @param connection DB Connection (implicit)
		 * @return Number of affected rows
		 */
		def updateTypeWithName(typeId: Int, nameWords: Seq[String])(implicit connection: Connection) =
		{
			val stationNameColumn = StationModel.nameColumn
			val stationNameConditions = nameWords.map(stationNameColumn.contains)
			// It is enough that the station name contains any of the specified words
			val stationNameCondition = stationNameConditions.head || stationNameConditions.tail
			
			connection(Update(table, model.typeIdAttName, typeId) + Where(mergeCondition(stationNameCondition)))
				.updatedRowCount
		}
	}
}
