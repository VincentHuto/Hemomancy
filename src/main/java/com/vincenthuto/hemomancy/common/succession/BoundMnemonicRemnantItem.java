package com.vincenthuto.hemomancy.common.succession;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import java.util.List;

public final class BoundMnemonicRemnantItem extends Item {
    public BoundMnemonicRemnantItem(Properties properties) { super(properties.stacksTo(1).fireResistant()); }
    public static ItemStack create(SuccessorRecord person, int generation) {
        var stack = new ItemStack(ItemInit.bound_mnemonic_remnant.get());
        var tag = person.save(); tag.putInt("Generation", generation);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag)); return stack;
    }
    public static CompoundTag data(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        var tag = data(stack);
        if (tag.hasUUID("Id")) {
            lines.add(Component.literal(tag.getString("Name")));
            lines.add(Component.translatable("hemomancy.succession.profession." + tag.getString("Profession")));
            lines.add(Component.translatable("hemomancy.succession.remnant.bound"));
            lines.add(Component.translatable("hemomancy.succession.remnant.generation", tag.getInt("Generation")));
        }
    }
}
