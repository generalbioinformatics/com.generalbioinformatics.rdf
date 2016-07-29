/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.io.File;
import java.io.OutputStream;

import com.generalbioinformatics.rdf.stream.Statement;

import nl.helixsoft.recordstream.RecordStream;
import nl.helixsoft.recordstream.Stream;
import nl.helixsoft.recordstream.StreamException;
import nl.helixsoft.stats.DataFrame;

/**
 * A triple store is an abstraction layer that wraps various ways of accessing a triple store.
 * Optionally implements local caching of sparql query results.
 * <p>
 * Using TripleStore has the following advantages compared to using e.g. a Jena Model directly
 * <ul>
 * <li>Ability to switch between implementations
 * <li>Allows accessing Virtuoso through direct JDBC/database access 
 * while allowing you to write code that is compatible with either Jena or Sesame openRdf
 * <li>Comes with a handy configuration dialog.
 * </ul>
 * <p>
 * Current known implementations:
 * <ul>
 * <li>local RDF file(s) through Jena. You can even use an empty Jena model and use SERVICE to query public SPARQL endpoints.
 * <li>Virtuoso using JDBC driver: this works directly on virtuoso, only way to get more than 10000 results on a default installation.
 * <li>Sparql endpoint through Jena: also works on virtuoso, but can have issues.
 * <li>OpenRdf / Sesame
 * </ul>
 */
public interface TripleStore 
{
	/** 
	 * Attempts to hide underlying implementation differences as much as possible.
	 * URIs and anonymous IDs will be returned as {@link com.generalbioinformatics.rdf.stream.RdfNode}
	 * Literals will be returned as basic classes such as Long, Integer, Boolean, String.
	 * Language information on strings is not returned.
	 * <p>
	 * Uses caching if enabled via setCacheDir 
	 */ 
	RecordStream sparqlSelect(String query) throws StreamException;
	
	/** Direct query without caching, for internal use */
	RecordStream _sparqlSelectDirect(String query) throws StreamException;

	NamespaceMap getNamespaces();
	
	//TODO: StreamException is not the appropriate exception type here.
	//TODO: return results as Stream<Statement>
	void sparqlConstruct(String query, OutputStream os) throws StreamException;
	
	Stream<Statement> sparqlConstruct(String query) throws StreamException;
	
	/**
	 * Setting the caching dir to a non-null value enables caching for select queries. 
	 * Setting it to null disables caching.
	 */
	void setCacheDir(File dir);
	
	/** Add a listener that will receive TripleStore events (e.g. when a query is returned) from this TripleStore */
	void addListener(TripleStoreListener l);
	/** Remove a previously registered listener */
	void removeListener (TripleStoreListener l);

	/** Same as sparqlSelect, but loads all results into memory at once and returns a DataFrame instead of a RecordStream */
	DataFrame sparqlSelectDF(String query) throws StreamException;
}
