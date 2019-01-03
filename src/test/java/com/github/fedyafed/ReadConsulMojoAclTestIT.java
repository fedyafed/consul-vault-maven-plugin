/*
 * Copyright (c) 2019 fedyafed.
 */

package com.github.fedyafed;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.acl.model.Acl;
import com.ecwid.consul.v1.acl.model.NewAcl;
import com.pszymczyk.consul.ConsulStarterBuilder;
import com.pszymczyk.consul.junit.ConsulResource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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
                + "}"
        );
        clientReadToken = client.aclCreate(clientReadAcl, MASTER_TOKEN).getValue();
    }

    /**
     * Test default mojo execution with ACL.
     */
    @Test
    public void testDefaultExecute() throws MojoExecutionException {
        client.setKVValue(PREFIX + "/" + KEY, VALUE, MASTER_TOKEN, null);
        client.setKVValue(FORBIDDEN_PREFIX + "/" + KEY, SECRET_VALUE, MASTER_TOKEN, null);

        ReadConsulMojo readConsulMojo = new ReadConsulMojo();
        readConsulMojo.setPort(httpPort);
        readConsulMojo.setProject(projectStub);
        readConsulMojo.setToken(clientReadToken);
        readConsulMojo.execute();

        Properties expectedProperties = new Properties();
        expectedProperties.put(KEY, VALUE);
        assertEquals(expectedProperties, projectStub.getProperties());
    }

    /**
     * Test forbidden mojo execution with ACL.
     */
    @Test
    public void testForbiddenExecute() throws MojoExecutionException {
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
}