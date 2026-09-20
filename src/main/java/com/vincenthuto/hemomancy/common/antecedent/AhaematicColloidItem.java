package com.vincenthuto.hemomancy.common.antecedent;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

/** A non-hematic specimen. It deliberately has no blood-vial use or injection behaviour. */
public final class AhaematicColloidItem extends Item {
    /** JEI, creative, commands, and any plain ItemStack use a real examinable specimen. */
    public static final AhaematicSample DEFAULT_SAMPLE = new AhaematicSample("entity", "minecraft:warden");

    public AhaematicColloidItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
                .component(DataComponentInit.AHAEMATIC_SAMPLE.get(), DEFAULT_SAMPLE));
    }

    @Override public Component getName(ItemStack stack) {
        return Component.translatable("item.hemomancy.ahaematic_" + (BloodSampleData.identified(stack) ? "colloid" : "unidentified"));
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hemomancy.antecedent.sample." + (BloodSampleData.identified(stack) ? "identified" : "unknown"))
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
