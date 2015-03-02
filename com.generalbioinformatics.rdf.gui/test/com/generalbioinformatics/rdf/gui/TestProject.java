package com.generalbioinformatics.rdf.gui;

import org.junit.Assert;
import org.junit.Test;

import com.generalbioinformatics.rdf.gui.MarrsProject;

public class TestProject
{
	interface Cmp { boolean test(int a, int b); }
	enum Oldest implements Cmp 
	{
		EQUAL { public boolean test (int a, int b) { return a == b; } },
		LAST { public boolean test (int a, int b) { return a < b; } },
		FIRST { public boolean test (int a, int b) { return a > b; } };
	}
	
	private void testpair(Cmp cmp, String a, String b)
	{
		int result = MarrsProject.comparePublicationVersions(a, b);
		Assert.assertTrue (cmp.test(result, 0));
	}
	
	@Test
	public void test1()
	{
		testpair (Oldest.EQUAL, null, null);
		testpair (Oldest.FIRST, null, "201308301519");
		testpair (Oldest.LAST,  "201308301519", null);
		testpair (Oldest.FIRST, "201308301519", MarrsProject.PUBLISHVERSION_REPLACEMENT_TOKEN);
		testpair (Oldest.LAST, MarrsProject.PUBLISHVERSION_REPLACEMENT_TOKEN, "201308301519");
		testpair (Oldest.EQUAL, MarrsProject.PUBLISHVERSION_REPLACEMENT_TOKEN, MarrsProject.PUBLISHVERSION_REPLACEMENT_TOKEN);
		
		testpair (Oldest.FIRST, "201205261100", "201308301519");
		testpair (Oldest.LAST,  "201308301519", "201205261100");
		testpair (Oldest.EQUAL, "201308301519", "201308301519");
	}
}
