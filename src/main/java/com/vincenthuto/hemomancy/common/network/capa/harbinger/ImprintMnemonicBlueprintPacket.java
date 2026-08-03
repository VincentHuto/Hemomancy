package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintItem;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.recipe.RiteDiscoveryRules;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server-authoritative request to consume one blank and create one filled blueprint. */
public record ImprintMnemonicBlueprintPacket(MnemonicBlueprintTarget target) implements CustomPacketPayload {
	public static final Type<ImprintMnemonicBlueprintPacket> TYPE =
			new Type<>(Hemomancy.rloc("imprint_mnemonic_blueprint"));
	public static final StreamCodec<FriendlyByteBuf, ImprintMnemonicBlueprintPacket> STREAM_CODEC = StreamCodec.of(
			(buf, packet) -> {
				buf.writeEnum(packet.target.type());
				buf.writeResourceLocation(packet.target.recipeId());
			}, buf -> new ImprintMnemonicBlueprintPacket(new MnemonicBlueprintTarget(
					buf.readEnum(MnemonicBlueprintTarget.Type.class), buf.readResourceLocation())));

	public static void handle(ImprintMnemonicBlueprintPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player)) return;
			Result result = process(player, packet.target);
			if (result == Result.INVALID_TARGET) {
				player.displayClientMessage(Component.translatable("item.hemomancy.mnemonic_blueprint.invalid_target")
						.withStyle(ChatFormatting.RED), true);
				return;
			}
			if (result == Result.NO_BLANK) {
				player.displayClientMessage(Component.translatable("item.hemomancy.mnemonic_blueprint.no_blank")
						.withStyle(ChatFormatting.RED), true);
				return;
			}
			player.displayClientMessage(Component.translatable("item.hemomancy.mnemonic_blueprint.imprinted",
					packet.target.recipeId().getPath()).withStyle(ChatFormatting.DARK_RED), true);
		});
	}

	public static Result process(ServerPlayer player, MnemonicBlueprintTarget target) {
		if (player == null || target == null || !isKnownAndUnlocked(player, target)) return Result.INVALID_TARGET;
		ItemStack blank = player.getInventory().items.stream()
				.filter(stack -> stack.is(ItemInit.mnemonic_blueprint.get()) && MnemonicBlueprintItem.isBlank(stack))
				.findFirst().orElse(ItemStack.EMPTY);
		if (blank.isEmpty()) return Result.NO_BLANK;
		blank.shrink(1);
		ItemStack filled = MnemonicBlueprintItem.create(ItemInit.mnemonic_blueprint.get(), target);
		if (!player.getInventory().add(filled)) player.drop(filled, false);
		player.inventoryMenu.broadcastChanges();
		return Result.IMPRINTED;
	}

	public enum Result {
		INVALID_TARGET,
		NO_BLANK,
		IMPRINTED
	}

	private static boolean isKnownAndUnlocked(ServerPlayer player, MnemonicBlueprintTarget target) {
		if (target.type() == MnemonicBlueprintTarget.Type.CARDINAL_RITE) {
			CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(player.level(), target.recipeId());
			if (recipe != null) {
				return !recipe.isUnstained() && RecipeDegreeGates.playerMeets(player, recipe)
						&& RiteDiscoveryRules.isDiscovered(player, recipe.getId());
			}
			return CardinalRiteFloorRegistry.get(target.recipeId()).isPresent();
		}
		BloodStructureRecipe recipe = BloodStructureRecipe.getStructureByLocation(player.level(), target.recipeId());
		return recipe != null && !recipe.isUnstained() && RecipeDegreeGates.playerMeets(player, recipe);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
