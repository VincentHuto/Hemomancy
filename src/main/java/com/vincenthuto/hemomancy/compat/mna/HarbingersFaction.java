package com.vincenthuto.hemomancy.compat.mna;

import javax.annotation.Nullable;

import com.mna.api.faction.BaseFaction;
import com.mna.api.sound.SFX;
import com.mna.api.tools.RLoc;
import com.mna.items.ItemInit;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HarbingersFaction extends BaseFaction {

	public HarbingersFaction() {
		super(MnAPlugin.FACTION_HARBINGERS_ID, MnAPlugin.HARBINGERS_MANA);
	}
	
	@Override
	public ResourceLocation getCastingResource(Player player) {
		return MnAPlugin.HARBINGERS_MANA;
	}

	@Override
	public boolean is(ResourceLocation factionId) {
		return super.is(factionId);
	}
//Did this too just to be sure
	@Override
	public ResourceLocation[] getCastingResources() {
		return new ResourceLocation[] { MnAPlugin.HARBINGERS_MANA };
	}

	@Override
	public ItemStack getFactionGrimoire() {
		return new ItemStack(ItemInit.GRIMOIRE_DEMON.get());
	}

	@Override
	public ResourceLocation getFactionIcon() {
		return Hemomancy.rloc("textures/item/faction_icon_harbinger.png");
	}

	@Override
	public int[] getManaweaveRGB() {
		return new int[] { 160, 0, 40 };

	}

	@Override
	public Item getTokenItem() {
		return MnAPlugin.mark_of_blood.get();
	}

	@Override
	public ChatFormatting getTornJournalPageFactionColor() {
		return ChatFormatting.DARK_RED;
	}

	@Override
	public SoundEvent getRaidSound() {
		return SFX.Event.Faction.FACTION_RAID_DEMONS;
	}

	@Nullable
	@Override
	public SoundEvent getHornSound() {
		return SFX.Event.Faction.FACTION_HORN_DEMONS;
	}

	@Override
	public Component getOcculusTaskPrompt(int i) {
		return Component.translatable("fnc:rituals/pact");
	}

	public ResourceLocation getSanctumStructure() {
		return RLoc.create("multiblock/council_circle");
	}


}
