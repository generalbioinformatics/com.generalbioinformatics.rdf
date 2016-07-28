package com.generalbioinformatics.rdf.stream;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/** 
 * Generic interface to write triples to something like a file or stream.
 * <p>
 * Currently, implementations of this interface write to an OutputStream in n-triple format, and focus on handling large volumes 
 * of triples with as little memory overhead as possible.
 */
public interface INtWriter 
{
	/** write out subject/predicate/object statement */
	void writeStatement(Object s, Object p, Object o) throws IOException;
	/** write out subject/predicate/object statement */
	void writeLiteral(Object s, Object p, Object o) throws IOException;
	/** For safety, close flush after you're done to make sure everythng is comitted to the underlying stream or file */
	void flush() throws IOException, InterruptedException, ExecutionException;
}
