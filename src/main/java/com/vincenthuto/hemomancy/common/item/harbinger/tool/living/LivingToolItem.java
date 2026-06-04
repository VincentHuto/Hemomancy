package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.google.common.collect.Sets;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;

public class LivingToolItem extends DiggerItem implements IDispellable, ITendencyAlignedWeapon {
	private static final HashSet<Block> EFFECTIVE_ON = Sets.newHashSet(Blocks.COBWEB);
	private final float attackDamage;
	private final float speed;
	@Nullable
	private final EnumBloodTendency weaponTendency;

	public LivingToolItem(float speedIn, float attackDamageIn, float attackSpeedIn, Tier tier, Properties builderIn) {
		this(speedIn, attackDamageIn, attackSpeedIn, null, tier, builderIn);
	}

	public LivingToolItem(float speedIn, float attackDamageIn, float attackSpeedIn,
			@Nullable EnumBloodTendency weaponTendency, Tier tier, Properties builderIn) {
		super(tier, BlockTags.WART_BLOCKS,
				builderIn.attributes(DiggerItem.createAttributes(tier, attackDamageIn, attackSpeedIn)));
		this.attackDamage = attackDamageIn;
		this.speed = speedIn;
		this.weaponTendency = weaponTendency;
	}

	protected float getLivingAttackDamage() {
		return this.attackDamage;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (EFFECTIVE_ON.contains(state.getBlock())) {
			return speed;
		} else {
			return 0.5f;
		}
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return Component
				.literal(HLTextUtils.stringToBloody(
						HLTextUtils.convertInitToLang(HLTextUtils.getItemRegistryName(stack.getItem()))))
				.withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		TendencyWeaponHelper.appendTendencyTooltip(stack, tooltip);
	}

	@Override
	@Nullable
	public EnumBloodTendency hemomancy$getWeaponTendency() {
		return weaponTendency;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (target.level().random.nextBoolean()) {
			if (attacker instanceof Player) {
				if (!attacker.level().isClientSide) {
					Player playerIn = (Player) attacker;
					IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(playerIn)
							.orElseThrow(NullPointerException::new);
					float damageMod = this.getLivingAttackDamage() * 25f;
					if (playerVolume.getBloodVolume() > damageMod) {
						playerVolume.drain(damageMod);
						PacketHandler.sendToPlayer((ServerPlayer) playerIn, new BloodVolumeServerPacket(playerVolume));
					} else {
						playerVolume.drain(damageMod);
						PacketHandler.sendToPlayer((ServerPlayer) playerIn, new BloodVolumeServerPacket(playerVolume));
						boolean restoredStaffForm = LivingStaffWeaponFormHelper.restoreStaffAfterBloodFailure(stack, attacker);
						if (!restoredStaffForm) {
							stack.hurtAndBreak(getMaxDamage(stack) + 10, attacker, EquipmentSlot.MAINHAND);
						}
						Vec3 pos = playerIn.position();
						if (attacker.level() instanceof ServerLevel serverLevel) {
							PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f, serverLevel);
						}
					}

				}
			}
		}

		if (target.level().random.nextDouble() > 0.75) {
			attacker.addEffect(new MobEffectInstance(EffectInit.blood_rush, 50, 2));
			target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 50, 2));
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	public boolean isCorrectToolForDrops(BlockState blockIn) {
		if (EFFECTIVE_ON.contains(blockIn.getBlock())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		return super.onEntitySwing(stack, entity);
	}
}
