/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.io.File;
import java.io.OutputStream;

import nl.helixsoft.recordstream.RecordStream;
import nl.helixsoft.recordstream.StreamException;

/**
 * A triple store is an abstraction layer that wraps various ways of accessing a triple store.
 * Optionally implements local caching of sparql query results.
 * <p>
 * Using TripleStore has the following advantages compared to using e.g. a Jena Model directly
 * <ul>
 * <li>Ability to switch between implementations
 * <li>Allows accessing Virtuoso through using the direct JDBC/database access 
 * while allowing you to write code that is compatible with either Jena or Sesame openRdf
 * <li>Comes with a handy configuration dialog.
 * </ul>
 * <p>
 * A few known implementations:
 * <ul>
 * <li>a local RDF file through Jena
 * <li>Virtuoso using JDBC driver -> this works directly on virtuoso, only way to get more than 10000 results on a default installation.
 * <li>Sparql endpoint through Jena -> also works on virtuoso, but can have issues.
 * <li>OpenRdf / Sesame
 * </ul>
 */
public interface TripleStore 
{
	/** Uses caching if enabled via setCacheDir */ 
	RecordStream sparqlSelect(String query) throws StreamException;
	
	/** Direct query without caching, for internal use */
	RecordStream _sparqlSelectDirect(String query) throws StreamException;

	NamespaceMap getNamespaces();
	
	//TODO: RecordStreamException is not the appropriate exception type here.
	void sparqlConstruct(String query, OutputStream os) throws StreamException;
	
	/**
	 * Setting the caching dir to a non-null value enables caching for select queries. 
	 * Setting it to null disables caching.
	 */
	void setCacheDir(File dir);
}
