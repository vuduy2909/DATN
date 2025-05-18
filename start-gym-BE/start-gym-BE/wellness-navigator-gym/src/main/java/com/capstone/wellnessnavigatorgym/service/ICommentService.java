package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.Comment;

import java.util.List;

public interface ICommentService {
    List<Comment> findAll();

    Comment findCommentById(Integer id);

    void saveComment(Comment comment);
}
