/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.io.OutputStream;
import java.util.Iterator;

import com.generalbioinformatics.rdf.stream.Statement;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import nl.helixsoft.recordstream.AbstractStream;
import nl.helixsoft.recordstream.RecordStream;
import nl.helixsoft.recordstream.Stream;
import nl.helixsoft.recordstream.StreamException;

/**
 * A {@link TripleStore} implementation for SPARQL endpoints,
 * implemented using the Jena Query Engine.
 * <p>
 * Can optionally use HTTP Basic authentication to connect to the sparql endpoint.
 */
public class JenaSparqlEndpoint extends AbstractTripleStore
{
	private final String sparql;

	/** Wrap a sparql endpoint using Jena Query Engine. */
	public JenaSparqlEndpoint(String sparql) 
	{
		this.sparql = sparql;
	}

	private String user = null;
	
	/** set the username for HTTP Basic Authentication */
	public void setUser (String user)
	{
		this.user = user;
	}
	
	private String pass = null;
	
	/** set the password for HTTP Basic Authentication */
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
			return new JenaRecordStream (rs, qe);
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

	/** Non-polymorphic, implementation-specifc method: get the result of a construct query as a Jena Model. */
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

	@Override
	public Stream<Statement> sparqlConstruct(String query) throws StreamException 
	{
		Model model = sparqlConstructAsModel(query);
		StmtIterator it = model.listStatements();
		Stream<Statement> result = new JenaModelStream(it);
				
		return result;
	}

	private class JenaModelStream extends AbstractStream<Statement>
	{
		private final StmtIterator it;
		
		JenaModelStream (StmtIterator it)
		{
			this.it = it;
		}

		@Override
		public Iterator<Statement> iterator() 
		{
			return new Iterator<Statement>()
			{
				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public Statement next() 
				{
					Statement result = new Statement();
					com.hp.hpl.jena.rdf.model.Statement st = it.next();
					
					if (st.getSubject().isAnon())
					{
						result.setSubjectAnon(st.getSubject().getId().toString());
					}
					else
					{
						result.setSubjectUri(st.getSubject().getURI());
					}
					
					result.setPredicateUri(st.getPredicate().getURI());
					
					if (st.getObject().isAnon())
					{
						result.setObjectAnon(st.getObject().asResource().getId().toString());
					}
					else if (st.getObject().isResource())
					{
						result.setObjectUri(st.getObject().asResource().getURI());
					}
					else
					{
						result.setLiteral(st.getLiteral().getValue());
					}
					
					return result;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Read-only iterator, remove not implemented");
				}
			};
			
			
		}
	}
}
