/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.IOException;
import java.io.InputStream;

import nl.helixsoft.util.ParseBuffer;

/**
 * My own implementation of a NT file parser, specifically
 * designed to handle huge files in a limited amount of memory.
 * <p>
 * The regular Jena API is designed to load entire files in memory,
 * and doesn't work well if the input files are too large. Maybe one
 * day Jena will support similar features and this class will be obsolete.
 * <p> 
 * Baded on the following resources:
 * This grammar: http://www.w3.org/2001/sw/RDFCore/ntriples/ is out of date and doesn't contain all possibilities.
 * Also based on http://www.w3.org/2011/rdf-wg/wiki/N-Triples-Format
 * 
 * TODO: could use some extra testing for UTF handling
 */
public class NtStream extends AbstractTripleStream
{	
	/**
	 * Simple Exception type to indicate a formatting error in the input file.
	 */
	public static class ParseException extends Exception
	{
		ParseException (String msg)
		{
			super(msg);
		}
	
		public ParseException(Throwable cause) 
		{
			super(cause);
		}
	}

	private final ParseBuffer is;
	
	private int start;
	private int end;
	private int line = 0;
	private int col = 0;
	
	public NtStream (InputStream is) throws IOException
	{
		// Wrapping the InputStream in a BufferedInputStream can give a speed boost.
		// ParseBuffer is like BufferedInputStream, but gives another speed boost because of zero-copy mechanic.
		this.is = new ParseBuffer(is);
		next = this.is.peek();
	}
	
	int next;
	
	/** consume one character from the stream */
	private void eat() throws IOException
	{
		col++;
		is.read();
		next = is.peek();
	}
	
	/** consume a URI between '<' and '>' */
	private boolean eatResource() throws IOException
	{	
		if (!eatChar('<')) return false;
		if (!eatURI()) return false;
		if (!eatChar('>')) return false;
		
		return true;
	}
	
	/** consume a URI, but allows some illegal characters that occur all too frequently in practice */
	private boolean eatURI() throws IOException 
	{
		start = is.getPos();
		int count = 0;
		
		while (true)
		{
			switch (next)
			{
			case '>':
				if (count == 0)
				{
					error = "Zero-length URI found";
					return false;
				}
				end = is.getPos();
				return true;
			case '<':
			case ' ':
			case '\t':
				// especially spaces do occur in URI's sometimes.
				// TODO: make fatal / warning: a configurable option
				System.out.println ("WARNING: illegal character in URI: '" + (char)next + "', so far: " + is.subString(start, is.getPos()));
				//NB: No break on purpose.
			default:
				eat();
				count++;
			}
		}
	}


	private String error;
	
	private boolean eatChar(int c) throws IOException 
	{
		if (next == c)
		{
			eat();
			return true;
		}
		else
			error = "Expected '" + (char)c + "' but found '" + (char)next + "'";
			return false;
	}


	/** eat zero or more white space characters */
	private boolean eatWhiteSpace(int min) throws IOException 
	{
		int count = 0;
		
		while (true)
		{
			switch (next)
			{
			case ' ':
			case '\t':
				eat();
				count++;
				break;
			default:
				if (count < min)
				{
					error = "Whitespace expected, but found '" + (char)next + "'";
					return false;
				}
				else return true;
			}
		}
		
	}
	
	public boolean eatLang() throws IOException
	{
		start = is.getPos();
		if (!Character.isLetter(next))
			return false;
		
		eatChar(next);

		while (true)
		{
			if (Character.isLetter(next) || next == '-')
			{
				//TODO: only one dash allowed, as e.g. @en-us
				eatChar(next);				
			}
			else
			{
				end = is.getPos();
				return true;
			}
		}
	}

	public boolean eatLiteral() throws IOException
	{
		x.setLength(0);
		eatChar ('"');
		if (!eatString())
			return false;
		
		current.setLiteral(x.toString());
		
		switch (next)
		{
		case '^':
			eatChar('^');
			x.setLength(0);
			if (!eatChar('^')) return false;
			if (!eatChar('<')) return false; 
			if (!eatURI()) return false;
			current.setLiteralType (is.subString(start, end));
			if (!eatChar('>')) return false;
			break;
		case '@':
			eatChar('@');
			x.setLength(0);
			if (!eatLang()) return false;
			current.setLiteralLanguage (is.subString(start, end));
		default:
			break;
		}
		eatWhiteSpace(0);
		return true;
	}

