package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.Comment;
import com.capstone.wellnessnavigatorgym.error.NotFoundById;
import com.capstone.wellnessnavigatorgym.repository.ICommentRepository;
import com.capstone.wellnessnavigatorgym.service.ICommentService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements ICommentService {

    @Autowired
    private ICommentRepository commentRepository;

    @Override
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    @SneakyThrows
    @Override
    public Comment findCommentById(Integer id) {
        Optional<Comment> comment = commentRepository.findCommentById(id);
        if (comment.isPresent()) {
            return comment.get();
        }
        throw new NotFoundById("Could not find any comments with code: " + id);
    }

    @Override
    public void saveComment(Comment comment) {
        commentRepository.insertComment(
                comment.getCommentText(),
                comment.getRating(),
                String.valueOf(comment.getCustomer().getCustomerId()),
                String.valueOf(comment.getExercise().getExerciseId()));
    }

}