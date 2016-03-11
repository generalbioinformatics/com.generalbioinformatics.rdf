package com.generalbioinformatics.rdf.stream;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.generalbioinformatics.rdf.NS;

import nl.helixsoft.util.StringUtils;

public class DefaultNtStreamValidator extends AbstractNtStreamValidator
{
	public enum Rule { LITERAL_EMPTY_STRING, LITERAL_NULL, LITERAL_START_OR_END_QUOTE, LITERAL_LOOKS_LIKE_URI };
	
	{
		// default rule settings
		ruleConfig.put(Rule.LITERAL_EMPTY_STRING, Level.ERROR);
		ruleConfig.put(Rule.LITERAL_NULL, Level.ERROR);
		ruleConfig.put(Rule.LITERAL_START_OR_END_QUOTE, Level.WARN);
		ruleConfig.put(Rule.LITERAL_LOOKS_LIKE_URI, Level.WARN);
	}
	
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

			applyRule (os.equals ("null"), 
					Rule.LITERAL_NULL, "Literal Fails strict validation: '" + os + "'");
			
			applyRule (os.equals(""), 
					Rule.LITERAL_EMPTY_STRING, "Literal triple contains empty string: '" + os + "'");
			
			applyRule (os.startsWith("\"") || os.endsWith("\""), 
					Rule.LITERAL_START_OR_END_QUOTE, "Literal triple contains quotes at start or end: '" + os + "'");
			
			applyRule (os.startsWith ("http://"), 
					Rule.LITERAL_LOOKS_LIKE_URI, "Literal triple looks like URI: '" + os + "'");
			
		}
	}

}
