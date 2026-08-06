package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.hematic.LivingSickleItemRenderer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class LivingSickleItem extends LivingToolItem implements HemoClientItemExtensionsProvider {
	private static final double SWEEP_RADIUS = 2.75D;

	public LivingSickleItem(float speedIn, float attackDamageIn, Tier tier, Properties properties) {
		super(speedIn, attackDamageIn, -2.8F, EnumBloodTendency.MORTEM, tier, properties);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (LivingSicklePruning.isTemporarySickle(stack)) return true;
		boolean hit = super.hurtEnemy(stack, target, attacker);
		if (!hit || LivingStaffWeaponFormHelper.wasRestoredOutOfHand(stack, attacker)
				|| attacker.level().isClientSide
				|| !(attacker instanceof Player player)) return hit;

		float executionBonus = LivingSickleCombatRules.executionBonus(target.getHealth(), target.getMaxHealth());
		if (target.isAlive() && executionBonus > 0.0F) {
			target.hurt(attacker.damageSources().playerAttack(player), executionBonus);
		}
		if (player.getAttackStrengthScale(0.5F) < 0.9F) return true;

		float sweepDamage = LivingSickleCombatRules.sweepDamage(
				(float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
		AABB area = target.getBoundingBox().inflate(SWEEP_RADIUS, 1.25D, SWEEP_RADIUS);
		for (LivingEntity other : attacker.level().getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity != attacker && entity != target && entity.isAlive()
						&& !entity.isAlliedTo(attacker) && attacker.canAttack(entity))) {
			other.hurt(attacker.damageSources().playerAttack(player), sweepDamage);
		}
		if (attacker.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.5D), target.getZ(),
					3, 0.65D, 0.2D, 0.65D, 0.0D);
			serverLevel.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
					SoundSource.PLAYERS, 0.8F, 0.72F);
		}
		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!LivingSicklePruning.isTemporarySickle(stack)) return super.use(level, player, hand);
		if (!level.isClientSide) {
			player.setItemInHand(hand, LivingSicklePruning.restoredWeaponStack(stack, player.registryAccess()));
		}
		return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return ClientExtensions.INSTANCE;
	}

	private static final class ClientExtensions implements IClientItemExtensions {
		private static final ClientExtensions INSTANCE = new ClientExtensions();

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new LivingSickleItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}
}
