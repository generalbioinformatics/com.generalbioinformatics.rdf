package com.generalbioinformatics.rdf.gui;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.generalbioinformatics.rdf.gui.MarrsException;
import com.generalbioinformatics.rdf.gui.MarrsProject;
import com.generalbioinformatics.rdf.gui.MarrsQuery;
import com.generalbioinformatics.rdf.gui.MarrsQuery.QueryType;

public class TestQuerySubstitution
{
	MarrsProject p;
	MarrsQuery mq;
	
	@Before
	public void setup()
	{
		p = new MarrsProject();
		mq = new MarrsQuery("test1", "select ?s ?p ?o FROM ${GRAPH} where { ?s ?p ?o . FILTER ?s IN (${ID|uri-list}) }", QueryType.QUERY_BACKBONE);
		p.addQuery(mq);
	}
	
	@Test
	public void test1()
	{
		Assert.assertEquals("uri-list", p.getQueryParameterFilter(mq, "ID"));

		Map<String, String> params = p.getQueryParameters(mq);
		Assert.assertEquals (2, params.size());
		Assert.assertEquals ("uri-list", params.get("ID"));
		Assert.assertTrue (params.containsKey("GRAPH"));
		Assert.assertNull (params.get("GRAPH"));
	}

	@Test
	public void testMissingParameter() throws MarrsException 
	{
		try
		{
			p.getSubstitutedQuery(mq);
			Assert.fail("Expected Marrs Exception");
		}
		catch (MarrsException e)
		{
			// OK, expected exception
		}
		
		p.getParameterModel().put("GRAPH", "<http://www.helixsoft.nl>");	
		p.getParameterModel().put("ID", "<http://identifiers.org/uniprot/P1234> <http://identifiers.org/ncbigene/3643>");	
		
		String q = p.getSubstitutedQuery(mq);
		Assert.assertEquals ("select ?s ?p ?o FROM <http://www.helixsoft.nl> where { ?s ?p ?o . FILTER ?s IN (<http://identifiers.org/uniprot/P1234> <http://identifiers.org/ncbigene/3643>) }", q);
	}

	@Test
	public void testBadUriList() throws MarrsException 
	{
		p.getParameterModel().put("GRAPH", "<http://www.helixsoft.nl>");	
		
		try
		{
			p.getParameterModel().put("ID", "<http://identifiers.org/uni");	
			p.getSubstitutedQuery(mq);
			Assert.fail("Expected Marrs Exception");
		}
		catch (MarrsException e) { /* OK, expected exception */ }

		try
		{
			p.getParameterModel().put("ID", "<http://identifiers.org/uniprot/P1234> <http://identifiers.org/ncbigene/");	
			p.getSubstitutedQuery(mq);
			Assert.fail("Expected Marrs Exception");
		}
		catch (MarrsException e) { /* OK, expected exception */ }

		try
		{
			p.getParameterModel().put("ID", "<http://identifiers.org<uniprot/P1234>");	
			p.getSubstitutedQuery(mq);
			Assert.fail("Expected Marrs Exception");
		}
		catch (MarrsException e) { /* OK, expected exception */ }

		try
		{
			p.getParameterModel().put("ID", "<http://identifiers.org<uniprot/P1234>");	
			p.getSubstitutedQuery(mq);
			Assert.fail("Expected Marrs Exception");
		}
		catch (MarrsException e) { /* OK, expected exception */ }

	}
	
}
