package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.enchanting.ResonantPattern;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.item.component.AncientRecordings;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Optional;

public class CylinderItem extends Item {
    public CylinderItem() {
        super(new Properties().stacksTo(16));
    }

    @Override
    public Component getName(ItemStack stack) {
        var pattern = stack.get(DataComponentInit.RESONANT_PATTERN.get());
        if (pattern == null) return super.getName(stack);
        String variant = pattern.master() && CylinderMedia.masterCapable(stack) ? ".master" : ".pattern";
        return Component.translatable(getDescriptionId(stack) + variant);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flags) {
        var pattern = stack.get(DataComponentInit.RESONANT_PATTERN.get());
        if (pattern != null) {
            appendPattern(pattern, context, lines);
            if (!pattern.master() && CylinderMedia.retainsShell(stack))
                lines.add(Component.translatable("tooltip.hemomancy.corpse_wax_cylinder.shell"));
            return;
        }
        if (stack.has(DataComponentInit.ANCIENT_RECORDING.get())) {
            lines.add(Component.translatable(AncientRecordings.get(stack) == null
                    ? "gui.hemomancy.clairaudiograph.unreadable" : "hemomancy.antecedent.cylinder.description"));
            return;
        }
        var recording = stack.get(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get());
        if (recording == null) {
            lines.add(Component.translatable("gui.hemomancy.clairaudiograph.blank"));
            return;
        }
        if (!recording.readable()) {
            lines.add(Component.translatable("gui.hemomancy.clairaudiograph.unreadable"));
            return;
        }
        var id = ResourceLocation.parse(recording.source());
        lines.add(BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(type -> type.getDescription())
                .orElse(Component.translatable("gui.hemomancy.clairaudiograph.unknown")));
        lines.add(Component.translatable("sound_kind.hemomancy." + recording.kind()));
        if (flags.isAdvanced()) {
            lines.add(Component.literal(recording.source()));
            lines.add(Component.literal(recording.sound()));
        }
    }

    private static void appendPattern(ResonantPattern pattern, TooltipContext context, List<Component> lines) {
        lines.add(Component.translatable(pattern.master()
                ? "tooltip.hemomancy.resonant_pattern.master" : "tooltip.hemomancy.resonant_pattern.single_use"));
        for (var entry : pattern.enchantments()) {
            var id = ResourceLocation.tryParse(entry.enchantment());
            var enchantment = id == null || context.registries() == null
                    ? Optional.<Holder.Reference<Enchantment>>empty()
                    : context.registries().lookupOrThrow(Registries.ENCHANTMENT)
                            .get(ResourceKey.create(Registries.ENCHANTMENT, id));
            lines.add(enchantment.map(holder -> Enchantment.getFullname(holder, entry.level()))
                    .orElse(Component.literal(entry.enchantment() + " " + entry.level())));
        }
        if (!pattern.curse().isBlank()) {
            lines.add(Component.translatable("hemomancy.scriptorium.curse." + pattern.curse())
                    .append(" " + pattern.curseSeverity()));
        }
    }
}
