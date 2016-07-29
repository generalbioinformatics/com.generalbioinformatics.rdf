/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.util.List;

import nl.helixsoft.recordstream.AbstractRecordStream;
import nl.helixsoft.recordstream.DefaultRecord;
import nl.helixsoft.recordstream.DefaultRecordMetaData;
import nl.helixsoft.recordstream.Record;
import nl.helixsoft.recordstream.RecordMetaData;
import nl.helixsoft.recordstream.StreamException;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

/**
 * A wrapper for OpenRdf {@link TupleQueryResult} implementing the RecordStream interface.
 */
public class OpenRdfRecordStream extends AbstractRecordStream
{
	private final TupleQueryResult tqs;
	private final RecordMetaData rmd;
	
	/** wrap an OpenRdf TupleQueryResult */
	public OpenRdfRecordStream(TupleQueryResult tqs) throws StreamException
	{
		this.tqs = tqs;
		List<String> names = tqs.getBindingNames();
		rmd = new DefaultRecordMetaData (names);
	}
	
	@Override
	public Record getNext() throws StreamException 
	{
		try {
			if (!tqs.hasNext()) return null;
			
			Object[] fields = new Object[rmd.getNumCols()];
			
			BindingSet b;
			b = tqs.next();
			for (int i = 0; i < rmd.getNumCols(); ++i)
				fields[i] = b.getBinding(rmd.getColumnName(i)).getValue();
			
			return new DefaultRecord(rmd, fields);
		} 
		catch (QueryEvaluationException e) 
		{
			throw new StreamException(e);
		}
		
	}

	@Override
	public RecordMetaData getMetaData() 
	{
		return rmd;
	}

	@Override
	public void close() {
		try {
			tqs.close();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
