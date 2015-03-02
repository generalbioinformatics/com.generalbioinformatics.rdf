/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.stream;

import java.io.IOException;
import java.util.Iterator;

import nl.helixsoft.recordstream.AbstractStream;

import com.generalbioinformatics.rdf.stream.NtStream.ParseException;

public abstract class AbstractTripleStream extends AbstractStream<Statement> implements TripleStream
{
	private class StatementStreamIterator implements Iterator<Statement>
	{
		private Statement next;
		private final TripleStream parent;
		
		StatementStreamIterator (TripleStream parent)
		{
			this.parent = parent;
			try {
				next = parent.getNext();
			} 
			catch (IOException e) 
			{
				throw new RuntimeException (e);
			}
			catch (ParseException e) 
			{
				throw new RuntimeException (e);
			}
		}

		@Override
		public boolean hasNext() 
		{
			return (next != null);
		}

		@Override
		public Statement next() 
		{
			Statement result = next;
			try {
				next = parent.getNext();
			} 
			catch (IOException e) 
			{
				throw new RuntimeException(e);
			}
			catch (ParseException e) 
			{
				throw new RuntimeException(e);
			}
			return result;
		}

		@Override
		public void remove() 
		{
			throw new UnsupportedOperationException();
		}
	}
		
	@Override
	public Iterator<Statement> iterator()
	{
		return new StatementStreamIterator(this);
	}

}
