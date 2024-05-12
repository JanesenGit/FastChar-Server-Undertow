package com.fastchar.server.undertow;

import com.fastchar.utils.FastStringUtils;
import io.undertow.UndertowMessages;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.URLResource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class JarResourceManager implements ResourceManager {

    private final String jarPath;

    JarResourceManager(File jarFile) {
        try {
            this.jarPath = jarFile.getAbsoluteFile().toURI().toURL().toString();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Resource getResource(String path) throws IOException {
        URL url = new URL("jar:" + this.jarPath + "!" + (path.startsWith("/") ? path : "/" + path));
        URLResource resource = new URLResource(url, path);
        if (FastStringUtils.isNotEmpty(path) && !"/".equals(path) && resource.getContentLength() < 0) {
            return null;
        }
        return resource;
    }

    @Override
    public boolean isResourceChangeListenerSupported() {
        return false;
    }

    @Override
    public void registerResourceChangeListener(ResourceChangeListener listener) {
        throw UndertowMessages.MESSAGES.resourceChangeListenerNotSupported();

    }

    @Override
    public void removeResourceChangeListener(ResourceChangeListener listener) {
        throw UndertowMessages.MESSAGES.resourceChangeListenerNotSupported();
    }

    @Override
    public void close() throws IOException {

    }

}