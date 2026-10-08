package com.vincenthuto.hemomancy.common.circus;

import java.util.List;
import java.util.Optional;

/** Teaching assignments refer to the shared summon catalogue; they do not define new bodies. */
public final class CircusCurriculum {
    public record Lesson(String summon, String teacher, int degree) {}

    private static final List<Lesson> LESSONS = List.of(
            new Lesson("veinwing_vulture", "circus_acrobat", 3),
            new Lesson("marrow_spitter", "circus_knife_thrower", 3),
            new Lesson("scarlet_mummer", "circus_stilt_walker", 4),
            new Lesson("gorebound_hulk", "circus_strongman", 4),
            new Lesson("sanguine_hound", "circus_beast_tamer", 4),
            new Lesson("cinder_bellows", "circus_fire_eater", 4),
            new Lesson("mnemonist_puppet", "circus_understudy", 5));

    private CircusCurriculum() {}

    public static List<Lesson> lessons() { return LESSONS; }

    public static Optional<Lesson> lesson(String summon) {
        return LESSONS.stream().filter(lesson -> lesson.summon().equals(summon)).findFirst();
    }

    public static Optional<Lesson> forTeacher(String teacher) {
        return LESSONS.stream().filter(lesson -> lesson.teacher().equals(teacher)).findFirst();
    }

    public static boolean instructionSatisfied(boolean taught, boolean known) { return taught || known; }

    public static boolean mayBeginOrdeal(String summon, int degree, boolean taught, boolean known) {
        return lesson(summon).filter(lesson -> degree >= lesson.degree())
                .map(lesson -> instructionSatisfied(taught, known)).orElse(false);
    }
}
