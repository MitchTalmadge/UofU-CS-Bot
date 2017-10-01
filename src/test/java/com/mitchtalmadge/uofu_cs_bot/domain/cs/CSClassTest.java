package com.mitchtalmadge.uofu_cs_bot.domain.cs;

import org.junit.Assert;
import org.junit.Test;

public class CSClassTest {

    private static final CSClass cs1410 = new CSClass(1410);
    private static final CSClass cs2420 = new CSClass(2420);
    private static final CSClass cs3500 = new CSClass(3500);
    private static final CSClass cs3810 = new CSClass(3810);

    @Test
    public void TestToString() throws Exception {
        Assert.assertEquals("1410", cs1410.toString());
        Assert.assertEquals("3500", cs3500.toString());
    }

    /**
     * Tests parsing numbers in string format.
     */
    @Test
    public void TestParseClassNumber() throws Exception {
        Assert.assertEquals(cs1410, new CSClass("1410"));
        Assert.assertEquals(cs2420, new CSClass("2420"));
        Assert.assertEquals(cs3810, new CSClass("  3810  "));
    }

    /**
     * Tests parsing class names with prefixes.
     */
    @Test
    public void TestParseWithPrefixes() throws Exception {
        Assert.assertEquals(cs3810, new CSClass("cs-3810"));
        Assert.assertEquals(cs2420, new CSClass("cs 2420"));
        Assert.assertEquals(cs1410, new CSClass("CS1410"));
    }

    /**
     * Tests parsing class names with suffixes.
     */
    @Test
    public void TestParseWithSuffixes() throws Exception {
        Assert.assertEquals(cs3810, new CSClass("3810-TA"));
        Assert.assertEquals(cs2420, new CSClass("2420 ta"));
        Assert.assertEquals(cs1410, new CSClass("1410 Prof"));
    }

    /**
     * Tests parsing class names with prefixes and suffixes.
     */
    @Test
    public void TestParseWithPrefixesAndSuffixes() throws Exception {
        Assert.assertEquals(cs3810, new CSClass("cs-3810 TA"));
        Assert.assertEquals(cs2420, new CSClass("cs 2420-prof"));
        Assert.assertEquals(cs1410, new CSClass("CS1410tA"));
    }

    @Test(expected = CSClass.InvalidClassNameException.class)
    public void TestInvalidPrefix() throws Exception {
        new CSClass("computerscience-3500");
    }

    @Test(expected = CSClass.InvalidClassNameException.class)
    public void TestInvalidSuffix() throws Exception {
        new CSClass("3500-admin");
    }

    @Test(expected = CSClass.InvalidClassNameException.class)
    public void TestInvalidCharacters() throws Exception {
        new CSClass("cs-34abc00");
    }

    @Test(expected = CSClass.InvalidClassNameException.class)
    public void TestTooManyClasses() throws Exception {
        new CSClass("cs3500, 1410");
    }
}