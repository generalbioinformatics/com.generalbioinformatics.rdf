/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * A basic implementation of the {@link NamespaceMap} interface.
 */
public class SimpleNamespaceMap implements NamespaceMap 
{
	protected BiMap<String, String> nsMap = HashBiMap.create();

	public void put (String prefix, String full)
	{
		nsMap.put (prefix, full);
	}

	public void getNamespace (String prefix)
	{
		nsMap.get (prefix);
	}

	public void getPrefix (String namespace)
	{
		nsMap.inverse().get (namespace);
	}

	/**
	 * Shorten full iri to prefix:localid
	 */
	public String shorten(String iri)
	{
		for (String ns : nsMap.values())
		{
			if (iri.startsWith(ns))
			{
				return nsMap.inverse().get(ns) + ":" + iri.substring(ns.length());
			}
		}
		return iri;
	}
	
	/**
	 * Expand short form prefix:localid to full iri
	 */
	public String expand(String shortForm)
	{
		for (String prefix : nsMap.keySet())
		{
			if (shortForm.startsWith(prefix + ":"))
			{
				return nsMap.get(prefix) + shortForm.substring(prefix.length() + 1);
			}
		}
		return shortForm;
	}

	
	@Override
	/**
	 * For a given iri, look up which namespace it uses and what the prefix is.
	 * This only returns a namespace if there  has been a prefix defined for it.
	 */
	public Namespace findPrefix(String iri) 
	{
		for (String ns : nsMap.values())
		{
			if (iri.startsWith(ns))
			{
				return new DefaultNamespace (nsMap.inverse().get(ns), ns);
			}
		}
		return null;
	}

}
