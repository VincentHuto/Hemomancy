package com.vincenthuto.hemomancy.common.mission.circus;

import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;

import java.util.List;
import java.util.Optional;

/** Teaching assignments refer to the shared summon catalogue; they do not define new bodies. */
public final class CircusCurriculum {
    /** One professor's lesson; {@code camp} names the {@code troupe_teacher_<camp>} structure tag the Waybill seeks. */
    public record Lesson(String summon, String teacher, int degree, String camp) {}

    private static final List<Lesson> LESSONS = List.of(
            new Lesson(PuppeteerSummonDefinitions.VEINWING_VULTURE, "circus_acrobat", 3, "air"),
            new Lesson(PuppeteerSummonDefinitions.MARROW_SPITTER, "circus_knife_thrower", 3, "blades"),
            new Lesson(PuppeteerSummonDefinitions.SCARLET_MUMMER, "circus_stilt_walker", 4, "pageantry"),
            new Lesson(PuppeteerSummonDefinitions.GOREBOUND_HULK, "circus_strongman", 4, "pageantry"),
            new Lesson(PuppeteerSummonDefinitions.SANGUINE_HOUND, "circus_beast_tamer", 4, "embers"),
            new Lesson(PuppeteerSummonDefinitions.CINDER_BELLOWS, "circus_fire_eater", 4, "embers"),
            new Lesson(PuppeteerSummonDefinitions.MNEMONIST_PUPPET, "circus_understudy", 5, "memory"));

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
