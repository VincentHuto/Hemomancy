package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendon.TendonAnchor;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieLeash;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbReach;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncTendonAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * The Tendon Line, two verbs on one manipulation. Projection ({@link Throw}) throws the line at the face the player
 * aims at and roots it there; thrown again, it moves. Absorption ({@link Haul}) reels the player toward the rooted
 * line for as long as it is held, wherever they look. The line snaps when the player gets too far from it, when its
 * block can no longer hold it, or when they arrive. The anchor lives on the player ({@link TendonAnchor}), synced to
 * the owner through {@code PlayerStateSync} and to the players tracking them. A haul is not flight, so {@code FlightArbiter} is not involved; it clears
 * fall distance every tick it pulls, as the old item did. Thrown into a creature that may be kept, the line roots in
 * it and leads it home ({@link MenagerieLeash}); held Absorption on a creature on the line reels it in.
 */
public final class TendonLineSculpting {
	private TendonLineSculpting() {
	}

	/** How far the line is thrown for {@code player}: three times the Projection reach. */
	public static double anchorReach(Player player) {
		return VerbReach.of(player, Verb.PROJECT) * TendonLineRules.REACH_SCALE;
	}

	/** Whether {@code face} of the block at {@code pos} can hold the line. Safe on both sides. */
	public static boolean canHold(Level level, BlockPos pos, Direction face) {
		return level.isLoaded(pos) && level.getBlockState(pos).isFaceSturdy(level, pos, face);
	}

	/**
	 * Sends the owner and the players tracking them where the line is rooted; drops an anchor left in another
	 * dimension first. Onlookers draw the cord from it.
	 */
	public static void sync(ServerPlayer player) {
		TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
		if (anchor.isSet() && !anchor.isSetIn(player.level().dimension())) anchor.clear();
		PacketHandler.sendToPlayerAndTrackers(player, PacketSyncTendonAnchor.of(player, anchor));
	}

	/** A player who starts tracking {@code target} is told where its line is rooted, if it is. */
	public static void sendTo(ServerPlayer watcher, Player target) {
		TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(target);
		if (anchor.isSetIn(target.level().dimension())) {
			PacketHandler.sendToPlayer(watcher, PacketSyncTendonAnchor.of(target, anchor));
		}
	}

