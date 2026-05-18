package com.java.vls.employee.portal.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tesla")
public class TeslaProperties {

    private String fleetApiBase;
    private String proxyBase;
    private String authTokenUrl;
    private String vehicleIdOrVin;
    private Long defaultTokenId = 1L;
    private long tokenRefreshSkewMinutes = 2L;

    public String getFleetApiBase() {
        return fleetApiBase;
    }

    public void setFleetApiBase(String fleetApiBase) {
        this.fleetApiBase = trimTrailingSlash(fleetApiBase);
    }

    public String getProxyBase() {
        return proxyBase;
    }

    public void setProxyBase(String proxyBase) {
        this.proxyBase = trimTrailingSlash(proxyBase);
    }

    public String getAuthTokenUrl() {
        return authTokenUrl;
    }

    public void setAuthTokenUrl(String authTokenUrl) {
        this.authTokenUrl = authTokenUrl;
    }

    public String getVehicleIdOrVin() {
        return vehicleIdOrVin;
    }

    public void setVehicleIdOrVin(String vehicleIdOrVin) {
        this.vehicleIdOrVin = vehicleIdOrVin;
    }

    public Long getDefaultTokenId() {
        return defaultTokenId;
    }

    public void setDefaultTokenId(Long defaultTokenId) {
        this.defaultTokenId = defaultTokenId;
    }

    public long getTokenRefreshSkewMinutes() {
        return tokenRefreshSkewMinutes;
    }

    public void setTokenRefreshSkewMinutes(long tokenRefreshSkewMinutes) {
        this.tokenRefreshSkewMinutes = tokenRefreshSkewMinutes;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
