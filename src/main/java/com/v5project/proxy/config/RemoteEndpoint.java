package com.v5project.proxy.config;

/**
 * @author beou on 10/21/18 08:25
 */
public class RemoteEndpoint {
    private String host;
    private int port;
    private boolean transparent;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }
}