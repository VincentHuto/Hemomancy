package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.saint.CanonGiftState;
import com.vincenthuto.hemomancy.common.manipulation.saint.UnclosingEyeRules;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;

/**
 * With hemomancy.canonGiftReview, the D-1 Saint gifts in a disposable night world: Crimson Tithe chaining three
 * bodies, its vessel filling and brimming, then the Vessel Empties; the Unclosing Eye's aperture over the charge, the
 * beam swept across three bodies, growth and the Burst, then the Lapse; Bloom of Rot sown, spreading, colonising and
 * lit; the Endless Hour planted, its seams and dark, the still set in cruor and the close. Each beat is captured first person and from
 * behind into build/canon-gift-review/screenshots, then the client stops. Run with canonGiftReviewClient and drop
 * {"op":"create","name":"CanonGiftReview","seed":1} into operator/inbox after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CanonGiftClientReview {
	private static final int PLACE = 40, TITHE = 70, EYE = 260, BLOOM = 440, HOUR = 700, END = 960;
	private static final int[] TITHE_SHOTS = {82, 120, 150, 168, 174, 186};
	private static final int[] EYE_SHOTS = {280, 300, 312, 322, 340, 360};
	private static final int[] BLOOM_SHOTS = {446, 490, 545, 564, 572, 600, 650};
	private static final int[] HOUR_SHOTS = {712, 740, 770, 800, 860, 903, 914};
	private static int ticks;
	private static BlockPos at;

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.canonGiftReview")) return;
		var mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("canon-gift-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.options.hideGui = true;
		var server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		ticks++;
		if (ticks == PLACE) server.execute(() -> place(server.getPlayerList().getPlayer(uuid)));
		if (at == null) return;
		if (ticks == TITHE) server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			ready(player, ManipulationInit.crimson_tithe.get());
			ManipulationChannelManager.start(player);
		});
		if (ticks == 140) server.execute(() -> {
			// fill the vessel past the brim line so the Brimming body reads
			var hold = CanonGiftState.tithe(uuid);
			if (hold != null) hold.vessel = 820;
		});
		if (ticks == 170) server.execute(() -> ManipulationChannelManager.stop(server.getPlayerList().getPlayer(uuid), true));
		if (ticks == EYE) server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			ready(player, ManipulationInit.unclosing_eye.get());
			ManipulationVisuals.attached(player, ManipulationVisuals.Form.EYE_CHARGE, 0.8D, 30, 1);
		});
		if (ticks == 290) server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			ManipulationVisuals.attached(player, ManipulationVisuals.Form.EYE_CHARGE, 0, 0, 0);
			ManipulationInit.unclosing_eye.get().getAction(player, player.level(), ItemStack.EMPTY, player.blockPosition(),
					UnclosingEyeRules.FULL_CHARGE);
		});
		// sweep the head across the three bodies while the beam is out
		if (ticks > 285 && ticks <= 330) {
			mc.player.setYRot(-25.0F + Math.max(0, ticks - 290) * 1.25F);
			mc.player.setXRot(1.0F);
		}
		if (ticks == BLOOM) server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			player.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, 0.0F, 30.0F);
			ready(player, ManipulationInit.bloom_of_rot.get());
			ManipulationInit.bloom_of_rot.get().getAction(player, player.level(), ItemStack.EMPTY, player.blockPosition());
		});
		if (ticks == 530) server.execute(() -> {
			// kindle the field so its cracks read, then light it with the second press
			var field = CanonGiftState.bloom(uuid);
			if (field != null) field.kindling = 80;
		});
		if (ticks == 560) server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			ManipulationInit.bloom_of_rot.get().tryPerformAction(player, player.level(), ItemStack.EMPTY, player.blockPosition(), 0);
		});
		if (ticks == HOUR) server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			player.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, 0.0F, 25.0F);
			ready(player, ManipulationInit.endless_hour.get());
			ManipulationInit.endless_hour.get().getAction(player, player.level(), ItemStack.EMPTY, player.blockPosition());
		});
		for (int shot : TITHE_SHOTS) capture(mc, shot, "tithe");
		for (int shot : EYE_SHOTS) capture(mc, shot, "eye");
		for (int shot : BLOOM_SHOTS) capture(mc, shot, "bloom");
		for (int shot : HOUR_SHOTS) capture(mc, shot, "hour");
		if (ticks >= END) {
			Hemomancy.LOGGER.info("CANON_GIFT_REVIEW captured {}",
					(TITHE_SHOTS.length + EYE_SHOTS.length + BLOOM_SHOTS.length + HOUR_SHOTS.length) * 2);
			mc.stop();
		}
	}

	private static void capture(Minecraft mc, int shot, String gift) {
		if (ticks == shot) {
			Screenshot.grab(mc.gameDirectory, gift + "-" + shot + "-first.png", mc.getMainRenderTarget(), message -> {});
			mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
		}
		if (ticks == shot + 2) {
			Screenshot.grab(mc.gameDirectory, gift + "-" + shot + "-behind.png", mc.getMainRenderTarget(), message -> {});
			mc.options.setCameraType(CameraType.FIRST_PERSON);
		}
	}

	private static void place(ServerPlayer player) {
		var server = player.getServer();
		var level = player.serverLevel();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"time set 18000", "gamerule doDaylightCycle false", "weather clear",
				"gamerule doMobSpawning false", "difficulty easy"})
			server.getCommands().performPrefixedCommand(source, cmd);
		player.setGameMode(GameType.CREATIVE);
		at = player.blockPosition();
		for (BlockPos pos : BlockPos.betweenClosed(at.offset(-14, -1, -6), at.offset(14, 8, 20)))
			level.setBlock(pos, pos.getY() < at.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		for (int x = -12; x <= 12; x += 6)
			for (int z = 0; z <= 18; z += 6) level.setBlock(at.offset(x, 7, z), Blocks.LIGHT.defaultBlockState(), 3);
		player.teleportTo(level, at.getX() + .5, at.getY(), at.getZ() + .5, 0.0F, 8.0F);
		int[][] spots = {{-3, 5}, {0, 7}, {3, 5}};
		for (int[] spot : spots) {
			Zombie zombie = EntityType.ZOMBIE.create(level);
			zombie.setNoAi(true);
			zombie.setSilent(true);
			zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400);
			zombie.setHealth(400);
			zombie.moveTo(at.getX() + .5 + spot[0], at.getY(), at.getZ() + .5 + spot[1], 180.0F, 0.0F);
			level.addFreshEntity(zombie);
		}
		Hemomancy.LOGGER.info("CANON_GIFT_REVIEW placed at {}", at);
	}

	private static void ready(ServerPlayer player, BloodManipulation manipulation) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		degree.setDegreeNumber(8);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(8000);
		volume.setBloodVolume(7000);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(manipulation, new ManipLevel(0, 0));
		known.setSelectedManip(manipulation);
		known.setEquippedManipNames(List.of(manipulation.getName()));
		HemoCapabilityAccess.getBloodTemperament(player).setTemperAlignment(manipulation.getTend(),
				(float) manipulation.getAlignLevel());
		TemperFixtures.carry(player);
		KnownManipulationEvents.syncPlayerEvent(player);
	}
}
