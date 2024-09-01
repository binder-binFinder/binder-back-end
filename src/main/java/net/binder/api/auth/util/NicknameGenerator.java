package net.binder.api.auth.util;

import java.util.Random;

public class NicknameGenerator {

    private static final Random RANDOM = new Random();

    public static String generateNewNickname(String nickname) {
        int number = RANDOM.nextInt(100000);
        return nickname + number;
    }
}
