package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * A low four-legged stalker with a cap for a head and a gill-maw beneath it. When the network hears a stranger's
 * footsteps, every hunter within reach comes: the near ones go for the body, the far ones walk to where it was heard.
 * It never seeks out an Apotheos, the organism's own, though it strikes back at anyone who hurts it.
 */
public class HunterFungusEntity extends Monster {
	@Nullable
	private BlockPos alarmPos;
	private long alarmUntil;

	public HunterFungusEntity(EntityType<? extends HunterFungusEntity> type, Level level) {
		super(type, level);
		xpReward = 8;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 26.0D)
				.add(Attributes.ATTACK_DAMAGE, 5.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.27D)
				.add(Attributes.FOLLOW_RANGE, 48.0D)
				.add(Attributes.ARMOR, 2.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.3D);
	}

	/** Every hunter within reach of a heard stranger answers: pounce when near, converge on the spot when far. */
	public static void converge(ServerPlayer player) {
		long now = player.level().getGameTime();
		for (HunterFungusEntity hunter : player.level().getEntitiesOfClass(HunterFungusEntity.class,
				player.getBoundingBox().inflate(HunterFungusRules.CONVERGE_RADIUS), Entity::isAlive)) {
			switch (HunterFungusRules.respond(hunter.getTarget() != null, hunter.distanceToSqr(player))) {
				case TARGET -> hunter.setTarget(player);
				case CONVERGE -> hunter.hearAlarm(player.blockPosition(), now);
				case IGNORE -> { }
			}
		}
	}

	public void hearAlarm(BlockPos pos, long now) {
		alarmPos = pos.immutable();
		alarmUntil = now + HunterFungusRules.MEMORY_TICKS;
	}

	@Nullable
	public BlockPos alarmPos() {
		return alarmPos;
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
		goalSelector.addGoal(3, new ConvergeOnAlarmGoal(this));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
				target -> target instanceof Player player && HunterFungusRules.mayHunt(
						ApotheosBodyEvents.isApotheos(player), player.isCreative() || player.isSpectator())));
	}

	/** Walks to where the network last heard a stranger, until it arrives, finds a target, or forgets. */
	private static final class ConvergeOnAlarmGoal extends Goal {
		private final HunterFungusEntity hunter;
		private int sinceRepath;

		ConvergeOnAlarmGoal(HunterFungusEntity hunter) {
			this.hunter = hunter;
			setFlags(EnumSet.of(Flag.MOVE));
		}

		private boolean listening() {
			BlockPos pos = hunter.alarmPos;
			if (pos == null || hunter.getTarget() != null || hunter.level().getGameTime() > hunter.alarmUntil) return false;
			double arrive = HunterFungusRules.ARRIVE_DISTANCE;
			return hunter.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) > arrive * arrive;
		}

		@Override
		public boolean canUse() {
			return listening();
		}

		@Override
		public boolean canContinueToUse() {
			return listening();
		}

		@Override
		public void start() {
			sinceRepath = 0;
		}

		@Override
		public void tick() {
			if (!HunterFungusRules.shouldRepath(sinceRepath++)) return;
			BlockPos pos = hunter.alarmPos;
			if (pos != null) hunter.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1.2D);
		}

		@Override
		public void stop() {
			hunter.getNavigation().stop();
			if (!listening()) hunter.alarmPos = null;
		}
	}
}
