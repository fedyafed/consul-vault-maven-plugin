/*
 * Copyright (c) 2018 fedyafed.
 */

package com.github.fedyafed;

import junit.framework.TestCase;

import java.io.File;

public class MyMojoTest extends TestCase {
    /**
     * Test get-set.
    */
    public void testSimple() {
        MyMojo myMojo = new MyMojo();
        File file = new File("./target/test/");
        myMojo.setOutputDirectory(file);
        assertEquals(myMojo.getOutputDirectory(), file);
    }
}