	/** Loosens the line and tells the owner. */
	public static void loosen(ServerPlayer player, String reasonKey) {
		TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
		if (!anchor.isSet()) return;
		anchor.clear();
		sync(player);
		if (reasonKey != null) {
			player.displayClientMessage(Component.translatable(reasonKey).withStyle(ChatFormatting.GRAY), true);
		}
		player.level().playSound(null, player.blockPosition(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.45F, 0.85F);
	}

	/** Projection: throw the line at the aimed face and root it there. */
	public static final class Throw implements VerbSculpting {
		@Override
		public Set<VerbTargetKind> wants() {
			return Set.of(VerbTargetKind.BLOCK, VerbTargetKind.CREATURE);
		}

		@Override
		public boolean claims(Player player, VerbTarget target) {
			if (target instanceof VerbTarget.Creature creature) {
				// Thrown into a creature the line roots in it and leads it (core-loop Track 5, the menagerie).
				return creature.living() instanceof Mob mob && !MenagerieLeash.leads(player, mob);
			}
			return target instanceof VerbTarget.Block block
					&& canHold(player.level(), block.pos(), block.hit().getDirection());
		}

		@Override
		public boolean admits(ServerPlayer player, VerbTarget target) {
			if (!(target instanceof VerbTarget.Creature creature) || !(creature.living() instanceof Mob mob)) return true;
			Component refusal = MenagerieLeash.refusal(player, mob);
			if (refusal == null) return true;
			player.displayClientMessage(refusal, true);
			return false;
		}

		@Override
		public int cadence() {
			return TendonLineRules.THROW_CADENCE;
		}

		@Override
		public boolean drawsOwnStream() {
			return true;
		}

		@Override
		public double bloodPerPulse() {
			return TendonLineRules.THROW_BLOOD;
		}

		@Override
		public double reachScale() {
			return TendonLineRules.REACH_SCALE;
		}

		@Override
		public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
			if (target instanceof VerbTarget.Creature creature && creature.living() instanceof Mob mob) {
				if (MenagerieLeash.tieLine(player, mob) && player.level() instanceof net.minecraft.server.level.ServerLevel level) {
					ManipulationVisuals.burst(level, ManipulationVisuals.Form.THREAD, player.getEyePosition(),
							mob.getBoundingBox().getCenter(), 0.35D, 12);
				}
				return;
			}
			if (!(target instanceof VerbTarget.Block block)) return;
			BlockPos pos = block.pos();
			Direction face = block.hit().getDirection();
			TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
			boolean moved = !anchor.isSetIn(player.level().dimension()) || !pos.equals(anchor.pos()) || face != anchor.face();
			anchor.set(pos, face, player.level().dimension());
			sync(player);
			if (!moved) return;
			Vec3 point = TendonAnchor.target(pos, face);
			player.displayClientMessage(Component.translatable("message.hemomancy.tendon_line.anchor",
					pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.DARK_RED), true);
			player.level().playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 0.65F, 0.75F);
			if (player.level() instanceof net.minecraft.server.level.ServerLevel level) {
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.THREAD, player.getEyePosition(), point, 0.35D, 12);
			}
		}
	}

	/**
	 * Absorption: reel the player toward the rooted line while it is held, wherever they look; aimed at a creature they
	 * lead, reel that creature in instead.
	 */
	public static final class Haul implements VerbSculpting {
		@Override
		public Set<VerbTargetKind> wants() {
			// Whatever the player looks at, the line is what they pull on: any block, or open air.
			return Set.of(VerbTargetKind.BLOCK, VerbTargetKind.CREATURE, VerbTargetKind.AIR);
		}

		@Override
		public boolean claims(Player player, VerbTarget target) {
			if (target instanceof VerbTarget.Creature creature && creature.living() instanceof Mob mob
					&& MenagerieLeash.leads(player, mob)) return true;
			TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
			return anchor.isSetIn(player.level().dimension());
		}

		@Override
		public int cadence() {
			return TendonLineRules.HAUL_CADENCE;
		}

		@Override
		public boolean drawsOwnStream() {
			return true;
		}

		@Override
		public double bloodPerPulse() {
			return TendonLineRules.HAUL_BLOOD_PER_TICK;
		}

		@Override
		public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
			if (target instanceof VerbTarget.Creature creature && creature.living() instanceof Mob mob
					&& MenagerieLeash.leads(player, mob)) {
				reelIn(player, mob);
				return;
			}
			TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
			if (!anchor.isSetIn(player.level().dimension())) return;
			if (!canHold(player.level(), anchor.pos(), anchor.face())) {
				loosen(player, "message.hemomancy.tendon_line.lost");
				return;
			}
			Vec3 to = anchor.target();
			Vec3 from = player.position().add(0.0D, player.getBbHeight() * 0.55D, 0.0D);
			double distance = from.distanceTo(to);
			if (TendonLineRules.shouldDetach(distance, anchorReach(player))) {
				loosen(player, "message.hemomancy.tendon_line.snapped");
				return;
			}
			player.fallDistance = 0.0F;
			player.resetFallDistance();
			if (TendonLineRules.hasArrived(distance)) {
				loosen(player, null);
				return;
			}
			TendonLineRules.Motion pull = TendonLineRules.pullMotion(from.x, from.y, from.z, to.x, to.y, to.z);
			Vec3 existing = player.getDeltaMovement().scale(TendonLineRules.EXISTING_MOTION_BLEND);
			player.setDeltaMovement(existing.add(pull.x(), pull.y(), pull.z()));
			player.hasImpulse = true;
			player.hurtMarked = true;
		}
	}

	/** Pulls a creature on the line toward its keeper, stopping when it reaches them. */
	static void reelIn(ServerPlayer keeper, Mob mob) {
		Vec3 from = mob.position().add(0.0D, mob.getBbHeight() * 0.5D, 0.0D);
		Vec3 to = keeper.position().add(0.0D, keeper.getBbHeight() * 0.55D, 0.0D);
		if (TendonLineRules.hasArrived(from.distanceTo(to))) return;
		TendonLineRules.Motion pull = TendonLineRules.pullMotion(from.x, from.y, from.z, to.x, to.y, to.z);
		Vec3 existing = mob.getDeltaMovement().scale(TendonLineRules.EXISTING_MOTION_BLEND);
		mob.setDeltaMovement(existing.add(pull.x(), pull.y(), pull.z()));
		mob.getNavigation().stop();
		mob.resetFallDistance();
		mob.hasImpulse = true;
		mob.hurtMarked = true;
	}
}
