/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import nl.helixsoft.util.DebugUtils;


//		ntripleDoc 	::= 	line* 	 
//		line 	::= 	ws* (comment | triple) ? eoln 	 
//		comment 	::= 	'#' (character - ( cr | lf ) )* 	 
//		triple 	::= 	subject ws+ predicate ws+ object ws* '.' ws* 	 
//		subject 	::= 	uriref | namedNode 	 
//		predicate 	::= 	uriref 	 
//		object 	::= 	uriref | namedNode | literal 	 
//		uriref 	::= 	'<' absoluteURI '>' 	 
//		namedNode 	::= 	'_:' name 	 
//		literal 	::= 	'"' string '"' 	 
//		ws 	::= 	space | tab 	 
//		eoln 	::= 	cr | lf | cr lf 	 
//		space 	::= 	#x20 /* US-ASCII space - decimal 32 */ 	 
//		cr 	::= 	#xD /* US-ASCII carriage return - decimal 13 */ 	 
//		lf 	::= 	#xA /* US-ASCII linefeed - decimal 10 */ 	 
//		tab 	::= 	#x9 /* US-ASCII horizontal tab - decimal 9 */ 	 
//		string 	::= 	character* with escapes. Defined in section Strings 	 
//		name 	::= 	[A-Za-z][A-Za-z0-9]* 	 
//		absoluteURI 	::= 	( character - ( '<' | '>' | space ) )+ 	 
//		character 	::= 	[#x20-#x7E] /* US-ASCII space to decimal 127 */ 	

/**
 * Format data as N-TRIPLES
 * <p>
 * Primarily used for formatting Sparql query results. This is a stream-based
 * writer that does not need to have all data in memory at once.
 */
public class NtWriter implements INtWriter 
{	
	private final OutputStream os;
	private static final String CHARSET = "UTF-8"; // NT is always in UTF-8 (even on windows), in accordance with N-triple specs. 
	
	private boolean validate = true; // perform validation checks before writing
	private NtStreamValidator strictValidator = new DefaultNtStreamValidator(); // perform a level of validation that is more strict than just the bare minimum defined by RDF 
	private boolean escapeUnicode = false;
	
	private long stmtCount = 0;
	
	public NtWriter (OutputStream os)
	{
		this.os = os;
	}

	@Override
	public void flush() throws IOException
	{
		os.flush();
	}

	public static void writeComment(Writer fos, String comment) throws IOException
	{
		DebugUtils.testNull ("fos", fos, "comment", comment);
		for (String s : comment.split ("\n"))
		{
			fos.write ('#');
			fos.write (s);
			fos.write ('\n');
		}
	}

	public static void writeComment(OutputStream os, String comment) throws IOException
	{
		DebugUtils.testNull ("os", os, "comment", comment);
		for (String s : comment.split ("\n"))
		{
			os.write ('#');
			os.write (s.getBytes());
			os.write ('\n');
		}
	}

	public static void writeEscapedString (OutputStream fos, String s) throws IOException
	{
		writeEscapedString(fos, s, false);
	}
	
	public static void writeEscapedString (OutputStream fos, String s, boolean escapeUnicode) throws IOException
	{		
		// Perform prescribed escaping
		// TODO: more efficient using state engine
		String s2 = escapeString(s, escapeUnicode);
		fos.write ('"');
		fos.write (s2.getBytes(CHARSET));
		fos.write ('"');
	}

	public void write(Statement st) throws IOException 
	{
		stmtCount++;
		if (validate)
		{
			if (!st.isSubjectAnon()) { validateUri (st.getSubjectUri()); }
			validateUri (st.getPredicateUri());
			if (!st.isObjectAnon() && !st.isLiteral()) validateUri (st.getObjectUri());
		}
		st.write (os, escapeUnicode);
	}

	@Override
	public void writeStatement(Object s, Object p, Object o) throws IOException 
	{
		stmtCount++;
		writeStatement (s.toString(), p.toString(), o.toString());
	}
	
	// basically checks against invalid patterns.
	//	# Match empty string, unprintable characters, space, and '>'
	private static Pattern basicInvalidUriPattern = Pattern.compile("(^$|[\\x00-\\x20>])");
	
	private void validateUri(String uri)
	{
		DebugUtils.testNull ("uri", uri);
		
		if (strictValidator != null)
		{
			strictValidator.validateUri(uri);
		}
		else
		{
			if (basicInvalidUriPattern.matcher(uri).find())
			{
				throw new RuntimeException ("URI is empty or contains illegal characters (" + uri + ")");
			}
		}

	}
	
	public void writeStatement(String s, String p, String o) throws IOException 
	{
		if (validate)
		{
			if (strictValidator != null)
			{
				strictValidator.validateStatement(s, p, o);
			}
			validateUri (s);
			validateUri (p);
			validateUri (o);
		}
		
		stmtCount++;
		writeResource (s);
		os.write (' ');
		writeResource (p);
		os.write (' ');
		writeResource (o);
		os.write (' ');
		os.write ('.');
		os.write ('\n');
	}

	public static <T> void writeLiteral(OutputStream os, String s, String p, T o) throws IOException 
	{
		writeLiteral (os, s, p, o, false);
	}

