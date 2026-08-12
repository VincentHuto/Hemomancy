package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.hematic.LivingTorchItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PlayerAnimationKind;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.util.CrimsonFireHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.HashSet;
import java.util.Set;

public class LivingTorchItem extends LivingToolItem implements HemoClientItemExtensionsProvider {
	public LivingTorchItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.1f, EnumBloodTendency.FLAMMEUS, tier, builderIn);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (LivingStaffWeaponFormHelper.wasRestoredOutOfHand(stack, attacker)) {
			return true;
		}
		CrimsonFireHelper.igniteCrimson(target, 4);
		if (!attacker.level().isClientSide) {
			attacker.level().playSound(null, target.blockPosition(), SoundEvents.FLINTANDSTEEL_USE,
					SoundSource.PLAYERS, 0.45f, 0.85f);
		}
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
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (!level.isClientSide && (blood == null || !blood.isActive() || isBlockingCardinalRite(player))) {
			return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
		}
		player.startUsingItem(hand);
		if (!level.isClientSide) {
			PacketHandler.syncPlayerAnimation(player, PlayerAnimationKind.LIVING_TORCH_BREATH, true, hand);
			level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE,
					SoundSource.PLAYERS, 0.8F, 0.72F);
		}
		return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
	}

	@Override
	public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
		if (level.isClientSide || !(living instanceof ServerPlayer player)) return;
		InteractionHand hand = player.getUsedItemHand();
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		boolean valid = player.isUsingItem() && player.getUseItem() == stack && player.isAlive()
				&& player.getServer() != null
				&& player.getServer().getPlayerList().getPlayer(player.getUUID()) == player
				&& !LivingStaffWeaponFormHelper.wasRestoredOutOfHand(stack, player)
				&& !isBlockingCardinalRite(player)
				&& blood != null && blood.isActive();
		if (!valid) {
			stopChannel(player, hand);
			return;
		}
		int elapsed = getUseDuration(stack, living) - remainingUseDuration;
		if (elapsed == LivingTorchBreathRules.WINDUP_TICKS - 1) {
			level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_BREATH,
					SoundSource.PLAYERS, 0.7F, 0.7F);
		}
		if (!LivingTorchBreathRules.shouldDrainBlood(elapsed)) return;
		if (!LivingTorchBreathRules.canPay(blood.getBloodVolume())) {
			stopChannel(player, hand);
			return;
		}
		blood.drain(LivingTorchBreathRules.BLOOD_COST_PER_TICK);
		blood.addBloodSpend(LivingTorchBreathRules.BLOOD_COST_PER_TICK);
		BloodVolumeEvents.syncVolume(player, blood);
		if (LivingTorchBreathRules.isDamagePulse(elapsed)) damageCone(player);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
		if (!level.isClientSide && living instanceof ServerPlayer player) {
			stopChannel(player, player.getUsedItemHand());
		}
	}

	public static void stopChannel(ServerPlayer player, InteractionHand hand) {
		PacketHandler.syncPlayerAnimation(player, PlayerAnimationKind.LIVING_TORCH_BREATH, false, hand);
		player.stopUsingItem();
	}

	private static void damageCone(ServerPlayer player) {
		Vec3 origin = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		AABB bounds = new AABB(origin, origin).inflate(LivingTorchBreathRules.RANGE);
		Set<Integer> hitThisPulse = new HashSet<>();
		for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, bounds,
				candidate -> candidate != player && candidate.isAlive()
						&& player.canAttack(candidate) && !candidate.isAlliedTo(player)
						&& !player.isAlliedTo(candidate))) {
			Vec3 aim = target.getBoundingBox().getCenter();
			boolean alreadyHit = !hitThisPulse.add(target.getId());
			if (!LivingTorchBreathRules.isInsideCone(origin.x, origin.y, origin.z,
					look.x, look.y, look.z, aim.x, aim.y, aim.z)
					|| !LivingTorchBreathRules.canHitCandidate(player.hasLineOfSight(target), true, alreadyHit)) continue;
			if (target.hurt(HemoDamageTypes.livingTorchBreath(player.level(), player),
					LivingTorchBreathRules.DAMAGE_PER_PULSE)) {
				CrimsonFireHelper.igniteCrimson(target, 4);
			}
		}
	}

	private static boolean isBlockingCardinalRite(Player player) {
		if (!(player.level() instanceof ServerLevel server)) return false;
		ActiveCardinalRite rite = CardinalRiteSavedData.get(server).getRite(player.getUUID());
		return rite != null && rite.isStaffPlanting();
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return RenderPropLivingTorch.INSTANCE;
	}
}

class RenderPropLivingTorch implements IClientItemExtensions {
	public static final RenderPropLivingTorch INSTANCE = new RenderPropLivingTorch();

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new LivingTorchItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
