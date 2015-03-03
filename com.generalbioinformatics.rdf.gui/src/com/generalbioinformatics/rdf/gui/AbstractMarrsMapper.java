/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import nl.helixsoft.gui.IndeterminateProgressDialog;
import nl.helixsoft.recordstream.Record;
import nl.helixsoft.recordstream.RecordStream;
import nl.helixsoft.recordstream.RecordStreamFormatter;
import nl.helixsoft.recordstream.StreamException;

import org.pathvisio.gui.dialogs.OkCancelDialog;

import com.generalbioinformatics.rdf.NS;
import com.generalbioinformatics.rdf.NamespaceMap;
import com.generalbioinformatics.rdf.TripleStore;

/**
 * Contains functionality common between cytoscape 2 and cytoscape 3 versions of the
 * General SPARQL app/plug-in, or potentially other network software.
 * 
 * @param <NodeType> type used for Nodes
 * @param <EdgeType> type used for Edges
 */
public abstract class AbstractMarrsMapper<NodeType, EdgeType> implements MarrsMapper 
{
	protected final TripleStoreManager conMgr;
	private NamespaceMap namespaces = new NS();
	
	protected AbstractMarrsMapper (TripleStoreManager conMgr)
	{
		this.conMgr = conMgr;
	}
	
	@Override
	public final int popupResults(String query) throws StreamException 
	{
		// TODO: wrap in swing worker.
		RecordStream rs = conMgr.getConnection().sparqlSelect(query);

		TableModel tm = RecordStreamFormatter.asTableModel(rs);
		int result = tm.getRowCount();

		if (result > 0)
		{
			OkCancelDialog dlg = new OkCancelDialog(getFrame(), "Search results", getFrame(), true);
			dlg.setDialogComponent(new JScrollPane(new JTable(tm)));
			dlg.setSize(400, 300);
			dlg.setVisible(true);
		}

		return result;
	}

	/**
	 * Set a node attribute, i.e. add attribute map associated with a particular node.
	 *  
	 * @param node reference to a Node
	 * @param key key in attribute map
	 * @param val value in attribute map. Can be of any type, but it's an error to mix types for a given key that 
	 *  may or may not lead to an exception.
	 */
	protected abstract void setNodeAttribute(NodeType node, String key, Object val);
	
	/**
	 * Set a edge attribute, i.e. add attribute map associated with a particular edge.
	 * The value can be of any type, but it's an error to mix types for a given key. 
	 * @param edge reference to an Edge
	 * @param key key in attribute map
	 * @param val value in attribute map. Can be of any type, but it's an error to mix types for a given key that 
	 *  may or may not lead to an exception.
	 */
	protected abstract void setEdgeAttribute(EdgeType edge, String key, Object val);
	
	/**
	 * Helper method for the BACKBONE query type.
	 */
	private void createNetworkHelper(final MarrsQuery mq, Record r, NodeType nSrc, NodeType nDest, EdgeType edge) 
	{
		// second pass: all other attributes
		for (int i = 0; i < r.getMetaData().getNumCols(); ++i)
		{
			String key = r.getMetaData().getColumnName(i);						
			if (key.startsWith("src_"))
			{
				String val = postProcess ("" + r.get(key), key, mq);
				setNodeAttribute (nSrc, key.substring("src_".length()), val);
				
			}
			else if (key.startsWith("dest_"))
			{
				String val = postProcess ("" + r.get(key), key, mq);
				setNodeAttribute (nDest, key.substring("dest_".length()), val);
			}
			else if ("interaction".equals(key))
			{
				// ignore, already done in first pass
			}
			else
			{
				setEdgeAttribute(edge, key, r.get(i));
			}
		}
	}

	protected String postProcess(String value, String var, MarrsQuery mq) 
	{
		String op = mq.getPostProcessingOperation(var);
		if ("shorten".equals (op))
		{
			return namespaces.shorten (value);
		}
		else
		{
			return value;
		}
	}

	protected abstract void flushView();
	protected abstract void finalizeNetworkAddition(Set<NodeType> nodesAdded, Set<EdgeType> edgesPostPoned);
	protected abstract void copyNodeCoordinates();
	
