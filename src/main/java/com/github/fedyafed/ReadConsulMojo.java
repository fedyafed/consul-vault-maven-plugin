/*
 * Copyright (c) 2018 fedyafed.
 */

package com.github.fedyafed;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Goal read a value from Consul KV by given key.
 */
@Mojo(name = "read-consul", defaultPhase = LifecyclePhase.INITIALIZE)
public class ReadConsulMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * Consul KV key prefixes.
     */
    @Parameter(defaultValue = "${consul.prefixes}")
    private List<String> prefixes = Collections.singletonList("config");

    /**
     * Consul hostname.
     */
    @Parameter(defaultValue = "${consul.host}")
    private String host = "localhost";

    /**
     * Consul HTTP port.
     */
    @Parameter(defaultValue = "${consul.port}")
    private int port = 8500;

    /**
     * Consul ACL token.
     */
    @Parameter(defaultValue = "${consul.token}")
    private String token;
    private ConsulClient client;

    /**
     * Mojo execution.
     *
     * @throws MojoExecutionException - default Mojo exception.
     */
    public void execute() throws MojoExecutionException {
        client = new ConsulClient(host, port);

        for (String prefix : prefixes) {
            setProperties(prefix);
        }
    }

    private void setProperties(String prefix) {
        Log log = getLog();
        if (!prefix.endsWith("/")) {
            prefix += '/';
        }

        List<GetValue> values = client.getKVValues(prefix, token).getValue();
        if (values != null) {
            log.info("Found properties: " + values.size() + " for prefix: '" + prefix + "'");
            for (GetValue value : values) {
                setProjectProperty(prefix, value);
            }
        } else {
            try {
                String singleKey = prefix.substring(0, prefix.length() - 1);
                GetValue value = client.getKVValue(singleKey, token).getValue();
                if (value == null) {
                    log.warn("Key prefix '" + prefix + "' is not found.");
                } else {
                    log.info("Path '" + singleKey + "' is a single key.");
                    setProjectProperty("", value);
                }
            } catch (OperationException e) {
                if (e.getStatusCode() == 403) {
                    log.warn("Access to key prefix '" + prefix + "' is forbidden by Consul ACL.");
                } else {
                    throw e;
                }
            }
        }
    }

    private void setProjectProperty(String prefix, GetValue value) {
        Log log = getLog();
        Properties properties = project.getProperties();
        String key = getPropertyKey(value.getKey(), prefix);
        String decodedValue = value.getDecodedValue(UTF_8);
        log.debug("Key: '" + key + "',\t value: '" + decodedValue + "'");
        properties.setProperty(key, decodedValue);
    }

    static String getPropertyKey(String key, String prefix) {
        return key
                .substring(prefix.length())
                .replace('/', '.');
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes;
    }

    String getHost() {
        return host;
    }

    void setHost(String host) {
        this.host = host;
    }

    int getPort() {
        return port;
    }

    void setPort(int port) {
        this.port = port;
    }

    String getToken() {
        return token;
    }

    void setToken(String token) {
        this.token = token;
    }

    void setProject(MavenProject project) {
        this.project = project;
    }
}
