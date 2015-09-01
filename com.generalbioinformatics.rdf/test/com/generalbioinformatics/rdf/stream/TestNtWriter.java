package com.generalbioinformatics.rdf.stream;

import java.io.IOException;

import com.generalbioinformatics.rdf.NS;

import junit.framework.TestCase;
import nl.helixsoft.util.StringUtils;

public class TestNtWriter extends TestCase 
{
	public void testNulls() throws IOException
	{
		try
		{
			NtWriter.writeLiteral(null, "http://example.com/s", "http://example.com/p", 5.0, false);
			fail ("Expected null pointer exception");
		}
		catch (NullPointerException e)
		{
			assertTrue (e.getMessage().contains("writeLiteral")); // exception should mention method
			assertTrue (e.getMessage().contains("os")); // exception should mention problematic argument
		}

		try
		{
			NtWriter.writeLiteral(System.out, "http://example.com/s", null, 5.0, false);
			fail ("Expected null pointer exception");
		}
		catch (NullPointerException e)
		{
			assertTrue (e.getMessage().contains("writeLiteral")); // exception should mention method
			assertTrue (e.getMessage().contains("p")); // exception should mention problematic argument
		}

	}
	
	public void testValidationStrict() throws IOException
	{
		NtWriter writer = new NtWriter (System.out);
		writer.setStrictValidation (true);

		// these will all pass basic validation but fail strict URI validation...
		String[] strictlyInvalid = new String[] {
				"notStrictlyFormattedURL",
				"http://incomplete#",
				"http://incomplete/",
				"https://secure/boo",
				"http://toolong/" + StringUtils.rep("a", 1900)
		};

		// positive control
		
		for (String testUri : strictlyInvalid)
		{
			try
			{
				writer.writeStatement(testUri, "http://example.com#predicate", "http://example.com#object");
				fail ("Should have received RuntimeException");
			}
			catch (RuntimeException ex) { /* expected */ }

			try
			{
				writer.writeStatement("http://example.com#subject", testUri, "http://example.com#object");
				fail ("Should have received RuntimeException");
			}
			catch (RuntimeException ex) { /* expected */ }
		}


		writer.setStrictValidation(false);
		
		// negative control
		
		for (String testUri : strictlyInvalid)
		{
			try
			{
				writer.writeStatement(testUri, "http://example.com#predicate", "http://example.com#object");
				writer.writeStatement("http://example.com#subject", testUri, "http://example.com#object");
				writer.writeStatement("http://example.com#subject", "http://example.com#predicate", testUri);
			}
			catch (RuntimeException ex)
			{
				fail ("Should not have received RuntimeException");
			}		
		}

	}

	public void testValidationStrict2() throws IOException
	{
		NtWriter nt = new NtWriter(System.out);

		try
		{
			nt.writeLiteral (NS.idUniprot + "P42336", NS.RDF + "type", "Protein");
			fail ("Should have received RuntimeException");
		}
		catch (RuntimeException e) { /* expected */ }
		
		try
		{
			nt.writeStatement (NS.idUniprot + "P42336", NS.RDFS + "label", NS.idHgncSymbol + "PIK3CA");
			fail ("Should have received RuntimeException");
		}
		catch (RuntimeException e) { /* expected */ }

	}
	
	public void testValidationEmpty() throws IOException
	{
		NtWriter writer = new NtWriter (System.out);
		
		try
		{
			// problem: empty string instead of uri
			writer.writeStatement("", "http://example.com#predicate", "http://example.com#object");
			fail ("Should have received RuntimeException");
		}
		catch (RuntimeException ex)
		{
			// expected
		}		
	}

	public void testValidationSpace() throws IOException
	{
		NtWriter writer = new NtWriter (System.out);
		
		try
		{
			// problem: space at end of uri
			writer.writeStatement("http://example.com#subject ", "http://example.com#predicate", "http://example.com#object");
			fail ("Should have received RuntimeRxception");
		}
		catch (RuntimeException ex)
		{
			// expected
		}		
	}

	public void testValidationIllegal() throws IOException
	{
		NtWriter writer = new NtWriter (System.out);
		
		// correct - no exception
		writer.writeStatement("http://example.com#subject", "http://example.com#predicate", "http://example.com#object");
		
		try
		{
			// problem: newline in uri
			writer.writeStatement("http://example.com\nsubject", "http://example.com#predicate", "http://example.com#object");
			fail ("Should have received RuntimeRxception");
		}
		catch (RuntimeException ex)
		{
			// expected
		}		
	}
	
	public void testEscapeLiteral()
	{
		assertEquals ("coke\\tsprite", NtWriter.escapeString("coke\tsprite", false));
		assertEquals ("unix\\nmac\\rdos\\r\\n", NtWriter.escapeString("unix\nmac\rdos\r\n", false));
		assertEquals ("euro\\u20AC", NtWriter.escapeString("euro\u20AC", true));
		assertEquals ("euro\u20AC", NtWriter.escapeString("euro\u20AC", false));
		assertEquals ("single'double\\\"", NtWriter.escapeString("single'double\"", false));
	}
	

}
