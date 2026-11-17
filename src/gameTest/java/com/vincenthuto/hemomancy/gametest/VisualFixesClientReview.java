package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.DirectBloodLinkData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.animus.TendonLineSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCancellationRules;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * With hemomancy.visualFixesReview, stages the seven visual fixes of core-loop round two P11 in a disposable world,
 * captures them into build/visual-fixes-client-review/screenshots and stops: wet, tacky and dry stains on a floor and a
 * wall; fitted Hematic Cleats beside plain hematic iron boots, front and side, and on the player's Blood Lust boots; planted Living Staffs
 * in running rites with five rings, none, and five while melting; a sutured Scriptorium whose owner is below the third
 * Degree, its stalled pip and its hover; Lignum Mortis refused on a log in adventure mode, then claimed in survival;
 * Pyretic Forge smelting ore in first and third person; and the Tendon Line thrown, slack from the side and hauled. A
 * real survival player holds the use key. Run with visualFixesReviewClient and drop
 * {"op":"create","name":"VisualFixes","seed":"1"} into operator/inbox after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class VisualFixesClientReview {
	private static final String PROPERTY = "hemomancy.visualFixesReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 60;
	private static final int PRESS_TICK = 12;
	private static final ResourceLocation FAKE_RITE = Hemomancy.rloc("cardinal_rite/antiseptic_ground");
	private static int ticks;
	private static int shot;
	private static int captured;
	private static BlockPos row;

	/** What a shot does on the client at one of its local ticks (open a screen, move the cursor, show fake rites). */
	private interface ClientStep {
		void at(Minecraft mc, int local);
	}

	/**
	 * One staged moment. {@code stand} and {@code aim} are offsets from the row; the use key is held from
	 * {@link #PRESS_TICK} until {@code release} when {@code use}; {@code scene} restages what the shot looks at (server
	 * side), or is null to keep the last one; {@code client} runs every local tick.
	 */
	private record Shot(String name, String manipulation, Verb verb, Supplier<Item> held, Vec3 stand, Vec3 aim, boolean use,
			int release, CameraType camera, int[] captures, BiConsumer<ServerLevel, ServerPlayer> scene, ClientStep client) {
	}

	private static final Vec3 STAINS = new Vec3(-8.5, 0.2, 5.0);
	private static final Vec3 STANDS = new Vec3(-2.25, 0.5, 4.0);
	private static final Vec3 FOCUS_A = new Vec3(2.5, 1.2, 7.5);
	private static final Vec3 FOCUS_B = new Vec3(5.5, 1.2, 7.5);
	private static final Vec3 SCRIPTORIUM = new Vec3(11.5, 0.5, 3.5);
	private static final Vec3 LOG = new Vec3(0.5, 1.5, 10.5);
	private static final Vec3 ORE = new Vec3(5.5, 0.15, 11.5);
	private static final Vec3 WALL_FACE = new Vec3(-3.5, 4.5, 15.0);
	private static final Vec3 FORGES = new Vec3(13.0, 0.6, 13.5);
	private static final ClientStep NONE = (mc, local) -> { };

	private static final List<Shot> SHOTS = List.of(
			new Shot("stains", null, Verb.PROJECT, () -> Items.AIR, new Vec3(-8.5, 0, 1.0), STAINS, false, 0,
					CameraType.FIRST_PERSON, new int[]{20}, VisualFixesClientReview::stains, NONE),
			new Shot("stains-wall-close", null, Verb.PROJECT, () -> Items.AIR, new Vec3(-8.5, 0, 4.0),
					new Vec3(-8.5, 1.4, 7.0), false, 0, CameraType.FIRST_PERSON, new int[]{20}, null, NONE),
			new Shot("cleats-front", null, Verb.PROJECT, () -> Items.AIR, new Vec3(-2.25, 0, 1.4), STANDS, false, 0,
					CameraType.FIRST_PERSON, new int[]{20}, (level, player) -> stands(level, 180.0F), NONE),
			new Shot("cleats-side", null, Verb.PROJECT, () -> Items.AIR, new Vec3(-2.25, 0, 1.4), STANDS, false, 0,
					CameraType.FIRST_PERSON, new int[]{20}, (level, player) -> stands(level, 90.0F), NONE),
			new Shot("cleats-worn", null, Verb.PROJECT, () -> Items.AIR, new Vec3(-2.25, 0, 1.4), new Vec3(-2.25, 1.0, -3.0),
					false, 0, CameraType.THIRD_PERSON_FRONT, new int[]{20}, null, NONE),
			new Shot("staff-rings", null, Verb.PROJECT, () -> Items.AIR, new Vec3(4.0, 0, 2.5), new Vec3(4.0, 1.4, 7.5), false, 0,
					CameraType.FIRST_PERSON, new int[]{30}, VisualFixesClientReview::foci, VisualFixesClientReview::fakeRites),
			new Shot("staff-rings-close", null, Verb.PROJECT, () -> Items.AIR, new Vec3(2.5, 0, 5.4), new Vec3(2.5, 1.6, 7.5),
					false, 0, CameraType.FIRST_PERSON, new int[]{30}, null, VisualFixesClientReview::fakeRites),
			new Shot("stalled-screen", null, Verb.PROJECT, () -> Items.AIR, new Vec3(11.5, 0, 0.5), SCRIPTORIUM, false, 0,
					CameraType.FIRST_PERSON, new int[]{30, 46}, VisualFixesClientReview::sutured, VisualFixesClientReview::screen),
			new Shot("refused-hud", "lignum_mortis", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(0.5, 0, 7.6), LOG,
					true, 50, CameraType.FIRST_PERSON, new int[]{34}, (level, player) -> logs(level, player, true), NONE),
			new Shot("claimed-hud", "lignum_mortis", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(0.5, 0, 7.6), LOG,
					true, 40, CameraType.FIRST_PERSON, new int[]{30}, (level, player) -> logs(level, player, false), NONE),
			new Shot("pyretic-first", "pyretic_forge", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(5.5, 0, 8.6), ORE,
					true, 50, CameraType.FIRST_PERSON, new int[]{24, 44}, VisualFixesClientReview::ore, NONE),
			new Shot("pyretic-third", "pyretic_forge", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(5.5, 0, 8.6), ORE,
					true, 50, CameraType.THIRD_PERSON_BACK, new int[]{30}, VisualFixesClientReview::ore, NONE),
			new Shot("tendon-throw", "tendon_line", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(-3.5, 0, 9.5),
					WALL_FACE, true, 24, CameraType.THIRD_PERSON_BACK, new int[]{30, 50}, VisualFixesClientReview::wall, NONE),
			new Shot("tendon-slack-side", "tendon_line", Verb.ABSORB, () -> Items.AIR, new Vec3(-3.5, 0, 9.5),
					new Vec3(4.0, 1.6, 9.5), false, 0, CameraType.THIRD_PERSON_BACK, new int[]{20}, null, NONE),
			new Shot("tendon-slack-first", "tendon_line", Verb.ABSORB, () -> Items.AIR, new Vec3(-3.5, 0, 9.5),
					WALL_FACE.add(1.5, -2.0, 0.0), false, 0, CameraType.FIRST_PERSON, new int[]{20}, null, NONE),
			new Shot("tendon-haul-side", "tendon_line", Verb.ABSORB, ItemInit.blood_absorption::get, new Vec3(-3.5, 0, 9.5),
					new Vec3(4.0, 1.6, 9.5), true, 30, CameraType.THIRD_PERSON_BACK, new int[]{16, 20}, null, NONE),
			new Shot("forges", null, Verb.PROJECT, () -> Items.AIR, new Vec3(13.0, 0, 9.8), FORGES, false, 0,
					CameraType.FIRST_PERSON, new int[]{8, 16}, VisualFixesClientReview::forges, NONE));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("visual-fixes-client-review")) return;
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (++ticks == SETUP_TICK) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), server));
		if (ticks < FIRST_SHOT_TICK || row == null) return;
		int local = (ticks - FIRST_SHOT_TICK) % SHOT_TICKS;
		if (local == 0) {
			if (shot == SHOTS.size()) {
				Hemomancy.LOGGER.info("VISUAL_FIXES_REVIEW captured {}", captured);
				mc.stop();
				return;
			}
			Shot current = SHOTS.get(shot);
			if (mc.screen != null) mc.player.closeContainer();
			mc.options.setCameraType(current.camera());
			mc.options.hideGui = false;
			mc.getToasts().clear();
			mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
			server.execute(() -> pose(server.getPlayerList().getPlayer(uuid), current));
		}
		if (shot >= SHOTS.size()) return;
		Shot current = SHOTS.get(shot);
		current.client().at(mc, local);
		if (local == PRESS_TICK && current.use()) {
			KeyMapping.click(mc.options.keyUse.getKey());
			KeyMapping.set(mc.options.keyUse.getKey(), true);
		}
		if (local == current.release() && current.use()) KeyMapping.set(mc.options.keyUse.getKey(), false);
		for (int at : current.captures()) {
			if (local != at) continue;
			mc.getToasts().clear();
			Screenshot.grab(mc.gameDirectory, "visual-" + shot + "-" + current.name() + "-" + at + ".png",
					mc.getMainRenderTarget(), message -> {});
			captured++;
			Hemomancy.LOGGER.info("VISUAL_FIXES_REVIEW shot {} {} at {} using={} anchor={} screen={}", shot, current.name(),
					at, mc.player.isUsingItem(), HemoCapabilityAccess.getTendonAnchor(mc.player).isSet(),
					mc.screen == null ? "none" : mc.screen.getClass().getSimpleName());
		}
		if (local == SHOT_TICKS - 1) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			if (current.name().startsWith("staff")) ActiveRiteClientData.set(List.of());
			shot++;
		}
	}

	/** A floor, the player's blood, Degree and memories, and the cleats on their feet. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		ServerLevel level = player.serverLevel();
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-13, -1, -4), row.offset(15, 12, 17)))
			level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		level.getEntitiesOfClass(Entity.class, new AABB(row).inflate(20.0D), entity -> !(entity instanceof ServerPlayer))
				.forEach(Entity::discard);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(5000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(5);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		temperament.setTemperAlignment(EnumBloodTemper.FLAMMEUS, 20.0F);
		BloodTemperamentEvents.syncTemperament(player, temperament);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		List<String> three = List.of("lignum_mortis", "pyretic_forge", "tendon_line", "blood_shot");
		for (String name : three) known.getKnownManips().put(ManipulationInit.getByName(name), new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(three));
		KnownManipulationEvents.syncPlayerEvent(player);
		player.setItemSlot(EquipmentSlot.FEET, CleatsFixtures.fitted(ItemInit.blood_lust_boots.get()));
		Hemomancy.LOGGER.info("VISUAL_FIXES_REVIEW staged at {}", row);
	}

	/** Selects, arms, places and aims the player for one shot, and restages its scene. */
	private static void pose(ServerPlayer player, Shot shot) {
		ServerLevel level = player.serverLevel();
		player.setGameMode(GameType.SURVIVAL);
		if (shot.scene() != null) shot.scene().accept(level, player);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.setSelectedManip(ManipulationInit.getByName(shot.manipulation() == null ? "blood_shot" : shot.manipulation()));
		known.setActiveVerb(shot.verb());
		KnownManipulationEvents.syncPlayerEvent(player);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(4500.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		player.setHealth(player.getMaxHealth());
		player.getInventory().selected = 0;
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(shot.held().get()));
		Vec3 eye = at(shot.stand()).add(0.0D, 1.62D, 0.0D);
		Vec3 target = at(shot.aim());
		double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
		player.teleportTo(level, eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
	}

	/** Wet, tacky and dry stains, three of each, on a pale floor and on a wall behind them. */
	private static void stains(ServerLevel level, ServerPlayer player) {
		BlockPos center = BlockPos.containing(at(STAINS)).atY(row.getY());
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, -1, 1)))
			level.setBlock(pos, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, 0, 2), center.offset(3, 3, 2)))
			level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
		int[] ages = {GroundRunRules.WET, GroundRunRules.TACKY, GroundRunRules.DRY};
		for (int a = 0; a < 3; a++) {
			for (int i = 0; i < 3; i++) {
				BlockPos floor = center.offset(-2 + a * 2 - (i == 1 ? 1 : 0), 0, -2 + i);
				level.setBlock(floor, stain(Direction.DOWN, ages[a]), 3);
				BlockPos wall = center.offset(-3 + a * 2 + (i == 2 ? 1 : 0), 1 + (i % 2), 1);
				level.setBlock(wall, stain(Direction.SOUTH, ages[a]), 3);
			}
		}
	}

	private static BlockState stain(Direction face, int age) {
		return BlockInit.blood_stain.get().defaultBlockState().setValue(MultifaceBlock.getFaceProperty(face), true)
				.setValue(BloodStainBlock.AGE, age);
	}

	/** Two armour stands side by side: hematic iron boots with cleats fitted on the left, plain ones on the right. */
	private static void stands(ServerLevel level, float yaw) {
		level.getEntitiesOfClass(ArmorStand.class, new AABB(row).inflate(20.0D)).forEach(Entity::discard);
		Vec3 base = at(STANDS);
		for (int i = 0; i < 2; i++) {
			ArmorStand stand = EntityType.ARMOR_STAND.create(level);
			if (stand == null) return;
			stand.moveTo(base.x - 0.75D + i * 1.5D, row.getY(), base.z, yaw, 0.0F);
			stand.setYBodyRot(yaw);
			stand.setYHeadRot(yaw);
			stand.setNoGravity(true);
			stand.setItemSlot(EquipmentSlot.FEET,
					i == 0 ? CleatsFixtures.fitted(ItemInit.hematic_iron_boots.get()) : new ItemStack(ItemInit.hematic_iron_boots.get()));
			level.addFreshEntity(stand);
		}
	}

	/** Three Cardinal Foci in a row for the fake rites the client draws. */
	private static void foci(ServerLevel level, ServerPlayer player) {
		for (int i = 0; i < 3; i++) {
			BlockPos focus = BlockPos.containing(at(FOCUS_A)).atY(row.getY()).offset(i * 3, 0, 0);
			level.setBlock(focus, BlockInit.cardinal_focus.get().defaultBlockState(), 3);
		}
	}

	/**
	 * Running rites the client draws over the three Foci: a planted Staff of five rings, one of none, and one of five
	 * melting as its rite is cancelled. Set on the client only: the renderer is what is under review.
	 */
	private static void fakeRites(Minecraft mc, int local) {
		if (local < 4 || local > SHOT_TICKS - 3) return;
		if (ActiveRiteClientData.getActiveRites().size() == 3) return;
		UUID owner = mc.player.getUUID();
		List<ActiveRiteData.RiteEntry> rites = new ArrayList<>();
		int[] rings = {5, 0, 5};
		for (int i = 0; i < 3; i++) {
			BlockPos focus = BlockPos.containing(at(FOCUS_A)).atY(row.getY()).offset(i * 3, 0, 0);
			int cancelling = i == 2 ? CardinalRiteCancellationRules.TOTAL_TICKS / 3 : 0;
			rites.add(new ActiveRiteData.RiteEntry(focus, 5, 0.4D, FAKE_RITE, false, "INSCRIPTION", 0, 0, 0, 0, 1,
					0, 0, 0, 0, -1, "", 1.0F, List.of(), List.of(), List.of(), List.of(), true, owner, cancelling, 400,
					"none", false, false, 200).withStaffRings(rings[i]));
		}
		ActiveRiteClientData.set(rites);
	}

	/** The Scriptorium, sutured to the player, who is below the third Degree: its suture stalls. */
	private static void sutured(ServerLevel level, ServerPlayer player) {
		BlockPos at = BlockPos.containing(at(SCRIPTORIUM));
		level.setBlock(at, BlockInit.enzymatic_scriptorium.get().defaultBlockState(), 3);
		var station = (EnzymaticScriptoriumBlockEntity) level.getBlockEntity(at);
		BlockBloodVolume tank = ReservoirBlood.settled(station);
		tank.fill(300.0D);
		station.sendUpdates();
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(2);
		BloodRoutingSavedData.get(level).setLink(at, DirectBloodLinkData.nearby(player.getUUID()));
	}

	/** Parks the cursor for the bare pip shot, then puts it on the Scriptorium's blood bar for its hover. */
	private static void screen(Minecraft mc, int local) {
		// The menu opens once the client has the station's block entity: a screen opened in the tick the block is
		// placed finds no station on the client.
		if (local == 10) {
			mc.getSingleplayerServer().execute(() -> {
				ServerPlayer player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
				var station = (EnzymaticScriptoriumBlockEntity) player.serverLevel().getBlockEntity(BlockPos.containing(at(SCRIPTORIUM)));
				player.openMenu(station, station.getBlockPos());
			});
		}
		if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
		if (local < 36) cursor(mc, 4, 4);
		else cursor(mc, screen.getGuiLeft() + 205, screen.getGuiTop() + 80);
		if (local == SHOT_TICKS - 2) {
			mc.getSingleplayerServer().execute(() -> {
				ServerPlayer player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
				HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(5);
			});
		}
	}

	private static void cursor(Minecraft mc, int guiX, int guiY) {
		var window = mc.getWindow();
		double x = guiX * (double) window.getScreenWidth() / window.getGuiScaledWidth();
		double y = guiY * (double) window.getScreenHeight() / window.getGuiScaledHeight();
		try {
			Field xpos = MouseHandler.class.getDeclaredField("xpos");
			Field ypos = MouseHandler.class.getDeclaredField("ypos");
			xpos.setAccessible(true);
			ypos.setAccessible(true);
			xpos.setDouble(mc.mouseHandler, x);
			ypos.setDouble(mc.mouseHandler, y);
		} catch (ReflectiveOperationException e) {
			Hemomancy.LOGGER.warn("VISUAL_FIXES_REVIEW cannot move the cursor", e);
		}
	}

	/** A three-log column to aim Lignum Mortis at; adventure mode makes the claim's pulse refused. */
	private static void logs(ServerLevel level, ServerPlayer player, boolean adventure) {
		BlockPos base = BlockPos.containing(at(LOG)).atY(row.getY());
		for (int y = 0; y < 3; y++) level.setBlock(base.above(y), Blocks.OAK_LOG.defaultBlockState(), 3);
		if (adventure) player.setGameMode(GameType.ADVENTURE);
	}

	private static void ore(ServerLevel level, ServerPlayer player) {
		Vec3 at = at(ORE);
		level.getEntitiesOfClass(ItemEntity.class, new AABB(BlockPos.containing(at)).inflate(3.0D)).forEach(Entity::discard);
		ItemEntity heap = new ItemEntity(level, at.x, row.getY() + 0.05D, at.z, new ItemStack(Items.RAW_IRON, 16));
		heap.setNoGravity(true);
		heap.setDeltaMovement(Vec3.ZERO);
		heap.setUnlimitedLifetime();
		heap.setPickUpDelay(32767);
		level.addFreshEntity(heap);
	}

	/** A tall wall to throw the line at; the line starts loose. */
	private static void wall(ServerLevel level, ServerPlayer player) {
		BlockPos face = BlockPos.containing(at(WALL_FACE));
		for (BlockPos pos : BlockPos.betweenClosed(face.offset(-2, -face.getY() + row.getY(), 0), face.offset(2, 3, 0)))
			level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 3);
		HemoCapabilityAccess.getTendonAnchor(player).clear();
		TendonLineSculpting.sync(player);
	}

	/**
	 * UN-54: the two forge cues that shrank with the Pyretic Forge change, side by side: a Will's attached forge on a
	 * still pig at the left (the review world is peaceful; back to 1.0 since B10) and the Brazier transmutation's bowl
	 * forge (0.8) over an Iron Brazier at the right. Both are held 40 ticks so the shots catch them whole.
	 */
	private static void forges(ServerLevel level, ServerPlayer player) {
		level.getEntitiesOfClass(net.minecraft.world.entity.animal.Pig.class, new AABB(row).inflate(24.0D)).forEach(Entity::discard);
		Vec3 left = at(FORGES).add(-1.5D, -0.6D, 0.0D);
		var pig = EntityType.PIG.create(level);
		if (pig == null) return;
		pig.moveTo(left.x, row.getY(), left.z, 180.0F, 0.0F);
		pig.setNoAi(true);
		pig.setSilent(true);
		level.addFreshEntity(pig);
		com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.attached(pig,
				com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.FORGE, 1.0D, 40, 1);
		BlockPos brazier = BlockPos.containing(at(FORGES).add(1.5D, 0.0D, 0.0D)).atY(row.getY());
		level.setBlock(brazier, BlockInit.iron_brazier.get().defaultBlockState(), 3);
		Vec3 bowl = com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierMount.bowl(level.getBlockState(brazier), brazier, 0.75D);
		com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.burst(level,
				com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.FORGE, bowl.add(0.0D, 0.4D, 0.0D), bowl, 0.8D, 40);
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}
}
