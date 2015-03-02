/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jsyntaxpane.DefaultSyntaxKit;
import nl.helixsoft.gui.TextUndo;

import org.pathvisio.gui.dialogs.OkCancelDialog;

/**
 * For editing a single query
 */
public class QueryDlg extends OkCancelDialog 
{
	private final MarrsQuery model;	
	private JEditorPane txtQ;
	
	static {
	
		DefaultSyntaxKit.initKit();
		DefaultSyntaxKit.registerContentType("text/sparql", SparqlSyntaxKit.class.getName());

	}
	
	public QueryDlg (Frame frame, MarrsQuery q) 
	{
		super(frame, "Add/Edit query", frame, true);
		model = q;
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		txtQ = new JEditorPane();
		TextUndo.addUndoFunctionality(txtQ);

		panel.add(new JScrollPane(txtQ), BorderLayout.CENTER);
		
		// NB: this innocuous line starts the syntax highlighting business, and must be called after
		// the JTextPane is added to the panel.
		txtQ.setContentType ("text/sparql");

		txtQ.setText (q.getQueryText());

		setSize (600, 500);
		setDialogComponent(panel);
	}
	
	@Override
	protected void okPressed() 
	{
		super.okPressed();
		model.setQueryText(txtQ.getText());
	}

}
