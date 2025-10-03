package com.Geo_Crypt.EPM.Dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class EncryptionRequest {
    private Long paperId;
    private Long polygonId;
    private List<Integer> vertexIndices;
    private MultipartFile file;
}
