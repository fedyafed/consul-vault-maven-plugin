/*
 * Copyright (c) 2019 fedyafed.
 */

package com.github.fedyafed;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.pszymczyk.consul.junit.ConsulResource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.net.Inet4Address;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ReadConsulMojoServiceTestIT {

    @ClassRule
    public static final ConsulResource consul = new ConsulResource();
    private static final String SERVICE_NAME = "test-service";
    private static final String SERVICE_SCHEME = "ftp";
    private static final String SERVICE_HOST = "test-host";
    private static final int SERVICE_PORT = 21;
    private static final String LOCALHOST_IP = Inet4Address.getLoopbackAddress().getHostAddress();

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
        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        assertEquals(expectedProperties, projectStub.getProperties());
    }

    /**
     * Find consul service.
     */
    @Test
    public void testFindMySelf() throws MojoExecutionException {
        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.setServices(Collections.singletonList("consul"));
        readConsulMojo.execute();

        int serverPort = consul.getServerPort();
        Properties expectedProperties = new Properties();
        expectedProperties.setProperty("consul.uri", "http://" + LOCALHOST_IP + ":" + serverPort);
        expectedProperties.setProperty("consul.scheme", "http");
        expectedProperties.setProperty("consul.host", LOCALHOST_IP);
        expectedProperties.setProperty("consul.port", String.valueOf(serverPort));
        assertEquals(expectedProperties, projectStub.getProperties());
    }

    /**
     * Find custom service with custom scheme.
     */
    @Test
    public void testFindCustomService() throws MojoExecutionException {
        NewService service = new NewService();
        service.setName(SERVICE_NAME);
        service.setAddress(SERVICE_HOST);
        service.setPort(SERVICE_PORT);
        service.setMeta(Collections.singletonMap("scheme", SERVICE_SCHEME));
        client.agentServiceRegister(service);

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.setServices(Collections.singletonList(SERVICE_NAME));
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        expectedProperties.setProperty(SERVICE_NAME + ".uri",
                String.format("%s://%s:%s", SERVICE_SCHEME, SERVICE_HOST, SERVICE_PORT));
        expectedProperties.setProperty(SERVICE_NAME + ".scheme", SERVICE_SCHEME);
        expectedProperties.setProperty(SERVICE_NAME + ".host", SERVICE_HOST);
        expectedProperties.setProperty(SERVICE_NAME + ".port", String.valueOf(SERVICE_PORT));
        assertEquals(expectedProperties, projectStub.getProperties());
    }

    /**
     * Test custom service with default http scheme.
     */
    @Test
    public void testFindCustomHttpService() throws MojoExecutionException {
        NewService service = new NewService();
        service.setName(SERVICE_NAME);
        service.setAddress(SERVICE_HOST);
        service.setPort(SERVICE_PORT);
        client.agentServiceRegister(service);

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.setServices(Collections.singletonList(SERVICE_NAME));
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        expectedProperties.setProperty(SERVICE_NAME + ".uri",
                String.format("%s://%s:%s", "http", SERVICE_HOST, SERVICE_PORT));
        expectedProperties.setProperty(SERVICE_NAME + ".scheme", "http");
        expectedProperties.setProperty(SERVICE_NAME + ".host", SERVICE_HOST);
        expectedProperties.setProperty(SERVICE_NAME + ".port", String.valueOf(SERVICE_PORT));
        assertEquals(expectedProperties, projectStub.getProperties());
    }
}