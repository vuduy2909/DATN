package com.capstone.wellnessnavigatorgym.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImageAnalysisResponseDTO {
    private String bodyType;
    private String analysis;
    private String recommendations;
    private Integer userDataId;
    
    public ImageAnalysisResponseDTO(String bodyType, String analysis, String recommendations) {
        this.bodyType = bodyType;
        this.analysis = analysis;
        this.recommendations = recommendations;
    }
    
    public ImageAnalysisResponseDTO(String bodyType, String analysis, String recommendations, Integer userDataId) {
        this.bodyType = bodyType;
        this.analysis = analysis;
        this.recommendations = recommendations;
        this.userDataId = userDataId;
    }
}
