package com.generalbioinformatics.rdf.stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.helixsoft.recordstream.Function;
import nl.helixsoft.recordstream.Predicate;

public abstract class InferenceFunctions 
{
	public static boolean anyStartsWith(Statement st, String prefix)
	{
		return (
				st.getSubjectUri().startsWith(prefix) ||
				st.getPredicateUri().startsWith(prefix) ||
				(
					!(st.isLiteral() || st.isObjectAnon()) && 
					st.getSubjectUri().startsWith(prefix)
				)
			);
	}
	
	public static boolean prefixReplacement(Statement st, String prefix, String replacement)
	{
		boolean result = false;
		
		String s = st.getSubjectUri();
		if (s.startsWith(prefix))
		{
			s = replacement + s.substring(prefix.length());
			st.setSubjectUri(s);
			result = true;
		}
		
		String p = st.getPredicateUri();
		if (p.startsWith(prefix))
		{
			p = replacement + p.substring(prefix.length());
			st.setPredicateUri(p);
			result = true;
		}

		if (!(st.isLiteral() || st.isObjectAnon()))
		{
			String o = st.getObjectUri();
			if (o.startsWith(prefix))
			{
				o = replacement + o.substring(prefix.length());
				st.setObjectUri(o);
				result = true;
			}
		}
		
		return result;
	}

	private InferenceFunctions() { } /* never instantiate */
	
	public static class ReplacePrefixes implements Function<Statement, Statement> 
	{
		//TODO: using PrefixTreeMap potentially faster
		private final Map<String, String> prefixMap = new HashMap<String, String>();
		private int count = 0;
		
		public int getCount() 
		{ 
			return count; 
		}
		
		ReplacePrefixes (String prefix, String replacement)
		{
			prefixMap.put (prefix, replacement);
		}

		ReplacePrefixes (Map<String, String> prefixes)
		{
			prefixMap.putAll(prefixes);
		}

		@Override
		public Statement apply(Statement t)
		{
			for (Map.Entry<String, String> e : prefixMap.entrySet())
			{
				boolean result = prefixReplacement (t, e.getKey(), e.getValue());
				
				//TODO: count per prefix?
				if (result) count++;
			}
			return t;
		}
	
	}
	
	public static class UriReplace implements Function<Statement, Statement> 
	{
		private int count = 0;
		private final Pattern pat;
		private final String replacement;
		
		public int getCount() 
		{ 
			return count; 
		}
		
		UriReplace (String pattern, String replacement)
		{
			this.pat = Pattern.compile(pattern);
			this.replacement = replacement;
		}

		@Override
		public Statement apply(Statement st)
		{
			boolean result = false;
			
			String s = st.getSubjectUri();
			Matcher mats = pat.matcher(s);
			if (mats.find())
			{
				s = mats.replaceFirst(replacement);
				st.setSubjectUri(s);
				result = true;
			}
			
			String p = st.getPredicateUri();
			Matcher matp = pat.matcher(p); 
			if (matp.find())
			{
				p = matp.replaceFirst(replacement);
				st.setPredicateUri(p);
				result = true;
			}

			if (!(st.isLiteral() || st.isObjectAnon()))
			{
				String o = st.getObjectUri();
				Matcher mato = pat.matcher(o); 
				if (mato.find())
				{
					o = mato.replaceFirst(replacement);
					st.setObjectUri(o);
					result = true;
				}
			}

			if (result) count++;
			return st;
		}
	
	}
	/**
	 * A predicate-predicate:
	 * A predicate function (meaning a function that has a yes/no result) 
	 * to filter for statements that have a certain predicate (meaning the second part of an rdf statement)
	 */
	public static class WithPredicate implements Predicate<Statement>
	{
		private final String uri;
		private int count = 0;
		
		WithPredicate (String uri)
		{
			this.uri = uri;
		}
		
		@Override
		public boolean accept(Statement st) 
		{
			boolean result = st.getPredicateUri().equals(uri);
			if (result) count++;
			return result;
		}
		
		public int getCount() { return count; }
	}
	
	public static class WithoutUriPatterns implements Predicate<Statement>
	{
		List<Pattern> cpatterns = new ArrayList<Pattern>();
		
		public WithoutUriPatterns(List<String> patterns)
		{
			for (String pat : patterns)
			{
				cpatterns.add(Pattern.compile(pat));
			}
		}

		private boolean anyMatch(String s)
		{
			for (Pattern cpat : cpatterns)
			{
				if (cpat.matcher(s).find())
				{
					return true;
				}
			}
			return false;
		}
		
		@Override
		public boolean accept(Statement st) 
		{
			String s = st.getSubjectUri();
			if (anyMatch(s))
			{
				return false;
			}
			
			String p = st.getPredicateUri();
			if (anyMatch(p))
			{
				return false;
			}

			if (!(st.isLiteral() || st.isObjectAnon()))
			{
				String o = st.getObjectUri();
				if (anyMatch(o))
				{
					return false;
				}
			}
			
			return true;
		}
	
	}
}
