package com.generalbioinformatics.rdf.stream;

public interface NtStreamValidator 
{
	void validateLiteral (Object s, Object p, Object o);
	void validateStatement (Object s, Object p, Object o);
	void validateUri(String uri);
	void validateLiteral(Object o);
}
