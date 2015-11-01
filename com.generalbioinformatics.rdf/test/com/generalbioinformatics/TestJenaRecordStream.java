package com.generalbioinformatics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.generalbioinformatics.rdf.TripleFile;
import com.generalbioinformatics.rdf.TripleStore;
import com.generalbioinformatics.rdf.stream.RdfNode;

import nl.helixsoft.recordstream.Record;
import nl.helixsoft.recordstream.RecordStream;

public class TestJenaRecordStream
{
	TripleStore con;
	
	@Before
	public void setUp() throws IOException
	{
		String s = "_:node1 <http://www.example.org#predicate> \"3.14159\"^^<http://www.w3.org/2001/XMLSchema#float> . ";
		ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
		con = new TripleFile(bais, "N-TRIPLE");
	}
	
	@Test
	public void testJena() throws IOException
	{
		// test detection of anonymous nodes...
		RecordStream rs = con.sparqlSelect(
				"SELECT (1 as ?one) (\"hello\" as ?hello) ?s ?p ?o "
				+ "WHERE { ?s ?p ?o }");
		
		Record r = rs.iterator().next();
		
		for (int i = 0; i < r.getMetaData().getNumCols(); ++i)
		{
			Object o = r.get(i);
			System.out.println (o + " -> " + o.getClass());
		}
		
		assertEquals (RdfNode.class, r.get("s").getClass());
		assertTrue (((RdfNode)r.get("s")).isAnon());
		
		assertEquals (RdfNode.class, r.get("p").getClass());
		assertTrue (((RdfNode)r.get("p")).isUri());
		
		assertEquals (Float.class, r.get("o").getClass());
		
		assertEquals (Integer.class, r.get("one").getClass());
		assertEquals (1, r.get("one"));
		
		assertEquals (String.class, r.get("hello").getClass());
		assertEquals ("hello", r.get("hello"));
	}
	
}
