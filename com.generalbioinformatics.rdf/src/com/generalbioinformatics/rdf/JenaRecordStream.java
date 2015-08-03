/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.util.ArrayList;
import java.util.List;

import nl.helixsoft.recordstream.AbstractRecordStream;
import nl.helixsoft.recordstream.DefaultRecord;
import nl.helixsoft.recordstream.DefaultRecordMetaData;
import nl.helixsoft.recordstream.Record;
import nl.helixsoft.recordstream.RecordMetaData;
import nl.helixsoft.recordstream.StreamException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Wrapper for a Jena {@link ResultSet}
 */
public class JenaRecordStream extends AbstractRecordStream
{
	private final ResultSet rs;
	private QuerySolution next;
	private List<String> names = new ArrayList<String>();
//	private Map<String, Integer> map = new HashMap<String, Integer>();
	
	private final RecordMetaData rmd;
	
	public JenaRecordStream(ResultSet rs) 
	{
		this.rs = rs;
		
		int i = 0;
		for (String name : rs.getResultVars())
		{
			names.add(name);
//			map.put(name, i);
			i++;			
		}
		
		if (rs.hasNext())
		{
			this.next = rs.next();
		}
		else
		{
			this.next = null;
		}
		
		rmd = new DefaultRecordMetaData(names);
	}

	@Override
	public Record getNext() throws StreamException
	{
		if (next == null) return null;
		
		Object[] fields = new Object[names.size()];
		
		int i = 0;
		for (String name : names)
		{
			RDFNode node = next.get(name);
			if (node == null)
			{
				fields[i] = null;
			}
			else if (node.isLiteral())
			{
				fields[i] = node.asLiteral().getValue();
			}
			else
			{
				fields[i] = node.asResource().toString();				
			}
			i++;
		}
		
		Record result =  new DefaultRecord (rmd, fields); 
		
		if (rs.hasNext())
			next = rs.next();
		else
			next = null;
		
		return result;
	}

	@Override
	public RecordMetaData getMetaData() 
	{
		return rmd;
	}

	@Override
	public void close() { }

}
