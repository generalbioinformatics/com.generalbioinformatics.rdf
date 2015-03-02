/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

/**
 * A collection of prefix / namespace mappings,
 * plus utility functions to abbreviate / expand identifiers.
 */
public interface NamespaceMap 
{
	public String shorten(String iri);
	public String expand(String shortForm);
	public Namespace findPrefix(String a);
}
