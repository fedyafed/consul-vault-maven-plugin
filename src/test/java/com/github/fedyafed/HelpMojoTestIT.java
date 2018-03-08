/*
 * Copyright (c) 2018 fedyafed.
 */

package com.github.fedyafed;

import com.github.fedyafed.generated.HelpMojo;
import junit.framework.TestCase;
import org.apache.maven.plugin.MojoExecutionException;

public class HelpMojoTestIT extends TestCase {

    /**
     * Test generated HelpMojo.
     * @throws MojoExecutionException - default Mojo exception.
     */
    public void testHelp() throws MojoExecutionException {
        HelpMojo helpMojo = new HelpMojo();
        helpMojo.execute();
    }
}
