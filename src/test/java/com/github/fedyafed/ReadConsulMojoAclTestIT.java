/*
 * Copyright (c) 2019 fedyafed.
 */

package com.github.fedyafed;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.acl.model.Acl;
import com.ecwid.consul.v1.acl.model.NewAcl;
import com.ecwid.consul.v1.agent.model.NewService;
import com.pszymczyk.consul.ConsulStarterBuilder;
import com.pszymczyk.consul.junit.ConsulResource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ReadConsulMojoAclTestIT {
    private static final String MASTER_TOKEN = "00000000-0000-0000-0000-000000000000";

    @ClassRule
    public static final ConsulResource consul = new ConsulResource(
            ConsulStarterBuilder.consulStarter()
                    .withConsulVersion("1.1.0")
                    .withCustomConfig("{\n"
                            + "  \"datacenter\": \"dc1\",\n"
                            + "  \"acl_datacenter\": \"dc1\",\n"
                            + "  \"acl_master_token\": \"" + MASTER_TOKEN + "\",\n"
                            + "  \"acl_token\": \"" + MASTER_TOKEN + "\",\n"
                            + "  \"acl_default_policy\": \"deny\",\n"
                            + "  \"acl_down_policy\": \"extend-cache\"\n"
                            + "}")
                    .build());

    private static final String PREFIX = "config";
    private static final String KEY = "test";
    private static final String FORBIDDEN_PREFIX = "secret_config";
    private static final String VALUE = "Test Value.";
    private static final String SECRET_VALUE = "Test Secret Value.";
    private static final String SERVICE_NAME = "test-service";
    private static final String SERVICE_SCHEME = "ftp";
    private static final String SERVICE_HOST = "test-host";
    private static final int SERVICE_PORT = 21;
    private static final String FORBIDDEN_SERVICE_NAME = "secret-service";
    private static final String FORBIDDEN_SERVICE_SCHEME = "https";
    private static final String FORBIDDEN_SERVICE_HOST = "secret-host";
    private static final int FORBIDDEN_SERVICE_PORT = 443;

    private MavenProject projectStub;
    private String clientReadToken;
    private int httpPort;
    private ConsulClient client;

    /**
     * Before each test execution.
     */
    @Before
    public void setUp() {
        projectStub = new MavenProject();
        consul.reset();
        httpPort = consul.getHttpPort();
        client = new ConsulClient("localhost", httpPort);

        List<String> defaultTokenIds = Arrays.asList(MASTER_TOKEN, "anonymous");
        client.getAclList(MASTER_TOKEN).getValue().stream()
                .map(Acl::getId)
                .filter(aclId -> !defaultTokenIds.contains(aclId))
                .forEach(aclId -> client.aclDestroy(aclId, MASTER_TOKEN));

        ConsulClient client = new ConsulClient("localhost", httpPort);
        NewAcl clientReadAcl = new NewAcl();
        clientReadAcl.setName("clientRead");
        clientReadAcl.setRules("key \"" + PREFIX + "\" {\n"
                + "  policy = \"read\"\n"
                + "}\n"
                + "service \"" + SERVICE_NAME + "\" {\n"
                + "  policy = \"read\"\n"
                + "}\n"
                + "node \"\" {\n"
                + "  policy = \"read\"\n"
                + "}"
        );
        clientReadToken = client.aclCreate(clientReadAcl, MASTER_TOKEN).getValue();
    }

    /**
     * Test default mojo execution from KV with ACL.
     */
    @Test
    public void testDefaultKVExecute() throws MojoExecutionException {
        client.setKVValue(PREFIX + "/" + KEY, VALUE, MASTER_TOKEN, null);
        client.setKVValue(FORBIDDEN_PREFIX + "/" + KEY, SECRET_VALUE, MASTER_TOKEN, null);

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.setToken(clientReadToken);
        readConsulMojo.setPrefixes(singletonList(PREFIX));
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        expectedProperties.put(KEY, VALUE);
        assertEquals(expectedProperties, projectStub.getProperties());
    }

    /**
     * Test forbidden mojo execution from KV with ACL.
     */
    @Test
    public void testForbiddenKVExecute() throws MojoExecutionException {
        client.setKVValue(PREFIX + "/" + KEY, VALUE, MASTER_TOKEN, null);
        client.setKVValue(FORBIDDEN_PREFIX + "/" + KEY, SECRET_VALUE, MASTER_TOKEN, null);

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.setToken(clientReadToken);
        readConsulMojo.setPrefixes(Arrays.asList(PREFIX, FORBIDDEN_PREFIX));
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        expectedProperties.put(KEY, VALUE);
        assertEquals(expectedProperties, projectStub.getProperties());
    }

    /**
     * Test mojo execution from service with ACL.
     */
    @Test
    public void testServiceExecute() throws MojoExecutionException {
        prepareTestServices();

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.setToken(clientReadToken);
        readConsulMojo.setServices(singletonList(SERVICE_NAME));
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
     * Test forbidden mojo execution from service with ACL.
     */
    @Test
    public void testForbiddenServiceExecute() throws MojoExecutionException {
        prepareTestServices();

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.setToken(clientReadToken);
        readConsulMojo.setServices(Arrays.asList(SERVICE_NAME, FORBIDDEN_SERVICE_NAME));
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        expectedProperties.setProperty(SERVICE_NAME + ".uri",
                String.format("%s://%s:%s", SERVICE_SCHEME, SERVICE_HOST, SERVICE_PORT));
        expectedProperties.setProperty(SERVICE_NAME + ".scheme", SERVICE_SCHEME);
        expectedProperties.setProperty(SERVICE_NAME + ".host", SERVICE_HOST);
        expectedProperties.setProperty(SERVICE_NAME + ".port", String.valueOf(SERVICE_PORT));
        assertEquals(expectedProperties, projectStub.getProperties());
    }

    private void prepareTestServices() {
        NewService service = new NewService();
        service.setName(SERVICE_NAME);
        service.setAddress(SERVICE_HOST);
        service.setPort(SERVICE_PORT);
        service.setMeta(Collections.singletonMap("scheme", SERVICE_SCHEME));
        client.agentServiceRegister(service, MASTER_TOKEN);

        service = new NewService();
        service.setName(FORBIDDEN_SERVICE_NAME);
        service.setAddress(FORBIDDEN_SERVICE_HOST);
        service.setPort(FORBIDDEN_SERVICE_PORT);
        service.setMeta(Collections.singletonMap("scheme", FORBIDDEN_SERVICE_SCHEME));
        client.agentServiceRegister(service, MASTER_TOKEN);
    }
}