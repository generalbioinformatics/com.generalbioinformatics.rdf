/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import nl.helixsoft.gui.table.MapTableModel;
import nl.helixsoft.util.StringUtils;

import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.Format.TextMode;
import org.jdom.output.XMLOutputter;
import org.pathvisio.desktop.util.ListWithPropertiesTableModel;
import org.xml.sax.InputSource;

import com.generalbioinformatics.rdf.gui.MarrsQuery.QueryType;

/**
 * Set of connected SPARQL queries, that can be used to navigate networks, and
 * that can be serialized and viewed together
 */
public class MarrsProject extends ListWithPropertiesTableModel<MarrsColumn, MarrsQuery> implements TableModelListener
{
	/* this replacement token is used in Project files before they are published. 
	 * Encountering it means a development version or a work in progress. */
	public static final String PUBLISHVERSION_REPLACEMENT_TOKEN = "@PUBLISHVERSION@";
	
	/* Current schema version for project files. Encountering a newer version may mean that plugin is out-of-date. */
	public final static double CURRENT_SCHEMAVERSION = 0.3;
	
	private MapTableModel<String, String> parameters = new MapTableModel<String, String>();
	private boolean dirty;
	private String pubVersion;
	
	MarrsProject()
	{
		setColumns(MarrsColumn.TITLE);
		parameters.addTableModelListener(this);
	}

	public void addQuery(MarrsQuery value)
	{
		addRow (value);
		dirty = true;
	}

	public MarrsQuery getQuery(int row)
	{
		return rows.get(row);
	}

	public String getQueryParameterFilter (MarrsQuery mq, String key)
	{
		String q = mq.getQueryText();
		Pattern pat = Pattern.compile("\\$\\{" + key + "(\\|([\\w-]+))?\\}");
		Matcher mat = pat.matcher(q);
		if (mat.find())
		{
			return mat.group(2);
		}
		else
			return null;
	}

	public String getPublishVersion()
	{
		return pubVersion;
	}
	
	/**
	 * Compare two publicationVersion strings
	 */
	public static int comparePublicationVersions (String one, String other)
	{
		final int OTHER_OLDER = -1;
		final int SAME = 0;
		final int THIS_OLDER = 1;
		
		/* RULE 1: null is always older */ 
		if (one == null) return (other == null ? SAME : THIS_OLDER);
		if (other == null) return OTHER_OLDER;
		
		/* RULE 2: @PUBLISHVERSION is always newer */
		if (PUBLISHVERSION_REPLACEMENT_TOKEN.equals (one))
				return (PUBLISHVERSION_REPLACEMENT_TOKEN.equals(other) ? SAME : OTHER_OLDER);
		if (PUBLISHVERSION_REPLACEMENT_TOKEN.equals (other)) return THIS_OLDER;
		
		/* RULE 3: Compare alphabetically */
		return other.compareTo(one);
	}
	
	/**
	 * Compare this project with another by publication version
	 */
	public boolean isSameOrNewerThan (MarrsProject other)
	{
		return comparePublicationVersions(pubVersion, other.pubVersion) <= 0;
	}
	
	/**
	 * Gets a set of query substitution parameters.
	 * The resulting map contains the parameters as keys, and the (optional) filters as values
	 */
	public Map<String, String> getQueryParameters(MarrsQuery mq)
	{
		Map<String, String> result = new HashMap<String, String>();
		
		String q = mq.getQueryText();
		Pattern pat = Pattern.compile("\\$\\{(\\w+)(\\|([\\w-]+))?\\}");
		Matcher mat = pat.matcher(q);
		while (mat.find())
		{
			String key = mat.group(1);
			String filter = mat.group(3);
			result.put (key, filter);
		}
		
		return result;
	}

	public void setQueryParameter (String key, String value)
	{
		parameters.put (key, value);
	}

