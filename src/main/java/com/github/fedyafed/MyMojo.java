/*
 * Copyright (c) 2018 fedyafed.
 */

package com.github.fedyafed;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "touch", defaultPhase = LifecyclePhase.INITIALIZE)
public class MyMojo extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;


    /**
     * Mojo execution.
     */
    public void execute()
            throws MojoExecutionException {
        File f = outputDirectory;

        if (!f.exists()) {
            boolean dirCreated = f.mkdirs();
            if (!dirCreated) {
                throw new MojoExecutionException("Error creating directory " + f);
            }
        }

        File touch = new File(f, "touch.txt");

        try (OutputStream stream = new FileOutputStream(touch);
             Writer w = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            w.write("touch.txt");
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + touch, e);
        }
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }
}
