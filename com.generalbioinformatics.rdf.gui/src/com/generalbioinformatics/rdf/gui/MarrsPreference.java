/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import java.io.File;

import nl.helixsoft.gui.preferences.Preference;

/**
 * Set of GUI preferences with default values. 
 */
public enum MarrsPreference implements Preference
{
	MARRS_HOST("localhost"), 
	MARRS_PASS("dba"), 
	MARRS_PORT("1111"), 
	MARRS_USER("dba"),

	MARRS_PROJECT_FILE(""), 
	MARRS_PROJECT_UPDATE_URL(""),
	MARRS_PLUGIN_UPDATE_URL(""),
	
	/** Name of most recently used driver */
	MARRS_DRIVER("Sparql endpoint"), 
	MARRS_RDF_FILE(new File(".").getAbsolutePath()),
	
	MARRS_SPARQL_ENDPOINT("http://dev0.generalbioinformatics.com/public-sparql"),
	MARRS_SPARQL_USER(""),
	MARRS_SPARQL_PASS(""),
	MARRS_SPARQL_AUTHENTICATE("" + false),
	
	/** Most recently used project location */
	MARRS_RECENT_1(""),
	MARRS_RECENT_2(""),
	MARRS_RECENT_3(""),
	MARRS_RECENT_4(""),
	MARRS_RECENT_5(""), 
	
	/** Classpath location where the vizmap properties can be found */
	@Deprecated
	MARRS_VIZMAP_PROPS_PATH("com/generalbioinformatics/cy2/MARRS-vizmap.props"),
	;

	public static int RECENT_FILE_NUM = 5;
	public static Preference[] RECENT_FILE_ARRAY = new Preference[] { MARRS_RECENT_1, MARRS_RECENT_2, MARRS_RECENT_3, MARRS_RECENT_4, MARRS_RECENT_5 };
	
	private MarrsPreference(String def)
	{
		this.def = def;
	}
	
	private final String def;
	
	@Override
	public String getDefault() 
	{
		return def;
	}
}