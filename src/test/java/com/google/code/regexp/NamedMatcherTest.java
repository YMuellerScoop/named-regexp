/**
 * Copyright (C) 2012 The named-regexp Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.regexp;

import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests {@link NamedMatcher}
 */
public class NamedMatcherTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFindSuccess() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
        NamedMatcher m = p.matcher("abcfoo");
        assertTrue(m.find());
    }

    @Test
    public void testFindFail() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
        NamedMatcher m = p.matcher("hello");
        assertFalse(m.find());
    }

    @Test
    public void testStartPositionWithGroupName() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
        NamedMatcher m = p.matcher("abcfooxyz");
        m.find();
        assertEquals(3, m.start("named"));
    }

    @Test
    public void testStartPositionWithGroupIndex() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
        NamedMatcher m = p.matcher("abcfooxyz");
        m.find();
        assertEquals(1, m.start(2)); // 2 = index of (b)
    }

    @Test
    public void testEndPositionWithGroupName() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
        NamedMatcher m = p.matcher("abcfooxyz");
        m.find();
        assertEquals(6, m.end("named"));
    }

    @Test
    public void testEndPositionWithGroupIndex() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
        NamedMatcher m = p.matcher("abcfooxyz");
        m.find();
        assertEquals(2, m.end(2)); // 2 = index of (b)
    }

    @Test
    public void testToMatchResult() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
        NamedMatcher m = p.matcher("abcfoo");

        m.find();
        NamedMatchResult r = m.toMatchResult();
        assertNotNull(r);

        assertEquals("foo", r.group("named"));

        assertEquals(0, r.start());
        assertEquals(3, r.start("named"));
        assertEquals(6, r.end());
        assertEquals(6, r.end("named"));
    }

    @Test
    public void testUsePattern() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
        NamedMatcher m = p.matcher("xyzabcxabcx");
        m.find();
        assertEquals(3, m.start());
        m.find();
        assertEquals(7, m.start());

        // no more matches, m.find() should return false
        assertFalse(m.find());

        m.usePattern(NamedPattern.compile("xy(?<named>z)"));
        m.reset();
        m.find();
        assertEquals(0, m.start());
    }

    @Test
    public void testReset() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
        NamedMatcher m = p.matcher("abcxabcx");

        // advance find to last match
        while(m.find()) ;

        // resetting should force m.find() to search from beginning
        m.reset();

        m.find();
        assertEquals(0, m.start());
    }

    @Test
    public void testResetCharSequence() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
        NamedMatcher m = p.matcher("abcxabcx");

        // make sure at least one match is found and then advance
        // find to the end
        assertTrue(m.find());
        while(m.find()) ;

        m.reset("dummy.*pattern");

        assertFalse(m.find());

        // move matching pattern to a diff position than original
        m.reset("hello world abcx foo bar");
        assertTrue(m.find());
        assertEquals(12, m.start());
    }

    @Test
    public void testNoMatchesForNamedGroup() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
        NamedMatcher m = p.matcher("abcd");
        assertFalse(m.find());

        // throws IllegalStateException("No match found")
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("No match found");
        m.group("named");
    }

    @Test
    public void testNamedGroupAfterUnnamedAndNoncaptureGroups() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
        NamedMatcher m = p.matcher("abcx");
        m.find();
        assertEquals("x", m.group("named"));
    }

    @Test
    public void testNamedGroupAfterUnnamedGroups() {
        NamedPattern p = NamedPattern.compile("(?:c)(?<named>x)");
        NamedMatcher m = p.matcher("abcx");
        m.find();
        assertEquals("x", m.group("named"));
    }

    @Test
    public void testNamedGroupAfterNoncaptureGroups() {
        NamedPattern p = NamedPattern.compile("(?:c)(?<named>x)");
        NamedMatcher m = p.matcher("abcx");
        m.find();
        assertEquals("x", m.group("named"));
    }

    @Test
    public void testNamedGroupOnly() {
        NamedPattern p = NamedPattern.compile("(?<named>x)");
        NamedMatcher m = p.matcher("abcx");
        m.find();
        assertEquals("x", m.group("named"));
    }

    @Test
    public void testMatchNamedGroupAfterAnotherNamedGroup() {
        NamedPattern p = NamedPattern.compile("(a)(?<foo>b)(?:c)(?<named>x)");
        NamedMatcher m = p.matcher("abcx");
        m.find();
        assertEquals("x", m.group("named"));
    }

    @Test
    public void testIndexOfNestedNamedGroup() {
        NamedPattern p = NamedPattern.compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))");
        NamedMatcher m = p.matcher("abcdx");
        m.find();
        assertEquals("x", m.group("named"));
    }

    @Test
    public void testOrderedGroupsHasMatchesInOrder() {
        NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
        NamedMatcher m = p.matcher("abcfoo");
        m.find();
        List<String> matches = m.orderedGroups();
        assertEquals(3, matches.size());
        assertEquals("a", matches.get(0));
        assertEquals("b", matches.get(1));
        assertEquals("foo", matches.get(2));
    }

    @Test
    public void testNamedGroupsDoesNotThrowIndexOutOfBounds() {
        // NamedMatcher.namedGroups() is used to get a map of
        // group names to group values. This should ignore unnamed
        // groups (exclude them from the map), but the unnamed
        // groups were throwing off the function, causing it to
        // fetch a named group at a non-existent index.
        // See Issue #1
        NamedPattern p = NamedPattern.compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))");
        NamedMatcher m = p.matcher("abcdx");
        try {
            m.namedGroups();
            // verified here: IndexOutOfBoundsException did not occur
        } catch (IndexOutOfBoundsException e) {
            fail("IndexOutOfBoundsException should have been fixed");
        }
    }

    @Test
    public void testNamedGroupsGetsOnlyNamedGroups() {
        NamedPattern p = NamedPattern.compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))");
        NamedMatcher m = p.matcher("abcdxyz");

        Map<String, String> map = m.namedGroups();
        assertEquals(3, map.size());
        assertEquals("b", map.get("foo"));
        assertEquals("dx", map.get("bar"));
        assertEquals("x", map.get("named"));
    }

    @Test
    public void testNamedGroupsWithNoMatchGetsEmptyMap() {
        NamedPattern p = NamedPattern.compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))");
        NamedMatcher m = p.matcher("nada");

        Map<String, String> map = m.namedGroups();
        assertEquals(0, map.size());
    }

    @Test
    public void testStandardPatternGetsOrigWithoutNamed() {
        final String ORIG_PATT = "(a)(b)(?:c)(?<named>x)";
        final String PATT_W_NO_NAMED_GRPS = "(a)(b)(?:c)(x)";
        NamedPattern p = NamedPattern.compile(ORIG_PATT);
        NamedMatcher m = p.matcher("abcx");
        assertEquals(PATT_W_NO_NAMED_GRPS, m.standardPattern().pattern());
    }

    @Test
    public void testNamedPatternCallGetsOriginalNamedPattern() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m = p.matcher("abcx");
        assertEquals(p, m.namedPattern());
    }

    @Test
    public void testMatchesCallReturnsTrueForMatch() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m = p.matcher("abcx");
        assertTrue(m.matches());
    }

    @Test
    public void testMatchesCallReturnsFalseForMismatch() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m = p.matcher("foo");
        assertFalse(m.matches());
    }

    @Test
    public void testFindCallReturnsTrueForMatchFromBeginning() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m = p.matcher("abcx");
        assertTrue(m.find(0));
    }

    @Test
    public void testFindCallReturnsFalseForMismatchFromBeginning() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m = p.matcher("foo");
        assertFalse(m.find(0));
    }

    @Test
    public void testFindCallReturnsTrueForMatchFromMiddle() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m = p.matcher("Lorem ipsum abcx dolor sit amet");
        assertTrue(m.find(5));
    }

    @Test
    public void testFindCallReturnsFalseForMismatchFromMiddle() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m = p.matcher("Lorem ipsum abcXx dolor sit amet");
        assertFalse(m.find(5));
    }

    @Test
    public void testFindCallReturnsTrueForMatchFromEnd() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m = p.matcher("Lorem ipsum abcx");
        assertTrue(m.find(12));
    }

    @Test
    public void testFindCallReturnsFalseForMismatchFromEnd() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m = p.matcher("Lorem ipsum abcXx");
        assertFalse(m.find(12));
    }

    @Test
    public void testLookingAtCallReturnsTrueWhenAtMatchingText() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m = p.matcher("abcx Lorem ipsum");
        assertTrue(m.lookingAt());
    }

    @Test
    public void testLookingAtCallReturnsFalseWhenAtMismatchingText() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m = p.matcher("Lorem abcx ipsum");
        assertFalse(m.lookingAt());
    }

    @Test
    public void testEqualsReturnsTrueForSameMatcher() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m1 = p.matcher("Lorem abcx ipsum");
        assertTrue(m1.equals(m1));
    }

    @Test
    public void testEqualsReturnsFalseForTwoMatchersWithIdenticalValues() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m1 = p.matcher("Lorem abcx ipsum");
        NamedMatcher m2 = p.matcher("Lorem abcx ipsum");
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testEqualsReturnsFalseForTwoMatchersWithDifferentValues() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m1 = p.matcher("Lorem abcx ipsum");
        NamedMatcher m2 = p.matcher("foo bar");
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testEqualsReturnsFalseForTwoMatcherWithDifferentParentPatterns() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p1 = NamedPattern.compile(PATT);
        NamedPattern p2 = NamedPattern.compile(PATT);
        NamedMatcher m1 = p1.matcher("Lorem abcx ipsum");
        NamedMatcher m2 = p2.matcher("Lorem abcx ipsum");
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testEqualsReturnsFalseWhenComparedWithNull() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m1 = p.matcher("Lorem abcx ipsum");
        assertFalse(m1.equals(null));
    }

    @Test
    public void testEqualsReturnsFalseWhenComparedWithDifferentDataType() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m1 = p.matcher("Lorem abcx ipsum");
        assertFalse(m1.equals(new Object()));
    }

    @Test
    public void testHashCodeGetsUniqueHashForTwoMatchersWithIdenticalValues() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m1 = p.matcher("Lorem abcx ipsum");
        NamedMatcher m2 = p.matcher("Lorem abcx ipsum");
        assertFalse(m1.hashCode() == m2.hashCode());
    }

    @Test
    public void testHashCodeGetsUniqueHashForTwoMatchersWithDifferentValues() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p = NamedPattern.compile(PATT);
        NamedMatcher m1 = p.matcher("Lorem abcx ipsum");
        NamedMatcher m2 = p.matcher("foo bar");
        assertFalse(m1.hashCode() == m2.hashCode());
    }

    @Test
    public void testHashCodeGetsUniqueHashForTwoMatchersWithDifferentParentPatterns() {
        final String PATT = "(a)(b)(?:c)(?<named>x)";
        NamedPattern p1 = NamedPattern.compile(PATT);
        NamedPattern p2 = NamedPattern.compile(PATT);
        NamedMatcher m1 = p1.matcher("Lorem abcx ipsum");
        NamedMatcher m2 = p2.matcher("Lorem abcx ipsum");
        assertFalse(m1.hashCode() == m2.hashCode());
    }
}
