package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.hematic.LivingSickleItemRenderer;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.projectile.LivingSickleHookEntity;
import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.client.particle.factory.DarkGlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class LivingSickleItem extends LivingToolItem implements HemoClientItemExtensionsProvider {
	public static final String MODE_KEY = "HemomancySickleMode";
	private static final ParticleColor SICKLE_BLOOD = new ParticleColor(220, 0, 20);

	public LivingSickleItem(float speedIn, float attackDamageIn, Tier tier, Properties properties) {
		super(speedIn, attackDamageIn, LivingSickleCombatRules.attackSpeed(LivingSickleMode.defaultMode()),
				EnumBloodTendency.MORTEM, tier, properties);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (LivingSicklePruning.isTemporarySickle(stack)) return true;
		boolean hit = super.hurtEnemy(stack, target, attacker);
		if (!hit || LivingStaffWeaponFormHelper.wasRestoredOutOfHand(stack, attacker)
				|| attacker.level().isClientSide
				|| !(attacker instanceof Player player)) return hit;

		if (mode(stack) != LivingSickleMode.SHORT_REAP) return true;
		float executionBonus = LivingSickleCombatRules.executionBonus(target.getHealth(), target.getMaxHealth());
		if (target.isAlive() && executionBonus > 0.0F) {
			target.hurt(attacker.damageSources().playerAttack(player), executionBonus);
		}
		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (LivingSicklePruning.isTemporarySickle(stack)) {
			if (!level.isClientSide) {
				player.setItemInHand(hand, LivingSicklePruning.restoredWeaponStack(stack, player.registryAccess()));
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
		}
		if (player.isShiftKeyDown()) {
			if (!level.isClientSide) setMode(stack, mode(stack).next(), player);
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
		}
		if (!level.isClientSide && level instanceof ServerLevel server) {
			if (mode(stack) == LivingSickleMode.BLOOD_HOOK) fireHook(server, player, hand);
			else performSpin(server, player, hand);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	public static LivingSickleMode mode(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data == null ? LivingSickleMode.defaultMode()
				: LivingSickleMode.byName(data.copyTag().getString(MODE_KEY));
	}

	private void setMode(ItemStack stack, LivingSickleMode mode, Player player) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putString(MODE_KEY, mode.name());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		stack.set(DataComponents.ATTRIBUTE_MODIFIERS,
				DiggerItem.createAttributes(getTier(), getLivingAttackDamage(), LivingSickleCombatRules.attackSpeed(mode)));
		player.displayClientMessage(Component.translatable(mode == LivingSickleMode.SHORT_REAP
				? "item.hemomancy.living_sickle.mode.short" : "item.hemomancy.living_sickle.mode.hook")
				.withStyle(mode == LivingSickleMode.SHORT_REAP ? ChatFormatting.RED : ChatFormatting.DARK_RED), true);
		player.playSound(mode == LivingSickleMode.SHORT_REAP ? SoundEvents.BEACON_ACTIVATE : SoundEvents.CHAIN_PLACE,
				0.65F, mode == LivingSickleMode.SHORT_REAP ? 1.25F : 0.72F);
	}

	private void performSpin(ServerLevel level, Player player, InteractionHand hand) {
		float damage = LivingSickleCombatRules.spinDamage(
				(float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
		AABB area = player.getBoundingBox().inflate(LivingSickleCombatRules.SPIN_RADIUS, 1.5D,
				LivingSickleCombatRules.SPIN_RADIUS);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity != player && entity.isAlive() && !entity.isAlliedTo(player)
						&& player.canAttack(entity))) {
			target.hurt(player.damageSources().playerAttack(player), damage);
		}
		player.swing(hand, true);
		player.getCooldowns().addCooldown(this, LivingSickleCombatRules.SPIN_COOLDOWN_TICKS);
		Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.52D, 0.0D);
		level.sendParticles(BloodCellParticleFactory.createData(SICKLE_BLOOD), center.x, center.y, center.z,
				34, 2.4D, 0.65D, 2.4D, 0.09D);
		level.sendParticles(DarkGlowParticleFactory.createData(new ParticleColor(18, 0, 5)),
				center.x, center.y, center.z, 22, 2.8D, 0.5D, 2.8D, 0.05D);
		for (int i = 0; i < 4; i++) {
			double angle = i * Math.PI * 0.5D;
			PacketHandler.sendClawSlash(center.add(Math.cos(angle) * 1.6D, 0.0D, Math.sin(angle) * 1.6D),
					new Vec3(Math.cos(angle), 0.0D, Math.sin(angle)), SICKLE_BLOOD,
					(i & 1) == 0, 1.2F, 64.0D, level);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			HemomancyTendrilEffects.silentSeverance(serverPlayer, LivingSickleCombatRules.SPIN_RADIUS);
		}
		level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
				SoundSource.PLAYERS, 1.15F, 0.68F);
	}

	private void fireHook(ServerLevel level, Player player, InteractionHand hand) {
		LivingSickleHookEntity hook = new LivingSickleHookEntity(level, player);
		hook.setAttackDamage(LivingSickleCombatRules.hookDamage(
				(float) player.getAttributeValue(Attributes.ATTACK_DAMAGE)));
		hook.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 0.4F);
		level.addFreshEntity(hook);
		hook.spawnBloodTendril();
		player.swing(hand, true);
		player.getCooldowns().addCooldown(this, LivingSickleCombatRules.HOOK_COOLDOWN_TICKS);
		level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(),
				SoundSource.PLAYERS, 0.9F, 0.75F);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		if (LivingSicklePruning.isTemporarySickle(stack)) return;
		tooltip.add(Component.translatable(mode(stack) == LivingSickleMode.SHORT_REAP
				? "item.hemomancy.living_sickle.tooltip.short" : "item.hemomancy.living_sickle.tooltip.hook")
				.withStyle(ChatFormatting.RED));
		tooltip.add(Component.translatable("item.hemomancy.living_sickle.tooltip.switch")
				.withStyle(ChatFormatting.GRAY));
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
