package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.MycelialLanternBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.BloodGutterBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.VenousBasinBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.CatalystCropBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalStrainBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalStrainRules;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalStrains;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialLanternBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.VenousBasinBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.plant.PlantingBasinBlockEntity;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGrowthRules;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswer;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswerRules;
import com.vincenthuto.hemomancy.common.world.chum.ChumPools;
import com.vincenthuto.hemomancy.common.world.farm.ScabDrip;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * With hemomancy.farmingReview, stages core-loop Track 7 in a closed room of a disposable world, captures it into
 * build/farming-client-review/screenshots and stops. A real survival player: a ceiling of scab spikes dripping on a
 * wheat field from the basins above it; planting basins, dry and fed by a worked gutter; the Mycelial Lantern's cloche
 * growing wheat and carrots under its bell; Vital Effusion's Sculpting held on a gourd stem; and chum blooming in a pond
 * while the player pours blood into it. Round two (P7) adds the strain beds on their three woods, a close look at the
 * flesh and Brined strains, glowroot and emberpod in basins and under a cloche, a floor spike under a dripping spike, and
 * a Brined Votary rising in a strong pool's bloom. Run with farmingReviewClient and drop
 * {"op":"create","name":"FarmReview","seed":"1"} into operator/inbox after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class FarmingClientReview {
	private static final String PROPERTY = "hemomancy.farmingReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 100;
	private static final int PRESS_TICK = 12;
	private static int ticks;
	private static int shot;
	private static int captured;
	private static BlockPos row;
	private static final List<BlockPos> TIPS = new ArrayList<>();

	/**
	 * One staged moment. Offsets are from the row (the player's feet at setup; the floor is one below). {@code use}
	 * holds the use key from {@link #PRESS_TICK} to {@code release}; {@code scene} restages the room (server side);
	 * {@code during} runs on the server every tick of the shot, or is null.
	 */
	private record Shot(String name, String manipulation, Supplier<Item> held, Vec3 stand, Vec3 aim, boolean use, int release,
			int[] captures, BiConsumer<ServerLevel, ServerPlayer> scene, BiConsumer<ServerLevel, Integer> during) {
	}

	private static final List<Shot> SHOTS = List.of(
			new Shot("drip-field", null, () -> Items.AIR, new Vec3(0.5, 0, -1.5), new Vec3(0.5, 1.6, 5.0), false, 0,
					new int[]{40, 70}, FarmingClientReview::dripField, FarmingClientReview::drips),
			new Shot("planting-basins", null, () -> Items.AIR, new Vec3(0.5, 1, 1.2), new Vec3(0.0, 0.6, 4.5), false, 0,
					new int[]{50}, FarmingClientReview::plantingBasins, null),
			new Shot("cloche", null, () -> Items.AIR, new Vec3(1.5, 0, 0.9), new Vec3(1.5, 0.75, 3.5), false, 0,
					new int[]{30}, FarmingClientReview::cloches, null),
			new Shot("vital-effusion", "vital_effusion", ItemInit.blood_projection::get, new Vec3(0.5, 0, 1.2),
					new Vec3(0.5, 0.06, 3.5), true, 70, new int[]{30, 64}, FarmingClientReview::gourd, null),
			new Shot("chum", null, ItemInit.blood_projection::get, new Vec3(0.5, 0, 1.5), new Vec3(0.5, -0.12, 5.5), true, 80,
					new int[]{50, 92}, FarmingClientReview::pond, null),
			// Round two, P7: strain beds, the catalyst crops, a floor spike under a drip, and a hunter answering a pool.
			new Shot("strain-bed", null, () -> Items.AIR, new Vec3(0.5, 0, -1.5), new Vec3(0.5, -0.4, 4.5), false, 0,
					new int[]{40}, FarmingClientReview::strainBed, null),
			new Shot("strain-close", null, () -> Items.AIR, new Vec3(0.5, 0, 1.9), new Vec3(0.5, 0.25, 4.6), false, 0,
					new int[]{40}, FarmingClientReview::strainClose, null),
			new Shot("catalyst-crops", null, () -> Items.AIR, new Vec3(0.5, 0, 0.9), new Vec3(0.5, 0.2, 4.5), false, 0,
					new int[]{40}, FarmingClientReview::catalystCrops, null),
			new Shot("floor-spike", null, () -> Items.AIR, new Vec3(0.5, 0, -2.6), new Vec3(0.5, 2.0, 4.5), false, 0,
					new int[]{50, 78}, FarmingClientReview::floorSpike, FarmingClientReview::floorDrips),
			new Shot("chum-hunter", null, () -> Items.AIR, new Vec3(0.5, 0, 1.2), new Vec3(0.5, -0.6, 6.0), false, 0,
					new int[]{21, 30, 48}, FarmingClientReview::huntedPond, FarmingClientReview::drawVotary));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("farming-client-review")) return;
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
		mc.getToasts().clear();
		MinecraftServer server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (++ticks == SETUP_TICK) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), server));
		if (ticks < FIRST_SHOT_TICK || row == null) return;
		int local = (ticks - FIRST_SHOT_TICK) % SHOT_TICKS;
		if (local == 0) {
			if (shot == SHOTS.size()) {
				Hemomancy.LOGGER.info("FARMING_REVIEW captured {}", captured);
				mc.stop();
				return;
			}
			Shot current = SHOTS.get(shot);
			mc.options.setCameraType(CameraType.FIRST_PERSON);
			mc.options.hideGui = false;
			server.execute(() -> pose(server.getPlayerList().getPlayer(uuid), current));
		}
		if (shot >= SHOTS.size()) return;
		Shot current = SHOTS.get(shot);
		if (current.during() != null) {
			int at = local;
			server.execute(() -> current.during().accept(server.getPlayerList().getPlayer(uuid).serverLevel(), at));
		}
		if (local == PRESS_TICK && current.use()) {
			KeyMapping.click(mc.options.keyUse.getKey());
			KeyMapping.set(mc.options.keyUse.getKey(), true);
		}
		if (local == current.release() && current.use()) KeyMapping.set(mc.options.keyUse.getKey(), false);
		for (int at : current.captures()) {
			if (local != at) continue;
			Screenshot.grab(mc.gameDirectory, "farming-" + shot + "-" + current.name() + "-" + at + ".png",
					mc.getMainRenderTarget(), message -> {});
			captured++;
			Hemomancy.LOGGER.info("FARMING_REVIEW shot {} {} at {} using={}", shot, current.name(), at, mc.player.isUsingItem());
		}
		if (local == SHOT_TICKS - 1) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			shot++;
		}
	}

	/** A closed room of deepslate, the player's blood, Vital Effusion equipped, the Third Degree. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 18000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false", "gamerule randomTickSpeed 0"})
			server.getCommands().performPrefixedCommand(source, cmd);
		ServerLevel level = player.serverLevel();
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-12, -5, -4), row.offset(14, 8, 14))) {
			boolean shell = pos.getY() <= row.getY() - 1 || pos.getY() == row.getY() + 8 || pos.getX() == row.getX() - 12
					|| pos.getX() == row.getX() + 14 || pos.getZ() == row.getZ() - 4 || pos.getZ() == row.getZ() + 14;
			level.setBlock(pos, !shell ? Blocks.AIR.defaultBlockState() : pos.getY() <= row.getY() - 1
					? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
		}
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(4500.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		temperament.setTemperAlignment(EnumBloodTemper.ANIMUS, 30.0F);
		BloodTemperamentEvents.syncTemperament(player, temperament);
		TemperFixtures.carry(player);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(3);
		player.getInventory().clearContent();
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.vital_effusion.get(), new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(List.of("vital_effusion")));
		KnownManipulationEvents.syncPlayerEvent(player);
		Hemomancy.LOGGER.info("FARMING_REVIEW staged at {}", row);
	}

	/** Selects, arms, places and aims the player for one shot, and restages its scene. */
	private static void pose(ServerPlayer player, Shot shot) {
		ServerLevel level = player.serverLevel();
		shot.scene().accept(level, player);
		// Crops popped by the restaging fall as items; the shot should not show them.
		level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new AABB(row).inflate(16.0D)).forEach(Entity::discard);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		// Nothing selected means nothing equipped, so the client's selection cannot fall back to a memory.
		known.setEquippedManipNames(shot.manipulation() == null ? new ArrayList<>() : new ArrayList<>(List.of(shot.manipulation())));
		known.setSelectedManip(shot.manipulation() == null ? BloodManipulation.BLANK : ManipulationInit.getByName(shot.manipulation()));
		known.setActiveVerb(Verb.PROJECT);
		KnownManipulationEvents.syncPlayerEvent(player);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(4500.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false));
		player.setHealth(player.getMaxHealth());
		player.getInventory().selected = 0;
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(shot.held().get()));
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		Vec3 eye = at(shot.stand()).add(0.0D, 1.62D, 0.0D);
		Vec3 target = at(shot.aim());
		double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
		if (shot.stand().y > 0.0D) level.setBlock(BlockPos.containing(at(shot.stand())).below(), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
		player.teleportTo(level, eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
	}

	// ── Scenes ──

	private static void clearRoom(ServerLevel level) {
		TIPS.clear();
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-11, 0, -3), row.offset(13, 7, 13))) {
			if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), QUIET);
		}
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-11, -4, -3), row.offset(13, -1, 13))) {
			level.setBlock(pos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), QUIET);
		}
		level.getEntitiesOfClass(Entity.class, new AABB(row).inflate(16.0D), entity -> !(entity instanceof ServerPlayer))
				.forEach(Entity::discard);
	}

	/** Invisible light over a scene, so crops stand (they need light 8) and read as in a lit farm. */
	private static void lights(ServerLevel level, int... xz) {
		for (int i = 0; i + 1 < xz.length; i += 2) level.setBlock(cell(xz[i], 2, xz[i + 1]), Blocks.LIGHT.defaultBlockState(), 3);
	}

	private static BlockPos cell(int x, int y, int z) {
		return row.offset(x, y, z);
	}

	/** Set without telling neighbours: a crop placed in the dark before the light spreads would otherwise pop. */
	private static final int QUIET = 2 | 16;

	private static BlockState farmland() {
		return Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE);
	}

	private static BlockState wheat(int age) {
		return ((CropBlock) Blocks.WHEAT).getStateForAge(age);
	}

	/** A field of wheat under a low ceiling hung with scab spikes, worked basins of blood standing on the ceiling. */
	private static void dripField(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		lights(level, -5, 3, 5, 3, -5, 7, 5, 7, 0, 1, 0, 9);
		int[] ages = {3, 5, 4, 6, 4, 7, 5};
		for (int x = -3; x <= 3; x++) {
			for (int z = 3; z <= 7; z++) {
				level.setBlock(cell(x, -1, z), farmland(), QUIET);
				level.setBlock(cell(x, 0, z), wheat(ages[Math.floorMod(x * 3 + z, ages.length)]), QUIET);
			}
		}
		for (int x = -4; x <= 4; x++) {
			for (int z = 2; z <= 8; z++) level.setBlock(cell(x, 4, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
		}
		int[][] spikes = {{-2, 4}, {0, 6}, {2, 4}, {-1, 7}, {1, 3}, {3, 6}, {-3, 6}};
		for (int[] spike : spikes) {
			BlockPos basin = cell(spike[0], 5, spike[1]);
			level.setBlock(basin, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.WORKED, true), 3);
			((VenousBasinBlockEntity) level.getBlockEntity(basin)).configure().fill(1800.0D);
			boolean longer = (spike[0] + spike[1]) % 2 == 0;
			BlockPos tip = cell(spike[0], longer ? 2 : 3, spike[1]);
			if (longer) level.setBlock(cell(spike[0], 3, spike[1]), spike(DripstoneThickness.FRUSTUM), 2);
			level.setBlock(tip, spike(DripstoneThickness.TIP), 2);
			TIPS.add(tip.immutable());
		}
	}

	private static BlockState spike(DripstoneThickness thickness) {
		return BlockInit.pointed_blood_scorched_scab.get().defaultBlockState()
				.setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN).setValue(PointedDripstoneBlock.THICKNESS, thickness);
	}

	/** The spikes drip in turn, so a shot catches drops in the air. */
	private static void drips(ServerLevel level, int local) {
		if (TIPS.isEmpty() || local % 3 != 0) return;
		BlockPos tip = TIPS.get((local / 3) % TIPS.size());
		BlockState state = level.getBlockState(tip);
		if (ScabDrip.isHangingTip(state)) ScabDrip.drip(level, tip, state, level.random);
	}

	/** Three planting basins: two fed by a worked gutter (gourd, wheat), one dry; and a worked basin pouring. */
	private static void plantingBasins(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		lights(level, -4, 2, 4, 2);
		for (int x = -3; x <= 1; x++) {
			level.setBlock(cell(x, -1, 5), BlockInit.blood_gutter.get().defaultBlockState().setValue(BloodGutterBlock.WORKED, true)
					.setValue(BloodGutterBlock.EAST, x < 1).setValue(BloodGutterBlock.WEST, x > -3), 3);
		}
		BlockPos spout = cell(-4, 0, 5);
		level.setBlock(spout, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.WORKED, true)
				.setValue(VenousBasinBlock.FACING, Direction.EAST), 3);
		((VenousBasinBlockEntity) level.getBlockEntity(spout)).configure().fill(1800.0D);
		int[] xs = {-2, 0, 2};
		for (int i = 0; i < xs.length; i++) {
			BlockPos bed = cell(xs[i], 0, 4);
			level.setBlock(bed, BlockInit.planting_basin.get().defaultBlockState(), 3);
			PlantingBasinBlockEntity basin = (PlantingBasinBlockEntity) level.getBlockEntity(bed);
			if (i < 2) basin.configure().fill(300.0D);
		}
		level.setBlock(cell(-2, 1, 4), BlockInit.gourd_stem.get().defaultBlockState().setValue(StemBlock.AGE, 6), QUIET);
		level.setBlock(cell(0, 1, 4), wheat(6), QUIET);
		level.setBlock(cell(2, 1, 4), wheat(1), QUIET);
	}

	/** Two cloches: wheat near harvest, carrots half grown. */
	private static void cloches(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		cloche(level, cell(1, 0, 3), new ItemStack(Items.WHEAT_SEEDS), 6);
		cloche(level, cell(3, 0, 3), new ItemStack(Items.CARROT), 3);
	}

	private static void cloche(ServerLevel level, BlockPos pos, ItemStack seed, int stages) {
		level.setBlock(pos, BlockInit.mycelial_lantern.get().defaultBlockState().setValue(MycelialLanternBlock.FACING, Direction.NORTH), 3);
		MycelialLanternBlockEntity cloche = (MycelialLanternBlockEntity) level.getBlockEntity(pos);
		ReservoirBlood.settled(cloche).setMaxBloodVolume(1200.0D);
		ReservoirBlood.settled(cloche).fill(1000.0D);
		cloche.setItem(MycelialLanternBlockEntity.SLOT_CULTURE, seed);
		for (int i = 0; i < stages * BloodFedGrowthRules.CLOCHE_STAGE_TICKS; i++) {
			MycelialLanternBlockEntity.serverTick(level, pos, level.getBlockState(pos), cloche);
		}
		cloche.sendUpdates();
	}

	/** A young gourd stem on dry farmland, for Vital Effusion. */
	private static void gourd(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		lights(level, -3, 2, 3, 2);
		for (int x = -1; x <= 1; x++) level.setBlock(cell(x, -1, 3), farmland(), QUIET);
		level.setBlock(cell(0, 0, 3), BlockInit.gourd_stem.get().defaultBlockState(), QUIET);
		level.setBlock(cell(-1, 0, 3), wheat(1), QUIET);
		level.setBlock(cell(1, 0, 3), wheat(2), QUIET);
	}

	/** A pond of still water three deep, sunk in the floor. */
	private static void pond(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		for (int x = -4; x <= 4; x++) {
			for (int z = 3; z <= 10; z++) {
				for (int y = -3; y <= -1; y++) level.setBlock(cell(x, y, z), Blocks.WATER.defaultBlockState(), 2);
			}
		}
	}

	// ── Strain beds (core-loop round two, P7) ──

	private static final List<FungalStrains> FLESH = List.of(FungalStrains.VIVACIOUS, FungalStrains.FERVENT, FungalStrains.RUINOUS);
	private static final List<FungalStrains> BRINED = List.of(FungalStrains.NEUROTIC, FungalStrains.FRIGID, FungalStrains.FERRIC);
	private static final List<FungalStrains> COMMON = List.of(FungalStrains.INCANDESCENT, FungalStrains.UMBRAL);

	private static BlockState strain(FungalStrains strain, int age) {
		return BlockInit.strain(strain).get().defaultBlockState().setValue(FungalStrainBlock.AGE, age);
	}

	private static BlockState substrate(FungalStrains.Substrate group, int i) {
		return switch (group) {
			case FLESH -> BlockInit.ravenous_heartwood.get().defaultBlockState();
			case BRINED -> BlockInit.salt_stained_timber.get().defaultBlockState();
			case COMMON -> i % 2 == 0 ? Blocks.PODZOL.defaultBlockState() : Blocks.MYCELIUM.defaultBlockState();
		};
	}

	private static void plantingBasin(ServerLevel level, BlockPos pos, double blood) {
		level.setBlock(pos, BlockInit.planting_basin.get().defaultBlockState(), 3);
		if (blood > 0.0D) ((PlantingBasinBlockEntity) level.getBlockEntity(pos)).configure().fill(blood);
	}

	/**
	 * All eight strains ripe on their three woods, set into the floor with a fed planting basin behind each, and in front
	 * a row of one strain at each of its four stages.
	 */
	private static void strainBed(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		lights(level, -4, 2, 4, 2, 0, 6);
		int x = -5;
		for (List<FungalStrains> group : List.of(FLESH, BRINED, COMMON)) {
			for (int i = 0; i < group.size(); i++, x++) {
				FungalStrains strain = group.get(i);
				level.setBlock(cell(x, -1, 5), substrate(strain.substrate(), i), QUIET);
				level.setBlock(cell(x, 0, 5), strain(strain, FungalStrainRules.MAX_AGE), QUIET);
				plantingBasin(level, cell(x, -1, 6), 300.0D);
			}
			x++;
		}
		for (int age = 0; age <= FungalStrainRules.MAX_AGE; age++) {
			level.setBlock(cell(-2 + age, -1, 3), substrate(FungalStrains.Substrate.FLESH, 0), QUIET);
			level.setBlock(cell(-2 + age, 0, 3), strain(FungalStrains.FERVENT, age), QUIET);
		}
	}

	/** Close up: the flesh-feeding strains on Ravenous Heartwood, and behind them, raised, the Brined on Salt-Stained Timber. */
	private static void strainClose(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		lights(level, -3, 2, 3, 2);
		for (int i = 0; i < 3; i++) {
			level.setBlock(cell(i - 1, -1, 4), substrate(FungalStrains.Substrate.FLESH, i), QUIET);
			level.setBlock(cell(i - 1, 0, 4), strain(FLESH.get(i), i == 1 ? 2 : FungalStrainRules.MAX_AGE), QUIET);
			level.setBlock(cell(i - 1, 0, 5), substrate(FungalStrains.Substrate.BRINED, i), QUIET);
			level.setBlock(cell(i - 1, 1, 5), strain(BRINED.get(i), i == 1 ? 2 : FungalStrainRules.MAX_AGE), QUIET);
		}
	}

	/** Ripe glowroot and emberpod in fed planting basins, a young row of each, and glowroot growing under a cloche. */
	private static void catalystCrops(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		lights(level, -4, 2, 4, 2);
		int[] ages = {3, 3, 2, 1};
		for (int i = 0; i < 4; i++) {
			plantingBasin(level, cell(-4 + i, -1, 4), 300.0D);
			level.setBlock(cell(-4 + i, 0, 4), BlockInit.glowroot.get().defaultBlockState().setValue(CatalystCropBlock.AGE, ages[i]), QUIET);
			plantingBasin(level, cell(1 + i, -1, 4), 300.0D);
			level.setBlock(cell(1 + i, 0, 4), BlockInit.emberpod.get().defaultBlockState().setValue(CatalystCropBlock.AGE, ages[i]), QUIET);
		}
		cloche(level, cell(0, 0, 6), new ItemStack(ItemInit.glowroot.get()), 2);
	}

	/**
	 * A scab spike hanging under a basin of blood, dripping onto a spike it raised from the scab floor, which stops two
	 * blocks short of it; beside it a younger floor spike one block high.
	 */
	private static void floorSpike(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		lights(level, -3, 2, 3, 2);
		TIPS.clear();
		for (int x = -3; x <= 3; x++) {
			for (int z = 3; z <= 6; z++) {
				level.setBlock(cell(x, 6, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
				level.setBlock(cell(x, -1, z), BlockInit.blood_scorched_scab.get().defaultBlockState(), 2);
			}
		}
		for (int x : new int[]{-1, 2}) {
			BlockPos basin = cell(x, 7, 4);
			level.setBlock(basin, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.WORKED, true), 3);
			((VenousBasinBlockEntity) level.getBlockEntity(basin)).configure().fill(1800.0D);
			level.setBlock(cell(x, 5, 4), spike(DripstoneThickness.FRUSTUM), 2);
			level.setBlock(cell(x, 4, 4), spike(DripstoneThickness.TIP), 2);
			TIPS.add(cell(x, 4, 4).immutable());
		}
		level.setBlock(cell(-1, 0, 4), floorSpike(DripstoneThickness.FRUSTUM), 2);
		level.setBlock(cell(-1, 1, 4), floorSpike(DripstoneThickness.TIP), 2);
		level.setBlock(cell(2, 0, 4), floorSpike(DripstoneThickness.TIP), 2);
	}

	private static BlockState floorSpike(DripstoneThickness thickness) {
		return BlockInit.pointed_blood_scorched_scab.get().defaultBlockState()
				.setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.UP).setValue(PointedDripstoneBlock.THICKNESS, thickness);
	}

	/** The two spikes drip in turn, timed so each shot catches a drop in the air. */
	private static void floorDrips(ServerLevel level, int local) {
		if (TIPS.isEmpty() || local % 14 != 0) return;
		BlockPos tip = TIPS.get((local / 14) % TIPS.size());
		BlockState state = level.getBlockState(tip);
		if (ScabDrip.isHangingTip(state)) ScabDrip.drip(level, tip, state, level.random);
	}

	private static Entity hunter;

	/** The pond, a strong pool poured in it, and the night: a Brined Votary is about to answer. */
	private static void huntedPond(ServerLevel level, ServerPlayer player) {
		pond(level, player);
		hunter = null;
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		// A monster does not stand in a peaceful world, even one drawn by an event.
		level.getServer().getCommands().performPrefixedCommand(source, "difficulty easy");
		Vec3 surface = new Vec3(cell(0, -1, 6).getX() + 0.5D, cell(0, -1, 6).getY() + 0.89D, cell(0, -1, 6).getZ() + 0.5D);
		ChumPools.pour(level, surface, 400.0D, null);
	}

	/** Draws the Votary as the Court's home waters would, and holds it where it rose so the shots see it in the bloom. */
	private static void drawVotary(ServerLevel level, int local) {
		if (local != 18) return;
		Vec3 surface = new Vec3(cell(0, -1, 6).getX() + 0.5D, cell(0, -1, 6).getY() + 0.89D, cell(0, -1, 6).getZ() + 0.5D);
		ChumPools.Pool pool = ChumPools.at(level, surface);
		if (pool == null) return;
		hunter = WorldAnswer.drawHunter(level, surface, pool.radius(), pool.strength(), ChumPools.depth(level, surface),
				level.random, WorldAnswerRules.HunterWaters.HOME);
		Hemomancy.LOGGER.info("FARMING_REVIEW drew {}", hunter);
		if (hunter instanceof net.minecraft.world.entity.Mob mob) {
			mob.setNoAi(true);
			mob.setPos(mob.getX(), surface.y - 1.1D, mob.getZ());
			mob.setYRot(180.0F);
			mob.setYHeadRot(180.0F);
			mob.yBodyRot = 180.0F;
		}
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}
}