	StringBuilder x = new StringBuilder();
	
	private boolean eatString() throws IOException 
	{
		while (true)
		{
			switch (next)
			{
			case '\\':
				eat();
				switch (next)
				{
				case '\\':
				case '"':
					x.append((char)next);
					eat();
					break;
				case 'r':
					x.append('\r');
					eat();
					break;
				case 't':
					x.append('\t');
					eat();
					break;
				case 'n':
					x.append('\n');
					eat();
					break;
				case 'u': // unicode string, e.g. \\u00D6
					// could not find this in the official spec, however, Example08.nt from the 2004 RDF/XML spec has this.
					eat();
					int current = 0;
					for (int i = 0; i < 4; ++i)
					{
						int c = next;
						current *= 16;
						if (c >= '0' && c <= '9')
							current += (c - '0');
						else if (c >= 'A' && c <= 'F')
							current += (c - 'A' + 10);
						else if (c >= 'a' && c <= 'f')
							current += (c - 'a' + 10);
						else 
						{
							error = "Could not parse unicode character, unexpected character " + (char)c;
							return false;
						}
						eat();
					}
					x.append((char)current);
					break;
				default:
					x.append('\\');
					x.append((char)next);
					eat();
					break;
				}
				break;
			case '\n':
			case '\r':
				error = "Literal must be closed before EOL.";
				return false;
			case '"':
				eat();
				return true;
			default:
				x.append((char)next);
				eat();
			}
		}
	}
		
	private boolean eatComment() throws IOException
	{
		if (!eatChar ('#'))
			return false;
		
		while (true)
		{
			switch (next)
			{
			case '\n':
			case '\r':
				return true;
			default:
				eat();
			}
		}
	}
	
	private boolean eatEol() throws IOException
	{
		boolean result = false;
		
		if (next == '\r')
		{
			eat();
			result = true;
		}
		if (next == '\n')
		{
			eat();
			result = true;
		}
		
		if (!result)
			error = "EOL expected, but found '" + next + "'";
		
		line++;
		col = 0;
		
		return result;		
	}
	
	private boolean eatNamedNode() throws IOException
	{
		start = is.getPos();
		eatChar ('_');
		eatChar (':');
		if (!Character.isLetter(next))
			return false;
		eat();
		while (Character.isLetterOrDigit(next))
		{
			x.append((char)next);
			eat();
		}
		end = is.getPos();
		return true;
	}
	
	private Statement current;

	@Override /** @InheritDoc */
	public Statement getNext() throws IOException, NtStream.ParseException
	{
		if (next == -1) return null; // EOF
		current = new Statement();
		eatWhiteSpace(0);
		while (next == '#')
		{
			eatComment();
			if (!eatEol())
				throwParseException();
			eatWhiteSpace(0);
		}
		if (next == -1) return null; // EOF
		
		if (!eatSubject())
			throwParseException();
		if (!eatPredicate())
			throwParseException();
		if (!eatObject())
			throwParseException();
		
		if (!eatChar ('.')) return null;
		eatWhiteSpace(0);
		eatEol();
		
		return current;
	}

	private void throwParseException() throws NtStream.ParseException 
	{
		throw new NtStream.ParseException(error + "\nat line " + line + ":" + col +"\n" + current.toString());
	}

	private boolean eatPredicate() throws IOException 
	{
		if (!eatResource ()) return false;
		current.setPredicateUri(is.subString(start, end));
		if (!eatWhiteSpace(1)) return false;
		return true;
	}

	private boolean eatSubject() throws IOException 
	{
		if (next == '_')
		{
			if (!eatNamedNode()) return false;
			current.setSubjectAnon(is.subString(start, end));
		}
		else
		{
			if (!eatResource ()) return false;
			current.setSubjectUri(is.subString(start, end));
		}
		if (!eatWhiteSpace(1)) return false;
		return true;
	}
	
	public boolean eatObject() throws IOException
	{
		switch (next)
		{
		case '<':
			if (!eatResource()) return false;
			current.setObjectUri(is.subString(start, end));
			break;
		case '_':
			if (!eatNamedNode()) return false;
			current.setObjectAnon(is.subString(start, end));
			break;
		default:
			if (!eatLiteral()) return false;
			break;
		}
		eatWhiteSpace(0);
		return true;
	}
	
}
