package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingCrossbowItem;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public class ManipulationAcquisitionGameTests {
    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "manipulation_acquisition")
    public static void creativeRightClickLearnsFullMemoryWithoutProgressionOrConsumption(GameTestHelper h) {
        var p = player(h);
        try {
            p.setGameMode(GameType.CREATIVE);
            HemoCapabilityAccess.requireBloodVolume(p).setActive(false);
            HemoCapabilityAccess.getInitiatoryDegree(p).orElseThrow().setDegreeNumber(0);
            var memory = ItemInit.memory_guided_blood_shot.get();
            var manipulation = ManipulationInit.guided_blood_shot.get();
            var stack = new ItemStack(memory);
            p.setItemInHand(InteractionHand.MAIN_HAND, stack);

            memory.use(h.getLevel(), p, InteractionHand.MAIN_HAND);

            h.assertTrue(HemoCapabilityAccess.requireKnownManipulations(p).getManipLevel(manipulation) != null,
                    "Creative right-click did not learn the memory");
            h.assertTrue(stack.getCount() == 1, "Creative learning consumed the memory item");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "manipulation_acquisition")
    public static void loadedCrossbowSurvivesRestorationReloadAndAnotherForm(GameTestHelper h) {
        var p = player(h);
        try {
            ItemStack staff = new ItemStack(ItemInit.living_staff.get());
            staff.set(DataComponents.CUSTOM_NAME, Component.literal("Remember me"));
            p.setItemInHand(InteractionHand.MAIN_HAND, staff);
            var crossbow = ManipulationInit.getByName("conjure_crossbow");
            h.assertTrue(LivingStaffWeaponFormHelper.applySelection(p, crossbow), "Crossbow switch failed");
            ItemStack loaded = p.getMainHandItem();
            var data = loaded.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            var ammo = new ListTag();
            ammo.add(new ItemStack(Items.ARROW).save(p.registryAccess()));
            data.put("ChargedProjectiles", ammo); data.putBoolean("Charged", true);
            loaded.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            LivingStaffWeaponFormHelper.restoreMainHand(p);
            h.assertTrue(Component.literal("Remember me").equals(p.getMainHandItem().get(DataComponents.CUSTOM_NAME)), "Staff name lost");
            p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.parseOptional(p.registryAccess(),
                    (net.minecraft.nbt.CompoundTag) p.getMainHandItem().save(p.registryAccess())));
            LivingStaffWeaponFormHelper.applySelection(p, ManipulationInit.getByName("conjure_blade"));
            LivingStaffWeaponFormHelper.applySelection(p, crossbow);
            h.assertTrue(LivingCrossbowItem.isCharged(p.getMainHandItem()), "Loaded crossbow charge lost on restoration");
            h.assertTrue(LivingCrossbowItem.hasChargedProjectile(p.getMainHandItem(), Items.ARROW), "Loaded arrow lost");
            LivingCrossbowItem.fireProjectiles(h.getLevel(), p, InteractionHand.MAIN_HAND, p.getMainHandItem(), 1, 0);
            LivingStaffWeaponFormHelper.restoreMainHand(p);
            LivingStaffWeaponFormHelper.applySelection(p, crossbow);
            h.assertTrue(!LivingCrossbowItem.hasChargedProjectile(p.getMainHandItem(), Items.ARROW), "Spent arrow resurrected");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "manipulation_acquisition")
    public static void everyStaffFormPreservesStaffAndOccupiedOffhandOnBloodFailure(GameTestHelper h) {
        var p = player(h);
        try {
            for (String form : java.util.List.of("blade", "axe", "spear", "claws", "crossbow", "torch", "flail", "sickle")) {
                ItemStack staff = new ItemStack(ItemInit.living_staff.get());
                staff.set(DataComponents.CUSTOM_NAME, Component.literal(form));
                p.setItemInHand(InteractionHand.MAIN_HAND, staff);
                p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.DIAMOND));
                var blood = HemoCapabilityAccess.requireBloodVolume(p);
                blood.setBloodVolume(249);
                var manip = ManipulationInit.getByName("conjure_" + form);
                h.assertTrue(!LivingStaffWeaponFormHelper.applySelection(p, manip), form + " switched below cost");
                h.assertTrue(ItemStack.isSameItemSameComponents(staff, p.getMainHandItem()), form + " changed failed input");
                blood.setBloodVolume(250);
                h.assertTrue(LivingStaffWeaponFormHelper.applySelection(p, manip), form + " exact-cost switch failed");
                h.assertTrue(blood.getBloodVolume() == 0, form + " payment mismatch");
                LivingStaffWeaponFormHelper.restoreStaffAfterBloodFailure(p.getMainHandItem(), p);
                h.assertTrue(ItemStack.isSameItemSameComponents(staff, p.getMainHandItem()), form + " restoration lost staff data");
                h.assertTrue(p.getOffhandItem().is(Items.DIAMOND), form + " overwrote offhand");
            }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "manipulation_acquisition")
    public static void allFamiliesGateLearningAndShareProgressAfterReload(GameTestHelper h) {
        var p = player(h);
        try {
            HemoCapabilityAccess.getInitiatoryDegree(p).orElseThrow().setDegreeNumber(7);
            var known = HemoCapabilityAccess.requireKnownManipulations(p);
            int[] thresholds = {0, 10, 35, 85, 185};
            for (var family : ManipulationFamilyRegistry.families()) {
                known.getKnownManips().clear();
                var baseline = ManipulationInit.getByName(family.baselineId());
                h.assertTrue(KnownManipulationGrantHelper.grantMemory(p, baseline).success(), family.baselineId() + " baseline grant failed");
                for (var form : family.forms()) {
                    var manip = ManipulationInit.getByName(form.id());
                    known.getKnownManips().put(baseline, new ManipLevel(0, thresholds[form.requiredLevel()] - 1));
                    known.getManipLevel(baseline).tryLevelUp();
                    h.assertTrue(!KnownManipulationGrantHelper.checkMemoryGrant(p, manip).success(), form.id() + " learned below threshold");
                    known.getManipLevel(baseline).setXp(thresholds[form.requiredLevel()]);
                    known.getManipLevel(baseline).tryLevelUp();
                    h.assertTrue(KnownManipulationGrantHelper.grantMemory(p, manip).success(), form.id() + " learning rejected at threshold");
                    h.assertTrue(known.getManipLevel(baseline) == known.getManipLevel(manip), form.id() + " progress not shared");
                    known.setLoadout(0, new com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout(
                            "Family", form.id(), java.util.List.of(family.baselineId(), form.id())));
                    var reloaded = new KnownManipulations();
                    reloaded.deserializeNBT(p.registryAccess(), ((KnownManipulations) known).serializeNBT(p.registryAccess()));
                    h.assertTrue(reloaded.isManipulationAvailable(manip), form.id() + " unavailable after reload");
                    h.assertTrue(reloaded.getManipLevel(baseline) == reloaded.getManipLevel(manip), form.id() + " split progress after reload");
                    h.assertTrue(reloaded.getLoadout(0).manipNames().equals(java.util.List.of(family.baselineId(), form.id())), form.id() + " loadout lost forms");
                    known.getKnownManips().remove(manip);
                }
            }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "manipulation_acquisition")
    public static void liberationGrantRetainsAnUnlearnableRewardAndDoesNotDuplicateKnownMemory(GameTestHelper h) {
        var p = player(h);
        try {
            var memory = ItemInit.memory_thread_ripper.get();
            var manip = ManipulationInit.thread_ripper.get();
            var known = HemoCapabilityAccess.requireKnownManipulations(p);
            com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity.grantLiberationMemory(p);
            h.assertTrue(p.getInventory().countItem(memory) == 1, "Rejected reward vanished");
            h.assertTrue(!known.doesListContainName(known.getKnownManips(), manip), "Reward bypassed rank gate");
            p.getInventory().clearContent();
            HemoCapabilityAccess.getInitiatoryDegree(p).orElseThrow().setDegreeNumber(7);
            com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity.grantLiberationMemory(p);
            h.assertTrue(known.doesListContainName(known.getKnownManips(), manip), "Eligible reward not learned");
            com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity.grantLiberationMemory(p);
            h.assertTrue(p.getInventory().countItem(memory) == 0, "Known reward duplicated as item");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "manipulation_acquisition")
    public static void crudeMemoriesLearnWithoutLoomAndDuplicateUseKeepsItemAndBlood(GameTestHelper h) {
        var p = player(h);
        try {
            for (String name : java.util.List.of("blood_shot", "blood_rush", "deadly_gaze", "sanguine_mending", "hemorrhage", "glacial_grasp", "sanguine_ignition", "void_shroud")) {
                var item = (com.vincenthuto.hemomancy.common.item.harbinger.memories.CrudeMemoryShardItem)
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.get(Hemomancy.rloc("crude_memory_" + name));
                HemoCapabilityAccess.getInitiatoryDegree(p).orElseThrow().setDegreeNumber(
                        com.vincenthuto.hemomancy.common.manipulation.ManipulationRankGates.minDegreeForRank(item.getManip().getRank()));
                HemoCapabilityAccess.requireBloodVolume(p).setBloodVolume(1500);
                var stack = new ItemStack(item);
                p.setItemInHand(InteractionHand.MAIN_HAND, stack);
                item.use(h.getLevel(), p, InteractionHand.MAIN_HAND);
                h.assertTrue(stack.isEmpty(), name + " learning did not consume memory");
                h.assertTrue(HemoCapabilityAccess.requireKnownManipulations(p).getManipLevel(item.getManip()) != null, name + " not learned");
                h.assertTrue(HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume() == 1000, name + " wrong learning cost");
                var duplicate = new ItemStack(item); p.setItemInHand(InteractionHand.MAIN_HAND, duplicate);
                item.use(h.getLevel(), p, InteractionHand.MAIN_HAND);
                h.assertTrue(!duplicate.isEmpty() && HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume() == 1000, name + " duplicate consumed resources");
            }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "manipulation_acquisition", timeoutTicks = 120)
    public static void sustainedChannelCreditsFiveSecondsRatherThanEveryPulse(GameTestHelper h) {
        var p = player(h);
        var m = ManipulationInit.sanguine_mending.get();
        var known = HemoCapabilityAccess.requireKnownManipulations(p);
        known.getKnownManips().put(m, new ManipLevel(0, 0));
        known.setEquippedManipNames(java.util.List.of(m.getName())); known.setSelectedManip(m);
        var sword = new ItemStack(Items.DIAMOND_SWORD); sword.setDamageValue(1000);
        p.setItemInHand(InteractionHand.MAIN_HAND, sword);
        com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.start(p);
        h.assertTrue(known.getManipLevel(m).getXp() == 0, "START farmed mastery");
        for (int delay = 20; delay <= 100; delay += 20) {
            int tick = delay;
            h.runAfterDelay(delay, () -> {
                try {
                    com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.onPlayerTick(
                            new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
                    h.assertTrue(known.getManipLevel(m).getXp() == (tick == 100 ? 1 : 0), "Incorrect channel credit at " + tick);
                    if (tick == 100) {
                        com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.stop(p);
                        h.assertTrue(sword.getDamageValue() == 700, "Expected START plus five paid repairs");
                        h.succeed();
                    }
                } finally {
                    if (tick == 100) { com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.stop(p, false); p.discard(); }
                }
            });
        }
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "acq_" + UUID.randomUUID().toString().substring(0, 8)), false);
        var p = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, p, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { }
        };
        p.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2, 4, 2)));
        HemoCapabilityAccess.requireBloodVolume(p).setActive(true);
        HemoCapabilityAccess.requireBloodVolume(p).setBloodVolume(5000);
        return p;
    }
}
