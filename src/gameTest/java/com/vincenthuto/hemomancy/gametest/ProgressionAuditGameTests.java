package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.ConsecratedSyringeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BloodCrystalBlock;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.AlembicVesselRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeStartupResult;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Optional regression probes for unresolved findings in HARBINGER_PROGRESSION_AUDIT.md.
 * They assert the intended contract, not the buggy behavior. No production fixes are bundled.
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ProgressionAuditGameTests {
    private static final String EMPTY = "bastion/mobs/empty";

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "progression_audit", required = false)
    public static void auditAlembicCanHoldOneFlask(GameTestHelper helper) {
        GhastlyAlembicBlockEntity alembic = alembic(helper);
        double capacity = HemoCapabilityAccess.getBloodVolume(alembic).orElseThrow().getMaxBloodVolume();
        helper.assertTrue(capacity >= AlembicVesselRules.requiredBlood(AlembicVesselRules.Vessel.FLASK),
                "Alembic capacity " + capacity + " cannot reach flask requirement "
                        + AlembicVesselRules.requiredBlood(AlembicVesselRules.Vessel.FLASK));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "progression_audit", required = false)
    public static void auditDistillationPreservesReagentWithEmptyFlask(GameTestHelper helper) {
        GhastlyAlembicBlockEntity alembic = alembic(helper);
        alembic.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT, new ItemStack(BlockInit.devils_tooth.get()));
        alembic.setItem(GhastlyAlembicBlockEntity.SLOT_FLASK, new ItemStack(HLItemInit.cured_clay_flask.get()));
        for (int tick = 0; tick < 400; tick++) {
            GhastlyAlembicBlockEntity.serverTick(helper.getLevel(), alembic.getBlockPos(),
                    helper.getLevel().getBlockState(alembic.getBlockPos()), alembic);
        }
        ItemStack result = alembic.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT);
        helper.assertTrue(result.is(ItemInit.foul_paste.get()) && result.getCount() == 2,
                "Devil's Tooth must produce two Foul Paste with a bottling flask present; got " + result);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "progression_audit", required = false)
    public static void auditConsecratedSyringesCanStartBalancedSpin(GameTestHelper helper) {
        VialCentrifugeBlockEntity centrifuge = centrifuge(helper);
        ItemStack syringe = new ItemStack(ItemInit.consecrated_syringe.get());
        CompoundTag tag = new CompoundTag();
        tag.putString(ConsecratedSyringeItem.TAG_SAINT_TYPE, EnumSaintType.HEMORATH.name());
        syringe.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        centrifuge.setItem(2, syringe.copy());
        centrifuge.setItem(6, syringe.copy());
        var result = centrifuge.attemptStartup(null);
        helper.assertTrue(result == VialCentrifugeStartupResult.SUCCESS,
                "Balanced valid Saint syringes must start their advertised processing; got " + result);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "progression_audit", required = false)
    public static void auditRackTransferConservesPhysicalVials(GameTestHelper helper) {
        VialCentrifugeBlockEntity centrifuge = centrifuge(helper);
        ItemStack rack = new ItemStack(ItemInit.vial_rack.get());
        var vials = VialRackItem.getVials(rack);
        for (ItemStack vial : vials) {
            CompoundTag tag = new CompoundTag();
            tag.putString(BloodVialItem.TAG_ENTITY_TYPE, "minecraft:cow");
            tag.putBoolean(BloodVialItem.TAG_STATE, true);
            vial.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        VialRackItem.setVials(rack, vials);
        int moved = centrifuge.insertVialsFromRack(rack);
        int after = VialRackItem.getVials(rack).stream().mapToInt(ItemStack::getCount).sum()
                + centrifuge.getVialSlots().stream().mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(moved == 8 && after == 8,
                "Transferring eight samples must conserve eight vials; moved=" + moved + ", total=" + after);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "progression_audit", required = false)
    public static void auditLoomRequiresAnExplicitOutputChoice(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(0, 100, 0));
        helper.getLevel().setBlock(pos, BlockInit.somatic_loom.get().defaultBlockState(), 3);
        var loom = (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity) helper.getLevel().getBlockEntity(pos);
        loom.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        loom.addItem(null, new ItemStack(ItemInit.bleeding_bulb.get()), null);
        loom.addItem(null, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
        helper.assertTrue(!loom.hasValidRecipe(), "Ambiguous catalysts must wait for the player's output choice");
        ServerPlayer player = player(helper);
        loom.chooseNextRecipe(player);
        helper.assertTrue(loom.hasValidRecipe(), "An ordinary empty-hand choice must select an output");
        var selected = loom.getResultItem().copy();
        for (int i = 0; i < 19; i++) loom.addItem(null, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
        helper.assertTrue(ItemStack.isSameItemSameComponents(selected, loom.getResultItem()), "Stock cannot silently select another lesson");
        player.discard(); helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "progression_audit", required = false)
    public static void auditMonolithSurvivesTwoOrdinaryConversations(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(0, 100, 0));
        helper.getLevel().setBlock(pos, BlockInit.sanguine_monolith.get().defaultBlockState(), 3);
        ServerPlayer player = player(helper);
        HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(7);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        helper.getLevel().getBlockState(pos).useWithoutItem(helper.getLevel(), player, hit);
        helper.getLevel().getBlockState(pos).useWithoutItem(helper.getLevel(), player, hit);
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(BlockInit.sanguine_monolith.get()),
                "Merely reopening guidance twice must not destroy the monolith without choosing to press further");
        player.discard();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "progression_audit")
    public static void auditAlembicReagentControlThreeBatches(GameTestHelper helper) {
        GhastlyAlembicBlockEntity alembic = alembic(helper);
        for (int batch = 0; batch < 3; batch++) {
            alembic.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT, new ItemStack(BlockInit.devils_tooth.get()));
            for (int tick = 0; tick < 100; tick++) {
                GhastlyAlembicBlockEntity.serverTick(helper.getLevel(), alembic.getBlockPos(),
                        helper.getLevel().getBlockState(alembic.getBlockPos()), alembic);
            }
            ItemStack result = alembic.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT);
            helper.assertTrue(result.is(ItemInit.foul_paste.get()) && result.getCount() == 2 * (batch + 1),
                    "Five-second reagent control batch " + batch + " failed: " + result);
        }
        helper.assertTrue(alembic.getBloodVolume() == 300, "Three reagent batches must yield 300 stored blood");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "progression_audit", timeoutTicks = 25000)
    public static void auditAlembicGrowsCrystalUsingFourLeakPayments(GameTestHelper helper) {
        GhastlyAlembicBlockEntity alembic = alembic(helper);
        BlockPos crystalPos = alembic.getBlockPos().east();
        helper.getLevel().setBlock(crystalPos, Blocks.AIR.defaultBlockState(), 3);
        helper.getLevel().setBlock(crystalPos.below(), Blocks.BONE_BLOCK.defaultBlockState(), 3);
        HemoCapabilityAccess.getBloodVolume(alembic).orElseThrow().setBloodVolume(
                HemoServerConfig.ALEMBIC_LEAK_RATE_PER_TICK.get() * 4);
        helper.runAtTickTime(HemoServerConfig.ALEMBIC_LEAK_INTERVAL_TICKS.get() * 4 + 1,
                () -> assertCrystal(helper, alembic, crystalPos));
    }

    private static void assertCrystal(GameTestHelper helper, GhastlyAlembicBlockEntity alembic, BlockPos pos) {
        var state = helper.getLevel().getBlockState(pos);
        helper.assertTrue(state.is(BlockInit.blood_crystal.get()) && state.getValue(BloodCrystalBlock.AGE) == 3,
                "Bone substrate must grow a mature crystal after four successful leak payments");
        helper.assertTrue(alembic.getBloodVolume() == 0, "Crystal must consume exactly four configured leak payments");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "progression-audit"), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        return player;
    }

    private static GhastlyAlembicBlockEntity alembic(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(0, 100, 0));
        helper.getLevel().setBlock(pos.below(), Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        helper.getLevel().setBlock(pos, BlockInit.ghastly_alembic.get().defaultBlockState(), 3);
        GhastlyAlembicBlockEntity tile = (GhastlyAlembicBlockEntity) helper.getLevel().getBlockEntity(pos);
        tile.onLoad();
        HemoCapabilityAccess.getBloodVolume(tile).orElseThrow().setBloodVolume(0);
        return tile;
    }

    private static VialCentrifugeBlockEntity centrifuge(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(0, 100, 0));
        helper.getLevel().setBlock(pos, BlockInit.vial_centrifuge.get().defaultBlockState(), 3);
        return (VialCentrifugeBlockEntity) helper.getLevel().getBlockEntity(pos);
    }
}
