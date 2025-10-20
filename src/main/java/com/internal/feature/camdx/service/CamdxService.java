package com.internal.feature.camdx.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.internal.feature.camdx.dto.CamdxFaceRequest;
import com.internal.feature.camdx.dto.CamdxRequest;
import com.internal.feature.camdx.dto.CamdxValidateNidRequest;

public interface CamdxService {
    JsonNode validateNid(CamdxValidateNidRequest request);
    JsonNode validateNidFace(CamdxRequest request);
    JsonNode extractNid(CamdxFaceRequest request);
}
