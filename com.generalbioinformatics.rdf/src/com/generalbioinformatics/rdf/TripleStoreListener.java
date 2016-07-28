package com.generalbioinformatics.rdf;

/**
 * Defines a Listener class that receives events from a TripleStore,
 * such as when a query is successfully performed.
 */
public interface TripleStoreListener 
{
	/** This event is fired for every sparql select query that completes successfully.
	 * Is NOT called for queries that are retrieved from cache.
	 **/  
	public void queryPerformed(String q, long msec);
}
