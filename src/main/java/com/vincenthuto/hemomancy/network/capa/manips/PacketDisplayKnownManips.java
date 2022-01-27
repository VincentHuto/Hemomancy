package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.recipe.BloodCraftingRecipes;
import com.vincenthuto.hemomancy.recipe.RecipeBaseBloodCrafting;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketDisplayKnownManips {

	public PacketDisplayKnownManips() {
	}

	public static PacketDisplayKnownManips decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new PacketDisplayKnownManips();
	}

	public static void encode(final PacketDisplayKnownManips message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static List<RecipeBaseBloodCrafting> getMatchingRecipes(ItemStack stack) {
		List<RecipeBaseBloodCrafting> matchedRecipes = new ArrayList<RecipeBaseBloodCrafting>();
		for (RecipeBaseBloodCrafting recipe : BloodCraftingRecipes.RECIPES) {
			if (recipe.getHeldItem() == stack.getItem()) {
				matchedRecipes.add(recipe);
			}
		}
		return matchedRecipes;
	}

	public static void handle(final PacketDisplayKnownManips message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(NullPointerException::new);
			player.displayClientMessage(new TextComponent("Selected: " + known.getSelectedManip().getProperName()),
					false);
			for (int i = 0; i < known.getKnownManips().size(); i++) {
				player.displayClientMessage(
						new TextComponent("Manipulation " + i + ": " + known.getManipList().get(i).getProperName()),
						false);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}