/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Experimental wrapper for NtWriter that offloads writes to a separate thread.
 * Results in massive speedup due to utilising two processors instead of one, and reduced IO blocking.
 * (Measured 6x speed-up in some situations)
 * <p>
 * Still in testing phase, use at your own risk.
 */
public class AsyncNtWriter 
{	
	private final BlockingQueue<Object[]> queue;
	
	private final WriterThread writer;
	
	public void start()
	{
		writer.start();	
	}
	
	private class WriterThread extends Thread 
	{	
		private final OutputStream os;
		private final NtWriter nt;
		private boolean done = false;
		private Throwable exception = null;
		
		WriterThread(OutputStream os)
		{
			this.os = os;
			this.nt = new NtWriter(os);
		}
		
		public void run() 
		{
			try {
				while (true)
				{
					Object[] data = queue.take();
					if (data[0] == Boolean.TRUE)
					{
						nt.writeLiteral(data[1], data[2], data[3]);
					}
					else
					{
						nt.writeStatement(data[1], data[2], data[3]);
					}
				}
			}
			catch (Throwable t)
			{
				exception = t;
				t.printStackTrace(); 
			}
			
			finally {
				done = true;
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public void flush() throws IOException
		{
			//TODO: can this be exposed?
			os.flush();
		}

		public void close() throws IOException
		{
			//TODO: can this be exposed?
			os.close();
		}
		
	}
	
//	private static final String CHARSET = "UTF-8"; // NT is always in UTF-8 (even on windows), in accordance with N-triple specs. 
	
//	private boolean validate = true; // perform validation checks before writing
//	private NtStreamValidator strictValidator = new DefaultNtStreamValidator(); // perform a level of validation that is more strict than just the bare minimum defined by RDF 
//	private boolean escapeUnicode = false;
	
//	private long stmtCount = 0;
	
	private final int CAPACITY = 10000;
	
	public AsyncNtWriter (OutputStream os)
	{
		this.writer = new WriterThread (os);
		this.queue = new LinkedBlockingQueue<Object[]>(CAPACITY);
	}

	public void flush() throws IOException, InterruptedException, ExecutionException
	{
		//TODO... does this work?
		while (!(writer.done || queue.isEmpty()))
		{
			Thread.sleep(50);
		}
		if (writer.exception != null) throw new ExecutionException(writer.exception);
		writer.flush();
	}

	public void waitAndClose() throws IOException, InterruptedException, ExecutionException
	{
		flush();
		writer.close();
	}
	
/*
	public void write(Statement st) throws IOException 
	{
		stmtCount++;
		if (validate)
		{
			if (!st.isSubjectAnon()) { validateUri (st.getSubjectUri()); }
			validateUri (st.getPredicateUri());
			if (!st.isObjectAnon() && !st.isLiteral()) validateUri (st.getObjectUri());
		}
		st.write (os, escapeUnicode);
	}
*/
	
	public void writeStatement(Object s, Object p, Object o) throws IOException, InterruptedException 
	{
		queue.put(new Object[] { Boolean.FALSE, s, p, o });
	}
	
	/*
	
	public void writeStatement(String s, String p, String o) throws IOException 
	{
		if (validate)
		{
			if (strictValidator != null)
			{
				strictValidator.validateStatement(s, p, o);
			}
			validateUri (s);
			validateUri (p);
			validateUri (o);
		}
		
		stmtCount++;
		writeResource (s);
		os.write (' ');
		writeResource (p);
		os.write (' ');
		writeResource (o);
		os.write (' ');
		os.write ('.');
		os.write ('\n');
	}

	static Map<Class<?>, String> rdfTypes = new HashMap<Class<?>, String>();
	static {
		rdfTypes.put (Boolean.class, "boolean");
		rdfTypes.put (Integer.class, "int");
		rdfTypes.put (Double.class, "double");
		rdfTypes.put (Float.class, "float");
		rdfTypes.put (Date.class, "date");
		rdfTypes.put (Long.class, "long");
	}

	*/
	
	public void writeLiteral(Object s, Object p, Object o) throws IOException, InterruptedException 
	{
		queue.put(new Object[] { Boolean.TRUE, s, p, o });
	}

	/*
	public void writeLiteral(String s, String p, Object o) throws IOException 
	{
		if (validate)
		{
			if (strictValidator != null)
			{
				strictValidator.validateLiteral(s, p, o);
			}	
			validateUri (s);
			validateUri (p);
		}
		
		stmtCount++;
		AsyncNtWriter.writeLiteral(os, s, p, o, escapeUnicode);
	}
*/
	
//	/** returns the number of statements (literal and non-literal) */
//	public long getStatementCount()
//	{
//		return stmtCount;
//	}
		
	/**
	 * If escapeUnicode is true, then codepoints above 127 will be esacped with \\u
	 * If escapeUnicode is false, then codepoints above 127 will be encoded as UTF-8 format.
	 * Default value is false.
	 */
//	public void setEscapeUnicode(boolean value) 
//	{
//		escapeUnicode = value;
//	}
	
//	public void setStrictValidation(boolean value) 
//	{
//		if (value)
//		{
//			strictValidator = new DefaultNtStreamValidator();
//			validate = true;
//		}
//		else
//		{
//			strictValidator = null;
//		}
//		
//	}

	public NtStreamValidator getValidator() 
	{ 
		//TODO: must be synchronized
		return writer.nt.getValidator(); 
	}

	/**
	 * Supply a different triple validator instead of the default one.
	 * You may set this to null, in which case strict validation is disabled.
	 */
	public void setValidator(NtStreamValidator value) 
	{ 
		//TODO: must be synchronized		
		writer.nt.setValidator(value);
	}

}