	public String getSubstitutedQuery(MarrsQuery mq) throws MarrsException
	{
		String q = mq.getQueryText();
		Map<String, String> queryData = getQueryParameters(mq);
		
		for (String key : queryData.keySet())
		{			
			
			if (!parameters.containsKey(key))
			{
				throw new MarrsException("Missing substitution parameter: " + key);
			}
			
			String contents = parameters.get(key);
			String filter = queryData.get(key);
			if ("uri-list".equals (filter))
			{
				Pattern pat = Pattern.compile ("\\s*<[-#_:/%.0-9a-zA-Z]+>(\\s*,\\s*<[-#_:/%.0-9a-zA-Z]+>)*\\s*$");
				Matcher mat = pat.matcher(contents);
				if (!mat.matches())
				{
					throw new MarrsException("'" + contents + "' does not match pattern for parameter ${" + key + "|" + filter + "}");
				}
			}
			else if ("uri-bracketed".equals (filter)) /* difference between uri and uri-bracketed is the surrounding < and > characters */
			{
				Pattern pat = Pattern.compile ("\\s*<[-#_:/%.0-9a-zA-Z]+>\\s*");
				Matcher mat = pat.matcher(contents);
				if (!mat.matches())
				{
					throw new MarrsException("'" + contents + "' does not match pattern for parameter ${" + key + "|" + filter + "}");
				}
			}
			else if ("uri".equals (filter))
			{
				Pattern pat = Pattern.compile ("[-#_:/%.0-9a-zA-Z]+");
				Matcher mat = pat.matcher(contents);
				if (!mat.matches())
				{
					throw new MarrsException("'" + contents + "' does not match pattern for parameter ${" + key + "|" + filter + "}");
				}
			}
			else if ("literal".equals (filter))
			{
				Pattern pat = Pattern.compile ("^[^\"\\n]*$");
				Matcher mat = pat.matcher(contents);
				if (!mat.matches())
				{
					throw new MarrsException("'" + contents + "' does not match pattern for parameter ${" + key + "|" + filter + "}");
				}
			}
			else
			{
				/* most restrictive base pattern */
				Pattern pat = Pattern.compile ("[-#_:/%.0-9a-zA-Z]+");
				Matcher mat = pat.matcher(contents);
				if (!mat.matches())
				{
					throw new MarrsException("'" + contents + "' does not match pattern for parameter ${" + key + "}");
				}
			}
			
			// TODO: replace & appendReplace
			q = q.replaceAll ("\\$\\{" + key + "(\\|([\\w-]+))?\\}", contents);
		}
		System.out.println (q);
		return q;		
	}
	
	public String getSubstitutedQuery(int at) throws MarrsException
	{
		MarrsQuery q = rows.get(at);
		return getSubstitutedQuery (q);
	}

	public String getQueryParameter(String key) 
	{
		return parameters.get(key);
	}

	public MapTableModel<String, String> getParameterModel() 
	{
		return parameters;
	}

	public static MarrsProject createFromFile (File f) throws JDOMException, IOException
	{
		return MarrsProject.createFromFile(new InputSource(new FileInputStream(f)));
	}
	
