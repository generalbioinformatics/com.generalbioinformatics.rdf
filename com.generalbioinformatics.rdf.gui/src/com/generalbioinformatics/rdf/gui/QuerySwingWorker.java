/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import javax.swing.SwingWorker;

import nl.helixsoft.recordstream.Record;
import nl.helixsoft.recordstream.RecordStream;

import com.generalbioinformatics.rdf.TripleStore;

/**
 * {@link SwingWorker} that runs a SPARQL query in a background thread,
 * and processes the results on the Event thread.
 */
public class QuerySwingWorker extends SwingWorker<Integer, Record>
{
	private String query;
	private TripleStore con;
	
	public QuerySwingWorker(TripleStore con, String query)
	{
		this.con = con;
		this.query = query;
	}

	private String oldNote = null;
	
	private void setNote(String newNote)
	{
		firePropertyChange("note", oldNote, newNote);
		oldNote = newNote;
	}
	
	@Override
	protected Integer doInBackground() throws Exception 
	{
		int count = 0;
		setNote ("Running SPARQL query");
		RecordStream rs = con.sparqlSelect(query);
		Record r;
		while ((r = rs.getNext()) != null)
		{
			count++;
			publish(r);
			setNote ("Receiving: " + count + " results");
		}
		setNote ("Received all " + count + " results");
		return count;
	}
}