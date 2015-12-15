/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import nl.helixsoft.util.ObjectUtils;

/**
 * Helper class for Statement. A statement does not use RdfNodes internally for speed reasons.
 */
public class RdfNode 
{
	private final boolean fAnon;
	private final boolean fLiteral;
	
	private final String data;

	public boolean isAnon () { return fAnon; }
	public boolean isLiteral () { return fLiteral; }
	public boolean isUri () { return !fLiteral; }
	public String getUri ()  { return data; }
	public String getId () { return data; }
	
	RdfNode (String data, boolean isAnon, boolean isLiteral)
	{
		this.fAnon = isAnon;
		this.data = data;
		this.fLiteral = isLiteral;
	}

	public static RdfNode createUri (String uri)
	{
		if (uri == null) throw new NullPointerException();
		return new RdfNode (uri, false, false);
	}

	public static RdfNode createAnon (String id)
	{
		if (id == null) throw new NullPointerException();
		return new RdfNode (id, true, false);
	}

	public static RdfNode createLiteral (String data)
	{
		if (data == null) throw new NullPointerException();
		return new RdfNode (data, false, true);
	}
	
	public String format()
	{
		if (fLiteral) return '"' + data + '"';
		else if (fAnon) return "_:" + data;
		else return '<' + data + '>';
	}
	
	public String toString()
	{
		return data;
	}
	
	@Override
	public boolean equals(Object other) 
	{
		if (other == null) return false; 
		if (this == other) return true;
		if (other.getClass() != RdfNode.class) return false;
		RdfNode that = (RdfNode)other;
		
		return 
			ObjectUtils.safeEquals(this.data, that.data) &&
			this.fLiteral == that.fLiteral &&
			this.fAnon == that.fAnon;		
	}

	@Override
	public int hashCode() 
	{
		return (fAnon ? 3 : -5) +
				(fLiteral ? -17 : 13) +
				(data == null ? 23 : 29 * data.hashCode());
	}
	
	/**
	 * Backwards compatibility hack:
	 * support groovy notation like 
	 * 
	 * 		r.get("s") - "http://identifiers.org/" 
	 */
	public String minus (String o)
	{
		return toString().replaceFirst(o, "");
	}
}
