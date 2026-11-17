package com.vincenthuto.hemomancy.common.world.chum;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCatalysts;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswer;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Chum (core-loop Track 7): the blood pools a level's waters hold. Plain Projection at a water surface pours into the
 * pool there, or starts one ({@link #tryProject}, a link in the projection chain); a Temper catalyst in the off hand
 * colours it with its school. A pool fades lazily ({@link ChumRules#fade}), faster in flowing water and rain, and is
 * forgotten when it is gone. Inside one a fishing hook is lured faster (the fishing hook mixin) and a catch can be one
 * of the pool's (the {@code chum_catch} loot modifier); a strong pool in deep water now and then draws a fish to it.
 * No blocks are placed. Saved with the level as {@code hemomancy_chum_pools}.
 */
public final class ChumPools extends SavedData {
	public static final BloodAccounting.Source CHUM = BloodAccounting.Source.of("chum", Category.WORLD, false);
	private static final String DATA_NAME = "hemomancy_chum_pools";
	private static final SavedData.Factory<ChumPools> FACTORY = new SavedData.Factory<>(ChumPools::new, ChumPools::load, null);
	/** A pool reaches this far above and below its surface. */
	private static final double DEPTH_REACH = 4.0D;
	private static final ParticleColor BLOOM = new ParticleColor(132, 8, 16);
	/** A bloom lasts this long and is resent this often while the pool lasts, so they overlap. */
	private static final int BLOOM_TICKS = 50;
	private static final long BLOOM_RESEND_TICKS = 40L;
	private static final ParticleColor BLOOM_BRIGHT = new ParticleColor(176, 18, 26);

	/** One pool: its centre at the surface, the blood in it when last settled, and the school a catalyst gave it. */
	public static final class Pool {
		private double x;
		private double y;
		private double z;
		private double ml;
		private long settledAt;
		@Nullable
		private EnumBloodTemper school;

		Pool(double x, double y, double z, double ml, long settledAt, @Nullable EnumBloodTemper school) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.ml = ml;
			this.settledAt = settledAt;
			this.school = school;
		}

		public Vec3 centre() {
			return new Vec3(x, y, z);
		}

		public double ml() {
			return ml;
		}

		public double radius() {
			return ChumRules.radius(ml);
		}

		public double strength() {
			return ChumRules.strength(ml);
		}

		@Nullable
		public EnumBloodTemper school() {
			return school;
		}

		boolean holds(Vec3 pos) {
			double r = radius();
			double dx = pos.x - x, dz = pos.z - z;
			return dx * dx + dz * dz <= r * r && Math.abs(pos.y - y) <= DEPTH_REACH;
		}
	}

	private final List<Pool> pools = new ArrayList<>();

	public static ChumPools get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	// ── Reading ──

	/** The strongest pool holding {@code pos}, settled to now, or null. */
	@Nullable
	public static Pool at(ServerLevel level, Vec3 pos) {
		ChumPools data = get(level);
		Pool best = null;
		for (Pool pool : data.settled(level)) {
			if (pool.holds(pos) && (best == null || pool.ml > best.ml)) best = pool;
		}
		return best;
	}

	/** The strength of the chum at {@code pos}, 0 outside every pool. */
	public static double strengthAt(ServerLevel level, Vec3 pos) {
		Pool pool = at(level, pos);
		return pool == null ? 0.0D : pool.strength();
	}

	public List<Pool> pools() {
		return List.copyOf(pools);
	}

	/** Fades every pool to now and forgets the gone. */
	private List<Pool> settled(ServerLevel level) {
		long now = level.getGameTime();
		boolean changed = false;
		for (Iterator<Pool> it = pools.iterator(); it.hasNext(); ) {
			Pool pool = it.next();
			if (pool.settledAt == now) continue;
			pool.ml = ChumRules.fade(pool.ml, now - pool.settledAt, dispersesFast(level, pool.centre()));
			pool.settledAt = now;
			changed = true;
			if (pool.ml <= 0.0D) it.remove();
		}
		if (changed) setDirty();
		return pools;
	}

	/** Flowing water at the centre, or rain on it, carries the blood off faster. */
	private static boolean dispersesFast(ServerLevel level, Vec3 centre) {
		BlockPos pos = BlockPos.containing(centre.x, centre.y - 0.1D, centre.z);
		if (!level.isLoaded(pos)) return false;
		FluidState fluid = level.getFluidState(pos);
		return fluid.is(FluidTags.WATER) && !fluid.isSource() || level.isRainingAt(pos.above());
	}

	/** Moves the last settling of the pools holding {@code at} back {@code ticks}, as if that long had passed (tests). */
	public static void backdate(ServerLevel level, Vec3 at, long ticks) {
		ChumPools data = get(level);
		for (Pool pool : data.settled(level)) {
			if (pool.holds(at)) pool.settledAt -= Math.max(0L, ticks);
		}
		data.setDirty();
	}

	// ── Pouring ──

	/** Pours {@code ml} into the water at {@code point}: into the pool that holds it, or a new one. */
	public static Pool pour(ServerLevel level, Vec3 point, double ml, @Nullable EnumBloodTemper school) {
		ChumPools data = get(level);
		Pool pool = null;
		for (Pool candidate : data.settled(level)) {
			if (candidate.holds(point) && (pool == null || candidate.ml > pool.ml)) pool = candidate;
		}
		if (pool == null) {
			pool = new Pool(point.x, point.y, point.z, 0.0D, level.getGameTime(), null);
			data.pools.add(pool);
		}
		pool.x = ChumRules.blend(pool.x, pool.ml, point.x, ml);
		pool.z = ChumRules.blend(pool.z, pool.ml, point.z, ml);
		pool.ml = ChumRules.pour(pool.ml, ml);
		if (school != null) pool.school = school;
		data.setDirty();
		return pool;
	}

	/**
	 * The projection chain's water link: a struck water surface takes the hand's pour. A Temper catalyst in the off hand
	 * dissolves into the pool once, giving it its school. Returns the blood paid; empty when the hit is not water.
	 */
	public static java.util.OptionalDouble tryProject(ServerPlayer player, BlockHitResult hit) {
		if (!(player.level() instanceof ServerLevel level)) return java.util.OptionalDouble.empty();
		BlockPos pos = hit.getBlockPos();
		if (!level.getFluidState(pos).is(FluidTags.WATER) || level.getBlockEntity(pos) != null) return java.util.OptionalDouble.empty();
		double ml = ChumRules.ML_PER_TICK;
		if (!BloodAccounting.canPayInFull(player, ml, CHUM)) return java.util.OptionalDouble.of(0.0D);
		BloodAccounting.pay(player, ml, CHUM);
		Vec3 point = new Vec3(hit.getLocation().x, surface(level, pos), hit.getLocation().z);
		Pool existing = at(level, point);
		EnumBloodTemper school = null;
		ItemStack off = player.getOffhandItem();
		TemperCatalysts.Catalyst catalyst = TemperCatalysts.of(off);
		if (catalyst != null && (existing == null || existing.school != catalyst.school())) {
			school = catalyst.school();
			if (!player.getAbilities().instabuild) off.shrink(1);
			player.displayClientMessage(Component.translatable("message.hemomancy.chum.coloured",
					Component.translatable("blood_temper.hemomancy." + school.name().toLowerCase(Locale.ROOT)))
					.withStyle(ChatFormatting.DARK_RED), true);
		}
		pour(level, point, ml, school);
		WorldAnswer.chumPoured(level, pos, ml);
		level.sendParticles(HemoParticleData.bloodCell(BLOOM_BRIGHT), point.x, point.y - 0.05D, point.z, 2, 0.15D, 0.02D, 0.15D, 0.01D);
		if (level.getGameTime() % 10L == 0L) {
			level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.PLAYERS, 0.3F, 0.6F);
		}
		return java.util.OptionalDouble.of(ml);
	}

	/** The height of the water's surface over the water block at {@code pos}. */
	private static double surface(ServerLevel level, BlockPos pos) {
		BlockPos top = pos;
		while (level.getFluidState(top.above()).is(FluidTags.WATER) && top.getY() < pos.getY() + 16) top = top.above();
		return top.getY() + level.getFluidState(top).getHeight(level, top);
	}

	/** How many water blocks stand under the surface at {@code centre}. */
	public static int depth(ServerLevel level, Vec3 centre) {
		BlockPos pos = BlockPos.containing(centre.x, centre.y - 0.1D, centre.z);
		int depth = 0;
		while (depth < 16 && level.getFluidState(pos.below(depth)).is(FluidTags.WATER)) depth++;
		return depth;
	}

	// ── The world's side ──

	/** A level's few-times-a-second work: settle, bloom near players, and now and then draw a fish. */
	public static void tick(ServerLevel level) {
		ChumPools data = level.getDataStorage().get(FACTORY, DATA_NAME);
		if (data == null || data.pools.isEmpty()) return;
		long now = level.getGameTime();
		if (now % 10L != 0L) return;
		for (Pool pool : List.copyOf(data.settled(level))) {
			Vec3 centre = pool.centre();
			if (!level.isLoaded(BlockPos.containing(centre)) || level.getNearestPlayer(centre.x, centre.y, centre.z, 48.0D, false) == null) continue;
			bloom(level, pool, level.random);
			if (now % ChumRules.DRAW_INTERVAL_TICKS == 0L && level.random.nextFloat() < ChumRules.DRAW_CHANCE) {
				draw(level, pool, level.random);
				// The world's answer (Track 10): a strong pool also draws one of the sea's hunters.
				if (WorldAnswer.scavengersAwake(level)) {
					WorldAnswer.drawHunter(level, centre, pool.radius(), pool.strength(), depth(level, centre), level.random);
				}
			}
		}
	}

	/**
	 * A slow red bloom: the toolkit's pool laid thin and partly clear across the water out to the pool's radius, with
	 * dark threads sinking under it ({@code Form.CHUM_BLOOM}, resent while it lasts so the blooms overlap), and a few
	 * blood cells breaking the surface, more as the pool is stronger. A coloured pool's cells take its school's tint.
	 */
	public static void bloom(ServerLevel level, Pool pool, RandomSource random) {
		double r = pool.radius();
		Vec3 centre = pool.centre();
		if (level.getGameTime() % BLOOM_RESEND_TICKS == 0L) {
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.CHUM_BLOOM, centre, centre, r, BLOOM_TICKS,
					(int) Math.round(100.0D * pool.strength()));
		}
		int cells = 2 + (int) Math.round(4.0D * pool.strength());
		ParticleColor tint = pool.school == null ? BLOOM : blend(BLOOM, pool.school.getColor());
		for (int i = 0; i < cells; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double dist = Math.sqrt(random.nextDouble()) * r * 0.8D;
			level.sendParticles(HemoParticleData.bloodCell(i % 2 == 0 ? BLOOM_BRIGHT : tint), pool.x + Math.cos(angle) * dist,
					pool.y - 0.05D, pool.z + Math.sin(angle) * dist, 1, 0.05D, 0.0D, 0.05D, 0.002D);
		}
	}

	private static ParticleColor blend(ParticleColor blood, ParticleColor school) {
		return new ParticleColor((blood.getRed() * 2 + school.getRed()) / 3, (blood.getGreen() * 2 + school.getGreen()) / 3,
				(blood.getBlue() * 2 + school.getBlue()) / 3);
	}

	/**
	 * A strong pool in deep enough water draws a fish to it: a pelagic herring, at most {@link ChumRules#DRAW_CAP}
	 * near the pool. Hostile draws are the world's answer ({@code WorldAnswer.drawHunter}). Returns the creature drawn, or null.
	 */
	@Nullable
	public static Entity draw(ServerLevel level, Pool pool, RandomSource random) {
		Vec3 centre = pool.centre();
		if (!ChumRules.mayDraw(pool.strength(), depth(level, centre))) return null;
		double r = pool.radius();
		AABB around = new AABB(centre, centre).inflate(r, DEPTH_REACH, r);
		if (level.getEntities(EntityInit.pelagic_herring.get(), around, entity -> true).size() >= ChumRules.DRAW_CAP) return null;
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double dist = random.nextDouble() * r * 0.8D;
		BlockPos at = BlockPos.containing(centre.x + Math.cos(angle) * dist, centre.y - 1.5D, centre.z + Math.sin(angle) * dist);
		if (!level.getFluidState(at).is(FluidTags.WATER)) at = BlockPos.containing(centre.x, centre.y - 1.5D, centre.z);
		if (!level.getFluidState(at).is(FluidTags.WATER)) return null;
		return EntityInit.pelagic_herring.get().spawn(level, at, MobSpawnType.EVENT);
	}

	// ── Saving ──

	public static ChumPools load(CompoundTag tag, HolderLookup.Provider provider) {
		ChumPools data = new ChumPools();
		ListTag list = tag.getList("pools", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag pool = list.getCompound(i);
			EnumBloodTemper school = null;
			if (pool.contains("school")) {
				try {
					school = EnumBloodTemper.valueOf(pool.getString("school"));
				} catch (IllegalArgumentException ignored) {
					// An unknown school leaves the pool plain.
				}
			}
			data.pools.add(new Pool(pool.getDouble("x"), pool.getDouble("y"), pool.getDouble("z"), pool.getDouble("ml"),
					pool.getLong("settled_at"), school));
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (Pool pool : pools) {
			CompoundTag entry = new CompoundTag();
			entry.putDouble("x", pool.x);
			entry.putDouble("y", pool.y);
			entry.putDouble("z", pool.z);
			entry.putDouble("ml", pool.ml);
			entry.putLong("settled_at", pool.settledAt);
			if (pool.school != null) entry.putString("school", pool.school.name());
			list.add(entry);
		}
		tag.put("pools", list);
		return tag;
	}
}
