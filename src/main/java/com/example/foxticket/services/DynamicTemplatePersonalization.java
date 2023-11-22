package com.example.foxticket.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sendgrid.helpers.mail.objects.Personalization;

import java.util.HashMap;
import java.util.Map;

public class DynamicTemplatePersonalization extends Personalization {
    @JsonProperty("dynamic_template_data")
    private Map<String, Object> dynamicTemplateData;

    public DynamicTemplatePersonalization() {
        this.dynamicTemplateData = new HashMap<String, Object>();
    }

    @JsonProperty("dynamic_template_data")
    public Map<String, Object> getDynamicTemplateData() {
        return dynamicTemplateData;
    }

    public void addDynamicTemplateData(String key, Object value) {
        dynamicTemplateData.put(key, value);
    }
}
