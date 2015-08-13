package com.generalbioinformatics.rdf;

public interface TripleStoreListener 
{
	/** This event is fired for every sparql select query that completes successfully.
	 * Is NOT called for queries that are retrieved from cache.
	 **/  
	public void queryPerformed(String q, long msec);
}
