package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.structure.BloodStructureFormation;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Server-side manager for delayed blood structure crafting.
 * When a blood craft ring animation starts, the actual block breaking
 * and result spawning are deferred until the ring finishes collapsing.
 */
public class PendingBloodCraftManager {

	private static final List<PendingCraft> pendingCrafts = new ArrayList<>();
	private static final ParticleOptions CRAFT_CELL = HemoParticleData.bloodCell(new ParticleColor(150, 6, 16));

	public static class PendingCraft {
		private final ServerLevel level;
		private final BlockPattern.BlockPatternMatch patternMatch;
		private final int patternWidth;
		private final int patternHeight;
		private final int patternDepth;
		private final BlockPos hitPos;
		private final ItemStack result;
		private final UUID crafterId;
		private final BloodStructureFormation formation;
		private int remainingTicks;

		public PendingCraft(ServerLevel level, BlockPattern.BlockPatternMatch patternMatch,
				int patternWidth, int patternHeight, int patternDepth,
				BlockPos hitPos, ItemStack result, int delayTicks, ServerPlayer crafter) {
			this(level, patternMatch, patternWidth, patternHeight, patternDepth, hitPos, result, delayTicks, crafter, null);
		}

		public PendingCraft(ServerLevel level, BlockPattern.BlockPatternMatch patternMatch,
				int patternWidth, int patternHeight, int patternDepth,
				BlockPos hitPos, ItemStack result, int delayTicks, ServerPlayer crafter,
				BloodStructureFormation formation) {
			this.level = level;
			this.patternMatch = patternMatch;
			this.patternWidth = patternWidth;
			this.patternHeight = patternHeight;
			this.patternDepth = patternDepth;
			this.hitPos = hitPos;
			this.result = result;
			this.crafterId = crafter.getUUID();
			this.formation = formation;
			this.remainingTicks = delayTicks;
		}

		/** Tick down; returns true when complete. */
		public boolean tick() {
			if (--remainingTicks <= 0) {
				execute();
				return true;
			}
			return false;
		}

		private void execute() {
			boolean placed = formation != null && formation.place(level);
			// Clear the structure blocks
			for (int i = 0; i < patternWidth; ++i) {
				for (int j = 0; j < patternHeight; ++j) {
					for (int k = 0; k < patternDepth; ++k) {
						BlockInWorld cachedBlockInfo = patternMatch.getBlock(i, j, k);
						if (placed && cachedBlockInfo.getPos().equals(formation.pos())) {
							continue;
						}
						BlockPos pos = cachedBlockInfo.getPos();
						boolean solid = !level.getBlockState(pos).isAir();
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
						if (solid) level.sendParticles(CRAFT_CELL, pos.getX() + 0.5D, pos.getY() + 0.5D,
								pos.getZ() + 0.5D, 6, 0.3D, 0.3D, 0.3D, 0.03D);
					}
				}
			}

			// Sound & result
			level.playSound(null, hitPos, SoundInit.RITE_COMPLETE.get(), SoundSource.BLOCKS, 0.8F, 1.15F);
			if (!placed) {
				for (ItemStack drop : formation == null ? List.of(result) : formation.fallbackDrops(result)) {
					level.addFreshEntity(new ItemEntity(level,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5, drop));
				}
			}
			ServerPlayer crafter = level.getServer().getPlayerList().getPlayer(crafterId);
			if (crafter != null) {
				com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment.recordStructure(crafter, result);
				MachineAccessEvents.awardMachineCrafted(crafter, result);
				LivingStaffBondHelper.unlockFromBloodStructureCraft(crafter, result);
			}
		}
	}

	/** Formations whose craft ring is still closing: paid for, not yet placed. */
	public static List<BloodStructureFormation> pendingFormations() {
		List<BloodStructureFormation> formations = new ArrayList<>();
		for (PendingCraft craft : pendingCrafts) {
			if (craft.formation != null) formations.add(craft.formation);
		}
		return formations;
	}

	/** Schedule a craft to execute after delayTicks. */
	public static void schedule(PendingCraft craft) {
		pendingCrafts.add(craft);
	}

	/** Called every server tick to advance pending crafts. */
	public static void tick() {
		Iterator<PendingCraft> it = pendingCrafts.iterator();
		while (it.hasNext()) {
			if (it.next().tick()) {
				it.remove();
			}
		}
	}

	public static void clear() {
		pendingCrafts.clear();
	}
}
