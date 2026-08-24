package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.VeinMasonScarLesson;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AnchoriteGameTests {
    private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

    private AnchoriteGameTests() {}

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
    public static void canonicalScarTiersLoadFromRegistry(GameTestHelper helper) {
        Map<Integer, String[]> families = Map.of(
                1, new String[]{"heart", "pyre", "feral", "halo", "blight", "rime", "thorn", "shade"},
                2, new String[]{"marrow", "sol", "flux", "veil", "wither", "glacier", "anvil", "moon"},
                3, new String[]{"phoenix", "corona", "chimera", "transcendence", "oblivion", "descendence", "crucible", "eye"});
        for (var family : families.entrySet()) for (String name : family.getValue()) {
            var recipe = ItemScarPattern.getRecipeForScarId(Hemomancy.rloc("scar_" + name), helper.getLevel());
            helper.assertTrue(recipe != null && recipe.getTier() == family.getKey(), "wrong loaded tier for " + name);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
    public static void strongestTendencyRewardsCarryTierTwoAndThreeTemplates(GameTestHelper helper) {
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "anchorite-test"), ClientInformation.createDefault());
        HemoCapabilityAccess.getBloodTendency(player).ifPresent(tendency ->
                tendency.setTendency(Map.of(EnumBloodTendency.LUX, 20F)));
        ResourceLocation tierTwo = Hemomancy.rloc("scar_veil");
        ResourceLocation tierThree = Hemomancy.rloc("scar_transcendence");
        helper.assertTrue(ItemScarPattern.getScarIds(VeinMasonScarLesson.strongestForPlayer(player, 2).patternStack())
                .equals(java.util.List.of(tierTwo)), "tier-two reward did not select Veil");
        helper.assertTrue(VeinMasonScarLesson.strongestForPlayer(player, 2).catalyst() == Items.ENDER_PEARL,
                "tier-two reward did not include Veil's authored catalyst");
        helper.assertTrue(ItemScarPattern.getScarIds(VeinMasonScarLesson.strongestForPlayer(player, 3).patternStack())
                .equals(java.util.List.of(tierThree)), "tier-three reward did not select Transcendence");
        player.discard();
        helper.succeed();
    }
}
