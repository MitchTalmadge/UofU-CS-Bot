package com.mitchtalmadge.uofu_cs_bot.domain.cs;

import org.junit.Assert;
import org.junit.Test;

public class CourseTest {

  private static final Course cs1410 = new Course(1410);
  private static final Course cs2420 = new Course(2420);
  private static final Course cs3500 = new Course(3500);
  private static final Course cs3810 = new Course(3810);

  @Test
  public void TestToString() {
    Assert.assertEquals("1410", cs1410.toString());
    Assert.assertEquals("3500", cs3500.toString());
  }

  /**
   * Tests parsing numbers in string format.
   */
  @Test
  public void TestParseClassNumber() throws Exception {
    Assert.assertEquals(cs1410, new Course("1410"));
    Assert.assertEquals(cs2420, new Course("2420"));
    Assert.assertEquals(cs3810, new Course("  3810  "));
  }

  /**
   * Tests parsing class names with prefixes.
   */
  @Test
  public void TestParseWithPrefixes() throws Exception {
    Assert.assertEquals(cs3810, new Course("cs-3810"));
    Assert.assertEquals(cs2420, new Course("cs 2420"));
    Assert.assertEquals(cs1410, new Course("CS1410"));
  }

  /**
   * Tests parsing class names with suffixes.
   */
  @Test
  public void TestParseWithSuffixes() throws Exception {
    Assert.assertEquals(cs3810, new Course("3810-TA"));
    Assert.assertEquals(cs2420, new Course("2420 ta"));
    Assert.assertEquals(cs1410, new Course("1410 Prof"));
  }

  /**
   * Tests parsing class names with prefixes and suffixes.
   */
  @Test
  public void TestParseWithPrefixesAndSuffixes() throws Exception {
    Assert.assertEquals(cs3810, new Course("cs-3810 TA"));
    Assert.assertEquals(cs2420, new Course("cs 2420-prof"));
    Assert.assertEquals(cs1410, new Course("CS1410tA"));
  }

  @Test(expected = Course.InvalidCourseNameException.class)
  public void TestInvalidPrefix() throws Exception {
    new Course("computerscience-3500");
  }

  @Test(expected = Course.InvalidCourseNameException.class)
  public void TestInvalidSuffix() throws Exception {
    new Course("3500-admin");
  }

  @Test(expected = Course.InvalidCourseNameException.class)
  public void TestInvalidCharacters() throws Exception {
    new Course("cs-34abc00");
  }

  @Test(expected = Course.InvalidCourseNameException.class)
  public void TestTooManyClasses() throws Exception {
    new Course("cs3500, 1410");
  }
}
