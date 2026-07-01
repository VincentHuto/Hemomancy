package com.vincenthuto.hemomancy.common.item.harbinger.tile;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.tile.functional.SpecimenJarItemRenderer;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.util.SpecimenJarData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class SpecimenJarBlockItem extends BlockItem implements HemoClientItemExtensionsProvider {

	public SpecimenJarBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
			InteractionHand hand) {
		if (SpecimenJarData.hasSpecimen(stack) || !isCapturableSpecimen(target)) {
			return InteractionResult.PASS;
		}
		if (player.level().isClientSide) {
			return InteractionResult.SUCCESS;
		}

		ItemStack filled = new ItemStack(this);
		SpecimenJarData.setSpecimen(filled, SpecimenJarData.captureEntity(target));
		if (!player.getAbilities().instabuild && stack.getCount() <= 1) {
			player.setItemInHand(hand, filled);
		} else if (!player.getAbilities().instabuild) {
			stack.shrink(1);
			if (!player.addItem(filled)) {
				player.drop(filled, false);
			}
		} else if (!player.addItem(filled)) {
			player.drop(filled, false);
		}
		target.discard();
		return InteractionResult.CONSUME;
	}

	private static boolean isCapturableSpecimen(LivingEntity target) {
		return target.getType().is(EntityInit.SPECIMEN_JAR_CAPTURABLE)
				|| target.getType() == EntityInit.chthonian.get()
				|| target.getType() == EntityInit.chthonian_queen.get()
				|| target.getType() == EntityInit.chitinite.get()
				|| target.getType() == EntityInit.fervent_chitinite.get()
				|| target.getType() == EntityInit.hemolymphopoda.get()
				|| target.getType() == EntityInit.fargone.get()
				|| target.getType() == EntityInit.venom_rib_centipede.get()
				|| target.getType() == EntityInit.tooth_pecks.get();
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		if (SpecimenJarData.hasSpecimen(stack)) {
			tooltip.add(Component.translatable("tooltip.hemomancy.specimen_jar.contains",
					SpecimenJarData.getSpecimenName(SpecimenJarData.getSpecimen(stack))));
		} else {
			tooltip.add(Component.translatable("tooltip.hemomancy.specimen_jar.empty"));
		}
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new SpecimenJarItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}
}
