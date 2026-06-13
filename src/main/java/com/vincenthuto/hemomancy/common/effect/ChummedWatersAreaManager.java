package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ChummedWatersAreaManager {
	private static final List<ChummedArea> AREAS = new ArrayList<>();
	private static final DustParticleOptions CHUM_DUST = new DustParticleOptions(new Vector3f(0.62F, 0.02F, 0.08F), 1.15F);

	private ChummedWatersAreaManager() {
	}

	public static void addArea(ServerLevel level, Vec3 center) {
		prune(level);
		AREAS.add(new ChummedArea(level.dimension(), center, level.getGameTime() + ChummedWatersRules.DURATION_TICKS));
	}

	public static boolean isChummed(Level level, Vec3 pos) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return false;
		}
		prune(serverLevel);
		double radiusSqr = ChummedWatersRules.AREA_RADIUS * ChummedWatersRules.AREA_RADIUS;
		for (ChummedArea area : AREAS) {
			if (area.dimension.equals(level.dimension()) && area.center.distanceToSqr(pos) <= radiusSqr) {
				return true;
			}
		}
		return false;
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		MinecraftServer server = event.getServer();
		for (ServerLevel level : server.getAllLevels()) {
			spawnAmbientParticles(level);
		}
	}

	private static void spawnAmbientParticles(ServerLevel level) {
		prune(level);
		long gameTime = level.getGameTime();
		if (gameTime % 12L != 0L) {
			return;
		}
		double radius = ChummedWatersRules.AREA_RADIUS;
		for (ChummedArea area : AREAS) {
			if (!area.dimension.equals(level.dimension())) {
				continue;
			}
			for (int i = 0; i < 10; i++) {
				double angle = level.random.nextDouble() * Math.PI * 2.0D;
				double distance = Math.sqrt(level.random.nextDouble()) * radius;
				double x = area.center.x + Math.cos(angle) * distance;
				double z = area.center.z + Math.sin(angle) * distance;
				double y = area.center.y + 0.06D + level.random.nextDouble() * 0.08D;
				level.sendParticles(CHUM_DUST, x, y, z, 1, 0.05D, 0.01D, 0.05D, 0.0D);
				if (level.random.nextFloat() < 0.35F) {
					level.sendParticles(ParticleTypes.BUBBLE, x, y, z, 1, 0.04D, 0.02D, 0.04D, 0.002D);
				}
			}
		}
	}

	private static void prune(ServerLevel level) {
		long gameTime = level.getGameTime();
		Iterator<ChummedArea> iterator = AREAS.iterator();
		while (iterator.hasNext()) {
			ChummedArea area = iterator.next();
			if (area.dimension.equals(level.dimension()) && area.expiresAt <= gameTime) {
				iterator.remove();
			}
		}
	}

	private record ChummedArea(ResourceKey<Level> dimension, Vec3 center, long expiresAt) {
	}
}