	protected abstract NodeType createNodeIfNotExists(String nodeId, Set<NodeType> nodesAdded);
	protected abstract EdgeType createEdgeIfNotExists(NodeType nSrc, NodeType nDest, String interaction, Set<EdgeType> edgesPostponed);
	protected abstract JFrame getFrame();

	@Override
	public final int addAttributes(String query) throws StreamException
	{			
		TripleStore con = conMgr.getConnection();
		if (con == null) return -1; // already showed error dialog at this point.
		
		QuerySwingWorker worker = new QuerySwingWorker(con, query)
		{
			Set<NodeType> nodesAdded = new HashSet<NodeType>();
			
			@Override
			protected void process(List<Record> records)
			{
				for (Record r : records)
				{
					String src = "" + r.get("src");					
					NodeType nSrc = createNodeIfNotExists(src, nodesAdded);
					
					for (int i = 0; i < r.getMetaData().getNumCols(); ++i)
					{
						String key = r.getMetaData().getColumnName(i);
						if ("src".equals(key)) continue;
						
						setNodeAttribute(nSrc, key, r.get(i));
					}			
				}
			}
			
			@Override
			protected void done()
			{
				finalizeNetworkAddition(nodesAdded, null);
				copyNodeCoordinates();
				flushView();
			}
		};
		
		worker.execute();
		
		// dialog will be visible until swingWorker is done.
		IndeterminateProgressDialog.createAndShow(getFrame(), "Executing query", worker);	
		
		int count = 0;

		try {
			count = worker.get();
			return count;
		} 
		catch (InterruptedException e) 
		{
			JOptionPane.showMessageDialog(getFrame(), "Interrupted");
			e.printStackTrace();
			return -1;
		} 
		catch (ExecutionException e) 
		{
			JOptionPane.showMessageDialog(getFrame(), "Exectution exception: " + e.getCause().getMessage());
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public final int createNetwork(String query, final MarrsQuery mq) throws StreamException 
	{
		TripleStore con = conMgr.getConnection();
		if (con == null) return -1; // already showed error dialog at this point.
		
		QuerySwingWorker worker = new QuerySwingWorker(con, query)
		{
			Set<EdgeType> edgesPostponed = new HashSet<EdgeType>();
			Set<NodeType> nodesAdded = new HashSet<NodeType>();
			
			@Override
			protected void process(List<Record> records)
			{
				for (Record r : records)
				{
					String src = "" + r.get("src");
					String dest = "" + r.get("dest");

					NodeType nSrc = createNodeIfNotExists(src, nodesAdded);
					NodeType nDest = createNodeIfNotExists(dest, nodesAdded);
					
					String interaction = "pp";
					if (r.getMetaData().hasColumnName("interaction"))
					{
						interaction = "" + r.get("interaction"); 
					}
					
					// for some reason Cytoscape throws a fit if we add edges one by one
					// as they are published.
					// To avoid that, we save them up here, and add them in one go in the done() method.
					EdgeType edge = createEdgeIfNotExists(nSrc, nDest, interaction, edgesPostponed);
					
					createNetworkHelper(mq, r, nSrc, nDest, edge);
				}
			}
			
			@Override
			protected void done()
			{				
				finalizeNetworkAddition(nodesAdded, edgesPostponed);
				copyNodeCoordinates();
				flushView();
			}
		};
			

		
		int count;
		try {
			worker.execute();			
			// dialog will be visible until swingWorker is done.
			IndeterminateProgressDialog.createAndShow(getFrame(), "Executing query", worker);	
			count = worker.get();
			return count;
		} 
		catch (InterruptedException e) 
		{
			JOptionPane.showMessageDialog(getFrame(), "Interrupted");
			e.printStackTrace();
			return -1;
		} 
		catch (ExecutionException e) 
		{
			JOptionPane.showMessageDialog(getFrame(), "Exectution exception: " + e.getCause().getMessage());
			e.printStackTrace();
			return -1;
		}
	}

}
