/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.pathvisio.gui.dialogs.OkCancelDialog;

import com.generalbioinformatics.rdf.JenaSparqlEndpoint;
import com.generalbioinformatics.rdf.TripleFile;
import com.generalbioinformatics.rdf.TripleStore;
import com.generalbioinformatics.rdf.VirtuosoConnection;

import nl.helixsoft.debug.WorkerThread;
import nl.helixsoft.gui.IndeterminateProgressDialog;
import nl.helixsoft.gui.preferences.PreferenceManager;
import nl.helixsoft.param.ParameterModel;
import nl.helixsoft.param.ParameterPanel;
import nl.helixsoft.param.SimpleParameterModel;
import nl.helixsoft.param.StringEditor;
import nl.helixsoft.recordstream.StreamException;
import nl.helixsoft.recordstream.Supplier;

/**
 * Manages a connection to a Triple Store or SPARQL endpoint, or anything that can handle SPARQL queries. 
 * Handles configuring via GUI, and testing connection.
 */
public class TripleStoreManager implements Supplier<TripleStore>
{
	private TripleStore con = null;
	private final PreferenceManager prefs;
	
	public final AbstractAction configureAction = new ConfigureAction();
	private final JFrame frame;
	
	private final Driver[] drivers;
	private DriverSelectionModel driverSelection;
	
	public TripleStoreManager(JFrame frame, PreferenceManager prefs)
	{
		this.frame = frame;
		this.prefs = prefs;
		
		drivers = new Driver[] {
				new SparqlDriver(),
				new RdfFileDriver(),
				new VirtuosoDriver(),
				new JenaModelDriver(),
		};
		
		driverSelection = new DriverSelectionModel(drivers);
		
		final String preferredDriverName = prefs.get(MarrsPreference.MARRS_DRIVER);
		try
		{
			driverSelection.setDriver(preferredDriverName);
		}
		catch (IllegalArgumentException ex)
		{
			// the MarrsPreference setting is faulty, so we fall back to no driver selected...
		}
	}
	
	public TripleStore getConnection()
	{
		//TODO: thread safety of driver... driver is accessed from two threads...
		Driver driver = driverSelection.getDriver();
		//TODO: parent component
		ConnectionWorker worker = new ConnectionWorker(null, driver);
		worker.execute();
		//TODO: any exceptions during connection are shown in a dialog, and con returns null. Maybe better to rethrow the exception here? 
		
		// dialog will be visible until swingWorker is done.
		//TODO: parent component
		IndeterminateProgressDialog.createAndShow((JFrame)null, "Connecting to triple store", worker);	

		return con;
	}
	
	private class ConnectionWorker extends SwingWorker<TripleStore, Void>
	{
		private final Driver driver;
		private final Component parent;
		
		ConnectionWorker (Component parent, Driver driver)
		{
			this.driver = driver;
			this.parent = parent;
		}
		
		@Override
		protected TripleStore doInBackground() throws Exception 
		{
			TripleStore con = driver.getConnection();
			return con;
		}
		
		@Override
		public void done()
		{
			try
			{
				con = get();
				// TODO... test connection...
			} 
			catch (ExecutionException e) 
			{
				con = null;
				JOptionPane.showMessageDialog(parent, "<html>Could not connect:<br>" + e.getMessage());
				e.printStackTrace();
			} 
			catch (InterruptedException e) 
			{
				con = null;
				JOptionPane.showMessageDialog(parent, "<html>Could not connect:<br>" + e.getMessage());
				e.printStackTrace();
			} 
		}
	}
	
	private interface Driver extends ParameterModel
	{
		public String getName();
		
		@WorkerThread
		public TripleStore getConnection() throws Exception;
		
		public void savePreferences();
	}
	
	private class JenaModelDriver extends SimpleParameterModel implements Driver
	{
		public JenaModelDriver() 
		{
			super(new Object[][] { 
				
			});
		}

		@Override
		public String getName() {
			return "Empty Jena Model";
		}

		@Override
		public TripleStore getConnection() throws Exception 
		{
			return new TripleFile();
		}

