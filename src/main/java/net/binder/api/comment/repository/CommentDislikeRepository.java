package net.binder.api.comment.repository;

import net.binder.api.comment.entity.CommentDislike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentDislikeRepository extends JpaRepository<CommentDislike, Long> {
}
