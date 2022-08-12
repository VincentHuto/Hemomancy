package com.vincenthuto.hemomancy.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.entity.item.EntityFlyingCharm;
import com.vincenthuto.hemomancy.radial.CharmInventory;
import com.vincenthuto.hemomancy.radial.ICharmSlotItem;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class ItemVasculariumCharm extends Item implements ICharmSlotItem {

	public static final Capability<ICharmSlotItem> CHARM_SLOT_ITEM = CapabilityManager.get(new CapabilityToken<>() {
	});
	public static Capability<IItemHandler> ITEM_HANDLER = CapabilityManager.get(new CapabilityToken<>() {
	});

	public ItemVasculariumCharm(Properties properties, EnumBloodTendency tendencyIn, float deepenAmount) {
		super(properties.stacksTo(1));
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
				BlockPos blockpos = serverlevel.findNearestMapFeature(
						TagKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY,
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
					pPlayer.displayClientMessage(new TextComponent("Could Not Find A Temple"), true);
				}
			}

			return InteractionResultHolder.consume(itemstack);
		}

	}

	@Override
	public ICapabilityProvider initCapabilities(final ItemStack stack, CompoundTag nbt) {
		return new ICapabilityProvider() {
			final CharmInventory itemHandler = new CharmInventory(stack);

			final LazyOptional<ICharmSlotItem> extensionSlotInstance = LazyOptional.of(() -> ItemVasculariumCharm.this);
			final LazyOptional<IItemHandler> itemHandlerInstance = LazyOptional.of(() -> itemHandler);

			@Override
			@Nonnull
			public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final @Nullable Direction side) {
				if (cap == ITEM_HANDLER)
					return itemHandlerInstance.cast();
				if (cap == CHARM_SLOT_ITEM)
					return extensionSlotInstance.cast();
				return LazyOptional.empty();
			}
		};
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		// super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TextComponent(ChatFormatting.RED + "So you've chosen the path of blood."));
		tooltip.add(new TextComponent(ChatFormatting.RED + "Representative of your resolve."));
		tooltip.add(new TextComponent(ChatFormatting.RED + "Leads you to a place of solace."));
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.RARE;
	}

	@Override
	public boolean isFoil(ItemStack stack) {

		return true;
	}

	public static enum Commands {
		FOLLOW, INTERACT, MOVE, STAY, DIAGNOSTICS, EAT, ATTACK;

		private final ResourceLocation iconTexture = new ResourceLocation(Hemomancy.MOD_ID,
				"textures/gui/command_icons/" + this.toString().toLowerCase() + ".png");

		public ResourceLocation getIcon() {
			return this.iconTexture;
		}

	}

}
