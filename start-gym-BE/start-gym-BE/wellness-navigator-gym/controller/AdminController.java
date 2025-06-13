package com.example.wellnessnavigatorgym.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EntityManager entityManager;

    @PostMapping("/courses/{courseId}/days")
    public ResponseEntity<?> addCourseDay(@PathVariable Integer courseId) {
        try {
            // Get the current highest day number for this course to determine next day number
            String getMaxDaySQL = "SELECT MAX(CAST(SUBSTRING(d.day_name, 5) AS UNSIGNED)) as max_day_num " +
                                  "FROM days d " +
                                  "JOIN course_days cd ON d.day_id = cd.day_id " +
                                  "WHERE cd.course_id = :courseId AND cd.status = true " +
                                  "AND d.day_name REGEXP '^Day [0-9]+$'";
            Query maxDayQuery = entityManager.createNativeQuery(getMaxDaySQL);
            maxDayQuery.setParameter("courseId", courseId);
            Number maxDayNum = (Number) maxDayQuery.getSingleResult();
            int nextDayNumber = (maxDayNum != null) ? maxDayNum.intValue() + 1 : 1;
            String dayName = "Day " + nextDayNumber;
            // Check if day already exists
            String checkDaySQL = "SELECT COUNT(*) FROM days WHERE day_name = :dayName";
            Query checkDayQuery = entityManager.createNativeQuery(checkDaySQL);
            checkDayQuery.setParameter("dayName", dayName);
            Number exists = (Number) checkDayQuery.getSingleResult();
            if (exists != null && exists.intValue() > 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Day already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }
            // First create a new day
            String insertDaySQL = "INSERT INTO days (day_name) VALUES (:dayName)";
            Query insertDayQuery = entityManager.createNativeQuery(insertDaySQL);
            insertDayQuery.setParameter("dayName", dayName);
            insertDayQuery.executeUpdate();
            // Get the newly created day ID
            String getLastIdSQL = "SELECT LAST_INSERT_ID()";
            Number dayId = (Number) entityManager.createNativeQuery(getLastIdSQL).getSingleResult();
            // Create course_days association
            String insertCourseDaySQL = "INSERT INTO course_days (course_id, day_id, status) VALUES (:courseId, :dayId, true)";
            Query insertCourseDayQuery = entityManager.createNativeQuery(insertCourseDaySQL);
            insertCourseDayQuery.setParameter("courseId", courseId);
            insertCourseDayQuery.setParameter("dayId", dayId);
            insertCourseDayQuery.executeUpdate();
            Map<String, Object> response = new HashMap<>();
            response.put("dayId", dayId);
            response.put("dayName", dayName);
            response.put("dayNumber", nextDayNumber);
            response.put("courseId", courseId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to add course day: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/courses/{courseId}/days/{dayId}/exercises")
    public ResponseEntity<?> addExerciseToDay(@PathVariable Integer courseId, @PathVariable Integer dayId, @RequestBody Map<String, Object> exerciseData) {
        try {
            Integer exerciseId = (Integer) exerciseData.get("exerciseId");
            if (exerciseId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Missing exerciseId");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            // Check if exercise already exists in day
            String checkSQL = "SELECT COUNT(*) FROM exercise_day WHERE course_id = :courseId AND day_id = :dayId AND exercise_id = :exerciseId";
            Query checkQuery = entityManager.createNativeQuery(checkSQL);
            checkQuery.setParameter("courseId", courseId);
            checkQuery.setParameter("dayId", dayId);
            checkQuery.setParameter("exerciseId", exerciseId);
            Number exists = (Number) checkQuery.getSingleResult();
            if (exists != null && exists.intValue() > 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Exercise already exists in this day");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }
            // Insert exercise into day
            String insertSQL = "INSERT INTO exercise_day (course_id, day_id, exercise_id, status) VALUES (:courseId, :dayId, :exerciseId, 1)";
            Query insertQuery = entityManager.createNativeQuery(insertSQL);
            insertQuery.setParameter("courseId", courseId);
            insertQuery.setParameter("dayId", dayId);
            insertQuery.setParameter("exerciseId", exerciseId);
            insertQuery.executeUpdate();
            // Trả về danh sách day mới nhất
            return getCourseDays(courseId);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to add exercise: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/courses/{courseId}/days/{dayId}/exercises/{exerciseId}/toggle-status")
    public ResponseEntity<?> toggleExerciseDayStatus(@PathVariable Integer courseId, @PathVariable Integer dayId, @PathVariable Integer exerciseId) {
        try {
            // Toggle status
            String getStatusSQL = "SELECT status FROM exercise_day WHERE course_id = :courseId AND day_id = :dayId AND exercise_id = :exerciseId";
            Query getStatusQuery = entityManager.createNativeQuery(getStatusSQL);
            getStatusQuery.setParameter("courseId", courseId);
            getStatusQuery.setParameter("dayId", dayId);
            getStatusQuery.setParameter("exerciseId", exerciseId);
            Number status = (Number) getStatusQuery.getSingleResult();
            int newStatus = (status != null && status.intValue() == 1) ? 0 : 1;
            String updateSQL = "UPDATE exercise_day SET status = :newStatus WHERE course_id = :courseId AND day_id = :dayId AND exercise_id = :exerciseId";
            Query updateQuery = entityManager.createNativeQuery(updateSQL);
            updateQuery.setParameter("newStatus", newStatus);
            updateQuery.setParameter("courseId", courseId);
            updateQuery.setParameter("dayId", dayId);
            updateQuery.setParameter("exerciseId", exerciseId);
            updateQuery.executeUpdate();
            // Trả về danh sách day mới nhất
            return getCourseDays(courseId);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to toggle exercise status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/courses/{courseId}/days")
    public ResponseEntity<?> getCourseDays(@PathVariable Integer courseId) {
        try {
            // Query to get course days with exercises, lấy thêm status của exercise_day
            String sql = "SELECT cd.course_day_id, cd.course_id, cd.day_id, d.day_name, cd.status, " +
                        "e.exercise_id, e.exercise_name, e.target, e.body_part, e.instructions, ed.status as exercise_status " +
                        "FROM course_days cd " +
                        "JOIN days d ON cd.day_id = d.day_id " +
                        "LEFT JOIN exercise_day ed ON d.day_id = ed.day_id AND ed.course_id = cd.course_id " +
                        "LEFT JOIN exercise e ON ed.exercise_id = e.exercise_id " +
                        "WHERE cd.course_id = :courseId AND cd.status = true " +
                        "ORDER BY d.day_id, e.exercise_id";
            List<Tuple> results = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("courseId", courseId)
                .getResultList();
            // Group results by day
            Map<Integer, Map<String, Object>> daysMap = new LinkedHashMap<>();
            for (Tuple result : results) {
                Integer dayId = ((Number) result.get("day_id")).intValue();
                if (!daysMap.containsKey(dayId)) {
                    Map<String, Object> dayInfo = new HashMap<>();
                    dayInfo.put("dayId", dayId);
                    dayInfo.put("dayName", result.get("day_name"));
                    dayInfo.put("courseId", courseId);
                    dayInfo.put("status", result.get("status"));
                    dayInfo.put("exercises", new ArrayList<>());
                    daysMap.put(dayId, dayInfo);
                }
                // Add exercise if exists
                if (result.get("exercise_id") != null) {
                    Map<String, Object> exercise = new HashMap<>();
                    exercise.put("exerciseId", result.get("exercise_id"));
                    exercise.put("exerciseName", result.get("exercise_name"));
                    exercise.put("target", result.get("target"));
                    exercise.put("bodyPart", result.get("body_part"));
                    exercise.put("instructions", result.get("instructions"));
                    exercise.put("status", result.get("exercise_status")); // Thêm status của exercise_day
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> exercises = (List<Map<String, Object>>) daysMap.get(dayId).get("exercises");
                    exercises.add(exercise);
                }
            }
            List<Map<String, Object>> courseDays = new ArrayList<>(daysMap.values());
            return ResponseEntity.ok(courseDays);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch course days: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 