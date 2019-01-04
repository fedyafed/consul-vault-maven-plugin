/*
 * Copyright (c) 2018 fedyafed.
 */

package com.github.fedyafed;

import junit.framework.TestCase;

import java.util.Collections;

import static com.github.fedyafed.ReadConsulMojo.getPropertyKey;
import static java.util.Collections.singletonList;

public class ReadConsulMojoTest extends TestCase {
    public void testDefaultPrefixes() {
        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        assertEquals(singletonList("config"), readConsulMojo.getPrefixes());
    }

    public void testDefaultHost() {
        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        assertEquals("localhost", readConsulMojo.getHost());
    }

    public void testDefaultPort() {
        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        assertEquals(8500, readConsulMojo.getPort());
    }

    public void testDefaultToken() {
        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        assertEquals(null, readConsulMojo.getToken());
    }

    public void testDefaultServices() {
        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        assertEquals(Collections.emptyList(), readConsulMojo.getServices());
    }

    /**
     * Test property key converter.
     */
    public void testPropertyKey() {
        assertEquals("test", getPropertyKey("prefix/test", "prefix/"));
        assertEquals("test.key", getPropertyKey("prefix/test/key", "prefix/"));
        assertEquals("prefix.test.key", getPropertyKey("prefix/test/key", ""));
        assertEquals("test", getPropertyKey("prefix/x1/test", "prefix/x1/"));
    }
}
