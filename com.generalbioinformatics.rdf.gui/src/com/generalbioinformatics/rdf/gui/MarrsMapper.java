/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf.gui;

import nl.helixsoft.recordstream.StreamException;

/**
 * Interface for any GUI that wants to translate SPARQL to networks.
 */
public interface MarrsMapper 
{
	public int createNetwork(String q, MarrsQuery mq) throws StreamException;
	public int addAttributes(String q) throws StreamException;
	public void setProject(MarrsProject project);
	public int addAttributesMatrix(String query) throws StreamException;
	public int popupResults(String query) throws StreamException;
}
