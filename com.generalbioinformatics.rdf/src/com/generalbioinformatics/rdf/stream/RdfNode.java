/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

/**
 * Helper class for Statement. A statement does not use RdfNodes internally for speed reasons.
 */
public class RdfNode 
{
	private final boolean fAnon;
	private final boolean fLiteral;
	
	// note that for anonymous nodes, the namespace _: is stored in data.
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
	
	public String toString()
	{
		if (fLiteral) return '"' + data + '"';
		else if (fAnon) return "_:" + data;
		else return '<' + data + '>';
	}
	
	
}
