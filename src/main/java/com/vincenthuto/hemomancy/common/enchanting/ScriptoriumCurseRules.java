package com.vincenthuto.hemomancy.common.enchanting;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public final class ScriptoriumCurseRules {
    private ScriptoriumCurseRules() {}

    public static String applicableCurse(ItemStack stack) {
        if (stack.getItem() instanceof SwordItem) return "thirsting_edge";
        if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) return "hemorrhagic_string";
        if (stack.getItem() instanceof ArmorItem) return "open_vessel";
        if (stack.getItem() instanceof DiggerItem) return "sanguine_appetite";
        return "";
    }

    public static int severity(int excessLoad) { return excessLoad >= 4 ? 3 : excessLoad >= 2 ? 2 : 1; }

    public static int bloodCost(String curse, int severity) {
        int tier = Math.clamp(severity, 1, 3) - 1;
        return switch (curse) {
            case "sanguine_appetite" -> new int[]{1, 3, 6}[tier];
            case "thirsting_edge" -> new int[]{2, 4, 8}[tier];
            case "hemorrhagic_string" -> new int[]{3, 5, 10}[tier];
            case "open_vessel" -> new int[]{5, 10, 15}[tier];
            default -> 0;
        };
    }
}
