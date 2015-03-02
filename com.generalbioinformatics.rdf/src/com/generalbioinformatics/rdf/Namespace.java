/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

/**
 * a prefix and the namespace it stands for.
 */
public interface Namespace 
{
	public String getPrefix();
	public String toString(); // must be overridden to return full namespace
}
