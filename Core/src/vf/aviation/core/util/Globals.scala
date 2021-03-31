package vf.aviation.core.util

import utopia.flow.async.ThreadPool

import scala.concurrent.ExecutionContext

/**
 * Settings used globally within this project
 * @author Mikko Hilpinen
 * @since 29.3.2021, v0.1
 */
object Globals
{
	// ATTRIBUTES   ----------------------------
	
	/**
	 * Thread pool used for handling asynchronous operations
	 */
	val threadPool = new ThreadPool("Aviation-Data")
	
	
	// COMPUTED ---------------------------------
	
	/**
	 * @return Execution context used in asynchronous tasks
	 */
	implicit def executionContext: ExecutionContext = threadPool.executionContext
}
