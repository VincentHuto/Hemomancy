package com.vincenthuto.hemomancy.compat.mna.faction;

import com.mna.api.capabilities.resource.ICastingResourceGuiProvider;
import com.mna.api.capabilities.resource.SimpleCastingResource;
import com.vincenthuto.hemomancy.compat.mna.item.MnAPluginItemInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class HarbingersMana extends SimpleCastingResource {
    public HarbingersMana() {
        super(2400);
    }

    public int getRegenerationRate(LivingEntity caster) {
        return (int) ((float) 2400 * this.getRegenerationModifier(caster));
    }

    public ResourceLocation getRegistryName() {

        return HarbingerEventHandler.HARBINGERS_MANA;
    }

    public void setMaxAmountByLevel(int level) {
        this.setMaxAmount((float) (100 + 20 * level));
    }

    public static class HarbingersManaGui implements ICastingResourceGuiProvider {

        public ResourceLocation getTexture() {
            // TODO: this texture is not being loaded, and the default MnA texture is being used instead.
            return HarbingerEventHandler.HARBINGERS_HUD_TEXTURE;
        }

        @Override
        public int getXPBarColor() {
            return 0xFF802020;
        }

        @Override
        public int getBarColor() {
            return 0xFF601010;
        }

        @Override

        public int getBarManaCostEstimateColor() {
            return 0x80000000;
        }

        @Override

        public int getResourceNumericTextColor() {
            return 0x80FFFFFF;
        }

        @Override

        public int getBadgeSize() {
            return 64;
        }

        @Override

        public int getFrameU() {
            return 0;
        }

        @Override

        public int getFrameWidth() {
            return 153;
        }

        @Override

        public int getFrameHeight() {
            return 24;
        }

        @Override

        public int getFrameV() {
            return 0;
        }

        @Override

        public ItemStack getBadgeItem() {
            return new ItemStack(MnAPluginItemInit.mark_of_blood.get());
        }

        @Override

        public int getBadgeItemOffsetY() {
            return 9;
        }

        @Override

        public int getFillWidth() {
            return 128;
        }

        @Override

        public int getLevelDisplayY() {
            return this.getFrameHeight() - 1;
        }
    }
}

