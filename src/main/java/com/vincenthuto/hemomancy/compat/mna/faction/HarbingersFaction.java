package com.vincenthuto.hemomancy.compat.mna.faction;

import com.mna.api.faction.BaseFaction;
import com.mna.api.sound.SFX;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.mna.item.MnAPluginItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class HarbingersFaction extends BaseFaction {

    public HarbingersFaction() {
        super(HarbingerEventHandler.HARBINGERS_MANA);
    }

    @Override
    public boolean is(ResourceLocation factionId) {
        return super.is(factionId);
    }

    @Override
    public ItemStack getFactionGrimoire() {
        return new ItemStack(MnAPluginItemInit.harbinger_grimore.get());
    }

    @Override
    public ResourceLocation getFactionIcon() {
        return Hemomancy.rloc("textures/mna/faction_icon_harbinger.png");
    }

    @Override
    public int[] getManaweaveRGB() {
        return new int[]{160, 0, 40};

    }

    @Override
    public Item getTokenItem() {
        return MnAPluginItemInit.mark_of_blood.get();
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

    @Override
    public ResourceLocation getSanctumStructure() {
        return Hemomancy.rloc("qliphoth_sanctum");
    }




}
