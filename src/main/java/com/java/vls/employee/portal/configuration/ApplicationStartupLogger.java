package com.java.vls.employee.portal.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationStartupLogger {

    private final TeslaProperties teslaProperties;
    private final String serverAddress;
    private final String serverPort;
    private final String datasourceUrl;
    private final String datasourceUsername;

    public ApplicationStartupLogger(
            TeslaProperties teslaProperties,
            @Value("${server.address}") String serverAddress,
            @Value("${server.port}") String serverPort,
            @Value("${spring.datasource.url}") String datasourceUrl,
            @Value("${spring.datasource.username}") String datasourceUsername) {
        this.teslaProperties = teslaProperties;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.datasourceUrl = datasourceUrl;
        this.datasourceUsername = datasourceUsername;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logRuntimeConfiguration() {
        log.info("Employee Portal ready on {}:{}", serverAddress, serverPort);
        log.info("Database configured: url={}, username={}", maskJdbcPassword(datasourceUrl), datasourceUsername);
        log.info(
                "Tesla configuration: fleetApiBase={}, proxyBase={}, authTokenUrl={}, vehicleOverrideConfigured={}, defaultTokenId={}, refreshSkewMinutes={}",
                teslaProperties.getFleetApiBase(),
                teslaProperties.getProxyBase(),
                teslaProperties.getAuthTokenUrl(),
                teslaProperties.getVehicleIdOrVin() != null && !teslaProperties.getVehicleIdOrVin().isBlank(),
                teslaProperties.getDefaultTokenId(),
                teslaProperties.getTokenRefreshSkewMinutes()
        );
    }

    private String maskJdbcPassword(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("(?i)(password=)[^;&]+", "$1****");
    }
}
