package com.branegy.tools.api;

import org.osgi.framework.Version;

import com.branegy.service.core.exception.ApiException;

@SuppressWarnings("serial")
public class ToolHistoryClassDeserializationException extends ApiException {
    private boolean bundleInstalled;
    private Version requiredVersion;

    public ToolHistoryClassDeserializationException(Throwable throwable) {
        super(throwable);
    }

    public boolean isBundleInstalled() {
        return bundleInstalled;
    }

    public void setBundleInstalled(boolean bundleInstalled) {
        this.bundleInstalled = bundleInstalled;
    }

    public Version getRequiredVersion() {
        return requiredVersion;
    }

    public void setRequiredVersion(Version requiredVersion) {
        this.requiredVersion = requiredVersion;
    }
}
