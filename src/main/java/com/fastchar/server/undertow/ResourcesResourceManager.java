package com.fastchar.server.undertow;

import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.URLResource;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;

public class ResourcesResourceManager implements ResourceManager {
	private static final Pattern ENCODED_SLASH = Pattern.compile("%2F", Pattern.LITERAL);
    private final List<URL> resources;

	public ResourcesResourceManager(List<URL> resources) {
        this.resources = resources;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public Resource getResource(String path) {
        for (URL url : this.resources) {
            URLResource resource = getResource(url, path);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

    @Override
    public boolean isResourceChangeListenerSupported() {
        return false;
    }

    @Override
    public void registerResourceChangeListener(ResourceChangeListener listener) {
    }

    @Override
    public void removeResourceChangeListener(ResourceChangeListener listener) {

    }

    private URLResource getResource(URL resourceBaseUrl, String path) {
        try {
            String urlPath = URLEncoder.encode(ENCODED_SLASH.matcher(path).replaceAll("/"), "UTF-8");
            URL resourceUrl = new URL(resourceBaseUrl  + urlPath);
            URLResource resource = new URLResource(resourceUrl, path);
            if (resource.getContentLength() < 0) {
                return null;
            }
            return resource;
        } catch (Exception ex) {
            return null;
        }
    }

}