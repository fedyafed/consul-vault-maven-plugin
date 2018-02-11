/*
 * Copyright (c) 2018 fedyafed.
 */

package com.github.fedyafed;

import junit.framework.TestCase;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

public class MyMojoTestIT extends TestCase {

    /**
     * Test mojo execution.
     */
    public void testExecute() {
        try {
            MyMojo myMojo = new MyMojo();
            myMojo.setOutputDirectory(new File("./target/test/"));

            myMojo.execute();
        } catch (MojoExecutionException e) {
            fail(e.getMessage());
        }
    }
}