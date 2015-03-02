/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.IOException;

/**
 * This class selects certain triples out of a parent stream, like a 'grep' for tripples. 
 * It passes through only triples from the parent stream that match certain criteria.
 */
public class SelectStream extends AbstractTripleStream
{
	/**
	 * @param subjectUri the subject you want to select for, or null if you want to allow any subject
	 * @param predicateUri the predicate you want to select for, or null if you want to allow any predicate
	 * @param objectUri the object you want to select for, or null if you want to allow any object.
	 * 	NB For the time being, only object uri's are supported, no literals.
	 */
	public SelectStream(TripleStream parent, String subjectUri, String predicateUri, String objectUri)
	{
		this.parent = parent;
		this.subjectUri = subjectUri;
		this.predicateUri = predicateUri;
		this.objectUri = objectUri;
	}
	
	private final String subjectUri;
	private final String objectUri;
	private final String predicateUri;
	private final TripleStream parent;
	
	@Override /** @InheritDoc */
	public Statement getNext() throws IOException, NtStream.ParseException 
	{
		Statement result;
		do
		{
			result = parent.getNext();
			if (result == null) break;
			
			if (
					(subjectUri == null || result.getSubjectUri().equals(subjectUri)) &&
					(objectUri == null || result.getObjectUri().equals(objectUri)) &&
					(predicateUri == null || result.getPredicateUri().equals(predicateUri))
				)
			{
				break;
			}
		} while (true);
		
		return result;
	}

}
