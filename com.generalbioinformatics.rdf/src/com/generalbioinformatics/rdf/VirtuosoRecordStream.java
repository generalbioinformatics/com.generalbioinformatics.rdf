/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.generalbioinformatics.rdf.stream.RdfNode;

import nl.helixsoft.recordstream.AbstractRecordStream;
import nl.helixsoft.recordstream.DefaultRecord;
import nl.helixsoft.recordstream.DefaultRecordMetaData;
import nl.helixsoft.recordstream.Record;
import nl.helixsoft.recordstream.RecordMetaData;
import nl.helixsoft.recordstream.StreamException;
import virtuoso.jdbc3.VirtuosoExtendedString;

/**
 * TODO: this shares a lot of code with nl.helixsoft.recordstream.ResultSetRecordStream
 */
public class VirtuosoRecordStream extends AbstractRecordStream
{
	private final ResultSet rs;
	private boolean closed = false;
	private final RecordMetaData rmd;
	
	public VirtuosoRecordStream(ResultSet wrapped) throws StreamException 
	{ 
		this.rs = wrapped;
		try {
			int colNum = rs.getMetaData().getColumnCount();
			List<String> colnames = new ArrayList<String>();
			for (int col = 1; col <= colNum; ++col)
			{
				// use getColumnLabel instead of getColumnName, so ALIASed columns work
				// see: http://bugs.mysql.com/bug.php?id=43684
				colnames.add(rs.getMetaData().getColumnLabel(col));
			}
			rmd = new DefaultRecordMetaData(colnames);
		}
		catch (SQLException ex)
		{
			throw new StreamException(ex);
		}
	}

	@Override
	public Record getNext() throws StreamException 
	{
		try {
			if (closed)
				return null;
			
			if (!rs.next()) 
			{
				close();
				return null;
			}
			
			Object[] data = new Object[rmd.getNumCols()];
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
				// get rid of VirtuosoExtendedString as it doesn't implement equals and HashCode properly
				data[col-1] = result;
			}
			return new DefaultRecord(rmd, data);
		} catch (SQLException ex) {
			throw new StreamException(ex);
		}
	}

	public void close() 
	{
		closed = true;
		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	@Override
	public RecordMetaData getMetaData() 
	{
		return rmd;
	}
}