package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ability.EdaciousBloodburstArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.armor.ability.PhanstmalBloodlustArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.armor.ability.SheolicBastionBloodlustArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.event.HematicSalvageEvents;
import com.vincenthuto.hemomancy.common.event.MorphicNectarEvents;
import com.vincenthuto.hemomancy.common.event.worldevent.ChamberOfWillEvents;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * With hemomancy.sweepParticleReview, fires the particle moments of sweep batches B and E that no other scene shows, one
 * station each along a dark floor: the Chamber of Will theme change, the Edacious Bloodburst, the Masquerade of the
 * Forgotten, the Distorted Presence, the Crimson Retribution on a struck husk, Neural Overload on a husk, a Hematic
 * salvage scrap and the Morphic Nectar mutation burst. Each is its own handler's method (private ones by reflection)
 * fired at a stand-in player, who is not drawn. Shots land in build/sweep-particle-review/screenshots. Excluded from
 * release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SweepParticleClientReview {
	private static final UUID STAND_IN = UUID.fromString("2e7c41b9-8d05-4a6f-b3c2-91f0d6a7e5c8");
	private static int ticks;
	private static int shot;
	private static BlockPos row;
	private static Mob struck;
	private static Mob overloaded;
	/** Station x along the row, the eye height and distance, the height looked at, and the action fired. */
	private static final Object[][] SHOTS = {
			{0, 2.0, 3.4, 1.0, "chamber_theme"},
			{5, 2.6, 5.0, 1.0, "bloodburst"},
			{10, 2.4, 4.4, 1.0, "masquerade"},
			{15, 2.0, 3.4, 1.0, "distorted_presence"},
			{20, 2.0, 3.4, 1.0, "crimson_retribution"},
			{25, 2.0, 3.2, 1.0, "neural_overload"},
			{30, 1.8, 2.8, 1.0, "salvage_scrap"},
			{35, 1.4, 2.4, 0.5, "nectar_mutation"}};

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.sweepParticleReview")) return;
		var mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("sweep-particle-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.options.hideGui = true;
		var server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (++ticks == 40) {
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				var level = player.serverLevel();
				row = player.blockPosition().offset(0, 0, 8);
				var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
				for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
						"weather clear", "gamerule doMobSpawning false"})
					server.getCommands().performPrefixedCommand(source, cmd);
				for (BlockPos pos : BlockPos.betweenClosed(row.offset(-4, -1, -8), row.offset(39, 8, 6)))
					level.setBlock(pos, pos.getY() < row.getY() ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
							: Blocks.AIR.defaultBlockState(), 2);
				struck = place(level, EntityType.HUSK.create(level), row.offset(21, 0, 1));
				overloaded = place(level, EntityType.HUSK.create(level), row.offset(25, 0, 0));
				Hemomancy.LOGGER.info("SWEEP_PARTICLE_REVIEW placed {} stations", SHOTS.length);
			});
		}
		if (ticks < 80 || row == null) return;
		int local = (ticks - 80) % 60;
		if (local == 0) {
			if (shot == SHOTS.length) { mc.stop(); return; }
			Object[] at = SHOTS[shot];
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				double fx = row.getX() + (int) at[0] + .5, fy = row.getY() + (double) at[3], fz = row.getZ() + .5;
				double x = fx, y = row.getY() + (double) at[1], z = fz - (double) at[2];
				float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, fz - z));
				player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, 0, pitch);
			});
		}
		if (shot < SHOTS.length && local == 32) {
			Object[] at = SHOTS[shot];
			server.execute(() -> act(server.overworld(), (String) at[4], row.offset((int) at[0], 0, 0)));
		}
		if (local == 40 && shot < SHOTS.length) {
			Screenshot.grab(mc.gameDirectory, "sweep-particle-" + shot + "-" + SHOTS[shot][4] + ".png",
					mc.getMainRenderTarget(), message -> {});
			Hemomancy.LOGGER.info("SWEEP_PARTICLE_REVIEW captured {}", shot);
			shot++;
		}
	}

	private static void act(ServerLevel level, String action, BlockPos station) {
		ServerPlayer standIn = FakePlayerFactory.get(level, new GameProfile(STAND_IN, "SweepStandIn"));
		standIn.moveTo(station.getX() + .5, station.getY(), station.getZ() + .5, 0, 0);
		switch (action) {
			case "chamber_theme" -> invoke(ChamberOfWillEvents.class, "playThemeChangeEffect",
					new Class<?>[]{ServerLevel.class, ServerPlayer.class}, level, standIn);
			case "bloodburst" -> EdaciousBloodburstArmorAbilityHandler.activateBloodburst(standIn);
			case "masquerade" -> PhanstmalBloodlustArmorAbilityHandler.activateMasqueradeOfTheForgotten(standIn);
			case "distorted_presence" -> {
				standIn.setLastHurtByMob(struck);
				invoke(PhanstmalBloodlustArmorAbilityHandler.class, "triggerDistortedPresence",
						new Class<?>[]{ServerPlayer.class, LivingEntity.class}, standIn, struck);
			}
			case "crimson_retribution" -> invoke(SheolicBastionBloodlustArmorAbilityHandler.class, "triggerCrimsonRetribution",
					new Class<?>[]{ServerPlayer.class, LivingEntity.class}, standIn, struck);
			case "neural_overload" -> overloaded.addEffect(new MobEffectInstance(EffectInit.neural_overload, 200, 2));
			case "salvage_scrap" -> invoke(HematicSalvageEvents.class, "dropScrap", new Class<?>[]{ServerPlayer.class}, standIn);
			case "nectar_mutation" -> {
				var item = new ItemEntity(level, station.getX() + .5, station.getY() + .1, station.getZ() + .5,
						new ItemStack(Items.ROTTEN_FLESH));
				item.setNoGravity(true);
				item.setPickUpDelay(32767);
				level.addFreshEntity(item);
				invoke(MorphicNectarEvents.class, "spawnMutationBurst",
						new Class<?>[]{ServerLevel.class, ItemEntity.class, double.class}, level, item, 0.06);
			}
			default -> {}
		}
		Hemomancy.LOGGER.info("SWEEP_PARTICLE_REVIEW {} fired", action);
	}

	private static <T extends Mob> T place(ServerLevel level, T mob, BlockPos at) {
		mob.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, 180, 0);
		mob.setNoAi(true);
		mob.setPersistenceRequired();
		level.addFreshEntity(mob);
		return mob;
	}

	/** Calls a handler's own private particle method, so the scene shows exactly what the moment spawns. */
	private static void invoke(Class<?> type, String name, Class<?>[] parameters, Object... arguments) {
		try {
			Method method = type.getDeclaredMethod(name, parameters);
			method.setAccessible(true);
			method.invoke(null, arguments);
		} catch (ReflectiveOperationException e) {
			Hemomancy.LOGGER.error("SWEEP_PARTICLE_REVIEW could not run {}", name, e);
		}
	}
}
