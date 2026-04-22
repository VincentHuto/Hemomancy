package com.vincenthuto.hemomancy.common.item.scar.pattern;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.render.item.ScarPatternItemRenderer;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hemomancy.common.recipe.serializer.ScarRecipeSerializer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ItemScarPattern extends Item {

	String path;
	DeferredHolder<Item, Item> scar;

	public ItemScarPattern(Properties prop, DeferredHolder<Item, Item> scar, String recipePath) {
		super(prop.stacksTo(1));
		this.scar = scar;
		this.path = recipePath;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (worldIn.isClientSide) {
			//Hemomancy.proxy.openPatternGui(scar, getRecipe());
			playerIn.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
	}
	
	public DeferredHolder<Item, Item> getSCAR() {
		return scar;
	}
	
	public void setSCAR(DeferredHolder<Item, Item> scar) {
		this.scar = scar;
	}

	public ScarRecipe getRecipe() {
		return ScarRecipeSerializer.getRecipe(path);
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	public void getPatternGui() {
	//	Hemomancy.proxy.openPatternGui(scar, getRecipe());
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			final BlockEntityWithoutLevelRenderer renderer = new ScarPatternItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		});
	}

}
