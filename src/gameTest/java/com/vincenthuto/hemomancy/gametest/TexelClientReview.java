package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * With hemomancy.texelReview, lines up the models of texel batches T1 and T3 (09 T12, T14) on display entities: the
 * kitbashed held weapons, the regenerated instruments and set-piece items, the Living Staff heads and every fitting,
 * and the rescaled blocks. One wide shot per row and one close-up per model go to build/texel-review/screenshots.
 * Run with texelReviewClient. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class TexelClientReview {
	private static final List<String> HELD = List.of("living_baghnakh", "barbed_blade", "living_crossbow",
			"chitinite_mace", "hematic_microscope", "phlebotomists_field_case", "terrestrial_speculum");
	private static final List<String> INSTRUMENTS = List.of("living_syringe", "vial_rack", "wax_cylinder",
			"ambergris_cylinder", "corpse_wax_cylinder", "saint_sarcophagus", "pulse_pylon");
	private static final List<String> FITTINGS = List.of("worn_vow_fitting", "barbed_fitting", "chitinite_fitting",
			"prismatic_fitting", "crimson_vestment_fitting", "monolithic_frame_fitting", "assumed_limb_fitting");
	/** Staff heads 1 to 5 follow the first five Morphling strains; 0 is the bare staff. */
	private static final int STAFF_HEADS = 6;
	private static final double ITEM_SPACING = 1.25, BLOCK_SPACING = 1.6;
	/** Rows stand this far apart, each with a plain wall a block behind it, so a close-up never sees the next row. */
	private static final int ROW = 7;
	private static final int SHOT = 36, SETTLE = 26;
	private static final List<Shot> SHOTS = new ArrayList<>();
	private static int ticks;
	private static BlockPos origin;

	private record Shot(String name, double[] eye, double[] at, Runnable before) {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.texelReview")) return;
		var mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("texel-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		var server = mc.getSingleplayerServer();
		if (++ticks == 40) server.execute(() -> build(server, mc.player.getUUID()));
		if (ticks < 100 || origin == null) return;
		mc.options.hideGui = true;
		int t = ticks - 100, index = t / SHOT, local = t % SHOT;
		if (index >= SHOTS.size()) {
			if (local == 0) {
				Hemomancy.LOGGER.info("TEXEL_REVIEW captured {}", SHOTS.size());
				mc.stop();
			}
			return;
		}
		Shot shot = SHOTS.get(index);
		if (local == 0) {
			if (shot.before() != null) shot.before().run();
			look(server, mc, shot.eye(), shot.at());
		}
		if (local == SETTLE) {
			Screenshot.grab(mc.gameDirectory, "texel-" + String.format("%02d", index) + "-" + shot.name() + ".png",
					mc.getMainRenderTarget(), message -> {});
			Hemomancy.LOGGER.info("TEXEL_REVIEW captured {}", shot.name());
		}
	}

	private static void build(MinecraftServer server, java.util.UUID uuid) {
		var player = server.getPlayerList().getPlayer(uuid);
		var level = player.serverLevel();
		BlockPos base = player.blockPosition().offset(0, 0, 6);
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"time set 6000", "gamerule doDaylightCycle false", "weather clear",
				"gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		player.setGameMode(GameType.SPECTATOR);
		for (BlockPos pos : BlockPos.betweenClosed(base.offset(-12, -1, -8), base.offset(12, 8, ROW * 6 + 2)))
			level.setBlock(pos, pos.getY() < base.getY() ? Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		for (int row = 0; row < 6; row++)
			for (BlockPos pos : BlockPos.betweenClosed(base.offset(-11, 0, row * ROW + 2), base.offset(11, 3, row * ROW + 2)))
				level.setBlock(pos, Blocks.WHITE_CONCRETE.defaultBlockState(), 2);

		itemRow(level, base, 0, "held", HELD.stream().map(TexelClientReview::stack).toList(), HELD, 2.0F);
		itemRow(level, base, ROW, "instrument", INSTRUMENTS.stream().map(TexelClientReview::stack).toList(),
				INSTRUMENTS, 1.5F);
		List<ItemStack> staffs = new ArrayList<>();
		List<String> staffNames = new ArrayList<>();
		for (int i = 0; i < STAFF_HEADS; i++) {
			staffs.add(staff(level, i));
			staffNames.add(i == 0 ? "bare" : MorphlingStrains.ALL.get(i - 1).itemPath());
		}
		itemRow(level, base, ROW * 2, "staff", staffs, staffNames, 1.0F);
		double z = base.getZ() + .5 + ROW * 2, x = base.getX() + .5 - (STAFF_HEADS - 1) * ITEM_SPACING / 2;
		for (String fitting : FITTINGS) {
			SHOTS.add(new Shot("staff-" + fitting, new double[]{base.getX() + .5, base.getY() + 2.0, z - 4.6},
					new double[]{base.getX() + .5, base.getY() + 1.2, z}, () -> equipFitting(fitting)));
			SHOTS.add(new Shot("staff-" + fitting + "-close", new double[]{x, base.getY() + 1.7, z - 1.5},
					new double[]{x, base.getY() + 1.5, z}, null));
		}
		SHOTS.add(new Shot("staff-reset", new double[]{x, base.getY() + 1.7, z - 1.5},
				new double[]{x, base.getY() + 1.5, z}, () -> equipFitting(null)));

		blockRow(level, base, ROW * 3, "block", List.of(state("scrying_podium"), state("unstained_podium"),
				state("fungal_podium"), state("mortal_display"), state("rite_fragment_inscription"),
				state("semi_sentient_construct"), state("puffball_fungus")), List.of("scrying_podium",
				"unstained_podium", "fungal_podium", "mortal_display", "rite_fragment_inscription",
				"semi_sentient_construct", "puffball_fungus"));
		List<BlockState> growths = new ArrayList<>();
		List<String> growthNames = new ArrayList<>();
		for (int age = 0; age < 4; age++) {
			growths.add(state("blood_crystal", "facing", "up", "age", String.valueOf(age)));
			growthNames.add("blood_crystal_" + age);
		}
		for (int count = 1; count <= 4; count++) {
			growths.add(state("escharian_scyphus", "facing", "up", "count", String.valueOf(count)));
			growthNames.add("escharian_scyphus_" + count);
		}
		blockRow(level, base, ROW * 4, "growth", growths, growthNames);
		List<BlockState> effigies = new ArrayList<>();
		List<String> effigyNames = new ArrayList<>();
		for (int phase = 0; phase < 6; phase++) {
			effigies.add(state("vacant_effigy", "phase", String.valueOf(phase)));
			effigyNames.add("vacant_effigy_" + phase);
		}
		blockRow(level, base, ROW * 5, "effigy", effigies, effigyNames);
		// The Vial Centrifuge's flywheel is drawn by its block entity, so it stands as a real block.
		BlockPos centrifuge = base.offset(8, 0, ROW * 5);
		level.setBlock(centrifuge, state("vial_centrifuge"), 3);
		SHOTS.add(new Shot("vial_centrifuge", new double[]{centrifuge.getX() + 2.2, centrifuge.getY() + 1.6,
				centrifuge.getZ() - 1.6}, new double[]{centrifuge.getX() + .5, centrifuge.getY() + .5, centrifuge.getZ() + .5},
				null));
		origin = base;
		Hemomancy.LOGGER.info("TEXEL_REVIEW placed {} shots at {}", SHOTS.size(), base);
	}

	private static void itemRow(ServerLevel level, BlockPos base, int dz, String row, List<ItemStack> stacks,
			List<String> names, float scale) {
		double z = base.getZ() + .5 + dz, y = base.getY() + 1.0, spacing = ITEM_SPACING * Math.max(1, scale * .75);
		double first = base.getX() + .5 - (stacks.size() - 1) * spacing / 2;
		for (int i = 0; i < stacks.size(); i++) {
			var tag = new CompoundTag();
			tag.putString("id", "minecraft:item_display");
			tag.put("item", stacks.get(i).save(level.registryAccess()));
			tag.putString("item_display", "fixed");
			spawn(level, tag, first + i * spacing, y, z, scale, 0F, 180F);
		}
		SHOTS.add(new Shot(row, new double[]{base.getX() + .5, y + .8, z - 5.5}, new double[]{base.getX() + .5, y, z}, null));
		for (int i = 0; i < stacks.size(); i++) {
			double x = first + i * spacing;
			SHOTS.add(new Shot(row + "-" + names.get(i), new double[]{x + .3, y + .35, z - 1.2 * Math.max(1, scale * .8)},
					new double[]{x, y, z}, null));
		}
	}

	private static void blockRow(ServerLevel level, BlockPos base, int dz, String row, List<BlockState> states,
			List<String> names) {
		double z = base.getZ() + .5 + dz, y = base.getY();
		double first = base.getX() + .5 - (states.size() - 1) * BLOCK_SPACING / 2;
		for (int i = 0; i < states.size(); i++) {
			var tag = new CompoundTag();
			tag.putString("id", "minecraft:block_display");
			tag.put("block_state", NbtUtils.writeBlockState(states.get(i)));
			spawn(level, tag, first + i * BLOCK_SPACING, y, z, 1.0F, -.5F, 0F);
		}
		SHOTS.add(new Shot(row, new double[]{base.getX() + .5, y + 2.8, z - 4.3}, new double[]{base.getX() + .5, y + .5, z},
				null));
		for (int i = 0; i < states.size(); i++) {
			double x = first + i * BLOCK_SPACING;
			SHOTS.add(new Shot(row + "-" + names.get(i), new double[]{x + .7, y + 1.3, z - 1.5},
					new double[]{x, y + .35, z}, null));
		}
	}

	private static void spawn(ServerLevel level, CompoundTag tag, double x, double y, double z, float scale, float shift,
			float yaw) {
		var translation = floats(shift, 0, shift);
		var transformation = new CompoundTag();
		transformation.put("translation", translation);
		transformation.put("left_rotation", floats(0, 0, 0, 1));
		transformation.put("scale", floats(scale, scale, scale));
		transformation.put("right_rotation", floats(0, 0, 0, 1));
		tag.put("transformation", transformation);
		var entity = EntityType.loadEntityRecursive(tag, level, e -> {
			e.moveTo(x, y, z, yaw, 0F);
			return e;
		});
		if (entity != null) level.addFreshEntity(entity);
	}

	private static ListTag floats(float... values) {
		var list = new ListTag();
		for (float value : values) list.add(FloatTag.valueOf(value));
		return list;
	}

	private static ItemStack stack(String id) {
		return new ItemStack(BuiltInRegistries.ITEM.get(Hemomancy.rloc(id)));
	}

	/** A Living Staff whose first stored Morphling picks head {@code head}, as the staff_visual property reads it. */
	private static ItemStack staff(ServerLevel level, int head) {
		var staff = stack("living_staff");
		if (head == 0) return staff;
		var morphling = stack(MorphlingStrains.ALL.get(head - 1).itemPath());
		var items = new ListTag();
		items.add(morphling.save(level.registryAccess()));
		var inventory = new CompoundTag();
		inventory.put("Items", items);
		var data = new CompoundTag();
		data.put("Inventory", inventory);
		staff.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
		return staff;
	}

	/** The staff's fitting look follows the viewing player's fitting slot; this sets it on the client copy only. */
	private static void equipFitting(String fitting) {
		var player = Minecraft.getInstance().player;
		HemoCapabilityAccess.getEquipment(player).setStackInSlot(HarbingerEquipmentMenu.FITTING_SLOT_INDEX,
				fitting == null ? ItemStack.EMPTY : stack(fitting));
	}

	private static BlockState state(String id, String... properties) {
		BlockState state = BuiltInRegistries.BLOCK.get(Hemomancy.rloc(id)).defaultBlockState();
		for (int i = 0; i + 1 < properties.length; i += 2) {
			Property<?> property = state.getBlock().getStateDefinition().getProperty(properties[i]);
			if (property != null) state = with(state, property, properties[i + 1]);
		}
		return state;
	}

	private static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, String value) {
		return property.getValue(value).map(v -> state.setValue(property, v)).orElse(state);
	}

	private static void look(MinecraftServer server, Minecraft mc, double[] eye, double[] at) {
		var uuid = mc.player.getUUID();
		server.execute(() -> {
			var player = server.getPlayerList().getPlayer(uuid);
			float yaw = (float) Math.toDegrees(Math.atan2(-(at[0] - eye[0]), at[2] - eye[2]));
			float pitch = (float) -Math.toDegrees(Math.atan2(at[1] - eye[1], Math.hypot(at[0] - eye[0], at[2] - eye[2])));
			player.teleportTo(player.serverLevel(), eye[0], eye[1] - player.getEyeHeight(), eye[2], yaw, pitch);
		});
	}
}
