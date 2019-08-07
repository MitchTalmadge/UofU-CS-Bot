package com.mitchtalmadge.uofu_cs_bot.util;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CSNamingConventionsTest {

  private static final Course cs1410 = new Course(1410);
  private static final Course cs2420 = new Course(2420);
  private static final Course cs3500 = new Course(3500);
  private static final Course cs3810 = new Course(3810);

  @Test
  public void TestRoleNamingConvention() {
    // No suffix
    Assert.assertEquals("cs-3500", CSNamingConventions.toRoleName(cs3500, CSSuffix.NONE));

    // TA suffix
    Assert.assertEquals("cs-1410-ta", CSNamingConventions.toRoleName(cs1410, CSSuffix.TA));

    // PROF suffix
    Assert.assertEquals("cs-2420-prof", CSNamingConventions.toRoleName(cs2420, CSSuffix.PROFESSOR));
  }

  @Test
  public void TestChannelNamingConvention() {
    Assert.assertEquals("cs-1410", CSNamingConventions.toChannelName(cs1410));
    Assert.assertEquals("cs-2420", CSNamingConventions.toChannelName(cs2420));
    Assert.assertEquals("cs-3500", CSNamingConventions.toChannelName(cs3500));
  }

  @Test
  public void TestNicknameClassGroupNamingConvention() {
    Map<Course, CSSuffix> classMap = new HashMap<>();
    classMap.put(cs3500, CSSuffix.NONE);
    classMap.put(cs1410, CSSuffix.TA);

    Assert.assertEquals("[1410TA,3500]", CSNamingConventions.toNicknameClassGroup(classMap));
  }
}
