package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

class LuxUmbraCombatContractTest {
    @Test void existingCombatAndTendrilContractsRemainValid() throws Exception {
        com.vincenthuto.hemomancy.common.manipulation.LuxTenebrisCombatStyleSourceTest.main(new String[0]);
        com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffectsTest.main(new String[0]);
    }
}
