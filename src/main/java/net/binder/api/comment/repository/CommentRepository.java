package net.binder.api.comment.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import net.binder.api.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentQueryRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Comment c WHERE c.id = :id")
    Optional<Comment> findByIdWithPessimisticLock(Long id);
}
