package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeStartupResult;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Supplied materials exercise the normal transfer, startup, processing and save paths. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MaterialAcceptanceGameTests {
    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "material_acceptance")
    public static void blockedCentrifugeCompletionCannotProduceFreeBloodOrPowder(GameTestHelper helper) {
        var machine = centrifuge(helper, 0);
        var rack = rack("minecraft:cow", 2);
        machine.insertVialsFromRack(rack);
        machine.setItem(6, machine.getItem(3));
        machine.setItem(3, ItemStack.EMPTY);
        helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "Balanced samples must start");
        // A player can fill the outputs while the rotor is running.
        machine.setItem(10, new ItemStack(ItemInit.vivacious_enzyme.get(), 64));
        machine.setItem(14, new ItemStack(ItemInit.vivacious_enzyme.get(), 64));
        tick(helper, machine, 200);
        helper.assertTrue(machine.getBloodVolume() == 0 && machine.getItem(18).isEmpty(),
                "A blocked batch cannot produce blood or powder without consuming samples");
        helper.assertTrue(BloodVialItem.getEntityType(machine.getItem(2)) == net.minecraft.world.entity.EntityType.COW
                && BloodVialItem.getEntityType(machine.getItem(6)) == net.minecraft.world.entity.EntityType.COW,
                "Blocked completion must retain both paid samples");
        machine.setItem(10, ItemStack.EMPTY);
        machine.setItem(14, ItemStack.EMPTY);
        helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "Clearing outputs must allow a normal retry");
        tick(helper, machine, 200);
        helper.assertTrue(machine.getBloodVolume() == 250
                && BloodVialItem.getEntityType(machine.getItem(2)) == null
                && BloodVialItem.getEntityType(machine.getItem(6)) == null,
                "Retry must consume both samples and award exactly one batch of blood");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "material_acceptance")
    public static void alembicFlaskBoundariesConserveBloodAndContainersAcrossReload(GameTestHelper helper) {
        int fixture = 0;
        for (int amount : new int[] {1999, 2000, 2499, 2500, 5000}) {
            var machine = alembic(helper, fixture++);
            var blood = HemoCapabilityAccess.getBloodVolume(machine).orElseThrow();
            blood.setBloodVolume(amount);
            machine.setItem(GhastlyAlembicBlockEntity.SLOT_FLASK, new ItemStack(HLItemInit.cured_clay_flask.get()));
            var saved = machine.saveWithoutMetadata(helper.getLevel().registryAccess());
            machine.loadWithComponents(saved, helper.getLevel().registryAccess());
            helper.assertTrue(machine.getBloodVolume() == amount && machine.getMaxBloodVolume() == 5000,
                    "Reload must preserve the exact stored amount and full-jug capacity");
            tickAlembic(helper, machine, 1);
            boolean filled = amount >= 2500;
            helper.assertTrue(machine.getBloodVolume() == amount - (filled ? 2500 : 0), "Flask transfer at " + amount + " must debit exactly 2500 or nothing");
            helper.assertTrue(filled ? machine.getItem(GhastlyAlembicBlockEntity.SLOT_FLASK).isEmpty()
                            && machine.getItem(GhastlyAlembicBlockEntity.SLOT_FLASK_OUTPUT).is(ItemInit.bloody_flask.get())
                            : machine.getItem(GhastlyAlembicBlockEntity.SLOT_FLASK).is(HLItemInit.cured_clay_flask.get())
                            && machine.getItem(GhastlyAlembicBlockEntity.SLOT_FLASK_OUTPUT).isEmpty(),
                    "Bottling must exchange exactly one physical flask only when paid");
        }
        for (int amount : new int[] {1999, 2000, 2499, 2500, 2501, 5000}) {
            var machine = alembic(helper, fixture++);
            HemoCapabilityAccess.getBloodVolume(machine).orElseThrow().setBloodVolume(amount);
            machine.setItem(GhastlyAlembicBlockEntity.SLOT_FLASK, new ItemStack(ItemInit.bloody_flask.get()));
            tickAlembic(helper, machine, 1);
            boolean fits = amount <= 2500;
            helper.assertTrue(machine.getBloodVolume() == amount + (fits ? 2500 : 0), "Incoming flask must never overflow at " + amount);
            helper.assertTrue(fits ? machine.getItem(GhastlyAlembicBlockEntity.SLOT_FLASK).isEmpty()
                            && machine.getItem(GhastlyAlembicBlockEntity.SLOT_FLASK_OUTPUT).is(HLItemInit.cured_clay_flask.get())
                            : machine.getItem(GhastlyAlembicBlockEntity.SLOT_FLASK).is(ItemInit.bloody_flask.get())
                            && machine.getItem(GhastlyAlembicBlockEntity.SLOT_FLASK_OUTPUT).isEmpty(),
                    "Rejected incoming transfer must retain its full flask");
        }
        var blocked = alembic(helper, fixture);
        HemoCapabilityAccess.getBloodVolume(blocked).orElseThrow().setBloodVolume(2500);
        blocked.setItem(GhastlyAlembicBlockEntity.SLOT_FLASK, new ItemStack(HLItemInit.cured_clay_flask.get()));
        blocked.setItem(GhastlyAlembicBlockEntity.SLOT_FLASK_OUTPUT, new ItemStack(ItemInit.bloody_flask.get(), 16));
        tickAlembic(helper, blocked, 1);
        helper.assertTrue(blocked.getBloodVolume() == 2500 && blocked.getItem(GhastlyAlembicBlockEntity.SLOT_FLASK).getCount() == 1
                && blocked.getItem(GhastlyAlembicBlockEntity.SLOT_FLASK_OUTPUT).getCount() == 16, "Full bottling output must preserve blood and both containers");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "material_acceptance")
    public static void alembicReportsStoppedReasonsAndPreservesBothRecipeModes(GameTestHelper helper) {
        var machine = alembic(helper, 0);
        machine.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT, new ItemStack(BlockInit.devils_tooth.get()));
        tickAlembic(helper, machine, 1);
        helper.assertTrue(machine.getProcessingStatus() == GhastlyAlembicBlockEntity.Status.NO_HEAT, "Unheated input must report missing heat");
        helper.getLevel().setBlock(machine.getBlockPos().below(), net.minecraft.world.level.block.Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        tickAlembic(helper, machine, 1);
        helper.assertTrue(machine.getProcessingStatus() == GhastlyAlembicBlockEntity.Status.DISTILLING, "Valid heated paste must report distilling");
        machine.setItem(GhastlyAlembicBlockEntity.SLOT_RESULT, new ItemStack(ItemInit.foul_paste.get(), 64));
        tickAlembic(helper, machine, 1);
        helper.assertTrue(machine.getProcessingStatus() == GhastlyAlembicBlockEntity.Status.OUTPUT_BLOCKED, "Full product slot must report its blockage");
        machine.setItem(GhastlyAlembicBlockEntity.SLOT_RESULT, ItemStack.EMPTY);
        HemoCapabilityAccess.getBloodVolume(machine).orElseThrow().setBloodVolume(5000);
        tickAlembic(helper, machine, 1);
        helper.assertTrue(machine.getProcessingStatus() == GhastlyAlembicBlockEntity.Status.TANK_FULL, "Full reservoir must stop byproduct cooking with a reason");
        HemoCapabilityAccess.getBloodVolume(machine).orElseThrow().setBloodVolume(0);
        tickAlembic(helper, machine, 100);
        helper.assertTrue(machine.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT).is(ItemInit.foul_paste.get())
                && machine.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT).getCount() == 2 && machine.getBloodVolume() == 100,
                "Normal paste must still produce two paste and 100 reservoir ml");
        machine.setItem(GhastlyAlembicBlockEntity.SLOT_RESULT, ItemStack.EMPTY);
        machine.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT, new ItemStack(ItemInit.sanguine_formation.get()));
        tickAlembic(helper, machine, 1);
        helper.assertTrue(machine.getProcessingStatus() == GhastlyAlembicBlockEntity.Status.MISSING_INGREDIENTS, "Tincture without catalyst and blood input must explain missing ingredients");
        machine.setItem(GhastlyAlembicBlockEntity.SLOT_CATALYST, new ItemStack(ItemInit.fervent_enzyme.get()));
        machine.setItem(GhastlyAlembicBlockEntity.SLOT_TINCTURE_BLOOD, new ItemStack(ItemInit.bloody_flask.get()));
        tickAlembic(helper, machine, 200);
        helper.assertTrue(machine.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT).is(ItemInit.tincture_sanguine_fists.get())
                && machine.getItem(GhastlyAlembicBlockEntity.SLOT_CATALYST).isEmpty()
                && machine.getItem(GhastlyAlembicBlockEntity.SLOT_TINCTURE_BLOOD).isEmpty() && machine.getBloodVolume() == 100,
                "Sanguine Fists must consume its authored reagents without adding free reservoir blood");
        helper.succeed();
    }

    private static GhastlyAlembicBlockEntity alembic(GameTestHelper helper, int index) {
        var pos = helper.absolutePos(new BlockPos(index * 2, 40, 0));
        helper.getLevel().setBlock(pos, BlockInit.ghastly_alembic.get().defaultBlockState(), 3);
        var machine = (GhastlyAlembicBlockEntity) helper.getLevel().getBlockEntity(pos);
        machine.onLoad();
        HemoCapabilityAccess.getBloodVolume(machine).orElseThrow().setBloodVolume(0);
        return machine;
    }

    private static void tickAlembic(GameTestHelper helper, GhastlyAlembicBlockEntity machine, int ticks) {
        for (int i = 0; i < ticks; i++) GhastlyAlembicBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), helper.getLevel().getBlockState(machine.getBlockPos()), machine);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "material_acceptance")
    public static void cowAndGoatBatchesConserveContainersThroughReload(GameTestHelper helper) {
        int fixture = 0;
        for (String species : new String[] {"minecraft:cow", "minecraft:goat"}) {
            for (int samples : new int[] {2, 8}) {
                var machine = centrifuge(helper, fixture++);
                var rack = rack(species, samples);
                helper.assertTrue(machine.insertVialsFromRack(rack) == samples, "Every sampled vial must transfer once");
                if (samples == 2) {
                    machine.setItem(6, machine.getItem(3));
                    machine.setItem(3, ItemStack.EMPTY); // Ordinary opposing-slot placement, supplied test input.
                }
                int containers = countContainers(machine, rack);
                helper.assertTrue(containers == 8, "Rack plus centrifuge must retain eight physical containers");
                helper.assertTrue(machine.insertVialsFromRack(rack) == 0 && countContainers(machine, rack) == containers,
                        "Repeated insertion must not manufacture vials");
                helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS,
                        "Balanced " + species + " batch of " + samples + " must start");
                tick(helper, machine, 100);
                var saved = machine.saveWithoutMetadata(helper.getLevel().registryAccess());
                machine.loadWithComponents(saved, helper.getLevel().registryAccess());
                tick(helper, machine, 99);
                helper.assertTrue(machine.isSpinning() && machine.getOutputSlots().stream().allMatch(ItemStack::isEmpty),
                        "Reload must retain the unfinished 200-tick process without early output");
                tick(helper, machine, 1);
                helper.assertTrue(!machine.isSpinning() && machine.getBloodVolume() == 250,
                        "A completed batch contributes exactly the authored 250 ml in 200 processing ticks");
                var enzyme = species.equals("minecraft:cow") ? ItemInit.vivacious_enzyme.get() : ItemInit.neurotic_enzyme.get();
                int produced = 0;
                for (var output : machine.getOutputSlots()) {
                    if (output.isEmpty()) continue;
                    helper.assertTrue(output.is(enzyme) && output.getCount() >= 1 && output.getCount() <= 4,
                            "Each ten-health sample must yield one to four of its species enzyme");
                    produced++;
                }
                helper.assertTrue(produced == samples && countContainers(machine, rack) == 8,
                        "Every processed sample returns its single empty vial and one enzyme output");
                helper.assertTrue(machine.getVialSlots().stream().allMatch(v -> v.isEmpty() || VialRackItem.isEmptyVial(v)),
                        "Processed vials must no longer contain a sample");
                helper.assertTrue(machine.getItem(18).isEmpty() || machine.getItem(18).getCount() <= samples,
                        "Auxiliary powder remains bounded by one independent chance per sample");
            }
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "material_acceptance")
    public static void partiallyFilledCentrifugeLeavesRejectedRackEntriesIntact(GameTestHelper helper) {
        var machine = centrifuge(helper, 0);
        for (int i = 2; i < 8; i++) machine.setItem(i, sample("minecraft:cow"));
        var rack = rack("minecraft:goat", 8);
        helper.assertTrue(machine.insertVialsFromRack(rack) == 2 && countContainers(machine, rack) == 14,
                "Two vacancies accept exactly two of the eight new physical vials");
        var rackBefore = rack.copy();
        var machineBefore = machine.saveWithoutMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(machine.insertVialsFromRack(rack) == 0 && ItemStack.isSameItemSameComponents(rack, rackBefore)
                        && machineBefore.getList("Items", 10).equals(machine.saveWithoutMetadata(helper.getLevel().registryAccess()).getList("Items", 10)),
                "A full machine must reject repeated transfer without changing either inventory");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "material_acceptance")
    public static void syringeDropSurvivesFullInventoryAndReloadThenPicksUpOnce(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        for (int i = 0; i < 36; i++) player.getInventory().setItem(i, new ItemStack(net.minecraft.world.item.Items.BARRIER, 64));
        var pos = helper.absolutePos(new BlockPos(2, 30, 2));
        var drop = new net.minecraft.world.entity.item.ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ItemInit.living_syringe.get()));
        drop.setNoPickUpDelay(); // Only the normal pickup delay is skipped in this inventory contract fixture.
        helper.assertTrue(drop.lifespan == 6000, "Syringe drops must use the bounded five-minute lifespan");
        drop.playerTouch(player);
        helper.assertTrue(!drop.isRemoved() && drop.getItem().getCount() == 1, "Full inventory must leave the actual syringe recoverable");
        var saved = new CompoundTag();
        drop.saveWithoutId(saved);
        var restored = new net.minecraft.world.entity.item.ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), ItemStack.EMPTY);
        restored.load(saved);
        drop.discard();
        helper.assertTrue(restored.lifespan == 6000 && restored.getItem().is(ItemInit.living_syringe.get()), "Reload must retain the actual syringe and its expiry");
        player.getInventory().setItem(0, ItemStack.EMPTY);
        restored.playerTouch(player);
        helper.assertTrue(restored.isRemoved() && player.getInventory().countItem(ItemInit.living_syringe.get()) == 1,
                "Freeing one slot must collect exactly one syringe");
        var expiring = new net.minecraft.world.entity.item.ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ItemInit.living_syringe.get()));
        var expiryState = new CompoundTag();
        expiring.saveWithoutId(expiryState);
        expiryState.putShort("Age", (short) 5999); // Accelerated waiting only; the normal entity tick performs expiry.
        expiring.load(expiryState);
        expiring.tick();
        helper.assertTrue(expiring.isRemoved(), "An uncollected syringe must still despawn at its bounded lifespan");
        player.discard();
        helper.succeed();
    }

    private static VialCentrifugeBlockEntity centrifuge(GameTestHelper helper, int index) {
        var pos = helper.absolutePos(new BlockPos(index * 2, 30, 0));
        helper.getLevel().setBlock(pos, BlockInit.vial_centrifuge.get().defaultBlockState(), 3);
        return (VialCentrifugeBlockEntity) helper.getLevel().getBlockEntity(pos);
    }

    private static ItemStack sample(String species) {
        var vial = new ItemStack(ItemInit.bloody_vial.get());
        var tag = new CompoundTag();
        tag.putString(BloodVialItem.TAG_ENTITY_TYPE, species);
        tag.putBoolean(BloodVialItem.TAG_STATE, true);
        vial.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return vial;
    }

    private static ItemStack rack(String species, int samples) {
        var rack = new ItemStack(ItemInit.vial_rack.get());
        var vials = VialRackItem.getVials(rack);
        for (int i = 0; i < samples; i++) vials.set(i, sample(species));
        VialRackItem.setVials(rack, vials);
        return rack;
    }

    private static int countContainers(VialCentrifugeBlockEntity machine, ItemStack rack) {
        return VialRackItem.getVials(rack).stream().mapToInt(ItemStack::getCount).sum()
                + machine.getVialSlots().stream().mapToInt(ItemStack::getCount).sum();
    }

    private static void tick(GameTestHelper helper, VialCentrifugeBlockEntity machine, int count) {
        for (int i = 0; i < count; i++) VialCentrifugeBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(),
                helper.getLevel().getBlockState(machine.getBlockPos()), machine);
    }
}
