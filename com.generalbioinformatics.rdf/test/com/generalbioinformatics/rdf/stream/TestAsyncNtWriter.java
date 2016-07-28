package com.generalbioinformatics.rdf.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.Thread.State;
import java.util.concurrent.ExecutionException;

import com.generalbioinformatics.rdf.stream.NtStream.ParseException;

import junit.framework.TestCase;

/** Test functioning of AsyncNtWriter */
public class TestAsyncNtWriter extends TestCase 
{
	/** make sure asyncNtWriter terminates if you call flush after writing the last triple */
	public void testShutdown() throws IOException, InterruptedException, ExecutionException
	{
		AsyncNtWriter writer = new AsyncNtWriter(System.out);
		Thread t = writer._getWriterThread();
		assertEquals (State.NEW, t.getState());		
		for (int i = 0; i < 10; ++i)
		{
			writer.writeLiteral("http://generalbioinformatics.com/example", "http://generalbioinformatics.com/example/count", i);
		}
		writer.flush();
		assertEquals (State.TERMINATED, t.getState());
	}

	/** make sure asyncNtWriter doesn't start thread if you don't write anything */
	public void testNoWriteShutdown() throws IOException, InterruptedException, ExecutionException
	{
		AsyncNtWriter writer = new AsyncNtWriter(System.out);
		Thread t = writer._getWriterThread();
		assertEquals (State.NEW, t.getState());		
		writer.flush();
		assertEquals (State.NEW, t.getState());
	}

	/** Simulate a very slow writing process */
	private class SlowMockStream extends OutputStream
	{
		int MAX_BUFFER_SIZE = 65536;		
		byte[] buffer = new byte[MAX_BUFFER_SIZE];
		int pos = 0;
		int delay = 0;
		
		@Override
		public void write(int b) throws IOException {
			buffer[pos++] = (byte)b;
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			assert (pos < MAX_BUFFER_SIZE);
		}
		
		public InputStream getInputStream()
		{
			return new ByteArrayInputStream(buffer);
		}
		
		@Override
		public String toString()
		{
			return new String(buffer, 0, pos);
		}
	}
	
	/** Check that all statements are written even in a very slow output stream 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 * @throws ParseException */
	public void testSlowFlush() throws IOException, InterruptedException, ExecutionException, ParseException
	{
		SlowMockStream buffer = new SlowMockStream();
		AsyncNtWriter writer = new AsyncNtWriter(buffer);
		Thread t = writer._getWriterThread();
		assertEquals (State.NEW, t.getState());		
		
		final int STATEMENT_NUM = 10;
		
		for (int i = 0; i < STATEMENT_NUM; ++i)
		{
			writer.writeLiteral("http://generalbioinformatics.com/example", "http://generalbioinformatics.com/example/count", i);
		}

		// the following call should wait until all statements have finished writing
		writer.flush();
		
		assertEquals (State.TERMINATED, t.getState());
				// now read it back and make sure contents is exactly as expected
		NtStream in = new NtStream(buffer.getInputStream());
		for (int i = 0; i < STATEMENT_NUM; ++i)
		{
			Statement st = in.getNext();
			assertEquals ("http://generalbioinformatics.com/example", st.getSubjectUri());
			assertEquals ("http://generalbioinformatics.com/example/count", st.getPredicateUri());
			assertEquals ("" + i, st.getLiteral());
		}
		
		//TODO - should return null now but doesn't ?
//		assertFalse (in.getNext());		
	}

}
