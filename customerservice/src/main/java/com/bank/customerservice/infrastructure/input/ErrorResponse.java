package com.bank.customerservice.infrastructure.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
    @JsonProperty("type")
    String type,
    
    @JsonProperty("title")
    String title,
    
    @JsonProperty("status")
    Integer status,
    
    @JsonProperty("detail")
    String detail,
    
    @JsonProperty("instance")
    String instance,
    
    @JsonProperty("timestamp")
    String timestamp
) {}
