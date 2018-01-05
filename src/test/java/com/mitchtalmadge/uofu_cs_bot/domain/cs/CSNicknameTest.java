package com.mitchtalmadge.uofu_cs_bot.domain.cs;

import org.junit.Assert;
import org.junit.Test;

public class CSNicknameTest {

    private static final CSClass cs1410 = new CSClass(1410);
    private static final CSClass cs2420 = new CSClass(2420);
    private static final CSClass cs3500 = new CSClass(3500);
    private static final CSClass cs3810 = new CSClass(3810);

    /**
     * Tests nicknames which include a comma separated list of class numbers inside square brackets.
     */
    @Test
    public void TestParseStandardNickname() throws Exception {
        // One class
        Assert.assertArrayEquals(new CSClass[]{cs3500}, new CSNickname("John Doe [3500]").getClasses().toArray());

        // Two classes
        Assert.assertArrayEquals(new CSClass[]{cs3500, cs3810}, new CSNickname("John Doe [3500, 3810]").getClasses().toArray());

        // Three classes
        Assert.assertArrayEquals(new CSClass[]{cs2420, cs3500, cs3810}, new CSNickname("John Doe [2420, 3500, 3810]").getClasses().toArray());
    }

    /**
     * Tests nicknames with comma separated lists of classes in random order inside square brackets.
     */
    @Test
    public void TestParseRandomOrderNickname() throws Exception {
        // Two classes
        Assert.assertArrayEquals(new CSClass[]{cs3500, cs3810}, new CSNickname("John Doe [3810, 3500]").getClasses().toArray());

        // Three classes
        Assert.assertArrayEquals(new CSClass[]{cs2420, cs3500, cs3810}, new CSNickname("John Doe [3500, 2420, 3810]").getClasses().toArray());
    }

    /**
     * Tests nicknames that include prefixes.
     */
    @Test
    public void TestParseWithPrefixes() throws Exception {
        // One class
        Assert.assertArrayEquals(new CSClass[]{cs3500}, new CSNickname("John Doe [cs-3500]").getClasses().toArray());

        // Two classes
        Assert.assertArrayEquals(new CSClass[]{cs3500, cs3810}, new CSNickname("John Doe [CS 3500, 3810]").getClasses().toArray());

        // Three classes
        Assert.assertArrayEquals(new CSClass[]{cs2420, cs3500, cs3810}, new CSNickname("John Doe [2420, Cs-3500, cs3810]").getClasses().toArray());
    }

    /**
     * Tests nicknames that include suffixes.
     */
    @Test
    public void TestParseWithSuffixes() throws Exception {
        // One class
        CSNickname oneClass = new CSNickname("John Doe [3500-TA]");
        Assert.assertArrayEquals(new CSClass[]{cs3500}, oneClass.getClasses().toArray());
        Assert.assertEquals(CSSuffix.TA, oneClass.getSuffixForClass(cs3500));

        // Two classes
        CSNickname twoClasses = new CSNickname("John Doe [3500 ta, 3810]");
        Assert.assertArrayEquals(new CSClass[]{cs3500, cs3810}, twoClasses.getClasses().toArray());
        Assert.assertEquals(CSSuffix.TA, twoClasses.getSuffixForClass(cs3500));
        Assert.assertEquals(CSSuffix.NONE, twoClasses.getSuffixForClass(cs3810));

        // Three classes
        CSNickname threeClasses = new CSNickname("John Doe [2420-ta, 3500 PROF, 3810]");
        Assert.assertArrayEquals(new CSClass[]{cs2420, cs3500, cs3810}, threeClasses.getClasses().toArray());
        Assert.assertEquals(CSSuffix.TA, threeClasses.getSuffixForClass(cs2420));
        Assert.assertEquals(CSSuffix.PROFESSOR, threeClasses.getSuffixForClass(cs3500));
        Assert.assertEquals(CSSuffix.NONE, threeClasses.getSuffixForClass(cs3810));
    }

