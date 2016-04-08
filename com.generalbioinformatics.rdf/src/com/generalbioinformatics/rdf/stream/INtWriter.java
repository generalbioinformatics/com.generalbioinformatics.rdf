package com.generalbioinformatics.rdf.stream;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface INtWriter 
{
	void writeStatement(Object s, Object p, Object o) throws IOException;
	void writeLiteral(Object s, Object p, Object o) throws IOException;
	void flush() throws IOException, InterruptedException, ExecutionException;
}
