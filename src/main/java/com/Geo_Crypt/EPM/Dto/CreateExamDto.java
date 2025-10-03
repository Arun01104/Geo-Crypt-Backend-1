package com.Geo_Crypt.EPM.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateExamDto {
    public String title;
    public String subject;
    public String polygonId;
    public String start;
    public String end;
    public Integer threshold;
}
