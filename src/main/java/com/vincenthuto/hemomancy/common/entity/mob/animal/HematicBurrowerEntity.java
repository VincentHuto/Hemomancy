package com.vincenthuto.hemomancy.common.entity.mob.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;

public class HematicBurrowerEntity extends PathfinderMob {
	private int frightenedTicks;
	private int diggingTicks;

	public HematicBurrowerEntity(EntityType<? extends HematicBurrowerEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 8.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.34D)
				.add(Attributes.FOLLOW_RANGE, 12.0D);
	}

	public static boolean canSpawnHere(EntityType<? extends HematicBurrowerEntity> type, ServerLevelAccessor level,
			MobSpawnType spawnType, BlockPos pos, RandomSource random) {
		BlockPos below = pos.below();
		boolean solidBelow = level.getBlockState(below).isSolidRender(level, below);
		boolean openAir = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
				&& level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
		int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
		return HematicBurrowerRules.canNaturalSpawn(solidBelow, openAir, level.canSeeSky(pos),
				level.getMaxLocalRawBrightness(pos), pos.getY(), surfaceY);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new FleeAnyMobGoal(this));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			return;
		}
		if (this.diggingTicks > 0) {
			this.diggingTicks--;
			this.spawnDigParticles();
			if (this.diggingTicks == 0) {
				this.dropEscapeOre();
				this.discard();
			}
			return;
		}
		LivingEntity threat = this.findThreat();
		if (threat != null) {
			this.frightenedTicks++;
			if (HematicBurrowerRules.shouldBurrowAway(this.frightenedTicks)) {
				this.startDigging();
			}
		} else {
			this.frightenedTicks = Math.max(0, this.frightenedTicks - 2);
		}
	}

	private void startDigging() {
		this.diggingTicks = HematicBurrowerRules.DIG_PARTICLE_TICKS;
		this.getNavigation().stop();
		this.level().playSound(null, this.blockPosition(), SoundEvents.GRAVEL_BREAK, SoundSource.NEUTRAL,
				0.8F, 0.65F + this.random.nextFloat() * 0.2F);
	}

	private void spawnDigParticles() {
		if (this.level() instanceof ServerLevel serverLevel) {
			BlockPos below = this.blockPosition().below();
			serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level().getBlockState(below)),
					this.getX(), this.getY() + 0.1D, this.getZ(),
					18, 0.45D, 0.18D, 0.45D, 0.05D);
		}
	}

	private void dropEscapeOre() {
		if (this.random.nextInt(3) != 0) {
			return;
		}
		String id = HematicBurrowerRules.escapeDropByRoll(this.random.nextInt(HematicBurrowerRules.totalEscapeDropWeight()));
		Item item = switch (id) {
			case "minecraft:raw_copper" -> Items.RAW_COPPER;
			case "minecraft:raw_iron" -> Items.RAW_IRON;
			default -> Items.COAL;
		};
		this.spawnAtLocation(new ItemStack(item));
	}

	private LivingEntity findThreat() {
		AABB bounds = this.getBoundingBox().inflate(HematicBurrowerRules.FLEE_RANGE_BLOCKS);
		return this.level().getEntitiesOfClass(LivingEntity.class, bounds, this::isThreat)
				.stream()
				.min(Comparator.comparingDouble(this::distanceToSqr))
				.orElse(null);
	}

	private boolean isThreat(LivingEntity living) {
		if (living == this || living instanceof HematicBurrowerEntity) {
			return false;
		}
		boolean creative = living instanceof Player player && player.isCreative();
		boolean spectator = living instanceof Player player && player.isSpectator();
		return HematicBurrowerRules.shouldFlee(living.isAlive(), creative, spectator, this.distanceTo(living));
	}

	@Override
	protected float getSoundVolume() {
		return 0.2F;
	}

	private static final class FleeAnyMobGoal extends Goal {
		private final HematicBurrowerEntity burrower;
		private LivingEntity threat;

		private FleeAnyMobGoal(HematicBurrowerEntity burrower) {
			this.burrower = burrower;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			this.threat = this.burrower.findThreat();
			return this.threat != null && this.burrower.diggingTicks <= 0;
		}

		@Override
		public void tick() {
			if (this.threat == null) {
				return;
			}
			Vec3 target = DefaultRandomPos.getPosAway(this.burrower, 9, 4, this.threat.position());
			if (target != null) {
				this.burrower.getNavigation().moveTo(target.x, target.y, target.z,
						HematicBurrowerRules.FLEE_NAVIGATION_SPEED);
			}
		}
	}
}
