package com.vincenthuto.hemomancy.common.item.harbinger.scar;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.ScarPatternItemRenderer;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hemomancy.common.recipe.serializer.ScarRecipeSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemScarPattern extends Item implements HemoClientItemExtensionsProvider {
	public static final String TAG_SCAR_IDS = "ScarIds";
	public static final int MAX_SCAR_IDS = 4;

	public ItemScarPattern(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (worldIn.isClientSide) {
			playerIn.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
	}

	public static ItemStack createTemplatePattern(ResourceLocation scarId) {
		return createPreparedPattern(List.of(scarId));
	}

	public static ItemStack createPreparedPattern(Collection<ResourceLocation> scarIds) {
		ItemStack stack = new ItemStack(ItemInit.scar_pattern.get());
		setScarIds(stack, scarIds);
		return stack;
	}

	public static boolean hasPreparedLoadout(ItemStack stack) {
		return !getScarIds(stack).isEmpty();
	}

	public static List<ResourceLocation> getScarIds(ItemStack stack) {
		List<ResourceLocation> ids = new ArrayList<>();
		if (stack.isEmpty() || !stack.has(DataComponents.CUSTOM_DATA)) {
			return ids;
		}
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		ListTag list = tag.getList(TAG_SCAR_IDS, Tag.TAG_STRING);
		for (int i = 0; i < list.size() && ids.size() < MAX_SCAR_IDS; i++) {
			ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
			if (id != null && !ids.contains(id)) {
				ids.add(id);
			}
		}
		return ids;
	}

	public static void setScarIds(ItemStack stack, Collection<ResourceLocation> scarIds) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		ListTag list = new ListTag();
		for (ResourceLocation id : scarIds) {
			if (id != null && list.size() < MAX_SCAR_IDS) {
				list.add(StringTag.valueOf(id.toString()));
			}
		}
		tag.put(TAG_SCAR_IDS, list);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static List<TemplateEntry> getTemplateEntries(ItemStack stack, Level level) {
		List<TemplateEntry> entries = new ArrayList<>();
		for (ResourceLocation scarId : getScarIds(stack)) {
			ScarRecipe recipe = getRecipeForScarId(scarId, level);
			if (recipe == null || recipe.getPattern() == null) {
				continue;
			}
			ItemStack resultIcon = recipe.getResultItem();
			String displayName = resultIcon.getHoverName().getString();
			entries.add(new TemplateEntry(scarId, resultIcon, displayName, recipe.getPattern(), recipe));
		}
		return entries;
	}

	public static ScarRecipe getPrimaryRecipe(ItemStack stack, Level level) {
		List<ResourceLocation> scarIds = getScarIds(stack);
		if (scarIds.isEmpty()) {
			return null;
		}
		return getRecipeForScarId(scarIds.get(0), level);
	}

	public static ScarRecipe getRecipeForScarId(ResourceLocation scarId, Level level) {
		String recipePath = recipePathForScarId(scarId);
		ScarRecipe recipe = ScarRecipeSerializer.getRecipe(recipePath);
		if (recipe != null) {
			return recipe;
		}

		recipe = getRecipeFromLevel(level, recipePath);
		if (recipe != null) {
			return recipe;
		}

		Level serverLevel = getServerLevel();
		recipe = getRecipeFromLevel(serverLevel, recipePath);
		if (recipe != null) {
			return recipe;
		}

		if (FMLEnvironment.dist.isClient()) {
			return getRecipeFromClientLevel(recipePath);
		}

		return null;
	}

	private static String recipePathForScarId(ResourceLocation scarId) {
		if (scarId == null) {
			return "";
		}
		if (scarId.getNamespace().equals(Hemomancy.MOD_ID)) {
			return scarId.getPath();
		}
		return scarId.toString();
	}

	private static Level getServerLevel() {
		if (ServerLifecycleHooks.getCurrentServer() == null) {
			return null;
		}
		return ServerLifecycleHooks.getCurrentServer().overworld();
	}

	private static ScarRecipe getRecipeFromLevel(Level level, String recipePath) {
		if (level == null) {
			return null;
		}

		for (RecipeHolder<ScarRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeInit.chisel_recipe.get())) {
			ResourceLocation id = holder.id();
			String idPath = id.getPath();
			if (idPath.equals(recipePath) || idPath.equals("scar/" + recipePath) || idPath.equals("chisel/" + recipePath)
					|| idPath.equals("rune/" + recipePath)) {
				return holder.value();
			}
		}

		return null;
	}

	@OnlyIn(Dist.CLIENT)
	private static ScarRecipe getRecipeFromClientLevel(String recipePath) {
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
		if (mc.level == null) {
			return null;
		}
		return getRecipeFromLevel(mc.level, recipePath);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		List<ResourceLocation> ids = getScarIds(stack);
		if (!ids.isEmpty()) {
			tooltip.add(Component.translatable("tooltip.hemomancy.scar_pattern.loadout", ids.size())
					.withStyle(ChatFormatting.DARK_RED));
			for (ResourceLocation id : ids) {
				tooltip.add(Component.literal(" - " + id.getPath()).withStyle(ChatFormatting.GRAY));
			}
		} else {
			tooltip.add(Component.translatable("tooltip.hemomancy.scar_pattern.blank").withStyle(ChatFormatting.GRAY));
		}
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

	public record TemplateEntry(ResourceLocation scarId, ItemStack resultIcon, String displayName, byte[][] pattern,
			ScarRecipe recipe) {
	}

}