	public static MarrsProject createFromFile (InputSource in) throws JDOMException, IOException
	{
		List<String> warnings = new ArrayList<String>();
		
		SAXBuilder builder  = new SAXBuilder(false); // no validation when reading the xml file
		// try to read the file; if an error occurs, catch the exception and print feedback

		// build JDOM tree
		Document doc = builder.build(in);

		// Copy the pathway information to a VPathway
		Element root = doc.getRootElement();
		if (!root.getName().equals("MarrsProject"))
		{
			throw new IllegalArgumentException ("Not a MarrsProject file");
		}

		MarrsProject project = new MarrsProject();
		project.title = root.getAttributeValue ("title");
		Double version = StringUtils.safeParseDouble(root.getAttributeValue ("schemaversion"));
		if (version == null || version > CURRENT_SCHEMAVERSION)
		{
			warnings.add ("WARNING: project file version is higher than this plugin version. You'll have to upgrade the plugin to make full use of any new features.");
		}

		// if missing, may be null. String values are allowed...
		project.pubVersion = root.getAttributeValue ("pubversion");
		
		for (Object o : root.getChildren("Query"))
		{
			Element eQuery = (Element)o;
			String title = eQuery.getAttributeValue("title");
			String typeName = eQuery.getAttributeValue("type");
			QueryType found = findQueryType(typeName);
			if (found == null)
			{
				warnings.add ("WARNING: Parse error, query type " + typeName + " unknown. Skipping query");
				continue;
			}
			
			String queryText = eQuery.getText();

			MarrsQuery q = new MarrsQuery(title, queryText, found);

			for (Object context : eQuery.getChildren("Context"))
			{
				q.setContextQuery(true);
				Element eContext = (Element)context;
				String key = eContext.getAttributeValue("key");
				String val = eContext.getAttributeValue("value");
				
				if (val != null)
					q.putContext(key, val);
			}

			Object ask = eQuery.getChild("AskBefore");
			if (ask != null)
			{
				String key = ((Element)ask).getAttributeValue("key");
				q.setAskBefore (key);
			}

			for (Object pp : eQuery.getChildren("PostProcessing"))
			{
				String var = ((Element)pp).getAttributeValue("var");
				String operation = ((Element)pp).getAttributeValue("operation");
				q.setPostProcessing (var, operation);
			}

			project.addQuery(q);
		}

		for (Object o : root.getChildren("Param"))
		{
			Element eParam = (Element)o;
			String key = eParam.getAttributeValue("key");
			String val = eParam.getAttributeValue("val");
			project.setQueryParameter(key, val);
		}
		project.dirty = false;

		if (warnings.size() > 0)
		{
			//TODO: set dialog root
			JOptionPane.showMessageDialog(null, "<html><ul><li>" + StringUtils.join("<br/><li>", warnings) + "</ul></html>");
		}

		return project;

	}


	private static QueryType findQueryType(String typeName) 
	{
		QueryType found = null;
		for (QueryType q : QueryType.values())
			if (q.name().equals(typeName)) found = q;
		return found;
	}

	private String title;

	public void saveToFile(File dest) throws IOException 
	{
		File temp = File.createTempFile(dest.getName(), ".tmp", dest.getParentFile());

		try
		{
			Element root = new Element("MarrsProject");
			root.setAttribute("title", title);
			root.setAttribute("schemaversion", "" + CURRENT_SCHEMAVERSION);

			for (MarrsQuery q : rows)
			{
				Element queryElt = new Element("Query");
				CDATA data = new CDATA(q.getQueryText());
				queryElt.addContent(data);

				queryElt.setAttribute ("title", q.getTitle());
				queryElt.setAttribute ("type", "" + q.getQueryType());
				
				if (q.getAskBefore() != null)
				{
					Element askElt = new Element ("AskBefore");
					askElt.setAttribute ("key", q.getAskBefore());
					queryElt.addContent(askElt);
				}

				for (Map.Entry<String, String> context : q.getContext().entrySet())
				{
					Element contextElt = new Element ("Context");
					contextElt.setAttribute ("key", context.getKey()); // for now, we only use "type" to filter context, so this is hardcoded.
					contextElt.setAttribute ("value", context.getValue());
					queryElt.addContent(contextElt);
				}

				for (String var : q.getPostProcessingVars())
				{
					Element processElt = new Element ("PostProcessing");
					processElt.setAttribute ("var", var); // for now, we only use "type" to filter context, so this is hardcoded.
					processElt.setAttribute ("operation", q.getPostProcessingOperation(var));
					queryElt.addContent(processElt);
				}
				
				root.addContent(queryElt);
			}		

			for (Map.Entry<String, String> param : parameters.entrySet())
			{
				Element paramElt = new Element("Param");
				paramElt.setAttribute("key", param.getKey());
				paramElt.setAttribute("val", param.getValue());
				root.addContent(paramElt);
			}
			
			Document doc = new Document(root);
			XMLOutputter xout = new XMLOutputter();
			Format format = Format.getPrettyFormat();
			format.setIndent("\t");
			format.setLineSeparator("\n");
			format.setTextMode(TextMode.PRESERVE);
			xout.setFormat(format);
			xout.output(doc, new FileOutputStream(temp));

			temp.renameTo(dest);
			dirty = false;
		}
		finally
		{
			temp.delete();
		}
	}

	@Override
	public void tableChanged(TableModelEvent arg0) 
	{
		dirty = true;
	}

	public String getTitle() 
	{
		return title;
	}

}