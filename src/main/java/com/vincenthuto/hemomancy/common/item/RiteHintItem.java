package com.vincenthuto.hemomancy.common.item;

import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.screen.item.RiteHintScreen;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.Level;

/**
 * A generic rite hint item — a blood-stained parchment containing the
 * blueprints for a specific cardinal rite. When right-clicked, it opens
 * a screen showing the 3D structure layout and rite information.
 * <p>
 * The rite is identified by a {@link ResourceLocation} stored in the
 * item's NBT under the key {@value #TAG_RITE_ID}. Any cardinal rite
 * recipe can be referenced.
 * <p>
 * Usage:
 * <pre>
 *   ItemStack hint = RiteHintItem.createForRite(
 *       new ResourceLocation("hemomancy", "cardinal_rite/sanguine_initiation"));
 * </pre>
 */
public class RiteHintItem extends Item {

	/** NBT key storing the rite recipe's ResourceLocation as a string. */
	public static final String TAG_RITE_ID = "RiteId";

	public RiteHintItem(Properties prop) {
		super(prop.stacksTo(1).rarity(Rarity.UNCOMMON));
	}

	/**
	 * Creates an {@link ItemStack} of this item pre-configured to reference the
	 * given cardinal rite recipe.
	 *
	 * @param riteId the full resource location of the rite recipe (e.g.
	 *               {@code hemomancy:cardinal_rite/sanguine_initiation})
	 * @return a ready-to-use stack with the rite id written into NBT
	 */
	public static ItemStack createForRite(Item riteHintItem, ResourceLocation riteId) {
		ItemStack stack = new ItemStack(riteHintItem);
		CompoundTag tag = stack.getOrCreateTag();
		tag.putString(TAG_RITE_ID, riteId.toString());
		return stack;
	}

	/**
	 * Reads the rite recipe {@link ResourceLocation} from the given stack's NBT.
	 *
	 * @return the rite id, or {@code null} if not present
	 */
	@Nullable
	public static ResourceLocation getRiteId(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains(TAG_RITE_ID)) {
			return ResourceLocation.tryParse(tag.getString(TAG_RITE_ID));
		}
		return null;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		ResourceLocation riteId = getRiteId(stack);
		if (riteId != null && level.isClientSide) {
			openRiteHintScreen(riteId);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	/**
	 * Client-only method to open the rite hint screen.
	 * Separated to avoid loading client classes on the server.
	 */
	@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
	private static void openRiteHintScreen(ResourceLocation riteId) {
		RiteHintScreen.open(riteId);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn,
								List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		ResourceLocation riteId = getRiteId(stack);
		if (riteId != null) {
			// Show the rite name derived from the path
			String path = riteId.getPath();
			if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
			String riteName = com.vincenthuto.hutoslib.client.HLTextUtils.toProperCase(path.replace("_", " "));
			tooltip.add(Component.translatable("item.hemomancy.rite_hint.tooltip.rite",
					Component.literal(riteName).withStyle(ChatFormatting.DARK_RED))
					.withStyle(ChatFormatting.GRAY));
		}
		tooltip.add(Component.translatable("item.hemomancy.rite_hint.tooltip.use")
				.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public Component getName(ItemStack stack) {
		// Dynamic name showing which rite it's for
		ResourceLocation riteId = getRiteId(stack);
		if (riteId != null) {
			String path = riteId.getPath();
			if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
			String riteName = com.vincenthuto.hutoslib.client.HLTextUtils.toProperCase(path.replace("_", " "));
			return Component.translatable("item.hemomancy.rite_hint.named", riteName);
		}
		return super.getName(stack);
	}
}
