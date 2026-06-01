package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.hematic.LivingSpearItemRenderer;
import com.vincenthuto.hemomancy.common.entity.projectile.DirectedBloodOrbEntity;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class LivingSpearItem extends LivingToolItem implements HemoClientItemExtensionsProvider {

	public static String TAG_STATE = "state";

	public LivingSpearItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, tier, builderIn);
	}


	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			if (stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(TAG_STATE)) {
				tooltip.add(Component.literal("State: Unleashed").withStyle(ChatFormatting.RED));
			} else {
				tooltip.add(Component.literal("State: Tame").withStyle(ChatFormatting.GRAY));
			}
		}
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.SPEAR;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000 / 2;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		/*
		 * if (stack.getOrCreateTag().getBoolean(TAG_STATE)) {
		 * attacker.heal(this.getAttackDamage() / 2); if (!attacker.world.isRemote) {
		 * Player playerIn = (Player) attacker; IBloodVolume playerVolume =
		 * HemoCapabilityAccess.getBloodVolume(playerIn)
		 * .orElseThrow(NullPointerException::new); float damageMod =
		 * this.getAttackDamage() * 75f; if (playerVolume.getBloodVolume() > damageMod)
		 * { playerVolume.subtractBloodVolume(damageMod);
		 * PacketHandler.sendToPlayer(* (ServerPlayer) playerIn, new
		 * PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
		 * playerVolume.getBloodVolume())); } else {
		 * playerVolume.subtractBloodVolume(damageMod);
		 * PacketHandler.sendToPlayer(* (ServerPlayer) playerIn, new
		 * PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
		 * playerVolume.getBloodVolume())); stack.damageItem(getMaxDamage() + 10,
		 * attacker, (p_220017_1_) -> {
		 * p_220017_1_.sendBreakAnimation(attacker.getActiveHand()); }); }
		 *
		 * } }
		 */
		return true;
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return RenderPropLivingSpear.INSTANCE;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		int i = this.getUseDuration(stack, entityLiving) - timeLeft;
		if (i >= 10) {
			if (entityLiving instanceof Player player) {

				float f7 = player.getYRot();
				float f = player.getXRot();
				float f1 = -Mth.sin(f7 * ((float) Math.PI / 180F)) * Mth.cos(f * ((float) Math.PI / 180F));
				float f2 = -Mth.sin(f * ((float) Math.PI / 180F));
				float f3 = Mth.cos(f7 * ((float) Math.PI / 180F)) * Mth.cos(f * ((float) Math.PI / 180F));
				float f4 = Mth.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
				float f5 = 3.0F * ((1.0F + 3) / 4.0F);
				f1 = f1 * (f5 / f4);
				f2 = f2 * (f5 / f4);
				f3 = f3 * (f5 / f4);
				player.push(f1, f2, f3);
				player.startAutoSpinAttack(20, this.getLivingAttackDamage(), stack);
				if (player.onGround()) {
					player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999F, 0.0D));
				}
				worldIn.playSound(null, player, SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

				Vec3 pos = player.position();
				if (player.level() instanceof ServerLevel serverLevel) {
					PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f, serverLevel);
				}

				/*
				 * IBloodVolume playerVolume =
				 * HemoCapabilityAccess.getBloodVolume(player)
				 * .orElseThrow(NullPointerException::new); if (playerVolume.getBloodVolume() >
				 * 50f) { if (!worldIn.isRemote) { playerVolume.subtractBloodVolume(50f);
				 * PacketHandler.sendToPlayer(* (ServerPlayer) player, new
				 * PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
				 * playerVolume.getBloodVolume()));
				 *
				 * this.summonDirectedOrb(worldIn, player);
				 *
				 * }
				 *
				 * stack.damageItem(1, player, (p_220009_1_) -> {
				 * p_220009_1_.sendBreakAnimation(player.getActiveHand()); }); } else {
				 * player.sendStatusMessage(Component.literal("Not enough blood to be shed"),
				 * true); }
				 */
			}

			if (entityLiving instanceof Player player) {
				player.awardStat(Stats.ITEM_USED.get(this));
			}
		}
	}

	public void summonDirectedOrb(Level worldIn, Player playerIn) {
		DirectedBloodOrbEntity miss = new DirectedBloodOrbEntity(playerIn, false);
		Vector3 vec = Vector3.fromEntityCenter(playerIn);
		miss.setPos(vec.x - 0.5, vec.y + 1, vec.z - 0.5);
		miss.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.0F, 1.0F);
		worldIn.addFreshEntity(miss);
	}
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getMainHandItem();
		playerIn.startUsingItem(handIn);

		if (stack.getItem() instanceof LivingSpearItem) {
			CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (!compound.getBoolean(TAG_STATE)) {
				playerIn.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			} else {
				playerIn.playSound(SoundEvents.BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			}
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
		}
		return super.use(worldIn, playerIn, handIn);
	}
}

class RenderPropLivingSpear implements IClientItemExtensions {

	public static RenderPropLivingSpear INSTANCE = new RenderPropLivingSpear();


	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new LivingSpearItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
