package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The Liber Immaculatus — the Unstained path journal.
 * All book logic lives in {@link LiberBookItem}; this class only supplies the
 * book identity, tooltip description, and unread-badge colour.
 */
public class UnstainedBookItem extends LiberBookItem {

    public UnstainedBookItem(Properties prop, ResourceLocation texture) {
        super(prop, texture,
                Hemomancy.rloc("liberimmaculatus"),
                "liberimmaculatus/");
    }

    @Override
    protected Component getDescription() {
        return Component.literal(ChatFormatting.AQUA + "A guide to the Unstained and their ways.");
    }
}
