package net.binder.api.comment.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.binder.api.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentTest {

    @Test
    @DisplayName("댓글 글자수가 60자를 초과하면 예외가 발생한다.")
    void createComment() {
        assertThatThrownBy(() -> new Comment(null, null, "a".repeat(61)))
                .isInstanceOf(BadRequestException.class);
    }
}