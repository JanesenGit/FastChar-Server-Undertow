package com.fastchar.server.undertow;

import com.fastchar.core.FastChar;
import com.fastchar.enums.FastServletType;
import com.fastchar.server.SameSite;
import com.fastchar.server.StaticResourceJars;
import com.fastchar.servlet.FastJakartaServletContainerInitializer;
import com.fastchar.servlet.FastJavaxServletContainerInitializer;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastMD5Utils;
import com.fastchar.utils.FastStringUtils;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import org.xnio.Option;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * undertow  <a href="https://undertow.io/">官网配置</a>
 */
public class FastUndertowConfig {
    private int port = 8080;

    private String deploymentName = "FastChar-Server-Undertow";

    private String host = "0.0.0.0";

    private String contextPath = "/";

    private String docBase;

    private String sessionDir;

    private boolean eagerFilterInit = true;

    private boolean preservePathOnForward;

    private Integer bufferSize;

    private Integer ioThreads;

    private Integer workerThreads;

    private Boolean directBuffers;

    private boolean http2;

    private SameSite cookieSameSite;


    private final List<URL> resources = new ArrayList<>();

    private final Set<String> webListenerClassNames = new HashSet<>();


    private final List<HttpHandler> handlers = new ArrayList<>();

    private final Undertow.Builder builder = Undertow.builder();


    public int getPort() {
        return port;
    }

    public FastUndertowConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public String getHost() {
        return host;
    }

    public FastUndertowConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public String getContextPath() {
        return contextPath;
    }

    public FastUndertowConfig setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public String getDocBase() {
        return docBase;
    }

    public FastUndertowConfig setDocBase(String docBase) {
        this.docBase = docBase;
        return this;
    }


    public FastUndertowConfig addResources(URL path) {
        this.resources.add(path);
        return this;
    }


