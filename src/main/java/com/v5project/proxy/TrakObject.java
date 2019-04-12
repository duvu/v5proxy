package com.v5project.proxy;

/**
 * @author beou on 4/12/19 12:59
 */
public class TrakObject {
    private String host;
    private int port;
    private Object data;

    public TrakObject(String host, int port, Object data) {
        this.host = host;
        this.port = port;
        this.data = data;
    }

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
