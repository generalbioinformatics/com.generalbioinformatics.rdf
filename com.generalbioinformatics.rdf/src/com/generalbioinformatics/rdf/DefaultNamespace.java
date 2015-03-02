/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

/**
 * Default implementation of Namespace interface.
 */
public class DefaultNamespace implements Namespace 
{
	private final String prefix;
	private final String full;
	
	@Override
	public String getPrefix() 
	{
		return prefix;
	}
	
	public String toString()
	{
		return full;
	}
	
	public DefaultNamespace (String prefix, String full)
	{
		this.prefix = prefix;
		this.full = full;
	}
}
