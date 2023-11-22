package com.example.foxticket.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SuccessMessage {
    @JsonProperty("success_message")
    private String successMessage;

    public SuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }
}
