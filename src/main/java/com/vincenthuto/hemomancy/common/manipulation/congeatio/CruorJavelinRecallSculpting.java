package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.projectile.CruorJavelinEntity;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbReach;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

/**
 * Cruor Javelin under held Absorption: the caster takes a stuck javelin of their own back. Aimed at one within reach,
 * the javelin thaws into the hand and half the blood that was drawn into it returns. It claims nothing else, so with no
 * javelin in the look Absorption is plain Absorption.
 */
public final class CruorJavelinRecallSculpting implements VerbSculpting {
	@Override
	public Set<VerbTargetKind> wants() {
		return EnumSet.of(VerbTargetKind.CREATURE, VerbTargetKind.BLOCK, VerbTargetKind.AIR);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return aimed(player) != null;
	}

	@Override
	public double bloodPerPulse() {
		return 0.0D;
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		CruorJavelinEntity javelin = aimed(player);
		if (javelin == null || !(player.level() instanceof ServerLevel level)) return;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		double back = Math.min(CruorJavelinThrowRules.refund(javelin.paid()), volume.getMaxBloodVolume() - volume.getBloodVolume());
		if (back > 0.0D) {
			volume.addBloodVolume(back);
			BloodVolumeEvents.syncVolume(player, volume);
		}
		javelin.shatter(level, javelin.position());
	}

	/** The caster's own stuck javelin nearest their look, within Absorption's reach, or null. */
	@Nullable
	public static CruorJavelinEntity aimed(Player player) {
		double reach = VerbReach.of(player, Verb.ABSORB);
		Vec3 eye = player.getEyePosition(), look = player.getLookAngle();
		return player.level().getEntitiesOfClass(CruorJavelinEntity.class, new AABB(eye, eye).inflate(reach + 1.0D),
						javelin -> javelin.isAlive() && javelin.stuck() && javelin.getOwner() == player).stream()
				.filter(javelin -> {
					Vec3 to = javelin.position().subtract(eye);
					double along = to.dot(look);
					return along > 0.0D && along <= reach && to.subtract(look.scale(along)).length() <= CruorJavelinThrowRules.RECALL_AIM;
				})
				.min(Comparator.comparingDouble(javelin -> javelin.position().distanceToSqr(eye))).orElse(null);
	}
}
