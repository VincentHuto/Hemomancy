package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;
import java.util.Map;

@GameTestHolder("mob_drop_validation")
@PrefixGameTestTemplate(false)
public final class ConsolidatedLootGameTests {
    @GameTest(template = "pool")
    public static void ordinarySourcesGeneratePlayerLootAndRespectKillGates(GameTestHelper h) {
        var sources = Map.ofEntries(
                Map.entry("chiton", "chitinous_husk"), Map.entry("phlegethontic_bombardier", "chitinous_husk"),
                Map.entry("bloody_belly_comb_jelly", "cuttlefish_chromatophores"), Map.entry("luminal_cicada", "cuttlefish_chromatophores"),
                Map.entry("excoriated", "calcified_blood_spine"), Map.entry("venous_strider", "calcified_blood_spine"),
                Map.entry("vampire_bat", "calcified_blood_spine"), Map.entry("peacock_spider", "puppeteering_thread"),
                Map.entry("hemojelly", "puppeteering_thread"), Map.entry("siphonophore", "ganglion_cluster"),
                Map.entry("choir_keeper", "venous_pinion"));
        for (var pair : sources.entrySet()) {
            var mob = (Mob) BuiltInRegistries.ENTITY_TYPE.get(Hemomancy.rloc(pair.getKey())).create(h.getLevel());
            var item = BuiltInRegistries.ITEM.get(Hemomancy.rloc(pair.getValue()));
            var playerLoot = loot(h, mob, true);
            int count = playerLoot.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
            h.assertTrue(count >= 1 && count <= 2, "Expected 1–2 shared material from " + pair.getKey() + ": " + playerLoot);
            h.assertTrue(loot(h, mob, false).stream().noneMatch(stack -> stack.is(item)), "Non-player kill bypassed gate: " + pair.getKey());
        }
        h.succeed();
    }

    @GameTest(template = "pool")
    public static void burningFoodLootCooksWithoutRemovingOtherDoeMaterials(GameTestHelper h) {
        var doe = EntityInit.crimson_doe.get().create(h.getLevel());
        doe.setRemainingFireTicks(100);
        var drops = loot(h, doe, true);
        h.assertTrue(drops.stream().anyMatch(stack -> stack.is(ItemInit.cooked_venison.get())), "Burning doe did not cook venison");
        h.assertTrue(drops.stream().anyMatch(stack -> stack.is(ItemInit.bleeding_bulb.get())), "Doe lost its existing bulb pool");
        h.assertTrue(drops.stream().noneMatch(stack -> stack.is(ItemInit.raw_venison.get())), "Burning doe still yielded raw meat");
        var herring = EntityInit.pelagic_herring.get().create(h.getLevel()); herring.setRemainingFireTicks(100);
        h.assertTrue(loot(h, herring, false).stream().anyMatch(stack -> stack.is(ItemInit.cooked_pelagic_herring.get())), "Burning fish failed unrestricted cooked food loot");
        h.succeed();
    }

    @GameTest(template = "pool")
    public static void circusActorsCannotFarmNewWildMaterials(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(8, 4, 8));
        var spider = EntityInit.peacock_spider.get().create(h.getLevel());
        spider.getPersistentData().putUUID("CircusTrialOwner", java.util.UUID.randomUUID());
        var bat = EntityInit.vampire_bat.get().create(h.getLevel()); bat.makeManifestedAggregate(java.util.UUID.randomUUID());
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        for (var actor : java.util.List.of(spider, bat)) {
            actor.moveTo(pos.getCenter()); h.getLevel().addFreshEntity(actor);
            actor.hurt(actor.damageSources().playerAttack(player), 1000);
        }
        var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4));
        h.assertTrue(drops.stream().noneMatch(drop -> drop.getItem().is(ItemInit.puppeteering_thread.get())
                || drop.getItem().is(ItemInit.chitinous_husk.get()) || drop.getItem().is(ItemInit.calcified_blood_spine.get())), "Controlled circus actors dropped wild materials");
        drops.forEach(ItemEntity::discard); spider.discard(); bat.discard(); h.succeed();
    }

    private static java.util.List<ItemStack> loot(GameTestHelper h, Mob mob, boolean playerKill) {
        var params = new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.THIS_ENTITY, mob)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, mob.damageSources().generic());
        if (playerKill) params.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, h.makeMockPlayer(GameType.SURVIVAL));
        var table = h.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
                Hemomancy.rloc("entities/" + BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath())));
        return table.getRandomItems(params.create(LootContextParamSets.ENTITY));
    }
}
