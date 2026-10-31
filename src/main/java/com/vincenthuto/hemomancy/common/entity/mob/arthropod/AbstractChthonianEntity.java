package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hutoslib.math.MathUtils;
import com.vincenthuto.hemomancy.common.entity.WallClimbPose;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * O-2: the Chthonian family (worker, queen): a termite spider that chews wood and wears down wooden tools.
 * Each kind keeps its own entity type, sounds and tool wear.
 */
public abstract class AbstractChthonianEntity extends Spider {

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	public int puffCooldown = 0;

	/** Client-only: the body turns onto any wall it climbs. */
	private final WallClimbPose climbPose = new WallClimbPose(state -> true);

	protected AbstractChthonianEntity(EntityType<? extends AbstractChthonianEntity> type, Level worldIn) {
		super(type, worldIn);

	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (puffCooldown > 0) {
			--puffCooldown;
		}

	}

	public void attackInBox(AABB box, int disabledShieldTime) {
		List<LivingEntity> attackables = level().getEntitiesOfClass(LivingEntity.class, box,
				entity -> entity != this && !hasPassenger(entity));
		for (LivingEntity attacking : attackables) {
			doHurtTarget(attacking);
			if (disabledShieldTime > 0 && attacking instanceof Player) {
				Player player = ((Player) attacking);
				if (player.isUsingItem()) {
					player.getCooldowns().addCooldown(Items.SHIELD, disabledShieldTime);
					player.stopUsingItem();
					level().broadcastEntityEvent(player, (byte) 9);
				}
			}
		}
	}
	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hit = super.doHurtTarget(target);
		if (hit && target instanceof Player player) {
			ItemStack mainHand = player.getMainHandItem();
			if (!mainHand.isEmpty() && mainHand.getItem() instanceof TieredItem t && t.getTier() == Tiers.WOOD) {
				mainHand.hurtAndBreak(woodenToolWear(), player, EquipmentSlot.MAINHAND);
			}
			ItemStack offHand = player.getOffhandItem();
			if (!offHand.isEmpty() && offHand.getItem() instanceof TieredItem t && t.getTier() == Tiers.WOOD) {
				offHand.hurtAndBreak(woodenToolWear(), player, EquipmentSlot.OFFHAND);
			}
		}
		return hit;
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	/** Durability a hit takes from a wooden tool in either hand. */
	protected abstract int woodenToolWear();

	public boolean isVulnerable() {
		return this.getHealth() <= this.getMaxHealth() / 4.0F;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new ChewWoodGoal(this, 1.2d, 16));
		this.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 0.8D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			this.climbPose.tick(this, isClimbing() || !onGround());
		}
		LivingEntity target = getTarget();
		if (target == null)
			return;
		double distFromTarget = distanceToSqr(target);

		getLookControl().setLookAt(target, getMaxHeadYRot(), getMaxHeadXRot());
		boolean isClose = distFromTarget < 5;
		if (getNavigation().isDone())
			getNavigation().moveTo(target, 1.2);
		if (isClose) {
			yBodyRot = (float) MathUtils.getAngle(AbstractChthonianEntity.this, target) + 90f;
		}
	}

	public WallClimbPose climbPose() {
		return this.climbPose;
	}

}
