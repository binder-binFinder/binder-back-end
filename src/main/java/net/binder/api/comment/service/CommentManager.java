package net.binder.api.comment.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.comment.entity.Comment;
import net.binder.api.comment.repository.CommentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CommentManager {

    private final CommentRepository commentRepository;

    @Transactional
    public Long add(Comment comment) {
        return commentRepository.save(comment)
                .getId();
    }

    @Transactional
    public void update(Comment comment, String content) {
        comment.modifyContent(content);
    }

    @Transactional
    public boolean delete(Comment comment) {
        return comment.softDelete();
    }
}
