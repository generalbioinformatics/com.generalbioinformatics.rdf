/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.generalbioinformatics.rdf.stream.RdfNode;

import nl.helixsoft.recordstream.DefaultRecord;
import nl.helixsoft.recordstream.Record;
import nl.helixsoft.recordstream.RecordMetaData;
import nl.helixsoft.recordstream.ResultSetRecordStream;
import nl.helixsoft.recordstream.StreamException;
import virtuoso.jdbc3.VirtuosoExtendedString;
import virtuoso.jdbc3.VirtuosoRdfBox;

/**
 * Turn a Virtuso {@link java.sql.ResultSet} into a record stream.
 * This is an extension of ResultsSetRecordStream with some virtuoso-specific processing.
 */
public class VirtuosoRecordStream extends ResultSetRecordStream
{
	public VirtuosoRecordStream(ResultSet wrapped) throws StreamException {
		super(wrapped);
	}

	public VirtuosoRecordStream(ResultSet wrapped, Statement st, Connection con) throws StreamException {
		super(wrapped, st, con);
	}

	@Override
	protected Record processRow(ResultSet rs) throws SQLException 
	{
		RecordMetaData rmd = getMetaData();
		
		Object[] data = new Object[rmd.getNumCols()];
		
		// attempt to replace Virtuoso-specific classes with generic classes as much as possible.
		// to meet the contract of TripleStore.sparqlSelect.
		// use RdfNode for URI's
		// use Long, Int, Boolean, String, etc. for literal classes.
		for (int col = 1; col <= rmd.getNumCols(); ++col)
		{
			Object o = rs.getObject(col);
			Object result = o;
			if (o instanceof VirtuosoExtendedString)
			{
				VirtuosoExtendedString vo = (VirtuosoExtendedString)o;
				if (vo.getStrType() != 1)
				{
					result = vo.toString(); // literal String
				}
				else
				{
					switch (vo.getIriType())
					{
					case VirtuosoExtendedString.IRI: // normal
						result = RdfNode.createUri(vo.toString());
						break;
					case VirtuosoExtendedString.BNODE: // anonymous
						result = RdfNode.createAnon(vo.toString().replaceAll("nodeID://", ""));
						break;
					default:
						throw new IllegalStateException("Unexpected iri type: " + vo.getIriType());
					}
				}
			}
			else if (o instanceof VirtuosoRdfBox)
			{
				// language attribute and other type information will be lost this way.
				result = ((VirtuosoRdfBox)o).rb_box;
			}
			// get rid of VirtuosoExtendedString as it doesn't implement equals and HashCode properly
			data[col-1] = result;
		}
		Record result = new DefaultRecord(rmd, data);
		return result;
	}

	
}