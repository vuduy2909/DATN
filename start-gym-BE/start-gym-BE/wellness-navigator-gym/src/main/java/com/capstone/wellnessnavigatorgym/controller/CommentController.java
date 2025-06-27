package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.ResponseToClient;
import com.capstone.wellnessnavigatorgym.dto.comment.CommentDto;
import com.capstone.wellnessnavigatorgym.dto.response.MessageResponse;
import com.capstone.wellnessnavigatorgym.entity.Comment;
import com.capstone.wellnessnavigatorgym.service.ICommentService;
import com.capstone.wellnessnavigatorgym.service.ICustomerService;
import com.capstone.wellnessnavigatorgym.service.IExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/v1/comment")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Comment Management", description = "APIs for managing exercise comments and ratings")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private ICommentService commentService;

    @GetMapping("")
    public ResponseEntity<List<Comment>> getAllComment() {
        List<Comment> commentList = this.commentService.findAll();
        if (commentList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(commentList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Integer id) {
        return new ResponseEntity<>(commentService.findCommentById(id), HttpStatus.OK);
    }

    @PostMapping( "/create")
    public ResponseEntity<?> createComment(@Valid @RequestBody CommentDto commentDto) {
        try {
            logger.info("=== CREATING NEW COMMENT ===");
            logger.info("Comment data: {}", commentDto);
            
            // Validate required fields
            if (commentDto.getCommentText() == null || commentDto.getCommentText().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Comment text is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (commentDto.getCustomer() == null || commentDto.getCustomer().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Customer ID is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (commentDto.getExercise() == null || commentDto.getExercise().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Exercise ID is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Course and day are optional - can be null for exercise-only comments
        Comment comment = new Comment(commentDto);
            
            // Save comment synchronously to handle errors properly
            commentService.saveComment(comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comment created successfully!");
            response.put("commentText", commentDto.getCommentText());
            response.put("customerId", commentDto.getCustomer());
            response.put("exerciseId", commentDto.getExercise());
            response.put("courseId", commentDto.getCourse());
            response.put("dayId", commentDto.getDay());
            
            logger.info("✅ Comment created successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            logger.error("Error creating comment: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}/report")
    public ResponseEntity<?> reportComment(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reportReason", "");
            Comment comment = commentService.findCommentById(id);
            comment.setIsReported(true);
            comment.setReportCount(comment.getReportCount() == null ? 1 : comment.getReportCount() + 1);
            comment.setReportReason(reason);
            commentService.saveComment(comment); // save lại comment đã update
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comment reported successfully");
            response.put("commentId", comment.getCommentId());
            response.put("reportCount", comment.getReportCount());
            response.put("isReported", comment.getIsReported());
            response.put("reportReason", comment.getReportReason());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        try {
            logger.info("=== UPDATING COMMENT {} ===", id);
            
            Comment comment = commentService.findCommentById(id);
            if (comment == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Comment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // Update comment text if provided
            if (body.containsKey("commentText")) {
                String newCommentText = (String) body.get("commentText");
                if (newCommentText == null || newCommentText.trim().isEmpty()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Comment text cannot be empty");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                comment.setCommentText(newCommentText);
            }
            
            // Update rating if provided
            if (body.containsKey("rating")) {
                Object ratingObj = body.get("rating");
                Integer newRating = null;
                if (ratingObj instanceof Integer) {
                    newRating = (Integer) ratingObj;
                } else if (ratingObj instanceof String) {
                    try {
                        newRating = Integer.parseInt((String) ratingObj);
                    } catch (NumberFormatException e) {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("error", "Invalid rating format");
                        return ResponseEntity.badRequest().body(errorResponse);
                    }
                }
                
                if (newRating != null && (newRating < 1 || newRating > 5)) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Rating must be between 1 and 5");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                comment.setRating(newRating);
            }
            
            // Save updated comment
            commentService.saveComment(comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comment updated successfully!");
            response.put("commentId", comment.getCommentId());
            response.put("commentText", comment.getCommentText());
            response.put("rating", comment.getRating());
            
            logger.info("✅ Comment {} updated successfully", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating comment {}: {}", id, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer id) {
        try {
            logger.info("=== DELETING COMMENT {} ===", id);
            
            Comment comment = commentService.findCommentById(id);
            if (comment == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Comment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // Delete comment
            commentService.deleteComment(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comment deleted successfully!");
            response.put("commentId", id);
            
            logger.info("✅ Comment {} deleted successfully", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting comment {}: {}", id, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
