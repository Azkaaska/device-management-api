package com.iot.deviceapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Input model for creating or updating a device")
public class DeviceInput {

    @JsonProperty("device_name")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Sensor Suhu Ruang Server")
    private String deviceName;

    @JsonProperty("device_type")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Thermometer")
    private String deviceType;

    @Schema(defaultValue = "ACTIVE", example = "ACTIVE")
    private String status = "ACTIVE";

    @JsonProperty("firmware_version")
    @Schema(example = "v2.1.0")
    private String firmwareVersion;

    @JsonProperty("device_metadata")
    @Schema(example = "{\"floor\": 3, \"room\": \"301\"}")
    private Map<String, Object> deviceMetadata;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Map<String, Object> getDeviceMetadata() {
        return deviceMetadata;
    }

    public void setDeviceMetadata(Map<String, Object> deviceMetadata) {
        this.deviceMetadata = deviceMetadata;
    }
}