    /**
     * Tests nicknames that include both prefixes and suffixes.
     */
    @Test
    public void TestParseWithPrefixesAndSuffixes() throws Exception {
        // One class
        CSNickname oneClass = new CSNickname("John Doe [cs3500-TA]");
        Assert.assertArrayEquals(new CSClass[]{cs3500}, oneClass.getClasses().toArray());
        Assert.assertEquals(CSSuffix.TA, oneClass.getSuffixForClass(cs3500));

        // Two classes
        CSNickname twoClasses = new CSNickname("John Doe [CS-3500 ta, cs 3810]");
        Assert.assertArrayEquals(new CSClass[]{cs3500, cs3810}, twoClasses.getClasses().toArray());
        Assert.assertEquals(CSSuffix.TA, twoClasses.getSuffixForClass(cs3500));
        Assert.assertEquals(CSSuffix.NONE, twoClasses.getSuffixForClass(cs3810));

        // Three classes
        CSNickname threeClasses = new CSNickname("John Doe [2420-ta, CS 3500 PROF, 3810]");
        Assert.assertArrayEquals(new CSClass[]{cs2420, cs3500, cs3810}, threeClasses.getClasses().toArray());
        Assert.assertEquals(CSSuffix.TA, threeClasses.getSuffixForClass(cs2420));
        Assert.assertEquals(CSSuffix.PROFESSOR, threeClasses.getSuffixForClass(cs3500));
        Assert.assertEquals(CSSuffix.NONE, threeClasses.getSuffixForClass(cs3810));
    }

    /**
     * Tests nicknames that use parentheses instead of square brackets.
     */
    @Test
    public void TestParentheses() throws Exception {
        // One class
        Assert.assertArrayEquals(new CSClass[]{cs3500}, new CSNickname("John Doe (3500)").getClasses().toArray());

        // Two classes
        Assert.assertArrayEquals(new CSClass[]{cs3500, cs3810}, new CSNickname("John Doe (3500, 3810)").getClasses().toArray());

        // Three classes
        Assert.assertArrayEquals(new CSClass[]{cs2420, cs3500, cs3810}, new CSNickname("John Doe (2420, 3500, 3810)").getClasses().toArray());
    }

    /**
     * Tests nicknames that use a mix of parentheses and square brackets.
     */
    @Test
    public void TestMixedParenthesesSquareBrackets() throws Exception {
        // One class
        Assert.assertArrayEquals(new CSClass[]{cs3500}, new CSNickname("John Doe (3500]").getClasses().toArray());

        // Two classes
        Assert.assertArrayEquals(new CSClass[]{cs3500, cs3810}, new CSNickname("John Doe [3500, 3810)").getClasses().toArray());

        // Three classes
        Assert.assertArrayEquals(new CSClass[]{cs2420, cs3500, cs3810}, new CSNickname("John Doe [2420, 3500, 3810)").getClasses().toArray());
    }

    /**
     * Tests nicknames which include more than one set of brackets in the nickname.
     */
    @Test
    public void TestMultipleBracketsInNickname() throws Exception {
        // One class
        Assert.assertArrayEquals(new CSClass[]{cs3500}, new CSNickname("John Doe [3500] [USD]").getClasses().toArray());

        // Two classes
        Assert.assertArrayEquals(new CSClass[]{cs3500, cs3810}, new CSNickname("[Super Cool Clan] John Doe [3500, 3810]").getClasses().toArray());

        // Three classes
        Assert.assertArrayEquals(new CSClass[]{cs2420, cs3500, cs3810}, new CSNickname("John Doe [Supreme Overlord][2420, 3500, 3810]").getClasses().toArray());
    }

    /**
     * Tests that a null nickname results in no classes parsed.
     */
    @Test
    public void TestNullNickname() throws Exception {
        Assert.assertArrayEquals(new CSClass[0], new CSNickname(null).getClasses().toArray());
    }

    /**
     * Tests updating a nickname to proper naming conventions.
     */
    @Test
    public void TestUpdateNicknameClassGroup() throws Exception {
        // One class
        Assert.assertEquals("John Doe [3500] [USD]", new CSNickname("John Doe (CS3500] [USD]").updateNicknameClassGroup("John Doe (CS3500] [USD]"));

        // Two classes
        Assert.assertEquals("[Super Cool Clan] John Doe [3500,3810TA]", new CSNickname("[Super Cool Clan] John Doe (cs-3500, CS-3810-TA)").updateNicknameClassGroup("[Super Cool Clan] John Doe (CS-3500, CS-3810-TA)"));

        // Three classes
        Assert.assertEquals("John Doe [Supreme Overlord][2420PROF,3500,3810]", new CSNickname("John Doe [Supreme Overlord][3500,    2420prof,      3810]").updateNicknameClassGroup("John Doe [Supreme Overlord][3500,    2420prof,      3810]"));
    }

    /**
     * Tests updating a nickname when the CSNickname has parsed no classes.
     */
    @Test
    public void TestUpdateNicknameClassGroupWithNoClasses() throws Exception {
        Assert.assertEquals("John Doe [USD]", new CSNickname("John Doe [USD]").updateNicknameClassGroup("John Doe [USD]"));
    }

    /**
     * Tests updating a nickname when the nickname provided is null.
     */
    @Test
    public void TestUpdateNicknameClassGroupWhenNull() throws Exception {
        Assert.assertEquals(null, new CSNickname(null).updateNicknameClassGroup(null));
    }
}