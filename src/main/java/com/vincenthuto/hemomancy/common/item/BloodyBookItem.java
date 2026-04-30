package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The Liber Sanguinum — the Harbinger path journal.
 * All book logic lives in {@link LiberBookItem}; this class only supplies the
 * book identity, tooltip description, and unread-badge colour.
 */
public class BloodyBookItem extends LiberBookItem {

    public BloodyBookItem(Properties prop, ResourceLocation texture) {
        super(prop, texture,
                Hemomancy.rloc("sanctumsanguinium"),
                "sanctumsanguinium/");
    }

    @Override
    protected Component getDescription() {
        return Component.literal(ChatFormatting.GOLD + "A guide to your blood and its power.");
    }
}

