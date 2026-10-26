package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.block.harbinger.plant.BroodTumorBlock;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The brood tumors one Spawn Mother has rooted in the Nursery floor: where each stands and when it was planted. They
 * stand only in owned fights. Each pulses while it ripens and hatches a brood fungling after
 * {@link MycophantBroodRules#TUMOR_HATCH_TICKS}, then breaks; a broken one is struck off the list. When she leaves the
 * fight, {@link #clear} takes every one still standing out of the world.
 */
public final class MycophantBroodTumors {
	private static final int PULSE_EVERY = 20;

	private final MycophantEntity owner;
	private final List<Tumor> tumors = new ArrayList<>();
	/** Her own count of ticks, saved with her, so hatching survives a reload. */
	private int clock;

	private record Tumor(BlockPos pos, int planted) {
	}

	public MycophantBroodTumors(MycophantEntity owner) {
		this.owner = owner;
	}

	public int standing() {
		return this.tumors.size();
	}

	/**
	 * Roots a tumor with its lower half at {@code pos}, or on the floor just under or over it; false in an unowned
	 * fight, with six already standing, or where nothing can root.
	 */
	public boolean plant(ServerLevel level, BlockPos pos) {
		if (!MycophantBroodRules.mayPlantTumor(this.owner.getEncounterOwner() != null, standing())) return false;
		BroodTumorBlock block = (BroodTumorBlock) BlockInit.brood_tumor.get();
		for (BlockPos at : new BlockPos[] { pos, pos.below(), pos.above() }) {
			if (!block.place(level, at)) continue;
			this.tumors.add(new Tumor(at.immutable(), this.clock));
			Vec3 center = Vec3.atBottomCenterOf(at);
			BossTelegraphs.spores(level, center.add(0.0D, 0.8D, 0.0D), MycophantMoves.SPORE, 14, 0.5D, 0.03D);
			BossTelegraphs.glow(level, center.add(0.0D, 1.0D, 0.0D), MycophantMoves.AMBER, 6, 0.5D);
			level.playSound(null, at, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.HOSTILE, 1.4F, 0.5F);
			return true;
		}
		return false;
	}

	/** Ripens, pulses and hatches every tumor still standing. */
	public void tick(ServerLevel level, MycophantEntity boss) {
		this.clock++;
		BroodTumorBlock block = (BroodTumorBlock) BlockInit.brood_tumor.get();
		Iterator<Tumor> it = this.tumors.iterator();
		while (it.hasNext()) {
			Tumor tumor = it.next();
			if (!level.isLoaded(tumor.pos())) continue;
			if (!block.isRoot(level, tumor.pos())) {
				it.remove();
				continue;
			}
			Vec3 center = Vec3.atBottomCenterOf(tumor.pos());
			if (MycophantBroodRules.hatchDue(tumor.planted(), this.clock)) {
				hatch(level, boss, center);
				block.burst(level, tumor.pos());
				it.remove();
				continue;
			}
			// It beats faster as it ripens.
			int age = this.clock - tumor.planted();
			int every = age > MycophantBroodRules.TUMOR_HATCH_TICKS - 100 ? PULSE_EVERY / 2 : PULSE_EVERY;
			if (Math.floorMod(age + tumor.pos().hashCode(), every) == 0)
				BossTelegraphs.glow(level, center.add(0.0D, 1.1D, 0.0D), MycophantMoves.AMBER, 3, 0.45D);
		}
	}

	/** A brood fungling crawls out, if the brood is not already full. */
	private static void hatch(ServerLevel level, MycophantEntity boss, Vec3 at) {
		MycophantMoves.dissolve(level, at.add(0.0D, 0.5D, 0.0D));
		level.playSound(null, at.x, at.y, at.z, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.HOSTILE, 1.4F, 0.7F);
		if (boss.livingBrood() >= MycophantBroodRules.BROOD_CAP) return;
		FunglingEntity fungling = EntityInit.fungling.get().create(level);
		if (fungling == null) return;
		fungling.moveTo(at.x, at.y + 0.1D, at.z, level.random.nextFloat() * 360.0F, 0.0F);
		fungling.setBrood(boss.getUUID());
		fungling.finalizeSpawn(level, level.getCurrentDifficultyAt(fungling.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
		LivingEntity quarry = boss.quarry();
		if (quarry != null) fungling.setTarget(quarry);
		level.addFreshEntity(fungling);
	}

	/** Takes every standing tumor out of the world (she yielded, failed her owner or left). */
	public void clear(ServerLevel level) {
		BroodTumorBlock block = (BroodTumorBlock) BlockInit.brood_tumor.get();
		for (Tumor tumor : this.tumors) {
			if (!block.isRoot(level, tumor.pos())) continue;
			MycophantMoves.dissolve(level, Vec3.atBottomCenterOf(tumor.pos()).add(0.0D, 0.5D, 0.0D));
			block.burst(level, tumor.pos());
		}
		this.tumors.clear();
	}

	public void save(CompoundTag tag) {
		ListTag list = new ListTag();
		for (Tumor tumor : this.tumors) {
			CompoundTag entry = new CompoundTag();
			entry.putInt("X", tumor.pos().getX());
			entry.putInt("Y", tumor.pos().getY());
			entry.putInt("Z", tumor.pos().getZ());
			entry.putInt("Planted", tumor.planted());
			list.add(entry);
		}
		tag.put("BroodTumors", list);
		tag.putInt("BroodTumorClock", this.clock);
	}

	public void load(CompoundTag tag) {
		this.tumors.clear();
		this.clock = tag.getInt("BroodTumorClock");
		for (Tag element : tag.getList("BroodTumors", Tag.TAG_COMPOUND)) {
			CompoundTag entry = (CompoundTag) element;
			this.tumors.add(new Tumor(new BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z")), entry.getInt("Planted")));
		}
	}
}
