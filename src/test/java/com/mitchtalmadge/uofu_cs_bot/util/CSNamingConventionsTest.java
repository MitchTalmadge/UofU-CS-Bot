package com.mitchtalmadge.uofu_cs_bot.util;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSClass;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class CSNamingConventionsTest {

    private static final CSClass cs1410 = new CSClass(1410);
    private static final CSClass cs2420 = new CSClass(2420);
    private static final CSClass cs3500 = new CSClass(3500);
    private static final CSClass cs3810 = new CSClass(3810);

    @Test
    public void TestRoleNamingConvention() throws Exception {
        // No suffix
        Assert.assertEquals("cs-3500", CSNamingConventions.toRoleName(cs3500, CSSuffix.NONE));

        // TA suffix
        Assert.assertEquals("cs-1410-ta", CSNamingConventions.toRoleName(cs1410, CSSuffix.TA));

        // PROF suffix
        Assert.assertEquals("cs-2420-prof", CSNamingConventions.toRoleName(cs2420, CSSuffix.PROFESSOR));
    }

    @Test
    public void TestChannelNamingConvention() throws Exception {
        Assert.assertEquals("cs-1410", CSNamingConventions.toChannelName(cs1410));
        Assert.assertEquals("cs-2420", CSNamingConventions.toChannelName(cs2420));
        Assert.assertEquals("cs-3500", CSNamingConventions.toChannelName(cs3500));
    }

    @Test
    public void TestNicknameClassGroupNamingConvention() throws Exception {
        Map<CSClass, CSSuffix> classMap = new HashMap<>();
        classMap.put(cs3500, CSSuffix.NONE);
        classMap.put(cs1410, CSSuffix.TA);

        Assert.assertEquals("[1410TA,3500]", CSNamingConventions.toNicknameClassGroup(classMap));
    }
}