package com.capstone.wellnessnavigatorgym.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageAnalysisResponseDTO {
    private String bodyType;
    private String analysis;
    private String recommendations;
}
