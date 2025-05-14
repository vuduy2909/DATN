package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.dto.tree.UserDataDTO;
import com.capstone.wellnessnavigatorgym.entity.TrackDataAi;

import java.util.List;

public interface ITrackDataAiService {
    List<TrackDataAi> getAllTrackDataAi();
    List<TrackDataAi> getFilteredTrackDataAi(UserDataDTO userDataDTO);
    TrackDataAi findTrackDataAiById(Integer id);
}
