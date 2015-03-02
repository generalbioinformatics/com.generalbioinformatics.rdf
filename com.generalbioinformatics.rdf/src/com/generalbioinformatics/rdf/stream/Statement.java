/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import nl.helixsoft.util.ObjectUtils;
import nl.helixsoft.util.StringUtils;

/**
 * Represents a single Triple from a stream.
 * <p>
 * TODO: examine possibility of merging with Jena Statement
 * <p>
 * This is a Value type, no overriding possible
 */
public final class Statement 
{
	private String sUri;
	private boolean sIsAnon;
	private String oUri;
	private String lit; // parsed, unescaped literal
	private String pUri;
	private String litLang; // must be upper or lowercase letters with one dash. E.g. "en", "sp", "en-us". 
	private String litType;
	private boolean oIsAnon;
	private boolean fLiteral;
	
	public boolean isLiteral()
	{
		return fLiteral;
	}
	
	public Statement()
	{
	}

	/**
	 * Use RdfNode helper to set Id and isAnon values simultaneously. 
	 * <br>
	 * This is a convenience method. In some circumstances so it could be slightly more
	 * efficient to call setSubjectUri setSubjectAnon directly.
	 */
	public void setSubject(RdfNode n)
	{
		this.sUri = n.getUri();
		this.sIsAnon = n.isAnon();
	}

	public void setObject(RdfNode n)
	{
		this.oUri = n.getUri();
		this.oIsAnon = n.isAnon();
	}

	public void setPredicate(RdfNode n)
	{
		this.pUri = n.getUri();
	}

	public void setSubjectUri(String sUri)
	{
		this.sUri = sUri;
		this.sIsAnon = false;
	}

	public void setSubjectAnon(String sUri)
	{
		this.sUri = sUri;
		this.sIsAnon = true;
	}

	public void setPredicateUri(String pUri)
	{
		this.pUri = pUri;
	}

	public void setObjectUri(String oUri)
	{
		this.oUri = oUri;
		this.fLiteral = false;
		this.oIsAnon = false;
	}

	public void setObjectAnon(String oUri) 
	{
		this.oUri = oUri;
		this.fLiteral = false;
		this.oIsAnon = true;
	}

	public void setLiteral(String lit)
	{
		// TODO: type etc.
		this.lit = lit;
		this.fLiteral = true;
	}
	
	public String getSubjectUri() 
	{
		return sUri;
	}

	/**
	 * get the predicate part as bracketed &lt;URI&gt;, or without brackets in 
	 * case this is an anonymous identifier.
	 */
	public String getSubjectString() 
	{
		return sIsAnon ? sUri : '<' + sUri + '>';
	}

	/**
	 * get the predicate part as bracketed &lt;URI&gt;
	 */
	public String getPredicateString() {
		return '<' + pUri + '>';
	}

	/**
	 * get the object part, either as bracketed &lt;URI&gt;, anonymous identifier 
	 * without brackets, or as literal string, quoted, escaped, and including optional language and type specifiers
	 */
	public String getFormattedObjectString() 
	{
		if (fLiteral) 
		{
			StringBuilder result = new StringBuilder();
			result.append ("\"");
			result.append (NtWriter.escapeString(lit, false));
			result.append ("\"");
			if (litType != null)
			{
				result.append ("^^<");
				result.append (litType);
				result.append (">");
			}
			if (litLang != null)
			{
				result.append ("@");
				result.append (litLang);
			}
			return result.toString();
		}
		else
			return oIsAnon ? oUri : '<' + oUri + '>';
	}

	/**
	 * Write this triple to an outputstream, formatted exactly
	 * according to N-Triple format.
	 */
	public void write (OutputStream os) throws IOException
	{
		// subject
		if (sIsAnon)
		{
			os.write((sUri == null ? "null" : sUri).getBytes());
		}
		else
		{
			os.write ('<');
			os.write ((sUri == null ? "null" : sUri).getBytes());
			os.write ('>');
		}
		os.write (' ');
		
		// predicate
		os.write ('<');
		os.write ((pUri == null ? "null" : pUri).getBytes());
		os.write ('>');
		os.write (' ');

		// object
		if (fLiteral)
		{
			NtWriter.writeEscapedString(os, lit);
			if (litType != null)
			{
				os.write ('^');
				os.write ('^');
				os.write ('<');
				os.write (litType.getBytes());
				os.write ('>');
			}
			
			if (litLang != null)
			{
				os.write ('@');
				os.write (litLang.getBytes());
			}
		}
		else
		{
			if (oIsAnon)
				os.write ((oUri == null ? "null" : oUri).getBytes());
			else
			{
				os.write ('<');
				os.write ((oUri == null ? "null" : oUri).getBytes());
				os.write ('>');
			}
		}
		os.write (' ');
		os.write ('.');
		os.write ('\n');
	}
	
	@Override
	/**
	 * String representation of the triple, formatted exactly according to N-Triple format.
	 */
	public String toString()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			write (baos);
			return new String (baos.toByteArray());
		} catch (IOException e) {
			return "IOException: " + e.getMessage();
		}
	}

	/**
	 * @returns the predicate as URI.
	 */
	public String getPredicateUri() 
	{
		return pUri;
	}

	/**
	 * @returns the URI of the object part of the triple, or null if this is a Literal statement.
	 */
	public String getObjectUri() 
	{
		return oUri;
	}
	
	/**
	 * @returns true if the object is anonymous, i.e. it's a reference to a blank node.
	 */
	public boolean isObjectAnon()
	{
		return oIsAnon;
	}

	public boolean isSubjectAnon() 
	{
		return sIsAnon;
	}

	public void setLiteralType(String value) 
	{
		litType = value;
	}

	public void setLiteralLanguage(String value) 
	{
		litLang = value;
	}

	/**
	 * Returns the literal value, unescaped, without language & type parts.
	 */
	public String getLiteral() 
	{
		return lit;
	}

	@Override
	public int hashCode()
	{
		return toString().toLowerCase().hashCode();
	}
	
	/**
	 * Not sure if I want to keep this. At the moment, only used for unit test.
	 */
	@Override
	public boolean equals(Object other)
	{
		Statement st = (Statement)other;
		if (st == null) return false;
		
		return
			ObjectUtils.safeEquals(sUri, st.sUri) &&
			ObjectUtils.safeEquals(oUri, st.oUri) &&
			ObjectUtils.safeEquals(pUri, st.pUri) &&
			ObjectUtils.safeEquals(lit, st.lit) &&
			StringUtils.safeEqualsIgnoreCase(litLang, st.litLang) &&
			ObjectUtils.safeEquals(litType, st.litType) &&
			(sIsAnon == st.sIsAnon) &&
			(oIsAnon == st.oIsAnon) &&
			(fLiteral == st.fLiteral);
	}
	
}
