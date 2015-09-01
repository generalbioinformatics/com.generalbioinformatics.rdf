/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.helixsoft.recordstream.RecordStream;
import nl.helixsoft.recordstream.StreamException;
import nl.helixsoft.util.FileUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;

/**
 * A triple store backed by a simple file in Rdf or TTL format, using Jena as intermediate.
 */
public class TripleFile extends AbstractTripleStore
{
	private Model _model = null;
	private File[] rdfFile = null;
	
	// lazy initialization of model
	private Model getModel() throws IOException
	{
		if (_model == null)
		{
			if (rdfFile == null || rdfFile.length == 0)
			{
				throw new IllegalStateException("No model loaded, no file specified");
			}
			
			_model = ModelFactory.createDefaultModel();
			
			for (File f : rdfFile)
			{
				InputStream fis = FileUtils.openZipStream(f); 
				_model.read(fis, null, deduceLang(f));
				fis.close();
			}
		}
		
		return _model;
	}

	
	public TripleFile (InputStream in, String lang) throws IOException
	{
		_model = ModelFactory.createDefaultModel();
		_model.read(in, null, lang);
		in.close();
	}

	/**
	 * Empty model. The only use of this is to use it to call
	 * other sparql endpoints with SERVICE queries.
	 */
	public TripleFile ()
	{
		_model = ModelFactory.createDefaultModel();
	}
				
	/**
	 * Create a memory triple store from one or more Files. This constructor recognizes the following formats by extension:
	 * .ttl
	 * .ttl.gz
	 * .nt
	 * .nt.gz
	 * .rdf
	 * .rdf.gz
	 * .owl
	 * .owl.gz
	 */
	public TripleFile (File... aRdfFile) throws IOException
	{
		assert (aRdfFile != null);
		assert (aRdfFile.length > 0);
		rdfFile = aRdfFile;
	}

	//TODO: move to utility class?
	public static String deduceLang(File aRdfFile) 
	{
		return deduceLang (aRdfFile.getName());
	}

	//TODO: move to utility class?
	public static String deduceLang(String aRdfFile) 
	{
		String lang = "RDF/XML";
		
		// remove any .gz extension
		String name = aRdfFile.replaceAll(".gz$", ""); 
		
		if (name.endsWith(".ttl"))
		{
			lang = "TURTLE";
		}
		else if (name.endsWith (".nt"))
		{
			lang = "N-TRIPLE";
		}
		return lang;
	}

	/**
	 * Allows overriding the syntax used, using one of the constants from com.hp.hpl.jena.query.Syntax. By Default, SPARQL 1.1
	 */
	public void setSyntax(Syntax value)
	{
		this.syntax = value;
	}
	
	private Syntax syntax = Syntax.syntaxSPARQL_11;
	
	@Override
	public RecordStream _sparqlSelectDirect(String query) throws StreamException 
	{
		try
		{
			Query q = QueryFactory.create(query, syntax);
	
			// Execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.create(q, getModel());
			ResultSet results = qe.execSelect();	
			return new JenaRecordStream(results, qe);
		}
		catch (JenaException ex)
		{
			throw new StreamException(ex);
		} catch (IOException ex) {
			throw new StreamException(ex);
		}
	}

	@Override
	public void sparqlConstruct(String query, OutputStream os) throws StreamException 
	{
		Query q = QueryFactory.create(query, syntax);
		
		// Execute the query and obtain results
		try {
			QueryExecution qe = QueryExecutionFactory.create(q, getModel());
			Model resultModel = qe.execConstruct();
			resultModel.write(os, "N-TRIPLE", null);		
		} catch (IOException ex) {
			throw new StreamException(ex);
		}
		
	}
	
}
