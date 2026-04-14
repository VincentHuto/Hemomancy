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
			return HarbingerEventHandler.HARBINGERS_HUD_TEXTURE;
		}

		public int getXPBarColor() {
			return 0xFFFF2020;
		}

		public int getBarColor() {
			return 0xFFFFF10;
		}

		public int getBarManaCostEstimateColor() {
			return 0xFFF00000;
		}

		public int getResourceNumericTextColor() {
			return 0x808030FF;
		}

		public int getBadgeSize() {
			return 64;
		}

		public int getFrameU() {
			return 0;
		}

		public int getFrameWidth() {
			return 153;
		}

		public int getFrameHeight() {
			return 24;
		}

		public int getFrameV() {
			return 0;
		}

		public ItemStack getBadgeItem() {
			return new ItemStack(MnAPluginItemInit.mark_of_blood.get());
		}

		public int getBadgeItemOffsetY() {
			return 9;
		}

		public int getFillWidth() {
			return 128;
		}

		public int getLevelDisplayY() {
			return this.getFrameHeight() - 1;
		}
	}
}

