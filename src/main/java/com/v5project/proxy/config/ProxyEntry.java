package com.v5project.proxy.config;

import java.util.List;

/**
 * @author beou on 10/21/18 08:25
 */
public class ProxyEntry {
    private int port;
    private int port1;
    private boolean duplex;
    private boolean enabled;
    private List<RemoteEndpoint> remoteList;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort1() {
        return port1;
    }

    public void setPort1(int port1) {
        this.port1 = port1;
    }

    public boolean isDuplex() {
        return duplex;
    }

    public void setDuplex(boolean duplex) {
        this.duplex = duplex;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<RemoteEndpoint> getRemoteList() {
        return remoteList;
    }

    public void setRemoteList(List<RemoteEndpoint> remoteList) {
        this.remoteList = remoteList;
    }
}
