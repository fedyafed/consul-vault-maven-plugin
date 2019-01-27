/*
 * Copyright (c) 2018 fedyafed.
 */

package com.github.fedyafed;

import com.ecwid.consul.v1.ConsulClient;
import com.pszymczyk.consul.junit.ConsulResource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Properties;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ReadConsulMojoKVTestIT {

    @ClassRule
    public static final ConsulResource consul = new ConsulResource();
    private static final String PREFIX = "config";
    private static final String KEY = "test";
    private static final String CUSTOM_PREFIX = "conf";
    private static final String CUSTOM_PREFIX_KEY = "test2";
    private static final String VALUE = "Test Value.";
    private static final String NEW_VALUE = "Test New Value.";

    private MavenProject projectStub;
    private ConsulClient client;
    private int httpPort;

    /**
     * Before each test execution.
     */
    @Before
    public void setUp() {
        projectStub = new MavenProject();
        consul.reset();
        httpPort = consul.getHttpPort();
        client = new ConsulClient("localhost", httpPort);
    }

    /**
     * Test default mojo execution.
     */
    @Test
    public void testDefaultExecute() throws MojoExecutionException {
        client.setKVValue(PREFIX + "/" + KEY, VALUE);

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        assertEquals(expectedProperties, projectStub.getProperties());
    }

    /**
     * Test mojo execution with custom prefix.
     */
    @Test
    public void testCustomPrefix() throws MojoExecutionException {
        client.setKVValue(PREFIX + "/" + KEY, VALUE);
        client.setKVValue(CUSTOM_PREFIX + "/" + CUSTOM_PREFIX_KEY, VALUE);

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.setPrefixes(singletonList(CUSTOM_PREFIX));
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        expectedProperties.put(CUSTOM_PREFIX_KEY, VALUE);
        assertEquals(expectedProperties, projectStub.getProperties());
    }

    /**
     * Test mojo execution with custom prefix.
     */
    @Test
    public void testSingleKeyPrefix() throws MojoExecutionException {
        client.setKVValue(PREFIX + "/" + KEY, VALUE);
        client.setKVValue(CUSTOM_PREFIX + "/" + CUSTOM_PREFIX_KEY, VALUE);

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.setPrefixes(singletonList(PREFIX + "/" + KEY));
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        expectedProperties.put(PREFIX + "." + KEY, VALUE);
        assertEquals(expectedProperties, projectStub.getProperties());
    }

    /**
     * Test mojo execution with absent prefix.
     */
    @Test
    public void testAbsentKeyPrefix() throws MojoExecutionException {
        client.setKVValue(PREFIX + "/" + KEY, VALUE);

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.setPrefixes(singletonList(CUSTOM_PREFIX));
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        assertEquals(expectedProperties, projectStub.getProperties());
    }

    /**
     * Test mojo execution with many prefixes.
     * Test overriding properties - last prefix win.
     */
    @Test
    public void testManyPrefixes() throws MojoExecutionException {
        client.setKVValue(PREFIX + "/" + KEY, VALUE);
        client.setKVValue(CUSTOM_PREFIX + "/" + CUSTOM_PREFIX_KEY, VALUE);
        client.setKVValue(CUSTOM_PREFIX + "/" + KEY, NEW_VALUE);

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        ArrayList<String> prefixes = new ArrayList<>();
        prefixes.add(PREFIX);
        prefixes.add(CUSTOM_PREFIX);
        readConsulMojo.setPrefixes(prefixes);
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        expectedProperties.put(KEY, NEW_VALUE);
        expectedProperties.put(CUSTOM_PREFIX_KEY, VALUE);
        assertEquals(expectedProperties, projectStub.getProperties());
    }
}