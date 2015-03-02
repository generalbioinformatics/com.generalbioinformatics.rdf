/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.io.OutputStream;

import nl.helixsoft.recordstream.RecordStream;
import nl.helixsoft.recordstream.StreamException;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * A {@link TripleStore} implementation for SPARQL endpoints,
 * implemented using the Jena library
 */
public class JenaSparqlEndpoint extends AbstractTripleStore
{
	private final String sparql;

	public JenaSparqlEndpoint(String sparql) 
	{
		this.sparql = sparql;
	}

	private String user = null;
	
	public void setUser (String user)
	{
		this.user = user;
	}
	
	private String pass = null;
	
	public void setPass (String pass)
	{
		this.pass = pass;
	}
	
	@Override
	public RecordStream _sparqlSelectDirect(String query) throws StreamException
	{
		//		QueryExecution qe = QueryExecutionFactory.sparqlService(sparql, query);

		// The following avoids the sparql being rejected by Jena
		try
		{
			QueryEngineHTTP qe = new QueryEngineHTTP(sparql, query);
			if (user != null && pass != null)
				qe.setBasicAuthentication(user, pass.toCharArray());
			ResultSet rs = qe.execSelect();
			return new JenaRecordStream (rs);
		}
		catch (Exception e)
		{
			throw new StreamException(e);
		}
	}

//	@Override
	public void sparqlConstruct(String query, OutputStream os)
	{
		Model model = sparqlConstructAsModel(query);
		model.write(os);
	}

	// non-polymorphic method
	public Model sparqlConstructAsModel (String query)
	{
		QueryEngineHTTP qe = new QueryEngineHTTP(sparql, query);
		if (user != null && pass != null)
			qe.setBasicAuthentication(user, pass.toCharArray());
		Model model = qe.execConstruct();
		return model;
	}
	
	@Override
	public int hashCode()
	{
		// hashCode that is somewhat stable between instantiations
		return sparql.hashCode();
	}
	
	@Override
	public String toString()
	{
		return "JenaSparqlEndpoint(" + sparql + ")";
	}

}
