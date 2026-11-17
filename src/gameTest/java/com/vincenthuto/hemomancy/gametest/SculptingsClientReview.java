package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.animus.TendonLineSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
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
 * With hemomancy.sculptingsReview, stages core-loop Track 2's first four Sculptings in a disposable world, captures them
 * into build/sculptings-client-review/screenshots and stops. A real survival player holds the use key: Lignum Mortis
 * crawling up a trunk and the trunk felled on release; Pyretic Forge smelting raw iron on the ground, heating sand and
 * the glass it leaves; a lit Iron Brazier drawing a log out under Pyretic Forge with Absorption; the Tendon Line thrown
 * at a high wall and the player hauled up it; and Blood Binding held on a pig. Run with sculptingsReviewClient and drop
 * {"op":"create","name":"SculptingsReview","seed":"1"} into operator/inbox after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SculptingsClientReview {
	private static final String PROPERTY = "hemomancy.sculptingsReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 80;
	private static final int PRESS_TICK = 12;
	private static int ticks;
	private static int shot;
	private static int captured;
	private static BlockPos row;

	/**
	 * One staged moment. {@code stand} and {@code aim} are offsets from the row; {@code captures} are the shot's local
	 * ticks to grab a frame at; the use key is held from {@link #PRESS_TICK} until {@code release}. {@code scene}
	 * restages what this shot works on (server side), or is null to keep the last shot's aftermath.
	 */
	private record Shot(String name, String manipulation, Verb verb, Supplier<Item> held, Vec3 stand, Vec3 aim,
			boolean use, int release, boolean thirdPerson, int[] captures, BiConsumer<ServerLevel, ServerPlayer> scene) {
	}

	private static final Vec3 TRUNK = new Vec3(-6.5, 1.5, 6.5);
	private static final Vec3 ORE = new Vec3(0.5, 0.15, 4.5);
	private static final Vec3 SAND = new Vec3(3.5, 0.5, 4.5);
	private static final Vec3 BRAZIER = new Vec3(6.5, 0.4, 4.5);
	private static final Vec3 WALL_FACE = new Vec3(10.5, 5.5, 10.0);
	private static final Vec3 PIG = new Vec3(-2.5, 0.6, 5.5);

	private static final List<Shot> SHOTS = List.of(
			new Shot("lignum-crawl", "lignum_mortis", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(-6.5, 0, 1.5), TRUNK, true, 56, false, new int[]{44}, SculptingsClientReview::tree),
			new Shot("lignum-felled", "lignum_mortis", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(-6.5, 0, 1.5), TRUNK.add(0, 2, 0), false, 0, false, new int[]{14}, null),
			new Shot("pyretic-items", "pyretic_forge", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(0.5, 0, 1.5), ORE, true, 40, false, new int[]{25}, SculptingsClientReview::ore),
			new Shot("pyretic-sand", "pyretic_forge", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(3.5, 0, 1.5), SAND, true, 60, false, new int[]{18, 40}, SculptingsClientReview::sand),
			new Shot("brazier-transmutes", "pyretic_forge", Verb.ABSORB, ItemInit.blood_absorption::get,
					new Vec3(6.5, 0, 1.5), BRAZIER, true, 56, false, new int[]{46}, SculptingsClientReview::brazier),
			new Shot("tendon-throw", "tendon_line", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(10.5, 0, 2.5), WALL_FACE, true, 20, false, new int[]{30}, SculptingsClientReview::wall),
			new Shot("tendon-haul", "tendon_line", Verb.ABSORB, ItemInit.blood_absorption::get,
					new Vec3(10.5, 0, 2.5), WALL_FACE.add(0, 0, -6), true, 56, true, new int[]{22, 34}, null),
			new Shot("binding-held", "blood_binding", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(-4.0, 0, 2.0), PIG, true, 60, false, new int[]{40}, SculptingsClientReview::pig));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("sculptings-client-review")) return;
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
				Hemomancy.LOGGER.info("SCULPTINGS_REVIEW captured {}", captured);
				mc.stop();
				return;
			}
			Shot current = SHOTS.get(shot);
			mc.options.setCameraType(current.thirdPerson() ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON);
			mc.options.hideGui = false;
			server.execute(() -> pose(server.getPlayerList().getPlayer(uuid), current));
		}
		if (shot >= SHOTS.size()) return;
		Shot current = SHOTS.get(shot);
		if (local == PRESS_TICK && current.use()) {
			KeyMapping.click(mc.options.keyUse.getKey());
			KeyMapping.set(mc.options.keyUse.getKey(), true);
		}
		if (local == current.release() && current.use()) KeyMapping.set(mc.options.keyUse.getKey(), false);
		for (int at : current.captures()) {
			if (local != at) continue;
			Screenshot.grab(mc.gameDirectory, "sculptings-" + shot + "-" + current.name() + "-" + at + ".png",
					mc.getMainRenderTarget(), message -> {});
			captured++;
			Hemomancy.LOGGER.info("SCULPTINGS_REVIEW shot {} {} at {} using={} anchor={}", shot, current.name(), at,
					mc.player.isUsingItem(), HemoCapabilityAccess.getTendonAnchor(mc.player).isSet());
		}
		if (local == SHOT_TICKS - 1) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			shot++;
		}
	}

	/** A flat floor, the player's blood and the four memories, equipped. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		ServerLevel level = player.serverLevel();
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-11, -1, -3), row.offset(13, 11, 13)))
			level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		level.getEntitiesOfClass(Entity.class, new AABB(row).inflate(16.0D), entity -> !(entity instanceof ServerPlayer))
				.forEach(Entity::discard);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(5000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		temperament.setTemperAlignment(EnumBloodTemper.FLAMMEUS, 20.0F);
		BloodTemperamentEvents.syncTemperament(player, temperament);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		List<String> four = List.of("lignum_mortis", "pyretic_forge", "tendon_line", "blood_binding");
		for (String name : four) known.getKnownManips().put(ManipulationInit.getByName(name), new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(four));
		KnownManipulationEvents.syncPlayerEvent(player);
		Hemomancy.LOGGER.info("SCULPTINGS_REVIEW staged at {}", row);
	}

	/** Selects, arms, places and aims the player for one shot, and restages its scene. */
	private static void pose(ServerPlayer player, Shot shot) {
		ServerLevel level = player.serverLevel();
		if (shot.scene() != null) shot.scene().accept(level, player);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.setSelectedManip(ManipulationInit.getByName(shot.manipulation()));
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

	/** A nine-log trunk with a crown of leaves. */
	private static void tree(ServerLevel level, ServerPlayer player) {
		BlockPos base = BlockPos.containing(at(TRUNK)).atY(row.getY());
		for (int y = 0; y < 9; y++) level.setBlock(base.above(y), Blocks.OAK_LOG.defaultBlockState(), 3);
		for (BlockPos pos : BlockPos.betweenClosed(base.offset(-2, 6, -2), base.offset(2, 9, 2))) {
			if (level.getBlockState(pos).isAir()) {
				level.setBlock(pos, Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 3);
			}
		}
	}

	private static void ore(ServerLevel level, ServerPlayer player) {
		Vec3 at = at(ORE);
		level.getEntitiesOfClass(ItemEntity.class, new AABB(BlockPos.containing(at)).inflate(2.0D)).forEach(Entity::discard);
		ItemEntity heap = new ItemEntity(level, at.x, row.getY() + 0.05D, at.z, new ItemStack(Items.RAW_IRON, 8));
		heap.setNoGravity(true);
		heap.setDeltaMovement(Vec3.ZERO);
		heap.setUnlimitedLifetime();
		heap.setPickUpDelay(32767);
		level.addFreshEntity(heap);
	}

	private static void sand(ServerLevel level, ServerPlayer player) {
		BlockPos at = BlockPos.containing(at(SAND));
		level.setBlock(at, Blocks.SAND.defaultBlockState(), 3);
		level.setBlock(at.below(), BlockInit.polished_venous_stone_bricks.get().defaultBlockState(), 3);
	}

	private static void brazier(ServerLevel level, ServerPlayer player) {
		BlockPos at = BlockPos.containing(at(BRAZIER));
		level.setBlock(at, BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.RITUAL_PHASE, 1), 3);
		IronBrazierBlockEntity brazier = (IronBrazierBlockEntity) level.getBlockEntity(at);
		brazier.setPooledBlood(BrazierBlock.BLOOD_TO_LIGHT);
		brazier.insertOffering(null, new ItemStack(Items.OAK_LOG));
	}

	/** A tall stone wall to throw the line at. */
	private static void wall(ServerLevel level, ServerPlayer player) {
		BlockPos face = BlockPos.containing(at(WALL_FACE));
		for (BlockPos pos : BlockPos.betweenClosed(face.offset(-2, -face.getY() + row.getY(), 0), face.offset(2, 3, 0)))
			level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 3);
		HemoCapabilityAccess.getTendonAnchor(player).clear();
		TendonLineSculpting.sync(player);
	}

	private static void pig(ServerLevel level, ServerPlayer player) {
		level.getEntitiesOfClass(Pig.class, new AABB(row).inflate(16.0D)).forEach(Entity::discard);
		Pig pig = EntityType.PIG.create(level);
		if (pig == null) return;
		Vec3 at = at(PIG);
		pig.moveTo(at.x, row.getY(), at.z, 180.0F, 0.0F);
		pig.setNoAi(true);
		level.addFreshEntity(pig);
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}
}
