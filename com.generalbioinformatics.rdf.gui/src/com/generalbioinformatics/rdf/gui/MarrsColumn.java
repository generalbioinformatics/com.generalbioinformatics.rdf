/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import org.pathvisio.desktop.util.PropertyColumn;

/**
 * Column headers, for when displaying project contents in a table.
 */
public enum MarrsColumn implements PropertyColumn
{
	TITLE("Title")
	;
	
	private final String title;
	
	private MarrsColumn(String title)
	{
		this.title = title;
	}
	
	@Override
	public String getTitle() 
	{
		return title;
	}

}
