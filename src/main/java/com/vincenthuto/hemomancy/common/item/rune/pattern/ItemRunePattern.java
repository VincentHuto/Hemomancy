package com.vincenthuto.hemomancy.common.item.rune.pattern;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.render.item.RunePatternItemRenderer;
import com.vincenthuto.hemomancy.common.recipe.ChiselRecipe;
import com.vincenthuto.hemomancy.common.recipe.serializer.ChiselRecipeSerializer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.RegistryObject;

public class ItemRunePattern extends Item {

	String path;
	RegistryObject<Item> rune;

	public ItemRunePattern(Properties prop, RegistryObject<Item> rune, String recipePath) {
		super(prop.stacksTo(1));
		this.rune = rune;
		this.path = recipePath;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (worldIn.isClientSide) {
			//Hemomancy.proxy.openPatternGui(rune, getRecipe());
			playerIn.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
	}
	
	public RegistryObject<Item> getRune() {
		return rune;
	}
	
	public void setRune(RegistryObject<Item> rune) {
		this.rune = rune;
	}

	public ChiselRecipe getRecipe() {
		return ChiselRecipeSerializer.getRecipe(path);
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	public void getPatternGui() {
	//	Hemomancy.proxy.openPatternGui(rune, getRecipe());
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			final BlockEntityWithoutLevelRenderer renderer = new RunePatternItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		});
	}

}
