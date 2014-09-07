package com.bitcoin.util;

public class Credential {
    private String login;

    private String password;

    private String host;

    private String protocol;

    private String path;

    private Integer port;

    public Credential(String login, String password, String host, String protocol, String path, Integer port) {
        this.login = login;
        this.password = password;
        this.host = host;
        this.protocol = protocol;
        this.path = path;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Credential that = (Credential) o;

        if (!host.equals(that.host)) return false;
        if (!login.equals(that.login)) return false;
        if (!password.equals(that.password)) return false;
        if (!path.equals(that.path)) return false;
        if (!port.equals(that.port)) return false;
        if (!protocol.equals(that.protocol)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = login.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + protocol.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + port.hashCode();
        return result;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }

    public String getHost() {
        return this.host;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getPath() {
        return path;
    }

    public int getPort() {
        return this.port;
    }

    @Override
    public String toString() {
        return String.format("%s://%s:%s@%s:%d", protocol, login, password, host, port);
    }
}
