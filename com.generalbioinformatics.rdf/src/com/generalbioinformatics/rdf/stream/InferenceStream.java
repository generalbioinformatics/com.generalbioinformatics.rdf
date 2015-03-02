/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.IOException;

import com.generalbioinformatics.rdf.stream.NtStream.ParseException;

/**
 * Transforms a stream of triples.
 * For each input triple, an output triple is generated, according to certain rules.
 * <p>
 * Note that this class does not check the source stream for certain patterns,
 * it is assumed that all triples in the source stream are good for inferencing.
 * If you do need to do checks on the source, wrap the source in a SelectTripleStream. 
 */
public class InferenceStream extends AbstractTripleStream
{
	private final String newPred;
	private final String newObj;
	private final TripleStream source;
	
	/**
	 * Reads a triple from the source one statement at a time.
	 * For each source statement, a new statement is emitted.
	 * The new statement has the same subject as the source, but the predicate 
	 * and object are replaced with the new values passed to this constructor.
	 * <p>
	 * Note that both predicate and object must be URI's. Literal's are not 
	 * supported for the time being. 
	 */
	public InferenceStream(TripleStream source, String predUri, String objUri)
	{
		this.newPred = predUri;
		this.newObj = objUri;
		this.source = source;
	}
	
	@Override /** @InheritDoc */
	public Statement getNext() throws IOException, ParseException 
	{
		Statement st = source.getNext();
		if (st != null)
		{
			st.setPredicateUri(newPred);
			st.setObjectUri(newObj);
		}
		return st;
	}

}
