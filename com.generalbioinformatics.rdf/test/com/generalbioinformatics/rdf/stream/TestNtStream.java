package com.generalbioinformatics.rdf.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.generalbioinformatics.rdf.stream.NtStream;
import com.generalbioinformatics.rdf.stream.Statement;
import com.generalbioinformatics.rdf.stream.NtStream.ParseException;

import junit.framework.TestCase;

public class TestNtStream extends TestCase
{
	public void testWhitespace() throws IOException, ParseException
	{
		String doc = 
				"<ra> <pa> <rb> .\n" + // base pattern
				"<ra> <pa> <rb> .\r" + // mac line end
				"<ra> <pa> <rb> .\r\n" + // dos line end
				"<ra> <pa> <rb>.\n" + // no white space before dot 
				"<ra> <pa> <rb>. \n" + // whitespace after dot
				"<ra> <pa>\t<rb> .\n" + // tab instead of whitespace
				" <ra> <pa> <rb> .\n" + // leading whitespace
				"<ra>  <pa> <rb> .\n" + // double whitespace
				"";
		InputStream is = new ByteArrayInputStream(doc.getBytes());
		
		NtStream sns = new NtStream(is);
		
		Statement st;
		
		for (int i = 0; i < 8; ++i)
		{
			st = sns.getNext();
			
			assertEquals ("<ra>", st.getSubjectString());
			assertEquals ("<pa>", st.getPredicateString());
			assertEquals ("<rb>", st.getFormattedObjectString());
		}
		
		st = sns.getNext();
		assertNull (st);
	}

	public void testComment() throws IOException, ParseException
	{
		String doc = 
				"#Comment line\n" +
				"<ra> <pa> <rb> .\n" +
				"";
		InputStream is = new ByteArrayInputStream(doc.getBytes());
		
		NtStream sns = new NtStream(is);
		
		Statement st;
		
		st = sns.getNext();
		assertEquals ("<ra>", st.getSubjectString());
		assertEquals ("<pa>", st.getPredicateString());
		assertEquals ("<rb>", st.getFormattedObjectString());

		st = sns.getNext();
		assertNull (st);
		
	}

	public void testAnon() throws IOException, ParseException
	{
		String doc = 
				"_:n0 <pa> <rb> .\n" +
				"<ra> <pa> _:n1 .\n" +
				"";
		InputStream is = new ByteArrayInputStream(doc.getBytes());
		
		NtStream sns = new NtStream(is);
		
		Statement st;
		
		st = sns.getNext();
		assertEquals ("_:n0", st.getSubjectString());
		assertEquals ("<pa>", st.getPredicateString());
		assertEquals ("<rb>", st.getFormattedObjectString());
		
		assertEquals ("rb", st.getObjectUri());
		assertEquals ("_:n0", st.getSubjectUri());
		assertEquals ("pa", st.getPredicateUri());
		
		st = sns.getNext();
		assertEquals ("<ra>", st.getSubjectString());
		assertEquals ("<pa>", st.getPredicateString());
		assertEquals ("_:n1", st.getFormattedObjectString());

		assertEquals ("ra", st.getSubjectUri());
		assertEquals ("_:n1", st.getObjectUri());

		st = sns.getNext();
		assertNull (st);
		
	}


	public void testLiterals() throws IOException, ParseException
	{
		String doc = 
				"<rc> <pb> \"l1\" .\n" +  // simple literal
				"<re> <pd> \"l3\"@n .\n" + // with language
				"<rd> <pc> \"l2\"^^<j> .\n" + // with type
				"<rf> <pe> \"multi-\\nline\" .\n" + // multi-line
				"<rg> <pf> \"with a quote: \\\"\" .\n" + // quoted
				"";
		InputStream is = new ByteArrayInputStream(doc.getBytes());
		NtStream sns = new NtStream(is);
		
		Statement st;

		st = sns.getNext();
		assertEquals ("<rc>", st.getSubjectString());
		assertEquals ("<pb>", st.getPredicateString());
		assertEquals ("\"l1\"", st.getFormattedObjectString());

		st = sns.getNext();
		assertEquals ("<re>", st.getSubjectString());
		assertEquals ("<pd>", st.getPredicateString());
		assertEquals ("\"l3\"@n", st.getFormattedObjectString());

		st = sns.getNext();
		assertEquals ("<rd>", st.getSubjectString());
		assertEquals ("<pc>", st.getPredicateString());
		assertEquals ("\"l2\"^^<j>", st.getFormattedObjectString());

		st = sns.getNext();
		assertEquals ("<rf>", st.getSubjectString());
		assertEquals ("<pe>", st.getPredicateString());
		assertEquals ("\"multi-\\nline\"", st.getFormattedObjectString());

		st = sns.getNext();
		assertEquals ("<rg>", st.getSubjectString());
		assertEquals ("<pf>", st.getPredicateString());
		assertEquals ("\"with a quote: \\\"\"", st.getFormattedObjectString());

		st = sns.getNext();
		assertNull (st);

	}
	
	public void testUnicode() throws IOException, ParseException
	{
		// a string containing escaped unicode characters.
		// note the double-slashed \\u, meaning the string is fully 7-bit ascii, and conversion to codepoints is done by the NT parser.
		String doc = 
			"<http://example.org/buecher/baum> <http://purl.org/dc/elements/1.1/description> \"Das Buch ist au\\u00DFergew\\u00F6hnlich\"@de . ";
		
		InputStream is = new ByteArrayInputStream(doc.getBytes());
		NtStream sns = new NtStream(is);
		
		Statement st;

		st = sns.getNext();
		assertEquals ("<http://example.org/buecher/baum>", st.getSubjectString());
		assertEquals ("<http://purl.org/dc/elements/1.1/description>", st.getPredicateString());
		assertEquals ("Das Buch ist au\u00DFergew\u00F6hnlich", st.getLiteral());

		st = sns.getNext();
		assertNull (st);

	}
	
	public void testUtf8() throws IOException, ParseException
	{
		// A string containing unicode encoded with utf8
		// note the single-slashed \\u, meaning the codepoints are embedded in java source
		// this is valid Ntriples...
		String utf8doc = 
			"<http://example.org/buecher/baum> <http://purl.org/dc/elements/1.1/description> \"Das Buch ist au\u00DFergew\u00F6hnlich\"@de . ";
		
		InputStream is = new ByteArrayInputStream(utf8doc.getBytes());
		NtStream sns = new NtStream(is);
		
		Statement st;

		st = sns.getNext();
		assertEquals ("<http://example.org/buecher/baum>", st.getSubjectString());
		assertEquals ("<http://purl.org/dc/elements/1.1/description>", st.getPredicateString());
		assertEquals ("Das Buch ist au\u00DFergew\u00F6hnlich", st.getLiteral());

		st = sns.getNext();
		assertNull (st);
		
	}

}
