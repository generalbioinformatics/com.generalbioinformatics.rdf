package com.generalbioinformatics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.generalbioinformatics.rdf.VirtuosoConnection;
import com.generalbioinformatics.rdf.stream.RdfNode;
import com.generalbioinformatics.rdf.stream.Statement;

import nl.helixsoft.recordstream.Record;
import nl.helixsoft.recordstream.RecordStream;
import nl.helixsoft.recordstream.Stream;
import nl.helixsoft.recordstream.StreamException;
public class TestVirtuosoRecordStream
{

	/**
	 * This test only works if there is a local virtuoso running on port 8890
	 */
	VirtuosoConnection con;
	boolean localVirtuosoRunning;
	
	@Before
	public void setUp() throws IOException, ClassNotFoundException
	{
		try
		{
			con = new VirtuosoConnection("localhost");
			
			// run test query
			con.sparqlSelect("SELECT (1 as ?a) WHERE {}"); 
			localVirtuosoRunning = true;
		}
		catch (StreamException e)
		{
			localVirtuosoRunning = false;
		}
	}
	
	@Test
	public void testSparqlSelect() throws IOException
	{
		assumeTrue(localVirtuosoRunning);
		
		// test detection of anonymous nodes...
		RecordStream rs = con.sparqlSelect(
				"SELECT (1 as ?one) (\"hello\" as ?hello) (<http://example.org#uri> as ?uri) (\"3.14\"^^<http://www.w3.org/2001/XMLSchema#float> as ?pi)"
				+ "WHERE { }");
		
		Record r = rs.iterator().next();
		
		for (int i = 0; i < r.getMetaData().getNumCols(); ++i)
		{
			Object o = r.get(i);
			System.out.println (o + " -> " + o.getClass());
		}
		
		assertEquals (RdfNode.class, r.get("uri").getClass());
		assertTrue (((RdfNode)r.get("uri")).isUri());
		
		assertEquals (Float.class, r.get("pi").getClass());
		
		assertEquals (Short.class, r.get("one").getClass());
		assertEquals ((short)1, r.get("one"));
		
		assertEquals (String.class, r.get("hello").getClass());
		assertEquals ("hello", r.get("hello"));
	}
	
	@Test
	public void testStatementStream() throws StreamException
	{
		assumeTrue(localVirtuosoRunning);
		
		Stream<Statement> stst = con.sparqlSelectAsStatementStream(
				"SELECT (<http://example.org#subject> as ?s) (<http://example.org#predicate> as ?p) (\"3.14\"^^<http://www.w3.org/2001/XMLSchema#float> as ?o) WHERE {}");
		
		Statement st = stst.iterator().next();
		
		
		assertEquals ("http://example.org#subject", st.getSubjectUri());
		assertEquals ("http://example.org#predicate", st.getPredicateUri());
		
		assertEquals ("3.14", st.getLiteral()); //NOTE: maybe future implementation will expose this as Float object.
	}	
	
}
