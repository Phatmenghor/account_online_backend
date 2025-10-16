package com.internal.feature.nid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.internal.feature.nid.dto.request.EkycFaceRequest;
import com.internal.feature.nid.dto.request.EkycRequest;
import com.internal.feature.nid.dto.request.ValidateNidRequest;

public interface NidService {
    JsonNode validateNid(ValidateNidRequest request);
    JsonNode validateNidFace(EkycRequest request);
    JsonNode extractNid(EkycFaceRequest request);
}
