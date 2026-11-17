package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.BloodVerbKeyPacket;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import com.vincenthuto.hemomancy.client.screen.manips.RadialChooseManipScreen;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * With hemomancy.verbReview, stages the verb framework of core-loop Track 1 in a disposable world and captures it
 * into build/verb-client-review/screenshots, then stops: plain Projection into stone (nothing selected), a review
 * Sculpting that claims stone (the channel HUD's icon, price and bright reticle, the stream landing on the claim), the same
 * Sculpting aimed at dirt it does not claim (dim reticle, the plain stream), an Absorption Sculpting drawing on items, the
 * verb key turning a held Projection hand into Absorption, a claiming Sculpting in third person (its sustained cast
 * pose), and the Living Staff drawing from a pig on the active verb with nothing selected. The player is a real
 * survival player holding right-click through the use key. Run with verbReviewClient and drop
 * {"op":"create","name":"VerbReview","seed":"1"} into operator/inbox after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class VerbClientReview {
	private static final String PROPERTY = "hemomancy.verbReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 70;
	private static int ticks;
	private static int shot;
	private static int radialTicks;
	private static BlockPos row;

	/** One staged moment: what is selected and held, where the player looks, and how the hand is used. */
	private record Shot(String name, String manipulation, Verb verb, Supplier<Item> held, Vec3 aimOffset, boolean use,
			boolean pressVerbKey, boolean thirdPerson) {
	}

	private static final Vec3 STONE = new Vec3(0.5, 1.5, 4.5);
	private static final Vec3 DIRT = new Vec3(2.5, 1.5, 4.5);
	private static final Vec3 SEEDS = new Vec3(-2.5, 0.2, 3.5);
	private static final Vec3 PIG = new Vec3(-5.5, 0.6, 4.5);

	private static final List<Shot> SHOTS = List.of(
			new Shot("plain-projection", null, Verb.PROJECT, ItemInit.blood_projection::get, STONE, true, false, false),
			new Shot("sculpting-claims-stone", "hematic_flare", Verb.PROJECT, ItemInit.blood_projection::get, STONE, true, false, false),
			new Shot("sculpting-passes-dirt", "hematic_flare", Verb.PROJECT, ItemInit.blood_projection::get, DIRT, true, false, false),
			new Shot("absorption-sculpting-items", "void_shroud", Verb.ABSORB, ItemInit.blood_absorption::get, SEEDS, true, false, false),
			new Shot("verb-key-turns-hand", null, Verb.PROJECT, ItemInit.blood_projection::get, STONE, false, true, false),
			new Shot("sculpting-third-person", "hematic_flare", Verb.PROJECT, ItemInit.blood_projection::get, STONE, true, false, true),
			new Shot("staff-absorbs-pig", null, Verb.ABSORB, ItemInit.living_staff::get, PIG, true, false, false));

	/** A review Sculpting: claims stone (Projection) or any items (Absorption) and does nothing else. */
	private record ReviewSculpting(Set<VerbTargetKind> wants, boolean stoneOnly) implements VerbSculpting {
		@Override
		public boolean claims(Player player, VerbTarget target) {
			return !stoneOnly || target instanceof VerbTarget.Block block && block.state().is(Blocks.STONE);
		}

		@Override
		public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		}

		@Override
		public double bloodPerPulse() {
			return 15.0D;
		}
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("verb-client-review")) return;
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (++ticks == SETUP_TICK) {
			ManipulationInit.getByName("hematic_flare")
					.setProjectionSculpting(() -> new ReviewSculpting(Set.of(VerbTargetKind.BLOCK), true));
			ManipulationInit.getByName("void_shroud")
					.setAbsorptionSculpting(() -> new ReviewSculpting(Set.of(VerbTargetKind.ITEMS), false));
			server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), server));
		}
		if (ticks < FIRST_SHOT_TICK || row == null) return;
		if (shot == SHOTS.size()) {
			radialShot(mc, server);
			return;
		}
		int local = (ticks - FIRST_SHOT_TICK) % SHOT_TICKS;
		if (local == 0) {
			if (shot == SHOTS.size()) {
				Hemomancy.LOGGER.info("VERB_REVIEW captured {}", shot);
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
		if (local == 8 && current.pressVerbKey()) PacketHandler.sendToServer(BloodVerbKeyPacket.INSTANCE);
		if (local == 12 && current.use()) {
			KeyMapping.click(mc.options.keyUse.getKey());
			KeyMapping.set(mc.options.keyUse.getKey(), true);
		}
		if (local == 50) {
			Screenshot.grab(mc.gameDirectory, "verb-" + shot + "-" + current.name() + ".png", mc.getMainRenderTarget(),
					message -> {});
			Hemomancy.LOGGER.info("VERB_REVIEW shot {} {} using={} verb={}", shot, current.name(),
					mc.player.isUsingItem(), HemoCapabilityAccess.getKnownManipulations(mc.player).getActiveVerb());
		}
		if (local == 56) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			shot++;
		}
	}

	/** A flat floor, a stone and a dirt block to aim at, seeds on the ground and a still pig. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		var level = player.serverLevel();
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-9, -1, -4), row.offset(9, 6, 9)))
			level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		level.setBlock(BlockPos.containing(at(STONE)), Blocks.STONE.defaultBlockState(), 3);
		level.setBlock(BlockPos.containing(at(DIRT)), Blocks.DIRT.defaultBlockState(), 3);
		// A reopened save keeps the last run's seeds and pig: clear them so each run stages one of each.
		var area = new net.minecraft.world.phys.AABB(row).inflate(12.0D);
		level.getEntitiesOfClass(ItemEntity.class, area).forEach(net.minecraft.world.entity.Entity::discard);
		level.getEntitiesOfClass(Pig.class, area).forEach(net.minecraft.world.entity.Entity::discard);
		Vec3 seeds = at(SEEDS);
		ItemEntity heap = new ItemEntity(level, seeds.x, row.getY() + 0.05, seeds.z, new ItemStack(Items.WHEAT_SEEDS, 5));
		heap.setNoGravity(true);
		heap.setDeltaMovement(Vec3.ZERO);
		heap.setUnlimitedLifetime();
		heap.setPickUpDelay(32767);
		level.addFreshEntity(heap);
		Pig pig = EntityType.PIG.create(level);
		if (pig != null) {
			Vec3 at = at(PIG);
			pig.moveTo(at.x, row.getY(), at.z, 180.0F, 0.0F);
			pig.setNoAi(true);
			pig.setInvulnerable(false);
			level.addFreshEntity(pig);
		}
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(5000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.getByName("hematic_flare"), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.getByName("void_shroud"), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.getByName("blood_shot"), new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(List.of("blood_shot", "hematic_flare", "void_shroud")));
		KnownManipulationEvents.syncPlayerEvent(player);
		Hemomancy.LOGGER.info("VERB_REVIEW staged at {}", row);
	}

	/** The last shot: the wheel held open, with both verbs and the cleats slice on its inner ring and Absorption the active one. */
	private static void radialShot(Minecraft mc, MinecraftServer server) {
		int local = radialTicks++;
		var uuid = mc.player.getUUID();
		ItemStack charm = new ItemStack(ItemInit.charm_of_vascularium.get());
		if (local == 0) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			mc.options.setCameraType(CameraType.FIRST_PERSON);
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				HemoCapabilityAccess.getEquipment(player).setStackInSlot(5, charm.copy());
				player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, CleatsFixtures.fitted(ItemInit.hematic_iron_boots.get()));
				var known = HemoCapabilityAccess.getKnownManipulations(player);
				for (String name : List.of("blood_shot", "hematic_flare", "void_shroud")) {
					if (!known.getEquippedManipNames().contains(name)) known.getEquippedManipNames().add(name);
				}
				known.setSelectedManip(ManipulationInit.getByName("hematic_flare"));
				known.setActiveVerb(Verb.ABSORB);
				KnownManipulationEvents.syncPlayerEvent(player);
			});
		}
		if (local == 10) {
			var equipment = HemoCapabilityAccess.getEquipment(mc.player);
			equipment.setStackInSlot(5, charm.copy());
			// The wheel closes itself when its key is not physically held; the review holds it open.
			mc.setScreen(new RadialChooseManipScreen(equipment) {
				@Override
				protected void processClick(boolean triggeredByMouse) {
					if (triggeredByMouse) super.processClick(true);
				}
			});
		}
		if (local == 50) {
			Screenshot.grab(mc.gameDirectory, "verb-" + shot + "-radial-inner-verbs.png", mc.getMainRenderTarget(),
					message -> {});
		}
		if (local == 56) {
			Hemomancy.LOGGER.info("VERB_REVIEW captured {}", shot + 1);
			mc.stop();
		}
	}

	/** Selects, arms and aims the player for one shot. */
	private static void pose(ServerPlayer player, Shot shot) {
		var level = player.serverLevel();
		// Each shot finds its blocks fresh: plain Projection infuses the stone it is held on.
		level.setBlock(BlockPos.containing(at(STONE)), Blocks.STONE.defaultBlockState(), 3);
		level.setBlock(BlockPos.containing(at(DIRT)), Blocks.DIRT.defaultBlockState(), 3);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		// "Nothing Sculpting-like selected" is an ordinary cast memory, Blood Shot, as a player would have it.
		BloodManipulation selected = ManipulationInit.getByName(shot.manipulation() == null ? "blood_shot" : shot.manipulation());
		known.setSelectedManip(selected);
		known.setActiveVerb(shot.verb());
		KnownManipulationEvents.syncPlayerEvent(player);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(4000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		player.getInventory().selected = 0;
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(shot.held().get()));
		Vec3 eye = Vec3.atBottomCenterOf(row).add(0.0D, 1.62D, 0.0D);
		Vec3 target = at(shot.aimOffset());
		double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
		player.teleportTo(player.serverLevel(), eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}
}
