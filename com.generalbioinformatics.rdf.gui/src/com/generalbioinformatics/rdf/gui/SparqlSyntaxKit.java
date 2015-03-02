/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import com.generalbioinformatics.rdf.gui.SparqlLexer;

import jsyntaxpane.DefaultSyntaxKit;

/**
 * Helper for syntax hihglighting of a SPARQL query
 */
public class SparqlSyntaxKit extends DefaultSyntaxKit 
{
	public SparqlSyntaxKit() {
		super(new SparqlLexer());
	}
}
