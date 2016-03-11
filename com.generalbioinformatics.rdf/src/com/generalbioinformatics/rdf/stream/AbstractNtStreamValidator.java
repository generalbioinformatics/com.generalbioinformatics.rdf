package com.generalbioinformatics.rdf.stream;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNtStreamValidator implements NtStreamValidator
{
	protected Map <Object, Level> ruleConfig = new HashMap<Object, Level>();
	
	Logger log = LoggerFactory.getLogger("com.generalbioinformatics.rdf.stream.AbstractNtStreamValidator");

	protected void applyRule (boolean test, Object rule, String message)
	{
		if (!test) return;
		
		Level ruleLevel = ruleConfig.get(rule);
		if (ruleLevel == null) ruleLevel = Level.ERROR; // default level is error
		switch (ruleLevel)
		{
			case ERROR:
				throw new RuntimeException (message);				
			case WARN:
				// TODO: method for collecting warnings...
				log.warn(message);
				break;
			case IGNORE: 
				break;
		}
	}
	
	public void setRuleStatus(Object rule, Level level) 
	{
		ruleConfig.put(rule, level);		
	}
}
