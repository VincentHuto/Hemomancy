package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.StrandClientState;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandForm;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * With hemomancy.lashTuning, a place to tune the lash by hand. Open or create any single-player world: a second later
 * the player stands on a cleared floor with active blood that never runs low, the Projection hand, and the three
 * lashes equipped (Searing Lash selected), facing a still husk, a ring of still husks and three that wander. A line
 * above the hotbar reads out the whip: its phase, charge, tip speed, how far the whip aim sits from the view and how
 * fast the view is turning. Every feel number is in {@code lash-tuning.json} in the game directory and is read again
 * within a second of the file being saved. Nothing is captured and nothing stops the game. Run with
 * {@code lashTuningClient}. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LashTuningClient {
	private static final String PROPERTY = "hemomancy.lashTuning";
	private static final int STAGE_TICK = 30;
	private static final List<String> LASHES = List.of("searing_lash", "blood_lash", "gloam_laceration");
	private static LashTuningFile file;
	private static Object stagedWorld;
	private static int ticks;
	private static BlockPos arena;
	private static float lastYaw, lastPitch;

	private LashTuningClient() {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) {
			stagedWorld = null;
			ticks = 0;
			return;
		}
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		ticks++;
		if (stagedWorld != server && ticks >= STAGE_TICK) {
			stagedWorld = server;
			server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), server));
		}
		if (file == null) file = new LashTuningFile(mc.gameDirectory.toPath());
		if (ticks % 20 == 0) {
			LashTuningFile.Poll poll = file.poll();
			if (poll.error() != null) {
				mc.player.displayClientMessage(Component.translatable("gui.hemomancy.lash_tuning.unread", String.valueOf(poll.error()))
						.withStyle(ChatFormatting.RED), false);
			} else if (poll.tuning() != null) {
				// The integrated server shares these statics, so one set reaches both sides.
				LashRules.setTuning(poll.tuning());
				mc.player.displayClientMessage(Component.translatable("gui.hemomancy.lash_tuning.read", String.valueOf(file.path().getFileName()))
						.withStyle(ChatFormatting.GRAY), false);
			}
			if (stagedWorld == server) server.execute(() -> upkeep(server.getPlayerList().getPlayer(uuid)));
		}
		readout(mc);
	}

	/** The whip, in one line above the hotbar. */
	private static void readout(Minecraft mc) {
		float turn = (float) Math.hypot(Mth.wrapDegrees(mc.player.getYRot() - lastYaw), mc.player.getXRot() - lastPitch);
		lastYaw = mc.player.getYRot();
		lastPitch = mc.player.getXRot();
		StrandClientState.Strand strand = StrandClientState.of(mc.player.getId());
		if (strand == null || strand.cue == null || strand.form() != StrandForm.LASH || !StrandClientState.has(mc.player)) return;
		LashRules.State lash = strand.lash;
		double tip = lash.seeded() ? lash.tip().distanceTo(lash.previous(LashRules.NODES - 1)) : 0.0D;
		double off = Math.hypot(Mth.wrapDegrees(strand.aimYaw - mc.player.getYRot()), strand.aimPitch - mc.player.getXRot());
		mc.gui.setOverlayMessage(Component.translatable("gui.hemomancy.lash_tuning.readout", lash.phase().name(),
				String.format(Locale.ROOT, "%.2f", lash.charge()), String.format(Locale.ROOT, "%.2f", lash.extent()),
				String.format(Locale.ROOT, "%.2f", tip), String.format(Locale.ROOT, "%.0f", off),
				String.format(Locale.ROOT, "%.1f", turn)), false);
	}

	private static void stage(ServerPlayer player, MinecraftServer server) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		arena = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String command : new String[]{"gamemode survival", "difficulty easy", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false", "gamerule keepInventory true"})
			server.getCommands().performPrefixedCommand(source, command);
		for (BlockPos pos : BlockPos.betweenClosed(arena.offset(-16, -1, -16), arena.offset(16, 8, 16)))
			level.setBlock(pos, pos.getY() < arena.getY() ? Blocks.STONE_BRICKS.defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		// Three pillars to lash round.
		for (int[] pillar : new int[][]{{-6, 5}, {6, 5}, {0, -8}}) {
			for (int y = 0; y < 4; y++)
				level.setBlock(arena.offset(pillar[0], y, pillar[1]), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
		}
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		for (EnumBloodTemper school : EnumBloodTemper.values()) temperament.setTemperAlignment(school, 20.0F);
		BloodTemperamentEvents.syncTemperament(player, temperament);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		for (String name : LASHES) known.getKnownManips().put(ManipulationInit.getByName(name), new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(LASHES));
		known.setSelectedManip(ManipulationInit.getByName(LASHES.getFirst()));
		known.setActiveVerb(Verb.PROJECT);
		KnownManipulationEvents.syncPlayerEvent(player);
		player.getInventory().selected = 0;
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
		player.teleportTo(level, arena.getX() + 0.5D, arena.getY(), arena.getZ() + 0.5D, 0.0F, 0.0F);
		upkeep(player);
		Hemomancy.LOGGER.info("LASH_TUNING staged at {}", arena);
	}

	/** Once a second: blood topped up, the player kept whole, and any fallen husk stood up again. */
	private static void upkeep(ServerPlayer player) {
		if (player == null || arena == null) return;
		ServerLevel level = player.serverLevel();
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		if (volume.getBloodVolume() < 4000.0D) {
			volume.setBloodVolume(5000.0D);
			BloodVolumeEvents.syncVolume(player, volume);
		}
		TemperFixtures.carry(player);
		player.setHealth(player.getMaxHealth());
		player.getFoodData().setFoodLevel(20);
		List<Husk> husks = level.getEntitiesOfClass(Husk.class, new AABB(arena).inflate(24.0D), Entity::isAlive);
		long still = husks.stream().filter(Mob::isNoAi).count();
		long wandering = husks.size() - still;
		if (still < 6) {
			husks.stream().filter(Mob::isNoAi).forEach(Entity::discard);
			// One straight ahead at six blocks, and five in an arc beyond the reach of a single stroke.
			husk(level, new Vec3(0.5D, 0.0D, 6.5D), true);
			for (int i = 0; i < 5; i++) {
				double angle = Math.toRadians(-60.0D + i * 30.0D);
				husk(level, new Vec3(0.5D + Math.sin(angle) * 9.0D, 0.0D, 0.5D + Math.cos(angle) * 9.0D), true);
			}
		}
		for (long i = wandering; i < 3; i++) husk(level, new Vec3(-8.0D + i * 8.0D, 0.0D, -10.0D), false);
	}

	private static void husk(ServerLevel level, Vec3 offset, boolean still) {
		Husk husk = EntityType.HUSK.create(level);
		if (husk == null) return;
		husk.moveTo(arena.getX() + offset.x, arena.getY(), arena.getZ() + offset.z, 180.0F, 0.0F);
		husk.setNoAi(still);
		husk.setPersistenceRequired();
		husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(still ? 400.0D : 60.0D);
		husk.setHealth(husk.getMaxHealth());
		if (still) husk.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		level.addFreshEntity(husk);
	}
}
