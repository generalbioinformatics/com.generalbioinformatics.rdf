/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for NtWriter that offloads writes to a separate thread.
 * Results in massive speedup due to utilising two processors instead of one, and reduced IO blocking.
 * (Measured 6x speed-up in some situations)
 * <p>
 * This class has been tested enough to be reasonably confident, nevertheless,
 * be cautious when using this, double-check the output and when in doubt, switch to a regular NtWriter.
 */
public class AsyncNtWriter implements INtWriter
{	
	Logger log = LoggerFactory.getLogger("com.generalbioinformatics.rdf.stream.AsyncNtWriter");

	/** 
	 * each element in our queue is a 4-object array
	 * object[0] is a message from the Message enum
	 * objects[1..3] make up the triple.
	*/
	private final BlockingQueue<Object[]> queue;
	enum Message { LITERAL, STATEMENT, EOF };
	
	private final WriterThread writer;
	private boolean started = false;
	
	@Deprecated
	/** NO-OP, gets started automatically */
	public void start()
	{		
	}
	
	
	/** Separate thread that consumes items from our blockingQueue and writes them out 
	 * (using an ordinary NtWriter internally) 
	 */
	private class WriterThread extends Thread 
	{	
		private final OutputStream os;
		private final NtWriter nt;
		private Throwable exception = null;
		
		WriterThread(OutputStream os)
		{
			this.os = os;
			this.nt = new NtWriter(os);
		}

		/** adjust validator of the wrapped NtWriter */
		public synchronized void setValidator (NtStreamValidator validator)
		{
			nt.setValidator(validator);
		}

		/** avoid using... leaks internal object from different thread */
		public synchronized NtStreamValidator getValidator ()
		{
			return nt.getValidator();
		}

		public void run() 
		{
			boolean done = false;
			try {
				// main loop continues until we encounter an EOF message, a.k.a. 'poison pill'
				while (!done)
				{
					Object[] data = queue.take();
					switch ((Message)data[0])
					{
					case LITERAL:
						nt.writeLiteral(data[1], data[2], data[3]);
						break;
					case STATEMENT:
						nt.writeStatement(data[1], data[2], data[3]);
						break;
					case EOF:
						done = true;
						break;
					}
				}
			}
			// keep track of any exceptions that occurred during writing
			// when an exception occurs, we stop processing, so it's important to check frequently for exceptions while filling the queue.
			catch (Throwable t)
			{
				exception = t;
				t.printStackTrace(); 
			}
			
			// clean up
			finally {
				try {
					os.flush();
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/** return true if an exception occurred during processing */
		public synchronized boolean hasException() 
		{
			return exception != null;
		}

		/** return exception that occurred during processing, or null if there hasn't been any (yet) */
		public synchronized Throwable getException() 
		{
			return exception;
		}
		
	}
	
	private final int CAPACITY = 10000;
	
	/** 
	 * An NtWriter that handles write operations on a separate thread,
	 * leading to better usage of system resources and better throughput
	 * compared to a regular NtWriter.
	 */
	public AsyncNtWriter (OutputStream os)
	{
		this.writer = new WriterThread(os);
		this.queue = new LinkedBlockingQueue<Object[]>(CAPACITY);
	}
	
	/** 
	 * start our thread if not already started. This is called whenever we post something on the queue, to make
	 * sure that the consumer is actually processing the queue.
	 */
	private void startThreadIfNecessary()
	{
		if (!started)
		{
			log.debug("Starting work thread: " + writer);
			writer.start();
			started = true;
		}
	}

	@Override
	public void flush() throws IOException, InterruptedException
	{
		// if we haven't started yet, we don't have to write anything
		if (!started) return;
		
		// otherwise, send poison pill and wait for child thread to complete
		queue.put(new Object[] { Message.EOF });
		writer.join();
		log.debug("Work thread stopped: " + writer);
		
		// if there was any exception in the child thread, re-throw
		if (writer.hasException()) throw new IOException(writer.getException());
	}
	
	@Override
	public void writeStatement(Object s, Object p, Object o) throws IOException 
	{
		try {
			startThreadIfNecessary();
			
			// if there was any exception in the child thread, re-throw. queue won't get processed anymore
			if (writer.hasException()) throw new IOException(writer.getException());
			
			queue.put(new Object[] { Message.STATEMENT, s, p, o });
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
		
	@Override
	public void writeLiteral(Object s, Object p, Object o) throws IOException 
	{
		try {
			startThreadIfNecessary();
			
			// if there was any exception in the child thread, re-throw. queue won't get processed anymore
			if (writer.hasException()) throw new IOException(writer.getException());

			queue.put(new Object[] { Message.LITERAL, s, p, o });
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
	
	/** avoid using. 
	 * This will leak a Validator object that lives on a different thread, and
	 * is not thread-safe to use. */
	@Deprecated
	public NtStreamValidator getValidator() 
	{ 
		return writer.getValidator(); 
	}

	/**
	 * Supply a different triple validator instead of the default one.
	 * You may set this to null, in which case strict validation is disabled.
	 */
	public void setValidator(NtStreamValidator value) 
	{ 
		writer.setValidator(value);
	}

	/** package private, for unit testing only */
	Thread _getWriterThread() {
		return writer;
	}

}
