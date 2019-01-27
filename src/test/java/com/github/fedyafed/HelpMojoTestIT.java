/*
 * Copyright (c) 2018 fedyafed.
 */

package com.github.fedyafed;

import com.github.fedyafed.generated.HelpMojo;
import junit.framework.TestCase;
import org.apache.maven.plugin.MojoExecutionException;

import java.lang.reflect.Field;

public class HelpMojoTestIT extends TestCase {

    /**
     * Test generated HelpMojo.
     * @throws MojoExecutionException - default Mojo exception.
     */
    public void testHelp() throws MojoExecutionException {
        HelpMojo helpMojo = new HelpMojo();
        helpMojo.execute();
    }

    /**
     * Test generated HelpMojo with detailed report.
     * @throws MojoExecutionException - default Mojo exception.
     */
    public void testDetailedHelp() throws MojoExecutionException {
        HelpMojo helpMojo = new HelpMojo();
        try {
            Field detail = HelpMojo.class.getDeclaredField("detail");
            detail.setAccessible(true);
            detail.setBoolean(helpMojo, true);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new MojoExecutionException("Can not turn on detailed output", e);
        }
        helpMojo.execute();
    }
}