    public FastUndertowConfig addResources(File path) {
        try {
            this.resources.add(path.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public FastUndertowConfig addWebListeners(String... webListenerClassNames) {
        this.webListenerClassNames.addAll(Arrays.asList(webListenerClassNames));
        return this;
    }


    public FastUndertowConfig addHandlers(HttpHandler... handlers) {
        this.handlers.addAll(Arrays.asList(handlers));
        return this;
    }


    public boolean isEagerFilterInit() {
        return eagerFilterInit;
    }

    public FastUndertowConfig setEagerFilterInit(boolean eagerFilterInit) {
        this.eagerFilterInit = eagerFilterInit;
        return this;
    }

    public boolean isPreservePathOnForward() {
        return preservePathOnForward;
    }

    public FastUndertowConfig setPreservePathOnForward(boolean preservePathOnForward) {
        this.preservePathOnForward = preservePathOnForward;
        return this;
    }

    public String getSessionDir() {
        return sessionDir;
    }

    public FastUndertowConfig setSessionDir(String sessionDir) {
        this.sessionDir = sessionDir;
        return this;
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public FastUndertowConfig setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public Integer getIoThreads() {
        return ioThreads;
    }

    public FastUndertowConfig setIoThreads(Integer ioThreads) {
        this.ioThreads = ioThreads;
        return this;
    }

    public Integer getWorkerThreads() {
        return workerThreads;
    }

    public FastUndertowConfig setWorkerThreads(Integer workerThreads) {
        this.workerThreads = workerThreads;
        return this;
    }

    public Boolean getDirectBuffers() {
        return directBuffers;
    }

    public FastUndertowConfig setDirectBuffers(Boolean directBuffers) {
        this.directBuffers = directBuffers;
        return this;
    }


    public <T> FastUndertowConfig setServerOption(Option<T> option, T value) {
        this.builder.setServerOption(option, value);
        return this;
    }

    public <T> FastUndertowConfig setSocketOption(Option<T> option, T value) {
        this.builder.setSocketOption(option, value);
        return this;
    }

    public <T> FastUndertowConfig setWorkerOption(Option<T> option, T value) {
        this.builder.setWorkerOption(option, value);
        return this;
    }


    public boolean isHttp2() {
        return http2;
    }

    public FastUndertowConfig setHttp2(boolean http2) {
        this.http2 = http2;
        return this;
    }

    public SameSite getCookieSameSite() {
        return cookieSameSite;
    }

    public FastUndertowConfig setCookieSameSite(SameSite cookieSameSite) {
        this.cookieSameSite = cookieSameSite;
        return this;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public FastUndertowConfig setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
        return this;
    }

    public List<HttpHandler> getHandlers() {
        return handlers;
    }


    public void configDefaultValue(DeploymentInfo deployment) {
        if (FastStringUtils.isEmpty(this.docBase)) {
            this.docBase = FastChar.getPath().getWebRootPath();
        }

        @SuppressWarnings("unchecked")
        Class<ServletContainerInitializerInfo> servletContainerInitializerInfo = (Class<ServletContainerInitializerInfo>) FastClassUtils.getClass("io.undertow.servlet.api.ServletContainerInitializerInfo");

        @SuppressWarnings("unchecked")
        Class<DeploymentInfo> deploymentClass = (Class<DeploymentInfo>) FastClassUtils.getClass("io.undertow.servlet.api.DeploymentInfo");

        List<Method> setDefaultMultipartConfig = FastClassUtils.getDeclaredMethod(deploymentClass, "setDefaultMultipartConfig");

        Class<?> jakarta = FastClassUtils.getClass(FastServletType.Jakarta.getTargetClass(), false);
        if (jakarta != null) {
            ServletContainerInitializerInfo newInstance = FastClassUtils.newInstance(servletContainerInitializerInfo, FastJakartaServletContainerInitializer.class, Collections.emptySet());
            deployment.addServletContainerInitializer(newInstance);

            Class<?> multipartConfigElement = FastClassUtils.getClass("jakarta.servlet.MultipartConfigElement");
            Object multipartConfigElementObj = FastClassUtils.newInstance(multipartConfigElement,
                    null,
                    (long) FastChar.getConstant().getAttachMaxPostSize(),
                    (long) FastChar.getConstant().getAttachMaxPostSize(), 0);
            try {
                setDefaultMultipartConfig.get(0).invoke(deployment, multipartConfigElementObj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            ServletContainerInitializerInfo newInstance = FastClassUtils.newInstance(servletContainerInitializerInfo, FastJavaxServletContainerInitializer.class, Collections.emptySet());
            deployment.addServletContainerInitializer(newInstance);
            Class<?> multipartConfigElement = FastClassUtils.getClass("javax.servlet.MultipartConfigElement");
            Object multipartConfigElementObj = FastClassUtils.newInstance(multipartConfigElement, null,
                    (long) FastChar.getConstant().getAttachMaxPostSize(),
                    (long) FastChar.getConstant().getAttachMaxPostSize(),
                    0);
            try {
                setDefaultMultipartConfig.get(0).invoke(deployment, multipartConfigElementObj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void configureResource(DeploymentInfo deployment) {

        File docBase = new File(this.docBase);
        List<URL> metaInfResourceUrls = new StaticResourceJars().getUrls();
        List<URL> resourceJarUrls = new ArrayList<>();
        List<ResourceManager> managers = new ArrayList<>();
        ResourceManager rootManager = (docBase.isDirectory() ? new FileResourceManager(docBase, 0)
                : new JarResourceManager(docBase));
        managers.add(rootManager);
        for (URL url : metaInfResourceUrls) {
            if ("file".equals(url.getProtocol())) {
                try {
                    File file = new File(url.toURI());
                    if (file.isFile()) {
                        for (String internalPath : StaticResourceJars.INTERNAL_PATHS) {
                            resourceJarUrls.add(new URL("jar:" + url + "!/" + FastStringUtils.stripStart(internalPath, "/")));
                        }
                    } else {
                        for (String internalPath : StaticResourceJars.INTERNAL_PATHS) {
                            File base = new File(file, internalPath);
                            if (base.exists()) {
                                managers.add(new FileResourceManager(base, 0));
                            }
                        }
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                resourceJarUrls.add(url);
            }
        }
        resourceJarUrls.addAll(this.resources);
        managers.add(new ResourcesResourceManager(resourceJarUrls));


        CompositeResourceManager resourceManager = new CompositeResourceManager(managers.toArray(new ResourceManager[0]));
        deployment.setResourceManager(resourceManager);
    }


    @SuppressWarnings("unchecked")
    public void configureWebListeners(DeploymentInfo deployment) {
        for (String className : this.webListenerClassNames) {
            Class<? extends EventListener> aClass = (Class<? extends EventListener>) FastClassUtils.getClass(className);
            deployment.addListener(new ListenerInfo(aClass));
        }
    }

    public void configureSession(DeploymentInfo deployment) {
        String realSessionDir = this.sessionDir;
        if (FastStringUtils.isEmpty(this.sessionDir)) {
            realSessionDir = Paths.get(this.docBase, "/.fastchar").normalize().toAbsolutePath().toString();
        }
        deployment.setSessionPersistenceManager(new FileSessionPersistence(new File(realSessionDir)));
        deployment.setSessionIdGenerator(() -> "FastChar" + FastMD5Utils.MD5(FastStringUtils.buildUUID()));
    }


    public Undertow.Builder getBuilder() {
        if (this.bufferSize != null) {
            this.builder.setBufferSize(this.bufferSize);
        }
        if (this.ioThreads != null) {
            this.builder.setIoThreads(this.ioThreads);
        }
        if (this.directBuffers != null) {
            this.builder.setDirectBuffers(this.directBuffers);
        }
        if (this.workerThreads != null) {
            this.builder.setWorkerThreads(this.workerThreads);
        }
        if (this.http2) {
            this.builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true);
        }
        this.builder.addHttpListener(this.port, this.host);
        return this.builder;
    }


}
