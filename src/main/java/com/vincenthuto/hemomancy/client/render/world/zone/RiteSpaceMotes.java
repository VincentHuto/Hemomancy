package com.vincenthuto.hemomancy.client.render.world.zone;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import com.vincenthuto.hemomancy.common.rite.zone.RiteBound;
import com.vincenthuto.hemomancy.common.network.rite.RiteSpaceSyncPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A rite space's lasting boundary: fine motes in its floor's glow trace the cube's top rim and corner seams, more
 * often while the player holds something rite-bound. Visual only; admission is decided on the server.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RiteSpaceMotes {
	private static final double VISIBLE_DISTANCE = 32.0D;
	private static final int IDLE_CHANCE = 4;
	private static final int HELD_CHANCE = 1;

	private RiteSpaceMotes() {}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		LocalPlayer player = minecraft.player;
		if (level == null || player == null || minecraft.isPaused()) return;
		boolean holdingBound = RiteBound.isRiteBound(player.getMainHandItem()) || RiteBound.isRiteBound(player.getOffhandItem());
		RandomSource random = level.random;
		for (RiteSpaceSyncPacket.Space space : RiteSpaceClientData.spaces()) {
			if (player.position().distanceToSqr(space.focus().getCenter()) > VISIBLE_DISTANCE * VISIBLE_DISTANCE) continue;
			if (random.nextInt(holdingBound ? HELD_CHANCE : IDLE_CHANCE) != 0) continue;
			spawnMote(level, space, random);
		}
	}

	private static void spawnMote(ClientLevel level, RiteSpaceSyncPacket.Space space, RandomSource random) {
		double edge = space.half() + 0.5D;
		double cx = space.focus().getX() + 0.5D;
		double cz = space.focus().getZ() + 0.5D;
		double bottom = space.focus().getY() + 1.0D;
		double top = bottom + 2.0D * space.half();
		double sx = random.nextBoolean() ? edge : -edge;
		double sz = random.nextBoolean() ? edge : -edge;
		double x;
		double y;
		double z;
		if (random.nextBoolean()) {
			// Along one of the four top edges.
			double along = (random.nextDouble() * 2.0D - 1.0D) * edge;
			boolean runsX = random.nextBoolean();
			x = cx + (runsX ? along : sx);
			z = cz + (runsX ? sz : along);
			y = top;
		} else {
			// Up one of the four corner seams.
			x = cx + sx;
			z = cz + sz;
			y = bottom + random.nextDouble() * (top - bottom);
		}
		int glow = CardinalRiteFloorVisualRules.palette(space.floorStyle()).glow();
		ParticleColor color = new ParticleColor((glow >> 16) & 0xFF, (glow >> 8) & 0xFF, glow & 0xFF);
		level.addParticle(HemoParticleData.ember(color, 0.035F + random.nextFloat() * 0.015F, 0.75F, 26 + random.nextInt(10)),
				x, y, z, 0.0D, 0.004D + random.nextDouble() * 0.006D, 0.0D);
	}
}
