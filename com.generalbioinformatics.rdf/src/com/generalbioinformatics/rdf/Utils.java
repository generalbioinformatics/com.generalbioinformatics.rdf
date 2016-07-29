/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.helixsoft.recordstream.RecordStream;
import nl.helixsoft.recordstream.RecordStreamFormatter;
import nl.helixsoft.recordstream.StreamException;
import nl.helixsoft.util.AttributesTable;
import nl.helixsoft.util.HFileUtils;

/** Miscellaneous utilities related to RDF */
public final class Utils 
{
	/** prevent instantiation */
	private Utils() {}

	/**
	 * set of standard attributes for node or edge, that can be defined for any
	 * iri
	 */
	public static <K> void iriAttributes(K key, String iri,
			AttributesTable<K> attrTable, NamespaceMap nsMap) {
		Namespace ns = nsMap.findPrefix(iri);
		if (ns == null) {
			System.err.println("No prefix found for: " + iri);
			attrTable.put(key, "label", iri);
		} else {
			attrTable.put(key, "prefix", ns.getPrefix());
			attrTable.put(key, "namespace", ns.toString());
			attrTable.put(key, "label", nsMap.shorten(iri));
		}
		attrTable.put(key, "iri", iri);
	}

	/**
	 * remove ^^xsd:integer / ^^xsd:long from end part
	 */
	public static long parseLongLiteral(String sCount) 
	{
		long iCount;
		Pattern pat = Pattern.compile("^\"?(\\d+)\"?\\^\\^http://www\\.w3\\.org/2001/XMLSchema\\#(integer|long)");
		Matcher mat = pat.matcher(sCount);
		if (mat.matches())
		{
			iCount = Long.parseLong(mat.group(1));
		}
		else
		{
			iCount = Long.parseLong(sCount);
		}
		return iCount;
	}

	/**
	 * This is a helper that takes a query and saves it to File with some metadata.
	 * Used by SparqlToTsv as well as VirtuosoConnection
	 */
	public static void queryResultsToFile(TripleStore tr, long delta, RecordStream rs, String query, OutputStream os) throws StreamException, UnknownHostException
	{		
		PrintStream fos = new PrintStream (os);

		// save to temp file
		RecordStreamFormatter.asTsv(fos, rs, null, true);

		// add some comments at the end of the table.
		fos.println ("#");
		fos.println ("# " + tr.toString());
		for (String qline : query.split ("\n"))
		{
			fos.println ("# " + qline);
		}
		fos.println ("# Date:" + new Date());
		String computername = HFileUtils.safeMachineName();
		String username = System.getProperty("user.name");
		fos.println ("# " + username + "@" + computername);
		// Important, because it helps to detect quiet time-out queries.
		fos.println ("# Query time: " + delta);
	}
	
}
