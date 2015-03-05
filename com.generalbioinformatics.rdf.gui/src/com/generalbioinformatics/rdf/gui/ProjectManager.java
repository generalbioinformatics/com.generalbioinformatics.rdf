/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import nl.helixsoft.gui.DownloadUtils;
import nl.helixsoft.gui.preferences.PreferenceManager;
import nl.helixsoft.recordstream.StreamException;
import nl.helixsoft.util.FileUtils;
import nl.helixsoft.util.StringUtils;

import org.apache.commons.codec.binary.Base64;
import org.jdom.JDOMException;
import org.xml.sax.InputSource;

/**
 * Manages loading and saving of a project, and also the list of most recently used project files.
 */
public class ProjectManager 
{
	private final PreferenceManager prefs;
	private final TripleStoreManager conMgr;
	private final MarrsMapper mapper;
	private final Frame frame;
	private final List<RecentItem> recentItems;
	public final List<AbstractAction> recentActions;
	
	private static class RecentItem
	{
		public RecentItem(File aFile, String aTitle) 
		{
			this.title = aTitle;
			this.file = aFile;
		}
		
		final String title;
		final File file;
	}
	
	// project may be null
	private MarrsProject project = null;
	
	/** @returns current project, null if there is no current project */ 
	public MarrsProject getProject()
	{
		return project;
	}
		
	private class RecentFileAction extends AbstractAction
	{
		private final int no;
		
		public RecentFileAction(int i) 
		{
			super (i + " - ");
			this.no = i;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			try {
				File f = recentItems.get(no).file;
				loadProject (f);
				prefs.setFile(MarrsPreference.MARRS_PROJECT_FILE, f);
				prefs.store();
			}
			catch (JDOMException e1) 
			{
				handleJdomException(e1);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(frame, "Error reading project file: " + e1.getMessage());
				e1.printStackTrace();
			}

		}
	}
	
	public ProjectManager(Frame frame, PreferenceManager prefs, TripleStoreManager conMgr, MarrsMapper mapper) 
	{
		this.prefs = prefs;
		this.conMgr = conMgr;
		this.frame = frame;
		this.mapper = mapper;
		
		project = null;
		updateActions();
		
		recentItems = new ArrayList<RecentItem>();
		recentActions = new ArrayList<AbstractAction>();
		
		for (int i = 0; i < MarrsPreference.RECENT_FILE_NUM; ++i)
		{
			File f = prefs.getFile(MarrsPreference.RECENT_FILE_ARRAY[i]);
			String title = prefs.get(MarrsPreference.RECENT_FILE_TITLE_ARRAY[i]);
			RecentItem item = new RecentItem(f, title);
			recentItems.add (item);
			AbstractAction action = new RecentFileAction(i);
			recentActions.add (action);
		}
		
		refreshRecentFilesMenu();		
	}

	private void refreshRecentFilesMenu() 
	{
		for (int i = 0; i < MarrsPreference.RECENT_FILE_NUM; ++i)
		{
			AbstractAction act = recentActions.get(i);
			RecentItem item = (i >= recentItems.size()) ? null : recentItems.get(i);
			act.setEnabled(item.file != null);
			String menuTitle = i + (item.file == null ? "" : (" - " + item.file.getName()));
			
			if (!StringUtils.emptyOrNull(item.title))
				menuTitle += " - " + item.title;
			
			act.putValue(Action.NAME, menuTitle);
		}
	}
	
	public void loadProject(InputStream is) throws JDOMException, IOException
	{
		MarrsProject p = MarrsProject.createFromFile(new InputSource(is));
		setProject (p);
	}
	
	public void setProject(MarrsProject p) 
	{
		project = p;
		updateActions();
		mapper.setProject(project);
	}

	public void loadProject(File projectFile) throws JDOMException, IOException
	{		
		InputStream is = new FileInputStream(projectFile);
		loadProject (is);
		
		// remove all occurrences of this file and reinsert at the top
		Iterator<RecentItem> it = recentItems.iterator();
		while (it.hasNext())
		{
			RecentItem i = it.next();
			if (i.file == projectFile)
			{
				it.remove();
			}
		}
				
		recentItems.add(0, new RecentItem (projectFile, getProject().getTitle()));
		if (recentItems.size() > MarrsPreference.RECENT_FILE_NUM) recentItems.remove(recentItems.size()-1);
		
		refreshRecentFilesMenu();
		
		for (int i = 0; i < MarrsPreference.RECENT_FILE_NUM; ++i)
		{
			prefs.setFile(MarrsPreference.RECENT_FILE_ARRAY[i], (i >= recentItems.size()) ? null : recentItems.get(i).file);
			prefs.set(MarrsPreference.RECENT_FILE_TITLE_ARRAY[i], (i >= recentItems.size()) ? null : recentItems.get(i).title);
		}		
	}
	
	private void updateActions()
	{
		editAction.setEnabled (project != null);
		saveAction.setEnabled (project != null);
		loadAction.setEnabled (true);
	}

	public final ProjectEditAction editAction = new ProjectEditAction();
	public final ProjectLoadAction loadAction = new ProjectLoadAction();
	public final ProjectSaveAction saveAction = new ProjectSaveAction();
//	public final ProjectDownloadAction downloadAction = new ProjectDownloadAction();

