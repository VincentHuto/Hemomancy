package com.vincenthuto.hemomancy.gametest;

import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeableStation;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * L-7: block blood is saved only through the {@code block_blood_volume} attachment (white humor through
 * {@code block_white_humor_volume}). A save from before still carries the block entity's own copy under its old key,
 * which is read when present; a fresh save no longer writes it and reloads to the same volume.
 */
@GameTestHolder("item_inventory_validation")
@PrefixGameTestTemplate(false)
public final class BlockBloodPersistenceGameTests {
	private static final String ATTACHMENTS = "neoforge:attachments";
	private static final double LEGACY_VOLUME = 37.0D;

	private record Store(Supplier<? extends Block> block, String legacyKey, boolean whiteHumor) {
	}

	private static final List<Store> STORES = List.of(
			new Store(BlockInit.ghastly_alembic, "bloodLevel", false),
			new Store(BlockInit.morphling_incubator, "bloodLevel", false),
			new Store(BlockInit.mycelial_crucible, "bloodLevel", false),
			new Store(BlockInit.mycelial_lantern, "bloodLevel", false),
			new Store(BlockInit.pallid_retort, "whiteHumorLevel", true),
			new Store(BlockInit.hematic_armature, "BloodLevel", false),
			new Store(BlockInit.somatic_loom, "bloodLevel", false),
			new Store(BlockInit.vial_centrifuge, "bloodLevel", false),
			new Store(BlockInit.sanguine_vigil, "bloodLevel", false),
			new Store(BlockInit.gourdvine_tap, "bloodLevel", false),
			new Store(BlockInit.enzymatic_scriptorium, "BloodVolume", false),
			new Store(BlockInit.resonant_forge, "BloodVolume", false),
			new Store(BlockInit.sanguine_condenser, "BloodVolume", false));

	private BlockBloodPersistenceGameTests() {
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void bloodIsSavedOnlyThroughTheAttachmentAndOldCopiesStillLoad(GameTestHelper helper) {
		var registries = helper.getLevel().registryAccess();
		for (Store store : STORES) {
			String name = store.block().get().getDescriptionId();
			BlockEntity first = place(helper, store, new BlockPos(1, 2, 1));
			BlockEntity second = place(helper, store, new BlockPos(3, 2, 3));

			CompoundTag legacy = first.saveWithoutMetadata(registries);
			legacy.remove(ATTACHMENTS);
			legacy.putDouble(store.legacyKey(), LEGACY_VOLUME);
			first.loadWithComponents(legacy, registries);
			helper.assertTrue(volume(first, store) == LEGACY_VOLUME, name + " did not read its old " + store.legacyKey());

			CompoundTag saved = first.saveWithoutMetadata(registries);
			helper.assertFalse(saved.contains(store.legacyKey()), name + " still saves its own " + store.legacyKey());
			helper.assertTrue(saved.getCompound(ATTACHMENTS).contains(store.whiteHumor()
					? "hemomancy:block_white_humor_volume" : "hemomancy:block_blood_volume"), name + " saved no attachment");

			second.loadWithComponents(saved, registries);
			helper.assertTrue(volume(second, store) == LEGACY_VOLUME, name + " lost its volume across a save");
			helper.setBlock(new BlockPos(1, 2, 1), net.minecraft.world.level.block.Blocks.AIR);
			helper.setBlock(new BlockPos(3, 2, 3), net.minecraft.world.level.block.Blocks.AIR);
		}
		helper.succeed();
	}

	/** A station upgrade rite aborts when the subject changes; blood in the attachment must still count as a change. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void stationUpgradeSnapshotsStillCarryBlood(GameTestHelper helper) {
		var registries = helper.getLevel().registryAccess();
		List<Store> stations = List.of(
				new Store(BlockInit.ghastly_alembic, "BloodLevel", false),
				new Store(BlockInit.hematic_armature, "BloodLevel", false),
				new Store(BlockInit.somatic_loom, "bloodLevel", false),
				new Store(BlockInit.vial_centrifuge, "bloodLevel", false),
				new Store(BlockInit.enzymatic_scriptorium, "BloodVolume", false),
				new Store(BlockInit.resonant_forge, "BloodVolume", false),
				new Store(BlockInit.sanguine_condenser, "BloodVolume", false));
		BlockPos pos = new BlockPos(1, 2, 1);
		for (Store store : stations) {
			String name = store.block().get().getDescriptionId();
			BlockEntity entity = place(helper, store, pos);
			helper.assertTrue(entity instanceof UpgradeableStation, name + " is not an upgradeable station");
			UpgradeableStation station = (UpgradeableStation) entity;
			var blood = HemoCapabilityAccess.getBloodVolume(entity).orElseThrow();
			blood.setBloodVolume(12.0D);
			CompoundTag before = station.upgradeSnapshot(registries);
			helper.assertTrue(before.getDouble(store.legacyKey()) == 12.0D, name + " snapshot lost its blood");
			blood.setBloodVolume(25.0D);
			helper.assertFalse(before.equals(station.upgradeSnapshot(registries)), name + " snapshot ignores a blood change");
			helper.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR);
		}
		helper.succeed();
	}

	private static BlockEntity place(GameTestHelper helper, Store store, BlockPos pos) {
		helper.setBlock(pos, store.block().get());
		BlockEntity entity = helper.getBlockEntity(pos);
		helper.assertTrue(entity != null, "No block entity for " + store.block().get().getDescriptionId());
		return entity;
	}

	private static double volume(BlockEntity entity, Store store) {
		return store.whiteHumor()
				? HemoCapabilityAccess.getWhiteHumorVolume(entity).orElseThrow().getWhiteHumorVolume()
				: HemoCapabilityAccess.getBloodVolume(entity).orElseThrow().getBloodVolume();
	}
}
