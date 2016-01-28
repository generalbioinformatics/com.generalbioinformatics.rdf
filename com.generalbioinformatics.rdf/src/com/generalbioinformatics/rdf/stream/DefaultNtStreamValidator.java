package com.generalbioinformatics.rdf.stream;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.generalbioinformatics.rdf.NS;

import nl.helixsoft.util.StringUtils;

public class DefaultNtStreamValidator implements NtStreamValidator
{
	Logger log = LoggerFactory.getLogger("com.generalbioinformatics.rdf.stream.DefaultNtStreamValidator");

	@Override
	public void validateLiteral(Object s, Object p, Object o) 
	{
		if (statementOnlyPredicates.contains(StringUtils.safeToString(p)))
		{
			throw new RuntimeException ("You must not use predicate " + p + " in a literal triple");
		}
	}

	private static Set<String> statementOnlyPredicates;
	static {
		statementOnlyPredicates = new HashSet<String>();
		statementOnlyPredicates.add (NS.RDF + "type");
	}
	
	private static Set<String> literalOnlyPredicates;
	static {
		literalOnlyPredicates = new HashSet<String>();
		literalOnlyPredicates.add (NS.RDFS + "comment");
		literalOnlyPredicates.add (NS.RDFS + "label");
	}
			
	@Override
	public void validateStatement(Object s, Object p, Object o) 
	{
		if (literalOnlyPredicates.contains(StringUtils.safeToString(p)))
		{
			throw new RuntimeException ("You must not use predicate " + p + " in a non-literal triple");
		}
	}

	// more strict formatting for URIs
	// maximum length accepted by virtuoso is ~1900 characters
	private static Pattern strictValidUriPattern = Pattern.compile("^(DOI:|http://)[^\\x00-\\x20>]+[/#][^\\x00-\\x20>]{1,1890}$");

	@Override
	public void validateUri(String uri) 
	{
		if (!strictValidUriPattern.matcher(uri).matches())
		{
			throw new RuntimeException ("URI Fails strict validation: '" + uri + "'");
		}
		
	}

	@Override
	public void validateLiteral(Object o) 
	{
		if (o instanceof String)
		{
			String os = (String)o;
			if (os.equals ("null")) 
				throw new RuntimeException ("Literal Fails strict validation: '" + os + "'");
			
			if (os.startsWith("\"") || os.endsWith("\""))
			{
				log.warn("Warning: literal triple contains quotes at start or end: '" + os + "'");;
			}
			
			// TODO: method for collecting warnings...
			if (os.startsWith ("http://"))
			{
				log.warn("Warning: literal triple looks like URI: '" + os + "'");
			}
		}
	}
}
