/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import nl.helixsoft.recordstream.AbstractStream;
import nl.helixsoft.recordstream.IteratorHelper;
import nl.helixsoft.recordstream.NextUntilNull;
import nl.helixsoft.recordstream.StreamException;
import virtuoso.jdbc3.VirtuosoExtendedString;
import virtuoso.jdbc3.VirtuosoRdfBox;

import com.generalbioinformatics.rdf.stream.Statement;

/**
 * TODO: this shares a lot of code with nl.helixsoft.recordstream.ResultSetRecordStream
 */
public class VirtuosoStatementStream extends AbstractStream<Statement> implements NextUntilNull<Statement>
{
	private final ResultSet rs;
	private boolean closed = false;
	
	public VirtuosoStatementStream(ResultSet wrapped) throws IOException 
	{ 
		this.rs = wrapped;
	}

	@Override
	public Iterator<Statement> iterator()
	{
		return new IteratorHelper<Statement>(this);
	}

	@Override
	public Statement getNext() throws StreamException 
	{
		try {
			if (closed)
				return null;
			
			if (!rs.next()) 
			{
				close();
				return null;
			}
			
			Statement result = new Statement();
			
			Object s = rs.getObject("s");
			Object p = rs.getObject("p");
			Object o = rs.getObject("o");

			if (s instanceof VirtuosoExtendedString)
			{
				VirtuosoExtendedString vs = (VirtuosoExtendedString)s;
				switch (vs.getIriType())
				{
				case VirtuosoExtendedString.IRI: // normal
					result.setSubjectUri(vs.toString());
					break;
				case VirtuosoExtendedString.BNODE: // anonymous
					result.setSubjectAnon(vs.toString().replaceAll("nodeID://", "_:"));
					break;
				default:
					throw new IllegalStateException("Unexpected iri type: " + vs.getIriType());
				}
			}
			else
			{
				throw new IllegalStateException("Unexpected class: " + o.getClass());
			}
			
			// get rid of VirtuosoExtendedString as it doesn't implement equals and HashCode properly
			if (o instanceof VirtuosoExtendedString)
			{
				VirtuosoExtendedString vo = (VirtuosoExtendedString)o;
				if (vo.getStrType() != 1)
				{
					result.setLiteral(vo.toString());
				}
				else
				{
					switch (vo.getIriType())
					{
					case VirtuosoExtendedString.IRI: // normal
						result.setObjectUri(vo.toString());
						break;
					case VirtuosoExtendedString.BNODE: // anonymous
						result.setObjectAnon(vo.toString().replaceAll("nodeID://", "_:"));
						break;
					default:
						throw new IllegalStateException("Unexpected iri type: " + vo.getIriType());
					}
				}
			}
			else if (o instanceof VirtuosoRdfBox)
			{
				VirtuosoRdfBox vrb = (VirtuosoRdfBox)o;
				result.setLiteral(vrb.toString());				
				result.setLiteralType(vrb.getType());				
				result.setLiteralLanguage(vrb.getLang());
			}
			else if (o instanceof Long)
			{
				//TODO: merge with NtWriter...
				result.setLiteral(o.toString());
				result.setLiteralType(NS.XSD + "long");
			}
			else if (o instanceof BigDecimal)
			{
				//TODO: merge with NtWriter...
				result.setLiteral(o.toString());
				result.setLiteralType(NS.XSD + "double");
			}
			else if (o instanceof Integer)
			{
				//TODO: merge with NtWriter...
				result.setLiteral(o.toString());
				result.setLiteralType(NS.XSD + "int");
			}
			else if (o instanceof Short)
			{
				//TODO: merge with NtWriter...
				result.setLiteral(o.toString());
				result.setLiteralType(NS.XSD + "short");
			}
			else if (o instanceof Boolean)
			{
				//TODO: merge with NtWriter...
				result.setLiteral(o.toString());
				result.setLiteralType(NS.XSD + "boolean");
			}
			else if (o instanceof Double)
			{
				//TODO: merge with NtWriter...
				result.setLiteral(o.toString());
				result.setLiteralType(NS.XSD + "double");
			}
			else if (o instanceof Float)
			{
				//TODO: merge with NtWriter...
				result.setLiteral(o.toString());
				result.setLiteralType(NS.XSD + "float");
			}
			else if (o instanceof Date)
			{
				//TODO: merge with NtWriter...
				SimpleDateFormat xsdDate = new SimpleDateFormat ("yyyy-MM-dd");
				result.setLiteral(xsdDate.format((Date)o));
				result.setLiteralType(NS.XSD + "date");
			}
			else
			{
				throw new IllegalStateException("Unexpected class: " + o.getClass());
			}
			
			result.setPredicateUri(p.toString());
			
			
			return result;
		} catch (SQLException ex) {
			throw new StreamException(ex);
		}
	}

	private void close() throws SQLException 
	{
		closed = true;
		rs.close();
	}
	
	@Override
	public void finalize()
	{
		try {
			if (!closed) rs.close();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
}