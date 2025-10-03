package com.Geo_Crypt.EPM.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class EncryptionResponse {
    private Long paperId;
    private String minioObjectName;
    private String ivHex;
    private Map<Integer,Long> vertexShareMapping;
}
