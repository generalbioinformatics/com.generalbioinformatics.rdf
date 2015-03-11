/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.pathvisio.desktop.util.RowWithProperties;

/**
 * A single SPARQL query that forms an element of a marrs project.
 */
public class MarrsQuery implements RowWithProperties<MarrsColumn>
{
	private String q;
	private String title;
	private QueryType qt;
	private String askBefore = null;
	private Map<String, String> contextTypes = new HashMap<String, String>();
	private Map<String, String> postProcess = new HashMap<String, String>();
	
	private boolean isContextQuery = false;
	
	public enum QueryType { 
		/** The results of the query are displayed in a pop-up window */
		QUERY_SEARCH,  
		
		/** The query must have src & dest (and optional edge) bindings. The results are used to build a network */
		QUERY_BACKBONE, 
		
		/** The query must have src bindings. The remaining columns are used as node attributes.
			Nodes do not have to pre-exist, so this can be used to form new nodes without edges in between */
		QUERY_NODE_ATTRIBUTE, 
		
		/** Same as a backbone query. Context queryes are no longer determined by query type, but by the presence of context types */
		@Deprecated
		QUERY_NODE_CONTEXT,
		
		/**
		 * The query must have three bindings: src, column and value.
		 * In essence, this forms a long form data matrix that is associated with each src node.
		 * Column maps to the column in the attribute table, value maps to the value in the attribute table 
		 */
		QUERY_NODE_MATRIX, 
	}
	
	public MarrsQuery(String title, String query, QueryType qt) 
	{
		q = query;
		this.title = title;
		this.qt = qt;
	}

	public String getQueryText() 
	{
		return q;
	}

	@Override
	public String getProperty(MarrsColumn prop) 
	{
		return title;
	}

	public String getTitle()
	{
		return title;
	}
	
	public QueryType getQueryType() 
	{
		return qt;
	}

	public void setQueryText(String text) 
	{
		q = text;
	}

	/** May return null! */
	public String getAskBefore() 
	{
		return askBefore;
	}

	public Map<String, String> getContext() 
	{
		return contextTypes;
	}

	public void putContext(String key, String val) 
	{
		contextTypes.put(key, val);
		isContextQuery = true; // automatic side effect
	}
	
	public boolean isContextQuery()
	{
		return isContextQuery;
	}	

	public void setAskBefore(String key) 
	{
		askBefore = key;
	}

	public void setContextQuery(boolean b) 
	{
		isContextQuery = b;		
	}

	public void setPostProcessing(String var, String operation) 
	{
		postProcess.put(var, operation);
	}

	public Set<String> getPostProcessingVars() 
	{
		return postProcess.keySet();
	}

	public String getPostProcessingOperation(String var) 
	{
		return postProcess.get(var);
	}
}