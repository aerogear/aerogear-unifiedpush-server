package org.jboss.aerogear.unifiedpush.rest.util;

import static org.junit.Assert.assertTrue;

import org.jboss.aerogear.unifiedpush.rest.util.URLUtils;
import org.junit.Test;

public class URLUtilsTest {

	
	@Test
	public void testRemoveLast() {
		String test1 = "https://xxx/x/xx/x/x";
		String test2 = "https://xxx/x/xx/x/x/";
		
		assertTrue(URLUtils.removeLastSlash(test1).equals(test1));
		assertTrue(URLUtils.removeLastSlash(test2).equals(test1));
	}
	
	@Test
	public void testLastPart() {
		String test1 = "https://xxx.com/yyyyy/";
		String test2 = "https://xxx.com/yyyyy?xxx=1&yyy=2";
		
		assertTrue(URLUtils.getLastPart(test1).equals("yyyyy"));
		assertTrue(URLUtils.getLastPart(test2).equals("yyyyy"));
	}
}
