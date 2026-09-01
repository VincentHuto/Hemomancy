package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.item.harbinger.MemoryOfVesperItem;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.CrudeMemoryShardItem;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.HematicMemoryItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class BrazierSpecialOfferingEffects {
	private static final ParticleColor DEEP_RED = new ParticleColor(155, 0, 18);
	private static final TendrilEffectConfig SCAR_TENDRILS = TendrilEffectConfig.defaults()
			.withColors(0xF0180008, 0xB8A00018)
			.withLifecycle(7, 5, 9)
			.withShape(18, 2, 0.075F, 0.08F)
			.withBranching(2, 1, 0.28F, 0.7F)
			.withWrithe(0.18F, 0.09F, 0.9F, -0.08F)
			.withRange(12.0F);

	private BrazierSpecialOfferingEffects() {
	}

	public static void spawn(ServerLevel level, BlockPos pos, ItemStack stack, boolean lit, boolean successful) {
		switch (select(stack, lit, successful)) {
			case SCAR_TENDRILS -> spawnScarTendrils(level, pos);
			case GRAFT_LIGHTNING -> spawnGraftLightning(level, pos);
			case MEMORY_GLOW -> spawnMemoryParticles(level, pos);
			case NONE -> {
			}
		}
	}

	public static void spawnPersistent(ServerLevel level, BlockPos pos, ItemStack stack, long gameTime) {
		BrazierSpecialOfferingRules.Effect effect = select(stack, true, true);
		if (!BrazierSpecialOfferingRules.shouldEmitPersistent(effect, gameTime)) {
			return;
		}
		switch (effect) {
			case SCAR_TENDRILS -> spawnScarTendrils(level, pos);
			case GRAFT_LIGHTNING -> spawnGraftLightningPulse(level, pos);
			case MEMORY_GLOW -> spawnMemoryAura(level, pos);
			case NONE -> {
			}
		}
	}

	static BrazierSpecialOfferingRules.Effect select(ItemStack stack, boolean lit, boolean successful) {
		boolean graft = LivingWeaponGraftData.fromStack(stack).isPresent();
		boolean scar = stack.getItem() instanceof ItemScar || stack.getItem() instanceof ItemScarPattern
				|| stack.is(ItemInit.runic_motif_paper.get());
		boolean memory = isMemoryItemType(stack.getItem().getClass());
		return BrazierSpecialOfferingRules.select(lit, successful, scar, graft, memory);
	}

	static boolean isMemoryItemType(Class<? extends Item> itemType) {
		return HematicMemoryItem.class.isAssignableFrom(itemType)
				|| BloodMemoryItem.class.isAssignableFrom(itemType)
				|| CrudeMemoryShardItem.class.isAssignableFrom(itemType)
				|| MemoryOfVesperItem.class.isAssignableFrom(itemType);
	}

	private static void spawnScarTendrils(ServerLevel level, BlockPos pos) {
		Vec3 center = Vec3.atCenterOf(pos).add(0.0D, 0.76D, 0.0D);
		long baseSeed = level.getGameTime() ^ pos.asLong();
		for (int i = 0; i < 5; i++) {
			double angle = Math.PI * 2.0D * i / 5.0D;
			Vec3 rim = center.add(Math.cos(angle) * 0.48D, -0.18D + (i % 2) * 0.12D,
					Math.sin(angle) * 0.48D);
			TendrilEffectSpawner.spawn(level, new TendrilAnchor.Point(rim), new TendrilAnchor.Point(center),
					SCAR_TENDRILS.withFixedSeed(true, baseSeed + i * 31L));
		}
	}

	private static void spawnGraftLightning(ServerLevel level, BlockPos pos) {
		Vec3 center = Vec3.atCenterOf(pos).add(0.0D, 0.78D, 0.0D);
		for (int i = 0; i < 4; i++) {
			double angle = Math.PI * 2.0D * i / 4.0D + level.random.nextDouble() * 0.35D;
			Vec3 rim = center.add(Math.cos(angle) * 0.62D, level.random.nextDouble() * 0.45D - 0.2D,
					Math.sin(angle) * 0.62D);
			HLPacketHandler.sendLightningSpawn(rim, center, 48.0F, level.dimension(), ParticleColor.BLACK,
					2.5F, 9, 8, 0.62F);
			HLPacketHandler.sendLightningSpawn(rim, center, 48.0F, level.dimension(), ParticleColor.RED,
					3.2F, 7, 8, 0.32F);
		}
	}

	private static void spawnGraftLightningPulse(ServerLevel level, BlockPos pos) {
		Vec3 center = Vec3.atCenterOf(pos).add(0.0D, 0.78D, 0.0D);
		double angle = level.random.nextDouble() * Math.PI * 2.0D;
		Vec3 rim = center.add(Math.cos(angle) * 0.58D, level.random.nextDouble() * 0.35D - 0.15D,
				Math.sin(angle) * 0.58D);
		HLPacketHandler.sendLightningSpawn(rim, center, 48.0F, level.dimension(), ParticleColor.BLACK,
				2.5F, 9, 8, 0.55F);
		HLPacketHandler.sendLightningSpawn(rim, center, 48.0F, level.dimension(), ParticleColor.RED,
				3.2F, 7, 8, 0.28F);
	}

	private static void spawnMemoryParticles(ServerLevel level, BlockPos pos) {
		double x = pos.getX() + 0.5D;
		double y = pos.getY() + 1.12D;
		double z = pos.getZ() + 0.5D;
		level.sendParticles(GlowParticleFactory.createData(DEEP_RED), x, y, z,
				18, 0.32D, 0.28D, 0.32D, 0.025D);
		level.sendParticles(BloodCellParticleFactory.createData(ParticleColor.BLOOD), x, y - 0.08D, z,
				24, 0.38D, 0.18D, 0.38D, 0.055D);
	}

	private static void spawnMemoryAura(ServerLevel level, BlockPos pos) {
		double x = pos.getX() + 0.5D;
		double y = pos.getY() + 1.08D;
		double z = pos.getZ() + 0.5D;
		level.sendParticles(GlowParticleFactory.createData(DEEP_RED), x, y, z,
				2, 0.22D, 0.16D, 0.22D, 0.008D);
		level.sendParticles(BloodCellParticleFactory.createData(ParticleColor.BLOOD), x, y - 0.08D, z,
				3, 0.28D, 0.12D, 0.28D, 0.018D);
	}
}
