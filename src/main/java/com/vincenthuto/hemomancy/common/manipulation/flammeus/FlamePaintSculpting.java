package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.HeldSkills;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandCue;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;

/**
 * Crimson Flame Conjuration under held Projection: painted fire. Held on the ground, crimson flame is laid where the
 * caster looks, a cell at a time as the aim moves, so a line of fire follows the aim. Each flame burns for a few
 * seconds and goes out by itself. The attack button makes the whole line flare: every flame the caster has standing
 * rears up, wounds and sears what stands in it, and is spent. Unlit lights near the aim are still kindled first, by
 * the memory's narrow Sculpting.
 *
 * <p>The flames are the R cast's own block, placed as the caster's edit, so a claim that refuses them refuses these.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class FlamePaintSculpting implements VerbSculpting {
	/** Blood a pulse costs before the cost ledger: 35 mL a second. */
	public static final double BLOOD_PER_PULSE = 17.5D;
	/** How long a painted flame burns, and the most a caster may have standing. */
	public static final int FLAME_TICKS = 100, MAX_FLAMES = 24;
	/** The flare's damage before Crimson Mastery, how long it sears, and how high it reaches. */
	public static final float FLARE_DAMAGE = 5.0F;
	public static final int SEARING_TICKS = 120;
	public static final double FLARE_HEIGHT = 3.0D;
	/** How fast the caster walks while painting, as a share of their pace. */
	public static final double PACE = 0.8D;

	/** One painted flame: whose, where, and when it goes out. */
	private record Flame(UUID owner, ResourceKey<Level> dimension, BlockPos pos, long until) {
	}

	private static final List<Flame> FLAMES = new ArrayList<>();
	private static final Map<UUID, Long> LAST_FLARE = new HashMap<>();

	@Override
	public Set<VerbTargetKind> wants() {
		return EnumSet.of(VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Block block && !StrandSculpting.takesBlood(player.level(), block);
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}

	@Override
	public boolean drawsOwnStream() {
		return false;
	}

	@Override
	public boolean holdsChannel() {
		return true;
	}

	@Override
	public boolean takesAccent() {
		return true;
	}

	@Override
	public OptionalDouble movement(Player player) {
		return HeldSkills.pace(player, PACE);
	}

	@Override
	public double reachScale(Player player) {
		return HeldSkills.reach(player);
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
	}

	/** Each tick the aim rests on a block, the cell against the struck face takes a flame if it can. */
	@Override
	public void hold(ServerPlayer player, VerbTarget aimed) {
		if (!(aimed instanceof VerbTarget.Block block) || !(player.level() instanceof ServerLevel level)) return;
		BlockPos at = block.pos().relative(block.hit().getDirection());
		if (standing(player) >= MAX_FLAMES || !level.getBlockState(at).canBeReplaced() || level.getBlockState(at).is(BlockInit.crimson_flames.get())) return;
		// Fire needs something to stand on.
		if (!level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP)) return;
		if (!ManipulationCombatHelper.mayAlter(player, at)
				|| !PlayerBlockEdits.place(player, at, BlockInit.crimson_flames.get().defaultBlockState())) return;
		FLAMES.add(new Flame(player.getUUID(), level.dimension(), at.immutable(), level.getGameTime() + FLAME_TICKS));
		level.playSound(null, at, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.35F, 1.2F);
	}

	/** The flare: every flame of the caster's rears up, strikes what stands in it, and is spent. */
	@Override
	public boolean accent(ServerPlayer player) {
		BloodManipulation flames = manipulation(player);
		if (flames == null || !(player.level() instanceof ServerLevel level)) return false;
		long now = level.getGameTime();
		Long last = LAST_FLARE.get(player.getUUID());
		if (last != null && now - last < 20) return false;
		List<Flame> mine = FLAMES.stream().filter(flame -> flame.owner().equals(player.getUUID()) && flame.dimension() == level.dimension()).toList();
		if (mine.isEmpty()) return false;
		float damage = HeldSkills.accentDamage(player, (float) (FLARE_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player)));
		int searing = HeldSkills.accentState(player, SEARING_TICKS);
		Set<Integer> struck = new HashSet<>();
		try (var cast = SchoolDamage.cast(flames, player, 1)) {
			for (Flame flame : mine) {
				AABB column = new AABB(flame.pos()).inflate(0.35D, 0.0D, 0.35D).expandTowards(0.0D, FLARE_HEIGHT - 1.0D, 0.0D);
				for (LivingEntity body : level.getEntitiesOfClass(LivingEntity.class, column, body -> ManipulationCombatHelper.canHarm(player, body))) {
					if (!struck.add(body.getId())) continue;
					body.invulnerableTime = 0;
					ManipulationCombatHelper.hurt(flames, player, body, level, damage, searing, 1);
				}
				Vec3 foot = Vec3.atBottomCenterOf(flame.pos());
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.STRAND_ACCENT, foot, foot.add(0.0D, 1.0D, 0.0D), 0.8D, 20,
						StrandCue.encodeAccent(StrandCue.Accent.ERUPTION, EnumBloodTemper.FLAMMEUS, EnumBloodTemper.ANIMUS));
				douse(level, flame.pos());
			}
		}
		FLAMES.removeAll(mine);
		LAST_FLARE.put(player.getUUID(), now);
		level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8F, 0.8F);
		return true;
	}

	private static void douse(ServerLevel level, BlockPos pos) {
		if (level.getBlockState(pos).is(BlockInit.crimson_flames.get())) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
	}

	/** Painted flames go out when their time is up. */
	@SubscribeEvent
	public static void tick(ServerTickEvent.Post event) {
		if (FLAMES.isEmpty()) return;
		for (Iterator<Flame> flames = FLAMES.iterator(); flames.hasNext(); ) {
			Flame flame = flames.next();
			ServerLevel level = event.getServer().getLevel(flame.dimension());
			if (level == null) {
				flames.remove();
			} else if (level.getGameTime() >= flame.until()) {
				douse(level, flame.pos());
				flames.remove();
			}
		}
	}

	/** Painted flames {@code player} has standing. */
	public static int standing(Player player) {
		int count = 0;
		for (Flame flame : FLAMES) if (flame.owner().equals(player.getUUID())) count++;
		return count;
	}

	private static BloodManipulation manipulation(Player player) {
		SculptingSessions.Session session = SculptingSessions.current(player);
		return session == null ? null : ManipulationInit.getByName(session.manipulation());
	}

	/** A server starting or stopping forgets its painted flames; any left burning are ordinary crimson flame. */
	public static void clearSessionState() {
		FLAMES.clear();
		LAST_FLARE.clear();
	}

	public static void logout(Player player) {
		LAST_FLARE.remove(player.getUUID());
	}
}
