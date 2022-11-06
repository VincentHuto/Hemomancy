package com.vincenthuto.hemomancy.item;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.rune.IRune;
import com.vincenthuto.hemomancy.capa.player.rune.RuneType;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.entity.item.EntityFlyingCharm;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class VasculariumCharmItem extends Item implements IRune {

	public static enum Commands {
		FOLLOW, INTERACT, MOVE, STAY, DIAGNOSTICS, EAT, ATTACK;

		private final ResourceLocation iconTexture = new ResourceLocation(Hemomancy.MOD_ID,
				"textures/gui/command_icons/" + this.toString().toLowerCase() + ".png");

		public ResourceLocation getIcon() {
			return this.iconTexture;
		}

	}

	public VasculariumCharmItem(Properties properties, EnumBloodTendency tendencyIn, float deepenAmount) {
		super(properties.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		// super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.literal(ChatFormatting.RED + "So you've chosen the path of blood."));
		tooltip.add(Component.literal(ChatFormatting.RED + "Representative of your resolve."));
		tooltip.add(Component.literal(ChatFormatting.RED + "Leads you to a place of solace."));
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.RARE;
	}

	@Override
	public RuneType getRuneType() {
		// TODO Auto-generated method stub
		return RuneType.VASC;
	}

	@Override
	public boolean isFoil(ItemStack stack) {

		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
		ItemStack itemstack = pPlayer.getItemInHand(pHand);

		HitResult hitresult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.NONE);
		if (hitresult.getType() == HitResult.Type.BLOCK
				&& pLevel.getBlockState(((BlockHitResult) hitresult).getBlockPos()).is(Blocks.END_PORTAL_FRAME)) {
			return InteractionResultHolder.pass(itemstack);
		} else {
			pPlayer.startUsingItem(pHand);
			if (pLevel instanceof ServerLevel) {
				ServerLevel serverlevel = (ServerLevel) pLevel;
				BlockPos blockpos = serverlevel.findNearestMapStructure(
						TagKey.create(Registry.STRUCTURE_REGISTRY,
								new ResourceLocation(Hemomancy.MOD_ID, "blood_temple")),
						pPlayer.blockPosition(), 100, false);

				if (blockpos != null) {
					EntityFlyingCharm eyeofender = new EntityFlyingCharm(pLevel, pPlayer.getX(), pPlayer.getY(0.5D),
							pPlayer.getZ());
					eyeofender.setItem(itemstack);
					eyeofender.signalTo(blockpos);
					pLevel.addFreshEntity(eyeofender);
					if (pPlayer instanceof ServerPlayer) {
						CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayer) pPlayer, blockpos);
					}

					pLevel.playSound((Player) null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
							SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5F,
							0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
					pLevel.levelEvent((Player) null, 1003, pPlayer.blockPosition(), 0);
					if (!pPlayer.getAbilities().instabuild) {
						// itemstack.shrink(1);
					}

					pPlayer.awardStat(Stats.ITEM_USED.get(this));
					pPlayer.swing(pHand, true);
					return InteractionResultHolder.success(itemstack);
				} else {
					pPlayer.displayClientMessage(Component.literal("Could Not Find A Temple"), true);
				}
			}

			return InteractionResultHolder.consume(itemstack);
		}

	}

}
