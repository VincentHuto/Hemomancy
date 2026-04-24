package com.vincenthuto.hemomancy.common.item.scar.pattern;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;


import com.vincenthuto.hemomancy.client.render.item.ScarPatternItemRenderer;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hemomancy.common.recipe.serializer.ScarRecipeSerializer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class ItemScarPattern extends Item implements HemoClientItemExtensionsProvider {

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
		ScarRecipe recipe = ScarRecipeSerializer.getRecipe(path);
		if (recipe != null) {
			return recipe;
		}

		Level serverLevel = getServerLevel();
		recipe = getRecipeFromLevel(serverLevel);
		if (recipe != null) {
			return recipe;
		}

		if (FMLEnvironment.dist.isClient()) {
			return getRecipeFromClientLevel();
		}

		return null;
	}

	private Level getServerLevel() {
		if (ServerLifecycleHooks.getCurrentServer() == null) {
			return null;
		}
		return ServerLifecycleHooks.getCurrentServer().overworld();
	}

	private ScarRecipe getRecipeFromLevel(Level level) {
		if (level == null) {
			return null;
		}

		for (RecipeHolder<ScarRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeInit.chisel_recipe.get())) {
			ResourceLocation id = holder.id();
			String idPath = id.getPath();
			if (idPath.equals(path) || idPath.equals("scar/" + path) || idPath.equals("chisel/" + path)
					|| idPath.equals("rune/" + path)) {
				return holder.value();
			}
		}

		return null;
	}

	@OnlyIn(Dist.CLIENT)
	private ScarRecipe getRecipeFromClientLevel() {
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
		if (mc.level == null) {
			return null;
		}
		return getRecipeFromLevel(mc.level);
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
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			final BlockEntityWithoutLevelRenderer renderer = new ScarPatternItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

}
