package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodGougeBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.world.ground.GroundFunction;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * Sanguine Magnetism as a Sculpting of Absorption (core-loop Track 6), the item draw: held toward items lying on the
 * ground, every dropped item within reach slides toward the player and is picked up on arrival. Items slide faster
 * over a wet stain or a lit iron-fed (pulling) gouge. It works on a full body, since Absorption Sculptings run before the
 * full-body stop.
 */
public final class ItemDrawSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 2.0D;
	public static final int CADENCE = 5;
	public static final double REACH_SCALE = 2.0D;
	/** Blocks a tick an item slides by hand; doubled over blood. */
	public static final double SLIDE = 0.3D;
	private static final double ARRIVED = 1.4D;
	private static final ParticleColor IRON = new ParticleColor(150, 40, 44);

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.ITEMS);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Items items && !items.items().isEmpty();
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(player.level() instanceof ServerLevel level)) return;
		double reach = SculptingDispatcher.reach(player, Verb.ABSORB, this);
		Vec3 home = player.position().add(0.0D, 0.4D, 0.0D);
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(player.blockPosition()).inflate(reach),
				item -> item.isAlive() && item.distanceToSqr(player) <= reach * reach)) {
			Vec3 toward = home.subtract(item.position());
			double distance = toward.length();
			if (distance <= ARRIVED) {
				item.setNoPickUpDelay();
				item.playerTouch(player);
				continue;
			}
			double speed = SLIDE * Math.max(0.0D, pulse.workScale()) * (overBlood(level, item.blockPosition()) ? 2.0D : 1.0D);
			Vec3 slide = toward.scale(Math.min(speed, distance) / distance);
			item.setDeltaMovement(slide.x, Math.max(slide.y, item.getDeltaMovement().y), slide.z);
			item.hurtMarked = true;
			if (level.random.nextInt(4) == 0) {
				level.sendParticles(HemoParticleData.glow(IRON), item.getX(), item.getY() + 0.15D, item.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	/** A wet stain or a lit pulling gouge under the item: blood it slides along faster. */
	public static boolean overBlood(Level level, BlockPos cell) {
		BlockState state = level.getBlockState(cell);
		if (state.getBlock() instanceof BloodStainBlock) {
			return MultifaceBlock.hasFace(state, Direction.DOWN) && state.getValue(BloodStainBlock.AGE) == GroundRunRules.WET;
		}
		return state.getBlock() instanceof BloodGougeBlock && state.getValue(BloodGougeBlock.LIT)
				&& state.getValue(BloodGougeBlock.FUNCTION) == GroundFunction.PULL;
	}

	@Override
	public int cadence() {
		return CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}

	@Override
	public double reachScale() {
		return REACH_SCALE;
	}
}
