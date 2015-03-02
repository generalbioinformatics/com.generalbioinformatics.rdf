/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.IOException;
import java.util.Set;

import com.generalbioinformatics.rdf.stream.NtStream.ParseException;

/**
 * This is a TripleStream wrapper that filters triples based on a set of values.
 * <p>
 * The effect is equivalent to the FILTER () clause of sparql.
 * <p>
 * For example, if you have a Set of strings containing all the URI's of
 * nodes of interest, then you can use this class to filter out all the statements that
 * contain URI's from that set.
 */
public class FilterSetStream extends AbstractTripleStream
{
	private final TripleStream parent;
	private final Set<String> filterValues;
	
	/** private constructor, use one of the static createXxx methods instead. */
	private FilterSetStream (TripleStream parent, Set<String> filterValues)
	{
		this.parent = parent;
		this.filterValues = filterValues;
	}
	
	/** create a FilterStream that only picks out statements with the <b>subject</b> URI from the filter set. */
	public static FilterSetStream createSubjectFilter(TripleStream parent, Set<String> filterValues)
	{
		return new FilterSetStream (parent, filterValues);
	}
	
	@Override /** @InheritDoc */
	public Statement getNext() throws IOException, ParseException 
	{
		Statement st;
		do {
			st = parent.getNext();
			if (st == null) break;
			if (filterValues.contains(st.getSubjectUri())) break;
		} while (true);
		return st;
	}

}
