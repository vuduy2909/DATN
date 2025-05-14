package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.EffectivenessUpdateDTO;
import com.capstone.wellnessnavigatorgym.dto.tree.RecommendationDTO;
import com.capstone.wellnessnavigatorgym.dto.tree.TreeNode;
import com.capstone.wellnessnavigatorgym.dto.tree.UserDataDTO;
import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.service.*;
import com.capstone.wellnessnavigatorgym.utils.BuildDecisionTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/track-data-ai")
@CrossOrigin(origins = "http://localhost:3000")
public class TrackDataAiController {

    @Autowired
    private ITrackDataAiService trackDataAiService;

    @Autowired
    private BuildDecisionTree buildDecisionTree;

    @Autowired
    private ICustomerCourseService customerCourseService;

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private IUserDataAiService userDataAiService;

    @Autowired
    private ICourseService courseService;

    @GetMapping("/{id}")
    public ResponseEntity<TrackDataAi> getTrackDataAiById(@PathVariable Integer id) {
        return new ResponseEntity<>(trackDataAiService.findTrackDataAiById(id), HttpStatus.OK);
    }

    @PostMapping("/recommend-course")
    public ResponseEntity<RecommendationDTO> recommendCourse(@RequestBody UserDataDTO userDataDTO) {

        // Lấy danh sách thuộc tính và kiểm tra chúng
        List<String> attributeNames = getAttributeNames();
        if (attributeNames == null || attributeNames.isEmpty()) {
            return ResponseEntity.badRequest().body(new RecommendationDTO(null, "Attribute names are missing or invalid"));
        }
        // Lấy dữ liệu và xây dựng cây quyết định
        List<TrackDataAi> trackDataAis = trackDataAiService.getAllTrackDataAi();
        if (trackDataAis.isEmpty()) {
            return ResponseEntity.badRequest().body(new RecommendationDTO(null, "No matching track data found with the given filters"));
        }


        // Tải cây quyết định
        List<Course> courses = courseService.findAll();
        buildDecisionTree = new BuildDecisionTree(trackDataAis);
        buildDecisionTree.setCourses(courses);
        TreeNode decisionTree = buildDecisionTree.buildDecisionTree();  // Thêm dòng này để tạo cây

        // Xuất cây ra file XML

//        buildDecisionTree.exportTreeToXml(decisionTree, "D:\\DuyTan_University\\Project_Capstone_1\\CAPSTONE1-KyoGYM\\C1SE.32-Capstone1-BE\\wellness-navigator-gym\\src\\main\\resources\\static\\DataTraining1.xml");
//        return null;
        buildDecisionTree.importTreeFromXml("D:\\DuyTan_University\\Project_Capstone_1\\CAPSTONE1-KyoGYM\\C1SE.32-Capstone1-BE\\wellness-navigator-gym\\src\\main\\resources\\static\\DataTraining1.xml");

        //   Tạo map dữ liệu người dùng từ TrackDataAi
        Map<String, Object> userData = extractUserDataFromTrackDataAi(userDataDTO);

        //  Duyệt qua cây quyết định và tìm đề xuất
        Course recommendations = buildDecisionTree.traverseDecisionTree(decisionTree, userData);

        Customer customer = customerService.findById(userDataDTO.getCustomerId());

        UserDataAi userDataAi = new UserDataAi();
        userDataAi.setActivityLevel(userDataDTO.getActivity_level());
        userDataAi.setAge(userDataDTO.getAge());
        userDataAi.setGender(userDataDTO.getGender());
        userDataAi.setBmi(userDataDTO.getBmi());
        userDataAi.setTrainingGoals(userDataDTO.getTraining_goals());
        userDataAi.setTrainingHistory(userDataDTO.getTraining_history());
        userDataAi.setEffective(true);
        userDataAi.setCustomer(customer);

        userDataAiService.saveUserDataAi(userDataAi);

        customerCourseService.saveRecommendedCourses(recommendations, userDataDTO.getCustomerId());

        return ResponseEntity.ok(new RecommendationDTO(recommendations, "Course recommendations generated successfully"));
    }

    private Map<String, Object> extractUserDataFromTrackDataAi(UserDataDTO userDataDTO) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("activity_level", userDataDTO.getActivity_level());
        userData.put("age", userDataDTO.getAge());
        userData.put("gender", userDataDTO.getGender());
        userData.put("bmi", userDataDTO.getBmi());
        userData.put("training_goals", userDataDTO.getTraining_goals());
        userData.put("training_history", userDataDTO.getTraining_history());
        return userData;
    }


    private List<String> getAttributeNames() {
        return Arrays.asList("activity_level", "age", "gender", "bmi", "training_goals", "training_history");
    }

    @PutMapping("/update-effectiveness/{userDataAiId}")
    public ResponseEntity<?> updateUserDataAiEffectiveness(@PathVariable Integer userDataAiId, @RequestBody EffectivenessUpdateDTO dto) {
        UserDataAi userDataAi = userDataAiService.findUserDataAiById(userDataAiId);
        userDataAi.setEffective(dto.getEffective());
        userDataAiService.saveUserDataAi(userDataAi);
        return ResponseEntity.ok("Effectiveness updated successfully for UserDataAi ID " + userDataAiId);
    }
}