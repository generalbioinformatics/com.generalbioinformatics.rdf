/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.generalbioinformatics.rdf.stream.NtStream.ParseException;

/**
 * This is supposed to read RDF files as a stream, similar to NtStream. However,
 * it's not yet finished. <br>
 * This implementation uses the Streaming API for XML (StAX).
 * <p>
 * Reference: http://www.w3.org/TR/REC-rdf-syntax/
 */
public class RdfStream extends AbstractTripleStream 
{
	private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
	private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	private XMLStreamReader parser;

	ParseState parseState = ParseState.START;
	private String xmlBase;

	private int sequentialId = 1;

	enum ParseState {
		START, RDF_OPEN, NODE, PROPERTY, COLLECTION_NODE, COLLECTION_CHILD, 
	};

	public RdfStream(InputStream parent) throws XMLStreamException,
	ParseException {
		this (parent, null);
	}
	
	public RdfStream(InputStream parent, String aXmlBase) throws XMLStreamException,
			ParseException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // avoid
																	// downloading
																	// DTD
		factory.setProperty(XMLInputFactory.IS_COALESCING, true);
		parser = factory.createXMLStreamReader(parent);
		xmlBase = aXmlBase;
		
		// parse until we've read RDF attributes
		while (parseState != ParseState.NODE) {
			int event = parser.next();
			switch (event) {
			case XMLStreamConstants.START_DOCUMENT:
				// System.out.println ("Start document");
				break;
			case XMLStreamConstants.END_DOCUMENT:
				parser.close();
				throw new NtStream.ParseException("Unexpected end of document");
			case XMLStreamConstants.START_ELEMENT:
				// must be RDF:
				stateStack.push(new State(parseState));
				if (!"RDF".equals(parser.getLocalName())) {
					if (parseState == ParseState.START) {
						parseState = ParseState.RDF_OPEN;
					} else
						throw new NtStream.ParseException(
								"Expected open tag 'RDF'");
				} else {
					parseState = ParseState.NODE; // ready to start parsing
				}
				String _xmlBase = parser.getAttributeValue(
						XML_NS, "base");
				if (_xmlBase != null)
					xmlBase = _xmlBase;
				for (int i = 0; i < parser.getNamespaceCount(); ++i) {
					nsPrefixes.put(parser.getNamespacePrefix(i),
							parser.getNamespaceURI(i));
					// System.out.println (parser.getNamespacePrefix(i) + ":" +
					// parser.getNamespaceURI(i));
				}
				break;
			default:
				System.out.println("Unhandled event: " + event);
				// throw new ParseException ("Unexpected event " + event );
			}
		}
	}

	Map<String, String> nsPrefixes = new HashMap<String, String>();

	Stack<RdfNode> currentNode = new Stack<RdfNode>();

	// normally, the parse types alternate between SUBJECT and PREDICATE (or
	// NODE and PROPERTY)
	// however, parseType=Resource can be used to omit blank nodes.
	// currentType keeps track of when this happens, when unwinding the tag
	// hierarchy
	Stack<State> stateStack = new Stack<State>();

	Queue<Statement> queue = new LinkedList<Statement>();

	private boolean eof = false;

	@Override
	public Statement getNext() throws IOException, NtStream.ParseException {
		if (eof)
			return null;

		if (queue.isEmpty()) {
			fillQueue();
		}

		if (eof)
			return null;

		return queue.remove();
	}

	private String currentLang = null;
	private Integer rdfSequence = null;
	
	private class State {
		public State(ParseState parseState) {
			this.state = parseState;
		}

		final ParseState state;

		RdfNode subject;
		String currentLang;
	}

	private String realId;
	
	private void reifyTriple(String realId, Statement result)
	{
		String realUri = makeUriAbsolute("#" + realId);
		
		Statement st = new Statement();
		st.setSubjectUri(realUri);
		st.setPredicateUri(RDF_NS + "type");
		st.setObjectUri(RDF_NS + "Statement");
		queue.add(st);
		
		st = new Statement();
		st.setSubjectUri(realUri);
		st.setPredicateUri(RDF_NS + "subject");
		st.setObject(result.getSubject());
		queue.add(st);

		st = new Statement();
		st.setSubjectUri(realUri);
		st.setPredicateUri(RDF_NS + "predicate");
		st.setObjectUri(result.getPredicateUri());
		queue.add(st);

		st = new Statement();
		
		st.setSubjectUri(realUri);
		st.setPredicateUri(RDF_NS + "object");
		
		if (result.isLiteral())
		{
			st.setLiteral(result.getLiteral());
			st.setLiteralLanguage(result.getLiteralLanguage());
		}
		else
		{
			if (st.isObjectAnon()) st.setObjectAnon(result.getObjectUri());
			else st.setObjectUri(result.getObjectUri());
		}
		queue.add(st); 

		realId = null;
	}
	
	private void fillQueue() throws ParseException {
		Statement result = new Statement();
		if (!currentNode.isEmpty()) {
			result.setSubject(currentNode.lastElement());
		}

		while (queue.isEmpty() && !eof) {
			try {
				int event;
				event = parser.next();

				switch (event) {
				case XMLStreamConstants.END_DOCUMENT:
					parser.close();
					result = null;
					eof = true;
					break;
				case XMLStreamConstants.START_ELEMENT:

					stateStack.push(new State(parseState));
					// check if there is an xml:lang attribute
					String lang = parser.getAttributeValue(
							XML_NS, "lang");
					if (lang != null) {
						currentLang = lang; // TODO: put this on a stack
					}
					
					String uri = parser.getName().getNamespaceURI()
							+ parser.getName().getLocalPart();
					if (parseState == ParseState.NODE) 
					{
						RdfNode node = parseCurrentNode();
						if (currentNode.isEmpty())
						{
							result.setSubject(node);
						}
						else
						{
							result.setObject(node);
							queue.add(result);
						}
						currentNode.push(node);
						parseTypeTriple(uri);
						parseLiteralPropertyAttributes(new Statement());
						parseState = ParseState.PROPERTY;
					}
					else if (parseState == ParseState.COLLECTION_CHILD)
					{
						// do nothing, just let the stack unwind...
					}
					else if (parseState == ParseState.COLLECTION_NODE)
					{
						// emit triple which is either
						
						// <collectionId> <property> _:anonId 
						// _:previousId rdf:rest _:anonId
												
						String id = generateAnon();
						result.setObjectAnon(id);
						if (result.getPredicateUri() == null) {
							result.setPredicateUri(RDF_NS + "rest");
						}
						queue.add(result);
						
						RdfNode node = parseCurrentNode();
						result = new Statement();
						result.setSubjectAnon(id);
						result.setPredicateUri(RDF_NS + "first");
						result.setObject(node);
						
						queue.add(result);		
						
						// we'll hook on the chain in the next round: 
						currentNode.push(RdfNode.createAnon(id));
						
						parseState = ParseState.COLLECTION_CHILD;
						
					} else if (parseState == ParseState.PROPERTY) {
						if (uri.equals(RDF_NS + "li"))
						{
							result.setPredicateUri(RDF_NS + "_" + (rdfSequence++));
						}
						else
						{
							result.setPredicateUri(uri);
							rdfSequence = null;
						}
						
						// is there an id on this property?
						realId = parser.getAttributeValue (RDF_NS, "ID");

						parseLiteralPropertyAttributes(result);
						RdfNode node = parseCurrentObject();		
						if (node != null) {
							result.setObject(node);
							queue.add(result);
							
							// if there is an id assigned to this property, re-ify the whole triple.
							if (realId != null) { reifyTriple(realId, result); }

							parseState = ParseState.NODE;
						}
						else {
							String dataType = parser
									.getAttributeValue(
											RDF_NS,
											"datatype");
							if (dataType != null) {
								result.setLiteralType(dataType);
							}

							String parseType = parser
									.getAttributeValue(
											RDF_NS,
											"parseType");
							if ("Resource".equals(parseType)) {
								String id = generateAnon();
								result.setObjectAnon(id);
								queue.add(result);
								currentNode.push(RdfNode.createAnon(id));
								parseState = ParseState.PROPERTY;
							} 
							else if ("Literal".equals(parseType)) 
							{								
								String lit = getXmlFragment();
								result.setLiteral(lit);
								result.setLiteralType("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");
								queue.add(result);
								parseState = ParseState.NODE;
							}
							else if ("Collection".equals(parseType))
							{								
								parseState = ParseState.COLLECTION_NODE;
							}
							else if (parseType == null)
							{
								parseState = ParseState.NODE;
							} else {
								throw new ParseException("Unrecognized parse type: " + parseType);
							}
						}
					}
					break;

				case XMLStreamConstants.CHARACTERS:
					if (parser.getText().matches("^\\s*$"))
						break;
					// ignore pure whitespace
					if (parseState == ParseState.NODE) {
						result.setLiteral(parser.getText());
						if (currentLang != null)
							result.setLiteralLanguage(currentLang);
						queue.add(result);
						if (realId != null) { reifyTriple(realId, result); }
					}
					// ignore
					break;
				case XMLStreamConstants.END_ELEMENT:
					if (parseState == ParseState.PROPERTY) {
						if (!currentNode.isEmpty())
							currentNode.pop();

						if (!currentNode.isEmpty())
							result.setSubject(currentNode.lastElement());

					}
					else if (parseState == ParseState.COLLECTION_NODE) {
						System.out.println ("End of collection");
						if (result.getPredicateUri() == null) {
							result.setPredicateUri(RDF_NS + "rest");
						}
						result.setObjectUri(RDF_NS + "nil");
						queue.add(result);
					}
					parseState = stateStack.pop().state;					
					break;
				}

			} catch (XMLStreamException e) {
				throw new NtStream.ParseException(e);
			}
		}
	}

	/** 
	 * Attempt at implementing XMLLiterals.
	 * This almost works, but in the end, there is no 100% reliable way to get the underlying XML data precisely.
	 * <p>
	 * So instead of doing something that works only 90% of the time, we just declare it unsupported. 
	 *  
	 */
	private String getXmlFragment () throws XMLStreamException
	{
		throw new UnsupportedOperationException("parseType=Literal not supported");
		/*
		StringBuilder result = new StringBuilder();
		boolean quit = false;
		int stack = 0;
		while(true)
		{
			int event;
			event = parser.next();
		
			// parse until just before the next start / end event after stack reaches 0.
			if(quit)
			{
				if (event == XMLStreamConstants.START_ELEMENT || event == XMLStreamConstants.END_ELEMENT)
				{
					break;
				}			
			}
			
			int namespaceCount = parser.getNamespaceCount();
            System.out.println("Number of namespaces defined: " + namespaceCount);
            
			switch (event)
			{
			
			case XMLStreamConstants.CHARACTERS: case XMLStreamConstants.CDATA: case XMLStreamConstants.COMMENT: case XMLStreamConstants.SPACE:
				result.append(parser.getText());
				break;
			case XMLStreamConstants.NAMESPACE:
				System.out.println("NAMESPACE");
				break;
			case XMLStreamConstants.ATTRIBUTE:
				System.out.println("ATTRIBUTE");
				break;
			case XMLStreamConstants.START_ELEMENT:
				result.append("<");
				result.append(parser.getPrefix() + ":" + parser.getLocalName());
				for (int i = 0; i < parser.getNamespaceCount(); ++i)
				{
					result.append(" ");
					result.append(parser.getNamespacePrefix(i) + "=\"" + parser.getNamespaceURI(i) + "\"");					
				}			
				
				for (int i = 0; i < parser.getAttributeCount(); ++i)
				{
					result.append(" ");
					result.append(parser.getAttributeLocalName(i) + "=\"" + parser.getAttributeValue(i) + "\"");
				}
				
				result.append(">");
				
				break;
			case XMLStreamConstants.END_ELEMENT:
				result.append("<" + parser.getPrefix() + ":" + parser.getLocalName() + "/>");
			}
			
			if (event == XMLStreamConstants.START_ELEMENT)
			{
				stack++;
			}
			else if (event == XMLStreamConstants.END_ELEMENT)
			{
				stack--;
				if (stack == 0) { quit = true; } // we've reached the end, parse until just before next START / END element. 
			}
		}
		
		return result.toString();
		*/
	}
	
	private void parseTypeTriple(String uri) {
		if (!uri.equals(RDF_NS + "Description")) {
			// emit an rdf:type triple.
			Statement st = new Statement();
			st.setSubject(currentNode.firstElement());
			st.setPredicateUri(RDF_NS + "type");
			st.setObjectUri(uri);
			queue.add(st);
		}
		
		if (uri.equals(RDF_NS + "Seq")) {
			rdfSequence = 1;
		}
	}

	/**
	 * Check for literal properties of the form:
	 * 
	 * <xxx ns:predicate="literal"/>
	 */
	private void parseLiteralPropertyAttributes(Statement result) {
		// check for other attributes...
		for (int i = 0; i < parser.getAttributeCount(); ++i) {
			String ns = parser.getAttributeNamespace(i);
			if (!ns.equals(RDF_NS)
					&& !ns.equals(XML_NS)) {
				String val = parser.getAttributeValue(i);
				String key = parser.getAttributeLocalName(i);

				if (parseState == ParseState.NODE)
				{
					result.setSubject(currentNode.lastElement());
				}
				else
				{
					RdfNode node = RdfNode.createAnon(generateAnon());
					result.setObject(node);
					queue.add(result);
					result = new Statement();
					result.setSubject(node);
				}
				result.setPredicateUri(ns + key);
				result.setLiteral(val);
				if (currentLang != null)
					result.setLiteralLanguage(currentLang);
				queue.add(result);
			}
		}
	}

	private RdfNode parseCurrentObject() 
	{
		RdfNode node;

		String uri = parser.getAttributeValue(
				RDF_NS, "resource");
		
		if (uri == null)
		{
			String nodeId = parser.getAttributeValue(
					RDF_NS, "nodeID");
			if (nodeId != null)
			{
				nodeId = "_:" + nodeId;			
				node = RdfNode.createAnon(nodeId);
			}
			else
			{
				node = null;
			}
		}
		else
		{
			uri = makeUriAbsolute(uri);
			node = RdfNode.createUri(uri);
		}
		
		return node;
	}
	
	private RdfNode parseCurrentNode() 
	{
		RdfNode node;

		String uri = parser.getAttributeValue(
				RDF_NS, "about");
		
		String rdfId = parser.getAttributeValue(
				RDF_NS, "ID");

		if (rdfId != null)
		{
			uri = "#" + rdfId;
		}
		
		if (uri == null)
		{
			String nodeId = parser.getAttributeValue(
					RDF_NS, "nodeID");
			if (nodeId == null) 
				nodeId = generateAnon();
			else
				nodeId = "_:" + nodeId;
			
			node = RdfNode.createAnon(nodeId);
		}
		else
		{
			uri = makeUriAbsolute(uri);
			node = RdfNode.createUri(uri);
		}
		
		return node;
	}

	private String makeUriAbsolute(String uri) 
	{
		// is it a relative or absolute URI?
		// according to https://en.wikipedia.org/wiki/Uniform_Resource_Identifier,
		// minimal requirement for absolute URI are "scheme:path"
		if (!uri.matches("^[\\w+.-]+:.*"))
		{
			assert (xmlBase != null) : "Need default namespace, none provided.";
			uri = xmlBase + uri;
		}
		return uri;
	}

	private String generateAnon() {
		String id = "_:genid" + sequentialId++;
		return id;
	}

}
