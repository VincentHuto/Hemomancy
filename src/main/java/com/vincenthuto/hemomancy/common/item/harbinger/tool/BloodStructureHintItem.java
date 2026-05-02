package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.screen.item.BloodStructureHintScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * A fragment of ancient Harbinger parchment containing the instructions for
 * raising a blood structure. When right-clicked it opens a screen showing
 * the 3D structure layout, the required held item, hit block, blood cost,
 * and the resulting crafted object.
 * <p>
 * The structure is identified by a {@link ResourceLocation} stored in the
 * item's NBT under the key {@value #TAG_STRUCTURE_ID}. Any blood structure
 * recipe can be referenced.
 * <p>
 * Usage:
 * <pre>
 *   ItemStack hint = BloodStructureHintItem.createForStructure(
 *       ItemInit.blood_structure_hint.get(),
 *       new ResourceLocation("hemomancy", "blood_structure/sanguine_monolith"));
 * </pre>
 */
public class BloodStructureHintItem extends Item {

	/** NBT key storing the blood structure recipe's ResourceLocation as a string. */
	public static final String TAG_STRUCTURE_ID = "StructureId";

	public BloodStructureHintItem(Properties prop) {
		super(prop.stacksTo(1).rarity(Rarity.RARE));
	}

	/**
	 * Creates an {@link ItemStack} of this item pre-configured to reference the
	 * given blood structure recipe.
	 *
	 * @param item        the BloodStructureHintItem instance (from ItemInit)
	 * @param structureId the full resource location of the blood structure recipe
	 *                    (e.g. {@code hemomancy:blood_structure/sanguine_monolith})
	 * @return a ready-to-use stack with the structure id written into NBT
	 */
	public static ItemStack createForStructure(Item item, ResourceLocation structureId) {
		ItemStack stack = new ItemStack(item);
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putString(TAG_STRUCTURE_ID, structureId.toString());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return stack;
	}

	/**
	 * Reads the blood structure recipe {@link ResourceLocation} from the given
	 * stack's NBT.
	 *
	 * @return the structure id, or {@code null} if not present
	 */
	@Nullable
	public static ResourceLocation getStructureId(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (tag.contains(TAG_STRUCTURE_ID)) {
			return ResourceLocation.tryParse(tag.getString(TAG_STRUCTURE_ID));
		}
		return null;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		ResourceLocation structureId = getStructureId(stack);
		if (structureId != null && level.isClientSide) {
			openBloodStructureHintScreen(structureId);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	/**
	 * Client-only method to open the blood structure hint screen.
	 * Separated to avoid loading client classes on the server.
	 */
	@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
	private static void openBloodStructureHintScreen(ResourceLocation structureId) {
		BloodStructureHintScreen.open(structureId);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context,
								List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		ResourceLocation structureId = getStructureId(stack);
		if (structureId != null) {
			String path = structureId.getPath();
			if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
			String structureName = com.vincenthuto.hutoslib.client.HLTextUtils.toProperCase(path.replace("_", " "));
			tooltip.add(Component.translatable("item.hemomancy.blood_structure_hint.tooltip.structure",
					Component.literal(structureName).withStyle(ChatFormatting.DARK_RED))
					.withStyle(ChatFormatting.GRAY));
		}
		tooltip.add(Component.translatable("item.hemomancy.blood_structure_hint.tooltip.use")
				.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public Component getName(ItemStack stack) {
		ResourceLocation structureId = getStructureId(stack);
		if (structureId != null) {
			String path = structureId.getPath();
			if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
			String structureName = com.vincenthuto.hutoslib.client.HLTextUtils.toProperCase(path.replace("_", " "));
			return Component.translatable("item.hemomancy.blood_structure_hint.named", structureName);
		}
		return super.getName(stack);
	}
}
