package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.entity.projectile.CruorJavelinEntity;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.HeldSkills;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
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
 * Cruor Javelin under held Projection: the javelin is drawn, then thrown. Holding draws frozen cruor into the hand as a
 * shaft that lengthens through three stages; letting go throws it as a real projectile ({@link CruorJavelinEntity}),
 * faster and harder the longer it was drawn. A draw let go too soon throws nothing, and the blood is lost. The attack
 * button does nothing here: the throw is the release.
 *
 * <p>Like a strand it opens on open air, a creature or a plain block, never on a block that takes blood.
 */
public final class CruorJavelinThrowSculpting implements VerbSculpting {
	/** One caster's draw: ticks held and blood paid into the shaft so far. */
	private static final class Draw {
		int ticks;
		double paid;
	}

	private static final Map<UUID, Draw> DRAWS = new HashMap<>();
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
		return CruorJavelinThrowRules.BLOOD_PER_PULSE;
	}

	/** It draws itself: the hand pours no ordinary stream toward the aim while this holds the channel. */
	@Override
	public boolean drawsOwnStream() {
		return true;
	}

	@Override
	public boolean holdsChannel() {
		return true;
	}

	@Override
	public OptionalDouble movement(Player player) {
		return HeldSkills.pace(player, CruorJavelinThrowRules.PACE);
	}

	@Override
	public double reachScale(Player player) {
		return HeldSkills.reach(player);
	}

	@Override
	public void begin(ServerPlayer player, VerbTarget target) {
		DRAWS.put(player.getUUID(), new Draw());
		player.level().playSound(null, player.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.6F, 1.3F);
	}

	@Override
	public void hold(ServerPlayer player, VerbTarget aimed) {
		Draw draw = DRAWS.get(player.getUUID());
		if (draw == null) return;
		int before = CruorJavelinThrowRules.stage(draw.ticks);
		draw.ticks++;
		// Each stage sets with a sound, so the hand can count them without watching.
		if (CruorJavelinThrowRules.stage(draw.ticks) > before) {
			player.level().playSound(null, player.blockPosition(), SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 0.7F,
					0.8F + 0.25F * CruorJavelinThrowRules.stage(draw.ticks));
			show(player, draw);
		}
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		Draw draw = DRAWS.get(player.getUUID());
		if (draw == null) return;
		draw.paid += pulse.paid();
		show(player, draw);
	}

	/** The shaft in the hand: a keyed cue on the caster whose size is how far the draw has come. */
	private static void show(ServerPlayer player, Draw draw) {
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.STRAND_ACCENT, player.position(),
				CruorJavelinThrowRules.length(CruorJavelinThrowRules.stage(draw.ticks)), CUE_TICKS,
				StrandCue.encodeAccent(StrandCue.Accent.JAVELIN, EnumBloodTemper.CONGEATIO, EnumBloodTemper.FERRIC));
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		Draw draw = DRAWS.remove(player.getUUID());
		if (draw == null) return;
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.STRAND_ACCENT, player.position(), 0.0D, 0,
				StrandCue.encodeAccent(StrandCue.Accent.JAVELIN, EnumBloodTemper.CONGEATIO, EnumBloodTemper.FERRIC));
		if (!CruorJavelinThrowRules.throwable(draw.ticks) || !player.isAlive() || !(player.level() instanceof ServerLevel level)) return;
		Vec3 from = StrandCue.hand(player.getEyePosition(), player.getLookAngle(), player.getYRot(), false).add(0.0D, 0.25D, 0.0D);
		CruorJavelinEntity.thrown(level, player, from, CruorJavelinThrowRules.stage(draw.ticks), draw.paid);
		level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
	}

	/** Ticks {@code player} has drawn their javelin, or 0 with none in hand. */
	public static int drawnTicks(Player player) {
		Draw draw = DRAWS.get(player.getUUID());
		return draw == null ? 0 : draw.ticks;
	}

	public static void clearSessionState() {
		DRAWS.clear();
	}

	public static void logout(Player player) {
		DRAWS.remove(player.getUUID());
	}
}
