/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.IOException;

import nl.helixsoft.recordstream.Stream;

import com.generalbioinformatics.rdf.stream.NtStream.ParseException;

/**
 * Iterate over a stream of Triples, getting one Statement at a time.
 * <p>
 * TODO: examine possibility of merging with Jena StmtIterator.
 */
public interface TripleStream extends Stream<Statement> 
{
	/**
	 * Parse the next triple in the stream.
	 * @returns the next triple, null if the end of the stream has been reached.
	 */
	public Statement getNext() throws IOException, ParseException;
	
}