	public static void writeLiteral(OutputStream os, String s, String p, String o) throws IOException 
	{
		DebugUtils.testNull ("os", os, "s", s, "p", p, "o", o);

		writeResource (os, s);
		os.write (' ');
		writeResource (os, p);
		os.write (' ');
		writeEscapedString(os, o, false);
		os.write (' ');
		os.write ('.');
		os.write ('\n');		
	}
	

	static Map<Class<?>, String> rdfTypes = new HashMap<Class<?>, String>();
	static {
		rdfTypes.put (Boolean.class, "boolean");
		rdfTypes.put (Integer.class, "int");
		rdfTypes.put (Double.class, "double");
		rdfTypes.put (Float.class, "float");
		rdfTypes.put (Date.class, "date");
		rdfTypes.put (Long.class, "long");
	}

	private static SimpleDateFormat xsdDate = new SimpleDateFormat ("yyyy-MM-dd");

	public static <T> void writeLiteral(OutputStream os, String s, String p, T o, boolean escapeUnicode) throws IOException 
	{	
		DebugUtils.testNull ("os", os, "p", p, "o", o);
		
		writeResource (os, s);
		os.write (' ');
		writeResource (os, p);
		os.write (' ');
		if (o.getClass() == Date.class)
		{
			os.write ('"');
			xsdDate.format(o).getBytes();
			os.write (xsdDate.format(o).getBytes(CHARSET));
			os.write ('"');
		}
		else if (o instanceof String)
		{
			writeEscapedString(os, (String)o, escapeUnicode);
		}
		else
		{
			writeEscapedString(os, o.toString(), escapeUnicode);
		}		

		if (rdfTypes.containsKey(o.getClass()))
		{
			os.write ("^^<http://www.w3.org/2001/XMLSchema#".getBytes(CHARSET));
			String rdfType = rdfTypes.get (o.getClass());			
			os.write (rdfType.getBytes(CHARSET));
			os.write ('>');
		}
		os.write (' ');
		os.write ('.');
		os.write ('\n');		
	}

	private void validateLiteral (Object o)
	{
		DebugUtils.testNull ("o", o);
		
		if (strictValidator != null)
		{
			strictValidator.validateLiteral(o);
		}
	}
	
	@Override
	public void writeLiteral(Object s, Object p, Object o) throws IOException 
	{
		if (validate)
		{
			if (strictValidator != null)
			{
				strictValidator.validateLiteral(s, p, o);
			}	
			validateUri (s.toString());
			validateUri (p.toString());
			validateLiteral (o);
		}
		
		stmtCount++;
		NtWriter.writeLiteral(os, s.toString(), p.toString(), o, escapeUnicode);
	}

	public void writeLiteral(String s, String p, Object o) throws IOException 
	{
		if (validate)
		{
			if (strictValidator != null)
			{
				strictValidator.validateLiteral(s, p, o);
			}	
			validateUri (s);
			validateUri (p);
			validateLiteral (o);
		}
		
		stmtCount++;
		NtWriter.writeLiteral(os, s, p, o, escapeUnicode);
	}

	/** returns the number of statements (literal and non-literal) */
	public long getStatementCount()
	{
		return stmtCount;
	}
	

	private void writeResource(String s) throws IOException 
	{
		NtWriter.writeResource(os, s);
	}

	private static void writeResource(OutputStream os, String s) throws IOException 
	{
		os.write ('<');
		os.write (s.getBytes(CHARSET));
		os.write ('>');
	}

	public static String escapeString(String s, boolean escapeUnicode) 
	{
		StringBuilder result = new StringBuilder();

		// note that this doesn't handle codepoints outside the BMP
		for (int i = 0, len = s.length(); i < len; ++i) 
		{
		    char c = s.charAt(i);
		    switch (c)
		    {
		    case '\\': result.append ("\\\\"); break;
		    case '\n': result.append ("\\n"); break;
		    case '\t': result.append ("\\t"); break;
		    case '\r': result.append ("\\r"); break;
		    case '"': result.append ("\\\""); break;
	    	default: {
	    			if (c > 127 && escapeUnicode)
	    			{
	    				result.append (String.format ("\\u%04X", (int)c));
	    			}
	    			else
	    			{
	    				result.append (c);
	    			}
	    			break;
	    		} 
		    }
		    
		}
		return result.toString();
	}

	/**
	 * If escapeUnicode is true, then codepoints above 127 will be escaped with \\u
	 * If escapeUnicode is false, then codepoints above 127 will be encoded as UTF-8 format.
	 * <p>
	 * Both are valid according to n-triple spec, but some tools can only handle one of the two properly,
	 * so sometimes there is a reason to prefer one format over the other. 
	 * (e.g rapper doesn't handle utf-8, and it's difficult to convert utf-8 to \\u escaped data with PHP...)  
	 * <p>
	 * Default value is false.
	 */
	public void setEscapeUnicode(boolean value) 
	{
		escapeUnicode = value;
	}
	
	public void setStrictValidation(boolean value) 
	{
		if (value)
		{
			strictValidator = new DefaultNtStreamValidator();
			validate = true;
		}
		else
		{
			strictValidator = null;
		}
		
	}

	public NtStreamValidator getValidator() { return strictValidator; }

	/**
	 * Supply a different triple validator instead of the default one.
	 * You may set this to null, in which case strict validation is disabled.
	 */
	public void setValidator(NtStreamValidator value) 
	{ 
		strictValidator = value;
		if (value != null) validate = true;
	}

}
