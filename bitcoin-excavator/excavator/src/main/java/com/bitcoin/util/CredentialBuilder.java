package com.bitcoin.util;

public class CredentialBuilder {
    private String login;

    private String password;

    private String host;

    private String protocol;

    private String path;

    private int port;

    public CredentialBuilder setLogin(String login) {
        this.login = login;
        return this;
    }

    public CredentialBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public CredentialBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public CredentialBuilder setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public CredentialBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public CredentialBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public Credential build() {
        return new Credential(login, password, host, protocol, path, port);
    }
}
