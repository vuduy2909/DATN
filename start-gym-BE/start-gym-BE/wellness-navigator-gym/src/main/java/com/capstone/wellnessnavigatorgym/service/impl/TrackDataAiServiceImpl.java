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
        // Use optimized query with JOIN FETCH to prevent N+1 queries
        return trackDataAiRepository.findAllWithCourse();
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
        List<TrackDataAi> result = trackDataAiRepository.findSimilarData(userDataDTO);
        if (result.isEmpty()) {
            // Fallback to a more general search if no specific matches found
            result = trackDataAiRepository.findByAgeAndGender(userDataDTO);
        }
        return result;
    }
    
    @Override
    public TrackDataAi saveTrackDataAi(TrackDataAi trackDataAi) {
        return trackDataAiRepository.save(trackDataAi);
    }
    
    @Override
    public Integer getNextTrackDataAiId() {
        List<TrackDataAi> allRecords = trackDataAiRepository.findAll();
        return allRecords.stream()
            .map(TrackDataAi::getTrackDataAiId)
            .filter(id -> id != null)
            .max(Integer::compareTo)
            .orElse(1000) + 1;
    }
}
