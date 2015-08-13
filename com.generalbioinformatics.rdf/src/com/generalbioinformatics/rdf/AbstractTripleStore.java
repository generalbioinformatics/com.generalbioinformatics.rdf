/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import nl.helixsoft.recordstream.RecordStream;
import nl.helixsoft.recordstream.StreamException;
import nl.helixsoft.recordstream.TsvRecordStream;
import nl.helixsoft.util.FileUtils;

/**
 * An abstract base class for TripleStores, 
 * providing a simple implementation of the caching feature.
 */
public abstract class AbstractTripleStore implements TripleStore
{
	private File cacheDir = null;
	
	/** {@inheritDoc} */
	@Override public void setCacheDir (File dir)
	{
		this.cacheDir = dir;
	}
	
	public RecordStream sparqlSelect(String query) throws StreamException
	{
		try
		{
			RecordStream rs;
	
			long start = System.currentTimeMillis();
	
			if (cacheDir == null)
			{
				rs = _sparqlSelectDirect(query);
				long delta = System.currentTimeMillis() - start;
				fireQueryPerformed (query, delta);
			}
			else
			{
				if (!cacheDir.exists())
				{
					if (!cacheDir.mkdirs())
						throw new IOException ("Could not create cache directory");
				}
	
				int hashCode = query.hashCode() + 3 * this.hashCode();
				String hashCodeString = String.format ("%08x", hashCode);
				File cacheSubdir = new File (cacheDir, hashCodeString.substring(0, 2)); 
				File out =  new File (cacheSubdir, hashCodeString + ".txt.gz");
	
				if (out.exists())
				{
					// merely "touch" the file, so we know the cached file was used recently.
					org.apache.commons.io.FileUtils.touch(out);
				}
				else
				{
					// make subdir if it doesn't exist.
					if (!cacheSubdir.exists()) { if (!cacheSubdir.mkdir()) throw new IOException ("Couldn't create directory " + cacheSubdir); }
	
					File tmp = File.createTempFile(hashCodeString + "-", ".tmp", cacheSubdir);
					try
					{
						OutputStream os = new FileOutputStream (tmp);
						GZIPOutputStream gos = new GZIPOutputStream(os);
						RecordStream rsx = _sparqlSelectDirect(query);
						long delta = System.currentTimeMillis() - start;
						Utils.queryResultsToFile(this, delta, rsx, query, gos);
						gos.finish();
						os.close();
						fireQueryPerformed (query, delta);
					}
					catch (RuntimeException e)
					{
						tmp.delete();
						throw (e);
					}					
					catch (StreamException e)
					{
						tmp.delete();
						throw (e);
					}
					tmp.renameTo(out);
				}
	
				InputStream is = FileUtils.openZipStream(out);
				rs = TsvRecordStream.open (is).filterComments().get();
			}		
	
			return rs;
		}
		catch (IOException ex)
		{
			throw new StreamException (ex);
		}
	}

	protected void fireQueryPerformed(String query, long delta) 
	{
		for (TripleStoreListener l : listeners)
		{
			l.queryPerformed(query, delta);
		}
	}

	private NS namespaces = new NS();

	public NamespaceMap getNamespaces() { return namespaces; }

	private List<TripleStoreListener> listeners = new ArrayList<TripleStoreListener>();
	
	@Override
	public void addListener(TripleStoreListener l) { listeners.add (l); }
	
	@Override
	public void removeListener(TripleStoreListener l) { listeners.remove (l); }
}
