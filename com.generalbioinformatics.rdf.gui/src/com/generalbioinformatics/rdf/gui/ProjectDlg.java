/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.helixsoft.recordstream.StreamException;
import nl.helixsoft.util.StringUtils;

import org.pathvisio.gui.dialogs.OkCancelDialog;

import com.generalbioinformatics.rdf.gui.MarrsQuery.QueryType;

/**
 * A dialog for viewing and editing a {@link MarrsProject}
 */
public class ProjectDlg extends OkCancelDialog 
{
	private final MarrsProject model;
	private final TripleStoreManager conMgr;
	
	private JTable table;
	private JTable paramsTable;

	private JButton btnAdd, btnRm, btnEdit, btnRun, btnRunAll;
	private final Frame frame;
	
	public ProjectDlg(Frame frame, MarrsProject p, TripleStoreManager conMgr, MarrsMapper mapper) 
	{
		super(frame, "MARRS Query list", frame, true);
		model = p;
		this.conMgr = conMgr;
		this.mapper = mapper;
		this.frame = frame;
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		table = new JTable(model);
		table.setFillsViewportHeight(true);
		panel.add(new JScrollPane(table), BorderLayout.CENTER);

		JLabel lblTitle = new JLabel ("Project title: " + p.getTitle());
		lblTitle.setToolTipText("Project version: " + p.getPublishVersion());
		panel.add(lblTitle, BorderLayout.NORTH);
		
		paramsTable = new JTable(model.getParameterModel());
		paramsTable.setFillsViewportHeight(true);
		paramsTable.setPreferredScrollableViewportSize(new Dimension (80, 80));
		
		JPanel paramsPanel = new JPanel();
		paramsPanel.setBorder (new EtchedBorder(EtchedBorder.RAISED));
		paramsPanel.setLayout (new BorderLayout());
		paramsPanel.add (new JLabel("Parameters"), BorderLayout.NORTH);
		paramsPanel.add (new JScrollPane(paramsTable), BorderLayout.CENTER);
		panel.add(paramsPanel, BorderLayout.SOUTH);
		
		paramsTable.addMouseListener(new MouseAdapter() 
		{						
			@Override
			public void mouseClicked(MouseEvent arg0) 
			{
				if (arg0.getClickCount() > 1)
				{
					int selectedItem  = paramsTable.getSelectedRow();
					if (selectedItem < 0) return; // ignore
					String key = model.getParameterModel().getRowKey(selectedItem);
					editParam(key);
				}
			}
		});

		JPanel btnPanel = new JPanel();
		btnAdd = new JButton("Add");
		btnRm = new JButton("Remove");
		btnEdit = new JButton("Edit");
		btnRun = new JButton("Run");
		btnRun.setEnabled(false);
		btnRunAll = new JButton("Run all");
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
		{	
			@Override
			public void valueChanged(ListSelectionEvent arg0) 
			{
				btnRun.setEnabled (arg0.getFirstIndex() >= 0);
			}
		});
		
		btnAdd.setEnabled(false);
		btnRm.setEnabled(false);
		btnEdit.addActionListener(new EditAction());
		
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.PAGE_AXIS));
//		btnPanel.add(btnAdd); // TODO ... implementation ...
//		btnPanel.add(btnRm); // TODO ... implementation ...
		btnPanel.add(btnEdit);
		btnPanel.add(btnRun);
		btnPanel.add(btnRunAll);
		
		btnRunAll.addActionListener(new RunAction(true));
		btnRun.addActionListener(new RunAction(false));
		
		setSize (500, 500);
		panel.add(btnPanel, BorderLayout.EAST);
		setDialogComponent(panel);
	}

	public void editParam (String key)
	{
		ProjectDlg.editParam (model, frame, key);
	}
	
	public static void editParam (MarrsProject model, Frame frame, String key)
	{
		OkCancelDialog dlg = new OkCancelDialog(frame, "Search results", frame, true);
		JTextArea txt = new JTextArea(10, 40);
		txt.setText (model.getQueryParameter(key));
		dlg.setDialogComponent(txt);
		dlg.setSize(400, 300);
		dlg.setVisible(true);

		if (dlg.getExitCode().equals(OkCancelDialog.OK))
		{
			String p1 = txt.getText().replaceAll ("\n", ", ");
			model.setQueryParameter(key, p1);				
		}
	}
	
	public class EditAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			int selectedRow = table.getSelectedRow();
			if (selectedRow < 0) 
			{
				JOptionPane.showMessageDialog(ProjectDlg.this, "No query selected");
				return;
			}
			
			MarrsQuery q = model.getQuery(selectedRow);
			
			QueryDlg dlg = new QueryDlg(frame, q);
			dlg.setVisible(true);
		}
	}
	
	public class RunAction extends AbstractAction
	{
		private boolean all;
		
		RunAction(boolean all)
		{
			this.all = all;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			//TODO: wrap in swingworker
			try
			{
				int count = 0;
				if (all)
				{
					count = runAll();
				}
				else
				{
					for (int row : table.getSelectedRows())
					{
						count += run(row);
					}
				}
				
				if (count == 0)
				{
					JOptionPane.showMessageDialog(ProjectDlg.this, "Query returned 0 results");
				}
			}
			catch (StreamException ex)
			{
				JOptionPane.showMessageDialog(frame, "<html>Error while executing query:<br>" + StringUtils.escapeHtml(ex.getMessage()) + "</html>");
			}
			catch (MarrsException ex)
			{
				JOptionPane.showMessageDialog(frame, "<html>Error while executing query:<br>" + StringUtils.escapeHtml(ex.getMessage()) + "</html>");
			}
		}
	}

	private final MarrsMapper mapper;

	/** run a single query 
	 * @throws MarrsException */
	public int run(int i) throws StreamException, MarrsException 
	{
		return ProjectDlg.run (mapper, model, frame, conMgr, i);
	}
	
	/** run a single query 
	 * @throws MarrsException */
	public static int run(MarrsMapper mapper, MarrsProject model, Frame frame, TripleStoreManager conMgr, int i) throws StreamException, MarrsException 
	{
		int result = 0;
		if (i < 0 || i > model.getRowCount()) throw new ArrayIndexOutOfBoundsException("Can't run query " + i);
		
		String key = model.getRow(i).getAskBefore();
		if (key != null)
		{
			ProjectDlg.editParam(model, frame, key);
		}
		
		String q = model.getSubstitutedQuery(i);
		MarrsQuery mq = model.getQuery(i);
		result = doQuery(mapper, q, mq);
		return result;
	}

	public static int doQuery(MarrsMapper mapper, String q,
			MarrsQuery mq) throws StreamException 
	{
		int result = 0;
		QueryType qt = mq.getQueryType();
		switch (qt)
		{
			case QUERY_NODE_CONTEXT:
			case QUERY_BACKBONE:
			{
				result = mapper.createNetwork(q, mq);
				break;
			}
			case QUERY_NODE_ATTRIBUTE:
			{
				result = mapper.addAttributes(q);
				break;
			}
			case QUERY_NODE_MATRIX:
			{
				result = mapper.addAttributesMatrix(q);
				break;
			}
			case QUERY_SEARCH:
			{
				result = mapper.popupResults(q);
				break;
			}
		}
		return result;
	}

	/**
	 * Run all node and backbone queries, but not search queries.
	 * @throws MarrsException 
	 */
	public int runAll() throws StreamException, MarrsException
	{
		int count = 0;
		for (int i = 0; i < model.getRowCount(); ++i)
		{
			if (model.getRow(i).getQueryType() != QueryType.QUERY_SEARCH)
				count += run(i);
		}
		return count;
	}

}
