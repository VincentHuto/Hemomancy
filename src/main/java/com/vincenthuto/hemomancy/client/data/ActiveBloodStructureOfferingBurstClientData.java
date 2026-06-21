package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodStructureOfferingBurst;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ActiveBloodStructureOfferingBurstClientData {
	private static final List<BurstEntry> ACTIVE_BURSTS = new ArrayList<>();

	private ActiveBloodStructureOfferingBurstClientData() {
	}

	public static void add(List<PacketBloodStructureOfferingBurst.OfferingBurst> bursts, BlockPos center,
			int durationTicks) {
		int duration = Math.max(1, durationTicks);
		for (PacketBloodStructureOfferingBurst.OfferingBurst burst : bursts) {
			if (!burst.stack().isEmpty()) {
				ACTIVE_BURSTS.add(new BurstEntry(burst.source(), center, burst.stack(), duration));
			}
		}
	}

	public static void tick() {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null || ACTIVE_BURSTS.isEmpty()) {
			return;
		}
		Iterator<BurstEntry> iterator = ACTIVE_BURSTS.iterator();
		while (iterator.hasNext()) {
			BurstEntry entry = iterator.next();
			entry.tick(level);
			if (entry.done()) {
				iterator.remove();
			}
		}
	}

	private static final class BurstEntry {
		private final Vec3 source;
		private final Vec3 target;
		private final ItemStack stack;
		private final int initialTicks;
		private int remainingTicks;

		private BurstEntry(BlockPos source, BlockPos target, ItemStack stack, int durationTicks) {
			this.source = Vec3.atCenterOf(source).add(0.0D, 0.75D, 0.0D);
			this.target = Vec3.atCenterOf(target).add(0.0D, 0.4D, 0.0D);
			this.stack = stack.copy();
			this.initialTicks = durationTicks;
			this.remainingTicks = durationTicks;
		}

		private void tick(ClientLevel level) {
			float progress = 1.0F - remainingTicks / (float) initialTicks;
			Vec3 path = target.subtract(source);
			Vec3 base = source.add(path.scale(Mth.clamp(progress, 0.0F, 1.0F)));
			Vec3 velocity = path.normalize().scale(0.08D);
			RandomSource random = level.random;

			for (int i = 0; i < 3; i++) {
				double scatter = 0.12D * (1.0D - progress);
				double ox = (random.nextDouble() - 0.5D) * scatter;
				double oy = (random.nextDouble() - 0.5D) * scatter;
				double oz = (random.nextDouble() - 0.5D) * scatter;
				level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
						base.x + ox, base.y + oy, base.z + oz,
						velocity.x, velocity.y, velocity.z);
			}
			remainingTicks--;
		}

		private boolean done() {
			return remainingTicks <= 0;
		}
	}
}
