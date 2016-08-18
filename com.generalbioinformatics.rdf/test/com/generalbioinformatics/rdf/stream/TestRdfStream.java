package com.generalbioinformatics.rdf.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import com.generalbioinformatics.rdf.stream.NtStream;
import com.generalbioinformatics.rdf.stream.RdfStream;
import com.generalbioinformatics.rdf.stream.Statement;
import com.generalbioinformatics.rdf.stream.TripleStream;
import com.generalbioinformatics.rdf.stream.NtStream.ParseException;

//TODO: test on all snippets from http://www.w3.org/TR/REC-rdf-syntax/
public class TestRdfStream extends TestCase
{
	public void testEx07() throws XMLStreamException, ParseException, IOException
	{
		String base = "07";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx08() throws XMLStreamException, ParseException, IOException
	{
		String base = "08";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx09() throws XMLStreamException, ParseException, IOException
	{
		String base = "09";
		try
		{
			exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
		}
		catch (UnsupportedOperationException ex)
		{
			// expected...
		}
	}

	public void testEx10() throws XMLStreamException, ParseException, IOException
	{
		String base = "10";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx11() throws XMLStreamException, ParseException, IOException
	{
		String base = "11";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx12() throws XMLStreamException, ParseException, IOException
	{
		String base = "12";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx13() throws XMLStreamException, ParseException, IOException
	{
		String base = "13";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx14() throws XMLStreamException, ParseException, IOException
	{
		String base = "14";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx15() throws XMLStreamException, ParseException, IOException
	{
		String base = "15";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx16() throws XMLStreamException, ParseException, IOException
	{
		String base = "16";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx17() throws XMLStreamException, ParseException, IOException
	{
		String base = "17";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx18() throws XMLStreamException, ParseException, IOException
	{
		String base = "18";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx19() throws XMLStreamException, ParseException, IOException
	{
		String base = "19";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	public void testEx20() throws XMLStreamException, ParseException, IOException
	{
		String base = "20";
		exampleTestHelper("example" + base + ".nt", "spec20040210-example" + base + ".rdf");
	}

	private void exampleTestHelper(String ntName, String rdfName) throws IOException,
			XMLStreamException, ParseException 
	{
		System.out.println (ntName + " " + rdfName);
		InputStream is;
		TripleStream ts;
		is = this.getClass().getResourceAsStream(ntName);
		assertNotNull ("Could not load resource " + ntName, is);
		ts = new NtStream (is);
		Set<Statement> l = (Set<Statement>)ts.into (new HashSet<Statement>());
	
		System.out.println (l);
		
		is = this.getClass().getResourceAsStream(rdfName);
		assertNotNull ("Could not load resource " + rdfName, is);
		
		ts = new RdfStream(is);
		for (Statement st : ts)
		{
			System.out.println (st);
			assertTrue ("missing triple: " + st, l.remove(st));
		}
		
		assertEquals ("MISSING: " + l, 0, l.size());
	}
	
	public void testSubjectAnon() throws XMLStreamException, ParseException, IOException
	{
		exampleTestHelper("subjectAnon.nt", "subjectAnon.rdf");
	}
	
	public void testTwoTriples() throws XMLStreamException, ParseException, IOException
	{
		String doc = 
			"<?xml version=\"1.0\"?>                                    " +
			"<rdf:RDF                                                   " +
			" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"  " +
			" xmlns:cd=\"http://www.recshop.fake/cd#\">                  " +
			" <rdf:Description                                           " +
			" rdf:about=\"http://www.recshop.fake/cd/Empire Burlesque\"> " +
			"  <cd:artist>Bob Dylan</cd:artist>                          " +
			"  <cd:country>USA</cd:country>                              " +
			" </rdf:Description>                                         " +
			"</rdf:RDF>                                                 ";
	
		InputStream is = new ByteArrayInputStream(doc.getBytes());
		TripleStream ts = new RdfStream(is);
		Statement st = ts.getNext();
		
		// first triple
		assertEquals ("http://www.recshop.fake/cd/Empire Burlesque", st.getSubjectUri());
		assertEquals ("http://www.recshop.fake/cd#artist", st.getPredicateUri());
		assertEquals ("\"Bob Dylan\"", st.getFormattedObjectString());

		st = ts.getNext();
		// second triple
		assertEquals ("http://www.recshop.fake/cd/Empire Burlesque", st.getSubjectUri());
		assertEquals ("http://www.recshop.fake/cd#country", st.getPredicateUri());
		assertEquals ("\"USA\"", st.getFormattedObjectString());
		
		st = ts.getNext();
		assertNull (st);
	}

	public void testType() throws XMLStreamException, ParseException, IOException
	{
		String doc = 
			"<?xml version=\"1.0\"?>                                     " +
			"<rdf:RDF                                                    " +
			" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"  " +
			" xmlns:cd=\"http://www.recshop.fake/cd#\">                  " +
			" <cd:CD                                                     " +
			" rdf:about=\"http://www.recshop.fake/cd/Empire Burlesque\"> " +
			"  <cd:country>USA</cd:country>                              " +
			" </cd:CD>                                                   " +
			"</rdf:RDF>                                                  ";
	
		InputStream is = new ByteArrayInputStream(doc.getBytes());
		TripleStream ts = new RdfStream(is);
		Statement st = ts.getNext();
		
		// first triple
		assertEquals ("http://www.recshop.fake/cd/Empire Burlesque", st.getSubjectUri());
		assertEquals ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", st.getPredicateUri());
		assertEquals ("http://www.recshop.fake/cd#CD", st.getObjectUri());

		st = ts.getNext();
		// second triple
		assertEquals ("http://www.recshop.fake/cd/Empire Burlesque", st.getSubjectUri());
		assertEquals ("http://www.recshop.fake/cd#country", st.getPredicateUri());
		assertEquals ("\"USA\"", st.getFormattedObjectString());
		
		st = ts.getNext();
		assertNull (st);
	}

	public void testNested() throws XMLStreamException, ParseException, IOException
	{
		String doc = 
			"<?xml version=\"1.0\"?>                                       " +
			"<rdf:RDF                                                      " +
			" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"    " +
			" xmlns:cd=\"http://www.recshop.fake/cd#\">                    " +
			" <rdf:Description                                             " +
			" rdf:about=\"http://www.recshop.fake/cd/Empire Burlesque\">   " +
			"  <cd:artist>                                                 " +
			"   <rdf:Description                                           " +
			"     rdf:about=\"http://www.recshop.fake/artist/Bob Dylan\">  " +
			"		<cd:name>Bob Dylan</cd:name>                           " +
			"   </rdf:Description>                                         " + 
			"  </cd:artist>                                                " +
			"  <cd:country>USA</cd:country>                                " +
			" </rdf:Description>                                           " +
			"</rdf:RDF>                                                    ";
	
		InputStream is = new ByteArrayInputStream(doc.getBytes());
		TripleStream ts = new RdfStream(is);
		Statement st = ts.getNext();
		
		// first triple
		assertEquals ("http://www.recshop.fake/cd/Empire Burlesque", st.getSubjectUri());
		assertEquals ("http://www.recshop.fake/cd#artist", st.getPredicateUri());
		assertEquals ("http://www.recshop.fake/artist/Bob Dylan", st.getObjectUri());

		st = ts.getNext();
		// second triple
		assertEquals ("http://www.recshop.fake/artist/Bob Dylan", st.getSubjectUri());
		assertEquals ("http://www.recshop.fake/cd#name", st.getPredicateUri());
		assertEquals ("\"Bob Dylan\"", st.getFormattedObjectString());

		st = ts.getNext();
		// third triple
		assertEquals ("http://www.recshop.fake/cd/Empire Burlesque", st.getSubjectUri());
		assertEquals ("http://www.recshop.fake/cd#country", st.getPredicateUri());
		assertEquals ("\"USA\"", st.getFormattedObjectString());
		
		st = ts.getNext();
		assertNull (st);
	}

	public void testGoHeader() throws XMLStreamException, ParseException, IOException
	{
		/** Snippet from the Gene Ontology, which has a different header */
		
		String doc = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +
		"<!DOCTYPE go:go PUBLIC \"-//Gene Ontology//Custom XML/RDF Version 2.0//EN\" \"http://www.geneontology.org/dtd/go.dtd\"> \n" +
		"\n" +
		"<go:go xmlns:go=\"http://www.geneontology.org/dtds/go.dtd#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"> \n" +
		"    <rdf:RDF> \n" +
		"        <go:term rdf:about=\"http://www.geneontology.org/go#GO:0000001\"> \n" +
		"            <go:accession>GO:0000001</go:accession> \n" +
		"            <go:name>mitochondrion inheritance</go:name> \n" +
		"            <go:synonym>mitochondrial inheritance</go:synonym> \n" +
		"            <go:definition>The distribution of mitochondria, including the mitochondrial genome, into daughter cells after mitosis or meiosis, mediated by interactions between mitochondria and the cytoskeleton.</go:definition> \n" +
		"            <go:is_a rdf:resource=\"http://www.geneontology.org/go#GO:0048308\" /> \n" +
		"            <go:is_a rdf:resource=\"http://www.geneontology.org/go#GO:0048311\" /> \n" +
		"        </go:term> \n" +
	    "    </rdf:RDF>  \n" +
        "</go:go>  \n";

		InputStream is = new ByteArrayInputStream(doc.getBytes());
		TripleStream ts = new RdfStream(is);
		Statement st = ts.getNext();

		// first triple
		assertEquals ("http://www.geneontology.org/go#GO:0000001", st.getSubjectUri());
		assertEquals ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", st.getPredicateUri());
		assertEquals ("http://www.geneontology.org/dtds/go.dtd#term", st.getObjectUri());
	}

	public void testRdfResource() throws XMLStreamException, ParseException, IOException
	{
		String doc = 
		"<rdf:RDF xmlns:go=\"http://www.geneontology.org/dtds/go.dtd#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"> \n" +
		"<go:term rdf:about=\"http://www.geneontology.org/go#GO:0045569\"> \n" +
        "	<go:accession>GO:0045569</go:accession> \n" +
        "	<go:is_a rdf:resource=\"http://www.geneontology.org/go#GO:0005515\" /> \n" +
        "</go:term> \n" +
	    "</rdf:RDF> \n";

		InputStream is = new ByteArrayInputStream(doc.getBytes());
		TripleStream ts = new RdfStream(is);
		Statement st = ts.getNext();

		// first triple
		assertEquals ("http://www.geneontology.org/go#GO:0045569", st.getSubjectUri());
		assertEquals ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", st.getPredicateUri());
		assertEquals ("http://www.geneontology.org/dtds/go.dtd#term", st.getObjectUri());

		st = ts.getNext();
		assertEquals ("http://www.geneontology.org/go#GO:0045569", st.getSubjectUri());
		assertEquals ("http://www.geneontology.org/dtds/go.dtd#accession", st.getPredicateUri());
		assertEquals ("\"GO:0045569\"", st.getFormattedObjectString());

		st = ts.getNext();
		assertEquals ("http://www.geneontology.org/go#GO:0045569", st.getSubjectUri());
		assertEquals ("http://www.geneontology.org/dtds/go.dtd#is_a", st.getPredicateUri());
		assertEquals ("http://www.geneontology.org/go#GO:0005515", st.getObjectUri());
		
		st = ts.getNext();
		assertNull (st);
	}

	public void testRdfParseType() throws XMLStreamException, ParseException, IOException
	{
		String doc = 
		"<rdf:RDF xmlns:go=\"http://www.geneontology.org/dtds/go.dtd#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"> \n" +
		"<go:term rdf:about=\"http://www.geneontology.org/go#GO:0045569\"> \n" +
        "	<go:dbxref rdf:parseType=\"Resource\"> \n" +
        "		<go:database_symbol>PIRSF</go:database_symbol> \n" +
        "		<go:reference>PIRSF037867</go:reference> \n" +
        "	</go:dbxref> \n" +
        "	<go:dbxref rdf:parseType=\"Resource\"> \n" +
        "		<go:database_symbol>InterPro</go:database_symbol> \n" +
        "		<go:reference>IPR016610</go:reference> \n" +
        "	</go:dbxref> \n" +
        "</go:term> \n" +
	    "</rdf:RDF> \n";

		InputStream is = new ByteArrayInputStream(doc.getBytes());
		TripleStream ts = new RdfStream(is);
		Statement st = ts.getNext();
	
		// first triple
		assertEquals ("http://www.geneontology.org/go#GO:0045569", st.getSubjectUri());
		assertEquals ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", st.getPredicateUri());
		assertEquals ("http://www.geneontology.org/dtds/go.dtd#term", st.getObjectUri());

		st = ts.getNext();
		assertEquals ("http://www.geneontology.org/go#GO:0045569", st.getSubjectUri());
		assertEquals ("http://www.geneontology.org/dtds/go.dtd#dbxref", st.getPredicateUri());
		assertTrue (st.isObjectAnon());

		st = ts.getNext();
		assertTrue (st.isSubjectAnon());
		assertEquals ("http://www.geneontology.org/dtds/go.dtd#database_symbol", st.getPredicateUri());
		assertEquals ("\"PIRSF\"", st.getFormattedObjectString());

		st = ts.getNext();
		assertTrue (st.isSubjectAnon());
		assertEquals ("http://www.geneontology.org/dtds/go.dtd#reference", st.getPredicateUri());
		assertEquals ("\"PIRSF037867\"", st.getFormattedObjectString());

		st = ts.getNext();
		assertEquals ("http://www.geneontology.org/go#GO:0045569", st.getSubjectUri());
		assertEquals ("http://www.geneontology.org/dtds/go.dtd#dbxref", st.getPredicateUri());
		assertTrue (st.isObjectAnon());

		st = ts.getNext();
		assertTrue (st.isSubjectAnon());
		assertEquals ("http://www.geneontology.org/dtds/go.dtd#database_symbol", st.getPredicateUri());
		assertEquals ("\"InterPro\"", st.getFormattedObjectString());

		st = ts.getNext();
		assertTrue (st.isSubjectAnon());
		assertEquals ("http://www.geneontology.org/dtds/go.dtd#reference", st.getPredicateUri());
		assertEquals ("\"IPR016610\"", st.getFormattedObjectString());

		st = ts.getNext();
		assertNull (st);
	}
}
