/*
 * Copyright (c) 2018 fedyafed.
 */

package com.github.fedyafed;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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
    private List<String> prefixes = Collections.emptyList();

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

    /**
     * Active services.
     */
    @Parameter(defaultValue = "${consul.services}")
    private List<String> services = Collections.emptyList();

    private ConsulClient client;
    private HealthServicesRequest servicesRequest;

    /**
     * Mojo execution.
     *
     * @throws MojoExecutionException - default Mojo exception.
     */
    public void execute() throws MojoExecutionException {
        getLog().debug(String.format("Connecting to Consul at: http://%s:%s", host, port));
        client = new ConsulClient(host, port);
        servicesRequest = HealthServicesRequest.newBuilder().setToken(token).build();

        prefixes.forEach(this::setPropertiesFromKV);
        services.forEach(this::setPropertiesFromService);
    }

    private void setPropertiesFromService(String serviceName) {
        Log log = getLog();
        List<HealthService> services = client.getHealthServices(serviceName, servicesRequest)
                .getValue();
        if (services != null && !services.isEmpty()) {
            HealthService service = services.get(0);
            setProjectPropertyFromService(service);
        } else {
            log.warn("Service '" + serviceName + "' is not found.");
        }
    }

    private void setPropertiesFromKV(String prefix) {
        Log log = getLog();
        if (!prefix.endsWith("/")) {
            prefix += '/';
        }

        List<GetValue> values = client.getKVValues(prefix, token).getValue();
        if (values != null) {
            log.info("Found properties: " + values.size() + " for prefix: '" + prefix + "'");
            for (GetValue value : values) {
                setProjectPropertyFromKV(prefix, value);
            }
        } else {
            try {
                String singleKey = prefix.substring(0, prefix.length() - 1);
                GetValue value = client.getKVValue(singleKey, token).getValue();
                if (value == null) {
                    log.warn("Key prefix '" + prefix + "' is not found.");
                } else {
                    log.info("Path '" + singleKey + "' is a single key.");
                    setProjectPropertyFromKV("", value);
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

    private void setProjectPropertyFromKV(String prefix, GetValue value) {
        Log log = getLog();
        Properties properties = project.getProperties();
        String key = getPropertyKey(value.getKey(), prefix);
        String decodedValue = value.getDecodedValue(UTF_8);
        log.debug("Key: '" + key + "',\t value: '" + decodedValue + "'");
        properties.setProperty(key, decodedValue);
    }

    private void setProjectPropertyFromService(HealthService healthService) {
        HealthService.Service service = healthService.getService();
        Log log = getLog();
        Properties properties = project.getProperties();
        String name = service.getService();
        String scheme = "http";
        String host = getServiceHost(healthService);
        Integer port = service.getPort();
        Map<String, String> metadata = getServiceMetadata(service);
        if (Boolean.parseBoolean(metadata.get("secure"))) {
            scheme = "https";
        }
        if (metadata.containsKey("scheme")) {
            scheme = metadata.get("scheme");
        }
        String uri;
        try {
            uri = new URI(scheme, null, host, port, null, null, null).toString();

            log.debug("Service: '" + name + "',\t URI: '" + uri + "'");

            properties.setProperty(name + ".uri", uri);
            properties.setProperty(name + ".scheme", scheme);
            properties.setProperty(name + ".host", host);
            properties.setProperty(name + ".port", String.valueOf(port));
        } catch (URISyntaxException e) {
            log.warn("Service '" + name + "' has invalid URI", e);
        }
    }

    private String getServiceHost(HealthService service) {
        String host = service.getService().getAddress();
        if (host != null && !host.isEmpty()) {
            return host;
        }
        return service.getNode().getAddress();
    }

    private Map<String, String> getServiceMetadata(HealthService.Service service) {
        Map<String, String> meta = service.getMeta();
        if (meta == null) {
            meta = new HashMap<>();
        }
        return meta;
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

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    void setProject(MavenProject project) {
        this.project = project;
    }
}
