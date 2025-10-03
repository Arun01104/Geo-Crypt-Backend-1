package com.Geo_Crypt.EPM.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VertexDto {
    public Integer idx;
    public double latitude;
    public double longitude;
}
