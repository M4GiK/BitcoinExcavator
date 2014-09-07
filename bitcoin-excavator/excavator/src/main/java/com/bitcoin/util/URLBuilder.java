package com.bitcoin.util;

import java.net.MalformedURLException;
import java.net.URL;

public class URLBuilder {
    public URL createURL(Credential credential) throws MalformedURLException {
        return new URL(credential.getProtocol(), credential.getHost(), credential.getPort(), credential.getPath());
    }
}
