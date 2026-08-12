package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.hematic.LivingFlailItemRenderer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.projectile.LivingFlailHeadProjectileEntity;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class LivingFlailItem extends LivingToolItem implements HemoClientItemExtensionsProvider {
	public static final double CHARGING_MOVEMENT_SCALE = LivingFlailRules.CHARGING_MOVEMENT_SCALE;
	private static final ParticleColor ICE_BLUE = new ParticleColor(95, 205, 255);

	public LivingFlailItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.9f, EnumBloodTendency.CONGEATIO, tier, builderIn);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (LivingStaffWeaponFormHelper.wasRestoredOutOfHand(stack, attacker)) {
			return true;
		}
		target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
		return true;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (LivingFlailDeployment.isDeployed(stack)) return InteractionResultHolder.fail(stack);
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(stack);
	}

	@Override
	public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
		if (!(living instanceof Player player) || LivingFlailDeployment.isDeployed(stack)) {
			living.releaseUsingItem();
			return;
		}
		Vec3 movement = player.getDeltaMovement();
		player.setDeltaMovement(movement.x * CHARGING_MOVEMENT_SCALE, movement.y,
				movement.z * CHARGING_MOVEMENT_SCALE);
		int usedTicks = getUseDuration(stack, living) - remainingUseDuration;
		if (!level.isClientSide && LivingFlailRules.shouldPlayMaximumCue(usedTicks)) {
			level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
					1.25F, 0.62F);
			if (level instanceof ServerLevel server) {
				server.sendParticles(GlowParticleFactory.createData(ICE_BLUE), player.getX(),
						player.getEyeY() - 0.35D, player.getZ(), 18, 0.45D, 0.25D, 0.45D, 0.04D);
			}
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
		if (!(living instanceof Player player) || level.isClientSide) return;
		InteractionHand hand = player.getUsedItemHand();
		if (player.getItemInHand(hand) != stack || LivingFlailDeployment.isDeployed(stack)) return;
		int usedTicks = getUseDuration(stack, living) - timeLeft;
		if (!LivingFlailRules.mayFire(usedTicks)) return;
		float charge = LivingFlailRules.charge(usedTicks);
		LivingFlailHeadProjectileEntity projectile = new LivingFlailHeadProjectileEntity(level, player);
		projectile.configure(charge, hand, EnumBloodTendency.CONGEATIO,
				TendencyWeaponHelper.getWeaponSecondaryTendency(stack).orElse(null));
		Vec3 look = player.getLookAngle().normalize();
		double angle = LivingFlailRules.orbitAngle(usedTicks, 0.0F);
		HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
		LivingFlailRules.LaunchGeometry geometry = LivingFlailRules.launchGeometry(look, angle,
				arm == HumanoidArm.RIGHT);
		Vec3 start = player.getEyePosition().add(geometry.offset());
		projectile.setPos(start);
		Vec3 launch = LivingFlailRules.launchDirection(look, geometry.tangent(), charge)
				.scale(LivingFlailRules.launchSpeed(charge));
		projectile.setDeltaMovement(launch);
		projectile.setYRot((float) (Math.atan2(launch.x, launch.z) * 180.0D / Math.PI));
		projectile.setXRot((float) (Math.atan2(launch.y, launch.horizontalDistance()) * 180.0D / Math.PI));
		LivingFlailDeployment.markDeployed(stack, projectile.getDeploymentId());
		if (!level.addFreshEntity(projectile)) LivingFlailDeployment.clear(stack, projectile.getDeploymentId());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.living_flail.charge"));
		tooltip.add(Component.translatable("item.hemomancy.living_flail.release"));
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return RenderPropLivingFlail.INSTANCE;
	}
}

class RenderPropLivingFlail implements IClientItemExtensions {
	public static final RenderPropLivingFlail INSTANCE = new RenderPropLivingFlail();

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new LivingFlailItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
