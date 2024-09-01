package net.binder.api.auth.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    JwtUtil jwtUtil;

    @Test
    @DisplayName("유저 이름과 권한 정보가 담긴 jwt 토큰을 발행할 수 있다.")
    void generateToken() {
        String username = "test";
        String role = "ROLE_ADMIN";

        String token = jwtUtil.generateToken(username, role);

        assertThat(jwtUtil.getUsername(token)).isEqualTo(username);
        assertThat(jwtUtil.getRole(token)).isEqualTo(role);
    }
}