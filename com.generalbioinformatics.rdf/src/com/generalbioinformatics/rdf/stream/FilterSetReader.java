/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.generalbioinformatics.rdf.stream.NtStream.ParseException;

/**
 * Create a Set of Strings based on the subjects of a TripleStream.
 * <p>
 * Reads statements from a triple stream. For each statement, either
 * subject, object or predicate are selected, and all put together 
 * into a Set&lt;String&gt;.
 * <p>
 * The resulting Set can be used in a {@link FilterSetStream}, to
 * select only triples that occur in a Set.
 * <p>
 * Note that the set is stored in memory, and attempting to read 
 * millions of URI's this way could cause memory problems.
 */
public class FilterSetReader 
{
	private Set<String> filter = null;
	private final TripleStream parent;
	
	private enum Flag { SUBJECTS, PREDICATES, OBJECTS };
	private final Flag flag;
	
	/**
	 * Private constructor. Use one of the createXxx static methods.
	 */
	private FilterSetReader(TripleStream parent, Flag flag)
	{
		this.parent = parent;
		this.flag = flag;
	}
	
	/**
	 * Create a new FilterSetReader that builds a set based on the <b>subjects</b> of 
	 * the parent stream.
	 */
	public static FilterSetReader createSubjectSet(TripleStream parent)
	{
		return new FilterSetReader (parent, Flag.SUBJECTS);
	}

	/**
	 * Create a new FilterSetReader that builds a set based on the <b>objects</b> of 
	 * the parent stream.
	 * <p>
	 * It is assumed that the parent stream contains no literals, only object URI's.
	 */
	public static FilterSetReader createObjectSet(TripleStream parent)
	{
		return new FilterSetReader (parent, Flag.OBJECTS);
	}

	/**
	 * Create a new FilterSetReader that builds a set based on the <b>predicates</b> of 
	 * the parent stream.
	 */
	public static FilterSetReader createPredicateSet(TripleStream parent)
	{
		return new FilterSetReader (parent, Flag.PREDICATES);
	}

	/**
	 * Obtain the set of URI-strings. Note that the actual work is done here.
	 */
	public Set<String> getFilter() throws IOException, ParseException
	{
		if (filter == null)
		{
			filter = new HashSet<String>();
			Statement st;
			switch (flag)
			{
			case SUBJECTS:
				while ((st = parent.getNext()) != null)
				{
					filter.add (st.getSubjectUri());
				}
				break;
			case OBJECTS:
				while ((st = parent.getNext()) != null)
				{
					filter.add (st.getObjectUri());
				}
				break;
			case PREDICATES:
				while ((st = parent.getNext()) != null)
				{
					filter.add (st.getPredicateUri());
				}
				break;
			}
		}
		return filter;
	}
	
}
