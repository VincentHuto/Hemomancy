package com.vincenthuto.hemomancy.common.circus;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CircusSchoolRulesTest {
    @Test void classroomEventsRequireTheCorrectLivingTeacherAndEligiblePlayer() {
        assertTrue(CircusSchoolRules.canTeach("circus_acrobat", "veinwing_vulture", 3, true, true, false));
        assertFalse(CircusSchoolRules.canTeach("circus_fire_eater", "veinwing_vulture", 4, true, true, false));
        assertFalse(CircusSchoolRules.canTeach("circus_acrobat", "veinwing_vulture", 3, true, true, true));
        assertFalse(CircusSchoolRules.canTeach("circus_acrobat", "veinwing_vulture", 3, true, false, false));
        assertFalse(CircusSchoolRules.canTeach("circus_stilt_walker", "scarlet_mummer", 3, true, true, false));
    }
    @Test void guestRewardsCannotDuplicateDuringOverflowOrOnTheSameDay() {
        assertTrue(CircusSchoolRules.canEarnGuestReward(0, -1, 0));
        assertFalse(CircusSchoolRules.canEarnGuestReward(3, 3, 0));
        assertFalse(CircusSchoolRules.canEarnGuestReward(4, 3, 2));
        assertTrue(CircusSchoolRules.canEarnGuestReward(4, 3, 0));
    }
}
