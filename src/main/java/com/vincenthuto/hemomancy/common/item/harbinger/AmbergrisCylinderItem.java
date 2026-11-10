package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.item.component.AncientRecordings;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class AmbergrisCylinderItem extends CylinderItem {
    @Override
    public Component getName(ItemStack stack) {
        if (!stack.has(DataComponentInit.RESONANT_PATTERN.get())) {
            var program = AncientRecordings.get(stack);
            if (program != null) return Component.translatable("hemomancy.antecedent.cylinder." + program.id());
        }
        return super.getName(stack);
    }
}
