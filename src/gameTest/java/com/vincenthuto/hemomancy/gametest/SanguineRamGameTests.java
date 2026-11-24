package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("registration_validation")
@PrefixGameTestTemplate(false)
public final class SanguineRamGameTests {
    @GameTest(template = "empty")
    public static void ramSpawnsWithCreatureAttributesAndMatchingEgg(GameTestHelper helper) {
        var type = EntityInit.sanguine_ram.get();
        helper.assertTrue(Hemomancy.rloc("sanguine_ram").equals(BuiltInRegistries.ENTITY_TYPE.getKey(type)),
                "The summon command must resolve the Sanguine Ram");
        helper.assertTrue(type.getCategory() == MobCategory.CREATURE, "The ram must be a passive creature");
        var egg = ItemInit.spawn_egg_sanguine_ram.get();
        helper.assertTrue(egg.getType(egg.getDefaultInstance()) == type, "The egg must spawn the ram");
        var ram = helper.spawn(type, new BlockPos(3, 2, 3));
        helper.assertTrue(ram.isAlive() && ram.getMaxHealth() > 0
                        && ram.getAttributeValue(Attributes.MOVEMENT_SPEED) > 0,
                "The spawned ram must have living movement attributes");
        ram.discard();
        helper.succeed();
    }
}
