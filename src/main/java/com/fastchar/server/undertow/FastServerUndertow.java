package com.fastchar.server.undertow;

import com.fastchar.core.FastChar;
import com.fastchar.server.ServerStartHandler;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletStackTraces;
import io.undertow.servlet.handlers.DefaultServlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FastServerUndertow {

    public static FastServerUndertow getInstance() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            Class<?> fromClass = Class.forName(stackTrace[2].getClassName());
            FastChar.getPath().setProjectJar(fromClass);
            return new FastServerUndertow().initServer();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private FastServerUndertow() {
    }

    private Undertow undertow;
    private DeploymentManager deploymentManager;

    private final ServerStartHandler starterHandler = new ServerStartHandler();


    private FastServerUndertow initServer() {
        starterHandler.setStartRunnable(() -> {
            if (undertow != null) {
                undertow.start();
            }
        }).setStopRunnable(this::stop);
        return this;
    }

    public synchronized void start(FastUndertowConfig undertowConfig) {
        try {
            if (undertow != null) {
                return;
            }
            FastChar.getConstant().setEmbedServer(true);

            DeploymentInfo deployment = Servlets.deployment();
            undertowConfig.configDefaultValue(deployment);

            deployment.setContextPath(undertowConfig.getContextPath());
            deployment.setClassLoader(FastServerUndertow.class.getClassLoader());
            deployment.setServletStackTraces(ServletStackTraces.NONE);
            deployment.setDefaultRequestEncoding("utf-8");
            deployment.setDefaultResponseEncoding("utf-8");
            deployment.setDefaultSessionTimeout(FastChar.getConstant().getSessionMaxInterval());
            deployment.setDefaultEncoding("utf-8");
            deployment.setDeploymentName(undertowConfig.getDeploymentName());
            deployment.setTempDir(new File(FastChar.getPath().getTempDir()));
            deployment.setEagerFilterInit(undertowConfig.isEagerFilterInit());
            deployment.setPreservePathOnForward(undertowConfig.isPreservePathOnForward());
            deployment.addServlet(Servlets.servlet("default", DefaultServlet.class));

            undertowConfig.configureResource(deployment);
            undertowConfig.configureWebListeners(deployment);
            undertowConfig.configureSession(deployment);

            deploymentManager = Servlets.newContainer().addDeployment(deployment);
            deploymentManager.deploy();

            SessionManager sessionManager = deploymentManager.getDeployment().getSessionManager();
            sessionManager.setDefaultSessionTimeout(FastChar.getConstant().getSessionMaxInterval());

            List<HttpHandler> handlers = new ArrayList<>();
            handlers.add(Handlers.proxyPeerAddress(deploymentManager.start()));
            handlers.add(new CookieSameSiteHandler(undertowConfig));
            handlers.addAll(undertowConfig.getHandlers());

            undertow = undertowConfig.getBuilder()
                    .setHandler(new HttpHandlerCollection(handlers)).build();

            this.starterHandler
                    .setPort(undertowConfig.getPort())
                    .setHost(undertowConfig.getHost())
                    .setContextPath(undertowConfig.getContextPath())
                    .start();
        } catch (Exception e) {
            stop();
            throw new RuntimeException(e);
        }
    }

    public synchronized void stop() {
        try {
            if (this.deploymentManager != null) {
                this.deploymentManager.stop();
                this.deploymentManager.undeploy();
                this.deploymentManager = null;
            }
            if (this.undertow != null) {
                this.undertow.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
