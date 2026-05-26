package com.java.vinod.tesla.dashboard.services.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleApiResponse {

    @JsonProperty("response")
    private List<Vehicle> response;

    public List<Vehicle> getResponse() { return response; }
    public void setResponse(List<Vehicle> response) { this.response = response; }
}