	private class ProjectEditAction extends AbstractAction
	{
		ProjectEditAction()
		{
			super ("View query list");
		}

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			ProjectDlg dlg = new ProjectDlg(frame, project, conMgr, mapper);
			dlg.setVisible(true);
		}
	}

	private class ProjectSaveAction extends AbstractAction
	{
		ProjectSaveAction()
		{
			super ("Save project");
		}

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			try {
				project.saveToFile(prefs.getFile(MarrsPreference.MARRS_PROJECT_FILE));
			} 
			catch (IOException e1) 
			{
				JOptionPane.showMessageDialog (frame, "Saving failed: " + e1.getMessage());
			}
		}
	}

	private class ProjectDownloadAction extends AbstractAction
	{
		ProjectDownloadAction()
		{
			super ("Refresh query list from server");
		}

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			// download file
			File local = getProjectCacheFile();
			String url = prefs.get(MarrsPreference.MARRS_PROJECT_UPDATE_URL);
			try {
				URLConnection conn = new URL(url).openConnection();

				//TODO: here we assume that if we need authentication for currently configured sparql endpoint,
				// we can use it also for the project download file. This may not always hold. 
				if (prefs.getBoolean(MarrsPreference.MARRS_SPARQL_AUTHENTICATE))
				{
					
					String authString = prefs.get(MarrsPreference.MARRS_SPARQL_USER) + ":" + prefs.get(MarrsPreference.MARRS_SPARQL_PASS);
					byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
					String authStringEnc = new String(authEncBytes);
					conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
				}
				
				File tmp = File.createTempFile("project-", ".xml", local.getParentFile());
				
				FileOutputStream out = new FileOutputStream (tmp);
				DownloadUtils.downloadStream(conn, out);
				out.close();
				
				MarrsProject downloaded = MarrsProject.createFromFile(tmp);
				
				String info = "Server version: " + downloaded.getPublishVersion() + "<br>Local version: " + project.getPublishVersion();
				
				if (!project.isSameOrNewerThan(downloaded))
				{
					boolean oldRemoved = local.exists() ? local.delete() : true;
					if (oldRemoved && tmp.renameTo(local))
					{
						loadProject (local);
						prefs.setFile(MarrsPreference.MARRS_PROJECT_FILE, local);
						prefs.store();
						JOptionPane.showMessageDialog(frame, "<html>Query list updated<br>" + info);
					}
					else
					{
						JOptionPane.showMessageDialog(frame, "<html>Problem updating, no permissions to write to<br>" + local);
					}
					
				}
				else
				{
					JOptionPane.showMessageDialog(frame, "<html>Query list already up-to-date<br>" + info);
				}
			} 
			catch (MalformedURLException e1) 
			{
				JOptionPane.showMessageDialog(frame, "Malformed URL: '" + url + "' " + e1.getMessage());
				e1.printStackTrace();
			}
			catch (JDOMException e1) 
			{
				handleJdomException(e1);
			} 
			catch (IOException e1) 
			{
				JOptionPane.showMessageDialog(frame, "Error reading project file: " + e1.getMessage());
				e1.printStackTrace();
			}
			
		}

		
	}

	public static File getProjectCacheFile() 
	{
		File dir = new File (FileUtils.getApplicationDir(), ".marrs");
		dir.mkdirs();
		File local = new File (dir, "project.xml");
		return local;
	}

	private class ProjectLoadAction extends AbstractAction
	{
		ProjectLoadAction()
		{
			super ("Load project");
		}

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			JFileChooser jfc = new JFileChooser();
			int result = jfc.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION)
			{
				File f = jfc.getSelectedFile();
				try {
					loadProject (f);
					prefs.setFile(MarrsPreference.MARRS_PROJECT_FILE, f);
					prefs.store();
				} 
				catch (JDOMException e1) 
				{
					handleJdomException(e1);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Error reading project file: " + e1.getMessage());
					e1.printStackTrace();
				}
			}
		}

	}

	private void handleJdomException(JDOMException e1) 
	{
		StackTraceElement[] elts = e1.getStackTrace();
		JOptionPane.showMessageDialog(frame, "<html>Parsing error, not a valid project file<br>" + e1.getMessage() + "<br>" + elts[0].toString());
		e1.printStackTrace();
	}

	public List<AbstractAction> getSearchQueries() 
	{
		List<AbstractAction> result = new ArrayList<AbstractAction>();
		if (project == null) return result;
		
		for (int i = 0; i < project.getRowCount(); ++i)
		{
			MarrsQuery q = project.getQuery(i);
			if (!q.isContextQuery())
			{
				result.add (new RunAction(i));
			}
		}
		
		return result;
	}

	public class RunAction extends AbstractAction
	{
		private int row;
		
		RunAction(int row)
		{
			super (project.getRow(row).getTitle());
			this.row = row;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			//TODO: wrap in swingworker
			try
			{
				int count = 0;
				count += ProjectDlg.run(mapper, project, frame, conMgr, row);
				
				if (count == 0)
				{
					JOptionPane.showMessageDialog(frame, "Query returned 0 results");
				}
			}
			catch (StreamException ex)
			{
				JOptionPane.showMessageDialog(frame, "<html>Error while executing query:<br>" + ex.getMessage() + "</html>");
			}
			catch (MarrsException ex)
			{
				JOptionPane.showMessageDialog(frame, "<html>Error while executing query:<br>" + ex.getMessage() + "</html>");
			}
		}
	}

}
