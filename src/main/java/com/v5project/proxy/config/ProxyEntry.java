package com.v5project.proxy.config;

import java.util.List;

/**
 * @author beou on 10/21/18 08:25
 */
public class ProxyEntry {
    private int port;
    private boolean duplex;
    private boolean enabled;
    private List<RemoteEndpoint> remoteList;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
