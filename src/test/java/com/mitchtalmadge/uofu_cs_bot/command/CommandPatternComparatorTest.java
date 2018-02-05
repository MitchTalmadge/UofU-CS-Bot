package com.mitchtalmadge.uofu_cs_bot.command;

import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;

public class CommandPatternComparatorTest {

    private static final CommandPattern STRICT_MATCH_A;
    private static final CommandPattern LOOSE_MATCH_A;
    private static final CommandPattern STRICT_MATCH_AB;
    private static final CommandPattern LOOSE_MATCH_AB;
    private static final CommandPattern STRICT_MATCH_ABC;
    private static final CommandPattern LOOSE_MATCH_ABC;
    private static final CommandPattern NO_MATCH;
    private static final CommandPattern EMPTY;

    private static final Command COMMAND = new Command(null, new String[]{"a", "b", "c"});

    static {
        STRICT_MATCH_A = new CommandPattern() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandPattern.class;
            }

            @Override
            public String[] value() {
                return new String[]{"a"};
            }

            @Override
            public boolean strict() {
                return true;
            }

        };

        LOOSE_MATCH_A = new CommandPattern() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandPattern.class;
            }

            @Override
            public String[] value() {
                return new String[]{"a"};
            }

            @Override
            public boolean strict() {
                return false;
            }

        };

        STRICT_MATCH_AB = new CommandPattern() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandPattern.class;
            }

            @Override
            public String[] value() {
                return new String[]{"a", "b"};
            }

            @Override
            public boolean strict() {
                return true;
            }

        };

        LOOSE_MATCH_AB = new CommandPattern() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandPattern.class;
            }

            @Override
            public String[] value() {
                return new String[]{"a", "b"};
            }

            @Override
            public boolean strict() {
                return false;
            }

        };

        STRICT_MATCH_ABC = new CommandPattern() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandPattern.class;
            }

            @Override
            public String[] value() {
                return new String[]{"a", "b", "c"};
            }

            @Override
            public boolean strict() {
                return true;
            }

        };

        LOOSE_MATCH_ABC = new CommandPattern() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandPattern.class;
            }

            @Override
            public String[] value() {
                return new String[]{"a", "b", "c"};
            }

            @Override
            public boolean strict() {
                return false;
            }

        };

        NO_MATCH = new CommandPattern() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandPattern.class;
            }

            @Override
            public String[] value() {
                return new String[]{"d"};
            }

            @Override
            public boolean strict() {
                return false;
            }

        };

        EMPTY = new CommandPattern() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CommandPattern.class;
            }

            @Override
            public String[] value() {
                return new String[0];
            }

            @Override
            public boolean strict() {
                return true;
            }

        };
    }

    /**
     * Tests cases where only one matches anything at all. The other side completely does not match the command.
     */
    @Test
    public void testOnlyOneMatches() {
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(NO_MATCH, STRICT_MATCH_ABC));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(NO_MATCH, LOOSE_MATCH_A));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(NO_MATCH, LOOSE_MATCH_AB));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(NO_MATCH, LOOSE_MATCH_ABC));

        assertEquals(-1, new CommandPatternComparator(COMMAND).compare(STRICT_MATCH_ABC, NO_MATCH));
        assertEquals(-1, new CommandPatternComparator(COMMAND).compare(LOOSE_MATCH_A, NO_MATCH));
        assertEquals(-1, new CommandPatternComparator(COMMAND).compare(LOOSE_MATCH_AB, NO_MATCH));
        assertEquals(-1, new CommandPatternComparator(COMMAND).compare(LOOSE_MATCH_ABC, NO_MATCH));
    }

    /**
     * Tests cases where nothing matches.
     */
    @Test
    public void testNoMatches() {
        assertEquals(0, new CommandPatternComparator(COMMAND).compare(STRICT_MATCH_AB, STRICT_MATCH_A));
        assertEquals(0, new CommandPatternComparator(COMMAND).compare(STRICT_MATCH_A, STRICT_MATCH_AB));

        assertEquals(0, new CommandPatternComparator(COMMAND).compare(NO_MATCH, STRICT_MATCH_A));
        assertEquals(0, new CommandPatternComparator(COMMAND).compare(NO_MATCH, STRICT_MATCH_AB));

        assertEquals(0, new CommandPatternComparator(COMMAND).compare(STRICT_MATCH_A, NO_MATCH));
        assertEquals(0, new CommandPatternComparator(COMMAND).compare(STRICT_MATCH_AB, NO_MATCH));
    }

    /**
     * Tests that strict command patterns will not match commands with more arguments than they specify.
     */
    @Test
    public void testStrictDoesNotMatch() {
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(STRICT_MATCH_A, LOOSE_MATCH_AB));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(STRICT_MATCH_A, LOOSE_MATCH_ABC));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(STRICT_MATCH_A, STRICT_MATCH_ABC));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(STRICT_MATCH_AB, LOOSE_MATCH_ABC));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(STRICT_MATCH_AB, STRICT_MATCH_ABC));

        // 0 means that none matched or both matched equally.
        assertEquals(0, new CommandPatternComparator(COMMAND).compare(STRICT_MATCH_A, STRICT_MATCH_AB));
    }

    /**
     * Tests that the more specific of two patterns will match.
     */
    @Test
    public void testMoreSpecific() {
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(LOOSE_MATCH_A, LOOSE_MATCH_AB));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(LOOSE_MATCH_AB, LOOSE_MATCH_ABC));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(LOOSE_MATCH_AB, STRICT_MATCH_ABC));
    }

    /**
     * Tests against an empty command pattern.
     */
    @Test
    public void testEmptyPattern() {
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(EMPTY, LOOSE_MATCH_A));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(EMPTY, LOOSE_MATCH_AB));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(EMPTY, LOOSE_MATCH_ABC));

        assertEquals(0, new CommandPatternComparator(COMMAND).compare(EMPTY, STRICT_MATCH_A));
        assertEquals(0, new CommandPatternComparator(COMMAND).compare(EMPTY, STRICT_MATCH_AB));
        assertEquals(1, new CommandPatternComparator(COMMAND).compare(EMPTY, STRICT_MATCH_ABC));

        assertEquals(0, new CommandPatternComparator(COMMAND).compare(EMPTY, NO_MATCH));
    }

}