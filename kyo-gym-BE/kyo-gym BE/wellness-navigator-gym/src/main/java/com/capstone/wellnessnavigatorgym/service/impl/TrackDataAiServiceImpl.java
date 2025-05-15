package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.dto.tree.UserDataDTO;
import com.capstone.wellnessnavigatorgym.entity.TrackDataAi;
import com.capstone.wellnessnavigatorgym.error.NotFoundById;
import com.capstone.wellnessnavigatorgym.repository.ITrackDataAiRepository;
import com.capstone.wellnessnavigatorgym.service.ITrackDataAiService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrackDataAiServiceImpl implements ITrackDataAiService {

    @Autowired
    private ITrackDataAiRepository trackDataAiRepository;

    public List<TrackDataAi> getAllTrackDataAi() {
        return trackDataAiRepository.findAll();
    }

//    @Override
//    public List<TrackDataAi> getFilteredTrackDataAi(UserDataDTO userDataDTO) {
//        return trackDataAiRepository.findCustomByUserData(userDataDTO);
//    }

    @SneakyThrows
    @Override
    public TrackDataAi findTrackDataAiById(Integer id) {
        Optional<TrackDataAi> trackDataAi = trackDataAiRepository.findById(id);
        if (trackDataAi.isPresent()) {
            return trackDataAi.get();
        }
        throw new NotFoundById("Could not find any AI tracking data with code: " + id);
    }

    @Override
    public List<TrackDataAi> getFilteredTrackDataAi(UserDataDTO userDataDTO) {
        return trackDataAiRepository.findCustomByUserData(userDataDTO);
    }

}
