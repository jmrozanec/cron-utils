package com.cronutils.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void join() {
        assertNull(StringUtils.join(null, ""));
        assertEquals(StringUtils.join(new Object[] {}, ""), "");
        assertEquals(StringUtils.join(new Object[] { null }, ""), "null");
        assertEquals(StringUtils.join(new Object[] { "a", "b", "c" }, "--"), "a--b--c");
        assertEquals(StringUtils.join(new Object[] { "a", "b", "c" }, null), "abc");
        assertEquals(StringUtils.join(new Object[] { "a", "b", "c" }, ""), "abc");
        assertEquals(StringUtils.join(new Object[] { null, "", "a" }, ","), "null,,a");
    }

}
