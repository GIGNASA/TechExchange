package com.example.techexchange.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MarketplaceStatsResponse {
    long totalDevices;
    long myDevices;
    long availableDevices;
    long activeRequests;
    long smartphones;
    long laptops;
    long gaming;
}
