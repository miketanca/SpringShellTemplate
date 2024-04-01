package com.bns.clouddb.minetworkconnector.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResult {
    private String latency;
    private String fqdnName;
    private String timestamp;
}
