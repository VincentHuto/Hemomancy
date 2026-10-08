package com.vincenthuto.hemomancy.common.circus;

public final class CircusSchoolRules {
    private CircusSchoolRules() {}
    public static boolean canTeach(String teacher, String summon, int degree, boolean active,
                                   boolean alive, boolean spectator) {
        return active && alive && !spectator && CircusCurriculum.lesson(summon)
                .map(lesson -> lesson.teacher().equals(teacher) && degree >= lesson.degree()).orElse(false);
    }
    public static boolean canEarnGuestReward(long day, long lastDay, int pending) {
        return day > lastDay && pending == 0;
    }
}