		@Override
		public void savePreferences() 
		{
			
		}
	}
	
	private class RdfFileDriver extends SimpleParameterModel implements Driver
	{
		public RdfFileDriver() 
		{
			super(new Object[][] { 
				{ "RDF/OWL/NT File", prefs.getFile(MarrsPreference.MARRS_RDF_FILE) }
			});
		}

		@Override
		public String getName() {
			return "local RDF File";
		}

		@Override
		public TripleStore getConnection() throws Exception 
		{
			return new TripleFile(getFile(0));
		}

		@Override
		public void savePreferences() 
		{
			prefs.setFile(MarrsPreference.MARRS_RDF_FILE, getFile(0));
		}
	}

	private class VirtuosoDriver extends SimpleParameterModel implements Driver
	{
		public VirtuosoDriver() 
		{
			super(new Object[][] 
					{
					{   "Host", prefs.get(MarrsPreference.MARRS_HOST) },
					{	"Port", prefs.getInt(MarrsPreference.MARRS_PORT) },
					{	"Username",	prefs.get(MarrsPreference.MARRS_USER) },
					{	"Password",	prefs.get(MarrsPreference.MARRS_PASS), StringEditor.Flags.PASSWORD },
				});
		}

		@Override
		public String getName() {
			return "Virtuoso JDBC";
		}

		@Override
		public TripleStore getConnection() throws Exception 
		{
			VirtuosoConnection con2 = new VirtuosoConnection();
			
			con2.setHost(this.getString(0));
			con2.setPort("" + this.getInteger(1));
			con2.setUser(this.getString(2));
			con2.setPass(this.getString(3));
			
			con2.init();
			return con2;
		}

		@Override
		public void savePreferences() 
		{
			prefs.set(MarrsPreference.MARRS_HOST, getString(0));
			
			prefs.setInt(MarrsPreference.MARRS_PORT, getInteger(1));
			
			prefs.set(MarrsPreference.MARRS_USER, getString(2));
			prefs.set(MarrsPreference.MARRS_PASS, getString(3));
		}

	}

	private class SparqlDriver extends SimpleParameterModel implements Driver
	{
		public SparqlDriver() 
		{
			super(new Object[][] {
					{ "Sparql endpoint URL", prefs.get(MarrsPreference.MARRS_SPARQL_ENDPOINT) },
					{ "Use authentication", prefs.getBoolean(MarrsPreference.MARRS_SPARQL_AUTHENTICATE) },
					{ "Username", prefs.get(MarrsPreference.MARRS_SPARQL_USER) },
					{ "Password", prefs.get(MarrsPreference.MARRS_SPARQL_PASS), StringEditor.Flags.PASSWORD },						
				});
		}

		@Override
		public String getName() 
		{
			return "Sparql endpoint";
		}

		@Override
		public TripleStore getConnection() throws Exception 
		{
			JenaSparqlEndpoint result = new JenaSparqlEndpoint(this.getString(0));
			if (this.getBoolean(1))
			{
				result.setUser(this.getString(2));
				result.setPass(this.getString(3));
			}			
			return result;
		}

		@Override
		public void savePreferences() 
		{
			prefs.set(MarrsPreference.MARRS_SPARQL_ENDPOINT, getString(0));			
			prefs.setBoolean(MarrsPreference.MARRS_SPARQL_AUTHENTICATE, getBoolean(1));
			prefs.set(MarrsPreference.MARRS_SPARQL_USER, getString(2));			
			prefs.set(MarrsPreference.MARRS_SPARQL_PASS, getString(3));			
		}
	}

	private class DriverSelectionModel
	{
		private Driver driver;
		private final Driver[] drivers;
		
		DriverSelectionModel(Driver[] drivers)
		{
			this.drivers = drivers;
		}
		
		List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
		
		public void addPropertyListener(PropertyChangeListener l)
		{
			listeners.add(l);
		}
		
		public void removePropertyListener (PropertyChangeListener l)
		{
			listeners.remove(l);
		}
		
		public void setDriver(Driver newValue)
		{
			if (driver == newValue) return;
			Driver oldValue = driver;
			driver = newValue;
			firePropertyChanged(new PropertyChangeEvent(this, "driver", oldValue, newValue));
		}

