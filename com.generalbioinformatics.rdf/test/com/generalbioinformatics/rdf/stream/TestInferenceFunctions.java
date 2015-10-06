package com.generalbioinformatics.rdf.stream;

import java.util.Arrays;

import com.generalbioinformatics.rdf.stream.InferenceFunctions.UriReplace;
import com.generalbioinformatics.rdf.stream.InferenceFunctions.WithoutUriPatterns;

import junit.framework.TestCase;

public class TestInferenceFunctions extends TestCase
{

	public void testUriReplace()
	{
		Statement st = new Statement ();
		st.setSubjectUri("http://purl.uniprot.org/uniprot/Q704S8");	
		st.setPredicateUri("http://purl.uniprot.org/uniprot/organism");
		st.setObjectUri("http://identifiers.org/taxonomy/10116");
		
		UriReplace repl = new UriReplace("http://purl.uniprot.org/(uniprot/[A-Z0-9])",
				"http://identifiers.org/$1");
		repl.apply(st);
		
		assertEquals("http://identifiers.org/uniprot/Q704S8", st.getSubjectUri());	 // replaced!
		assertEquals("http://purl.uniprot.org/uniprot/organism", st.getPredicateUri()); // not replaced!
		assertEquals("http://identifiers.org/taxonomy/10116", st.getObjectUri());
		
	}
	
	public void testWithoutPattern()
	{
		Statement st = new Statement ();
		st.setSubjectUri("http://purl.uniprot.org/position/22860153289390126tt1");	
		st.setPredicateUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		st.setObjectUri("http://biohackathon.org/resource/faldo#Position");
		
		WithoutUriPatterns pat = new WithoutUriPatterns(Arrays.asList(new String[] {"http://purl.uniprot.org/(position|range)"}));
		assertFalse (pat.accept(st));
	}
}
