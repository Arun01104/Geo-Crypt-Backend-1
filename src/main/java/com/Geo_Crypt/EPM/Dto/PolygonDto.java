package com.Geo_Crypt.EPM.Dto;


import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolygonDto {
    private String name;
    private List<VertexDto> vertices;
}