		private void firePropertyChanged(PropertyChangeEvent propertyChangeEvent) 
		{
			for (PropertyChangeListener l : listeners)
			{
				l.propertyChange(propertyChangeEvent);
			}
		}

		public void setDriver(String name)
		{
			for (Driver d : drivers)
			{
				if (d.getName().equals (name))
				{
					setDriver (d);
					return;
				}
			}
			throw new IllegalArgumentException (name + " not a known driver name");
		}

		public Driver getDriver()
		{
			return driver;
		}			
	}

	public class ConfigureDialog extends OkCancelDialog implements PropertyChangeListener
	{
		private JPanel pnlDriverSettings;
		private CardLayout cl;

		public ConfigureDialog() 
		{
			super(frame, "Configure RDF connection", frame, true);
			setDialogComponent(new JScrollPane(createDialogComponent()));
			setSize (640, 400);
			driverSelection.addPropertyListener(this);
		}
		
		public JPanel createDialogComponent()
		{
			JPanel result = new JPanel();
			result.setLayout (new BorderLayout());
			
			JPanel pnlDriverChoice = new JPanel();
			pnlDriverChoice.setLayout(new FlowLayout(FlowLayout.CENTER));
	        pnlDriverChoice.setBorder(BorderFactory.createCompoundBorder (
                    BorderFactory.createTitledBorder("Choose type of connection"),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			
			pnlDriverSettings = new JPanel ();
			cl = new CardLayout();
			pnlDriverSettings.setLayout(cl);
	        pnlDriverSettings.setBorder(BorderFactory.createCompoundBorder (
                    BorderFactory.createTitledBorder("Configuration"),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
	        
			ButtonGroup bgDrivers = new ButtonGroup();
			
			for (final Driver driver : drivers)
			{
				JRadioButton rbtn = new JRadioButton(driver.getName());
				pnlDriverChoice.add (rbtn);
				boolean isSelected = (driverSelection.getDriver() == driver);
				rbtn.setSelected(isSelected);
				final JPanel pnl = new ParameterPanel(driver);
				bgDrivers.add(rbtn);
				rbtn.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent arg0) 
					{
						 driverSelection.setDriver(driver);
					}
				});
				
				cl.addLayoutComponent(pnl, driver.getName());
				pnlDriverSettings.add (pnl, driver.getName());
			}

			cl.show(pnlDriverSettings, driverSelection.getDriver().getName());
			result.add (pnlDriverChoice, BorderLayout.NORTH);
			result.add (pnlDriverSettings, BorderLayout.CENTER);
			
			return result;
		}

		@Override
		public void cancelPressed()
		{	
			super.cancelPressed();
			driverSelection.removePropertyListener(this);
		}
		
		@Override
		public void okPressed()
		{	
			//TODO: need to contents of text fields is committed (see super.okPressed)
			
			getConnection();
			if (con == null) return;
			try {
				// run a test query
				con.sparqlSelect("select (1 as ?a) WHERE {}");

				super.okPressed();
				driverSelection.removePropertyListener(this);

				driverSelection.getDriver().savePreferences();
				prefs.set(MarrsPreference.MARRS_DRIVER, driverSelection.getDriver().getName());
				prefs.store();
				JOptionPane.showMessageDialog(ConfigureDialog.this, "<html>Connection success!");

			} 
			catch (StreamException e) 
			{
				JOptionPane.showMessageDialog(ConfigureDialog.this, "<html>Connection test failed:<br>" + e.getMessage() + 
						"<br>Please check settings and try again");
				e.printStackTrace();
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent arg0) 
		{
			cl.show(pnlDriverSettings, driverSelection.getDriver().getName());
		}
		
	}

	public class ConfigureAction extends AbstractAction
	{
		ConfigureAction()
		{
			super ("Configure connection");
		}

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			ConfigureDialog dlg = new ConfigureDialog();
			dlg.setVisible(true);
		}
	}

	@Override
	public TripleStore get() 
	{
		return getConnection();
	}

}
