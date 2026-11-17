package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.HeldSkills;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandCue;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;

/**
 * Blood Needle Lance under held Projection: a hovering volley. While Projection is held, barbed iron needles form round
 * the caster one at a time, each paid pulse another, up to six. Letting go looses every needle that has formed at what
 * the caster is looking at, in a tight group; the attack button looses one early. They are the R cast's own lance
 * needles: forged, barbed, piercing, and they pin what they strike.
 */
public final class NeedleVolleySculpting implements VerbSculpting {
	/** The most needles that hover at once. */
	public static final int MAX_NEEDLES = 6;
	/** Blood a pulse (a needle) costs before the cost ledger: 35 mL a second. */
	public static final double BLOOD_PER_PULSE = 17.5D;
	/** A loosed needle's base damage and speed: the R cast's lance needle. */
	public static final double NEEDLE_DAMAGE = 0.75D;
	public static final float NEEDLE_SPEED = 8.0F;
	/** How far apart the needles of one volley start, so six do not leave from one point. */
	public static final double SPREAD = 0.28D;
	/** How fast the caster walks with a volley hovering, as a share of their pace. */
	public static final double PACE = 0.75D;

	private static final Map<UUID, Integer> HOVERING = new HashMap<>();
	private static final int CUE_TICKS = DEFAULT_CADENCE + 8;

	@Override
	public Set<VerbTargetKind> wants() {
		return EnumSet.of(VerbTargetKind.CREATURE, VerbTargetKind.BLOCK, VerbTargetKind.AIR);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return !(target instanceof VerbTarget.Block block) || !StrandSculpting.takesBlood(player.level(), block);
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}

	/** A full volley costs nothing more to keep hovering. */
	@Override
	public double bloodPerPulse(Player player) {
		return hovering(player) >= MAX_NEEDLES ? 0.0D : BLOOD_PER_PULSE;
	}

	@Override
	public boolean drawsOwnStream() {
		return true;
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
	public void begin(ServerPlayer player, VerbTarget target) {
		HOVERING.put(player.getUUID(), 0);
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		int count = hovering(player);
		if (count < MAX_NEEDLES) {
			HOVERING.put(player.getUUID(), ++count);
			player.level().playSound(null, player.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.5F, 1.3F + 0.1F * count);
		}
		show(player, count, CUE_TICKS);
	}

	/** The needles round the caster: a keyed cue whose size is how many hover. */
	private static void show(ServerPlayer player, int count, int ticks) {
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.STRAND_ACCENT, player.position(), count, ticks,
				StrandCue.encodeAccent(StrandCue.Accent.VOLLEY, EnumBloodTemper.FERRIC, EnumBloodTemper.ANIMUS));
	}

	/** The attack button looses one needle early. */
	@Override
	public boolean accent(ServerPlayer player) {
		int count = hovering(player);
		if (count <= 0 || !loose(player, 1, true)) return false;
		HOVERING.put(player.getUUID(), count - 1);
		show(player, count - 1, CUE_TICKS);
		return true;
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		Integer count = HOVERING.remove(player.getUUID());
		if (count == null) return;
		show(player, 0, 0);
		if (count > 0 && player.isAlive()) loose(player, count, false);
	}

	/** Looses {@code count} lance needles from beside the caster's head along their look; an accent's needle takes Whipcrack. */
	private static boolean loose(ServerPlayer player, int count, boolean accent) {
		BloodManipulation lance = manipulation(player);
		if (lance == null) lance = ManipulationInit.getByName("blood_needle_lance");
		if (lance == null || !(player.level() instanceof ServerLevel level)) return false;
		Vec3 look = player.getLookAngle();
		Vec3 side = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
		side = side.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : side.normalize();
		Vec3 up = side.cross(look).normalize();
		float base = (float) (NEEDLE_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player));
		double damage = accent ? HeldSkills.accentDamage(player, base) : base;
		try (var cast = SchoolDamage.cast(lance, player, 1)) {
			for (int i = 0; i < count; i++) {
				// Round a small ring beside the head, as they hovered.
				double angle = i * Math.PI * 2.0D / MAX_NEEDLES;
				Vec3 from = player.getEyePosition().add(side.scale(Math.cos(angle) * SPREAD)).add(up.scale(Math.sin(angle) * SPREAD + 0.15D));
				BloodNeedleEntity needle = BloodNeedleEntity.fromManipulation(level, player, player.getMainHandItem());
				needle.setPos(from);
				needle.setBaseDamage(damage);
				needle.setForged(true);
				needle.setDamageTemper(lance.getTend());
				needle.setSecondaryDamageTemper(lance.getSecondaryTend());
				needle.configurePiercing((byte) 3);
				needle.shoot(look.x, look.y, look.z, NEEDLE_SPEED, 0.25F);
				level.addFreshEntity(needle);
			}
		}
		level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.6F, 1.4F);
		return true;
	}

	/** Needles hovering round {@code player}, or 0 with no volley held. */
	public static int hovering(Player player) {
		return HOVERING.getOrDefault(player.getUUID(), 0);
	}

	private static BloodManipulation manipulation(Player player) {
		SculptingSessions.Session session = SculptingSessions.current(player);
		return session == null ? null : ManipulationInit.getByName(session.manipulation());
	}

	public static void clearSessionState() {
		HOVERING.clear();
	}

	public static void logout(Player player) {
		HOVERING.remove(player.getUUID());
	}
}
