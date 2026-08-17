package com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingIdentity;
import com.vincenthuto.hemomancy.common.item.itemhandler.MorphlingJarItemHandler;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Moves the runtime Morphling copy back into one of the player's jars. */
public final class MorphlingDeathRecovery {
    private MorphlingDeathRecovery() {
    }

    public static boolean returnToJar(Iterable<MorphlingJarItemHandler> jars, ItemStack morphling) {
        if (morphling.isEmpty()) {
            return false;
        }

        List<MorphlingJarItemHandler> loadedJars = new ArrayList<>();
        for (MorphlingJarItemHandler jar : jars) {
            if (jar == null) {
                continue;
            }
            jar.load();
            loadedJars.add(jar);
        }

        for (MorphlingJarItemHandler jar : loadedJars) {
            for (int slot = 0; slot < jar.getSlots(); slot++) {
                ItemStack stored = jar.getStackInSlot(slot);
                if (MorphlingIdentity.matches(stored, morphling)) {
                    jar.setStackInSlot(slot, morphling.copy());
                    jar.save();
                    return true;
                }
            }
        }

        for (MorphlingJarItemHandler jar : loadedJars) {
            for (int slot = 0; slot < jar.getSlots(); slot++) {
                if (jar.getStackInSlot(slot).isEmpty()) {
                    jar.setStackInSlot(slot, morphling.copy());
                    jar.save();
                    return true;
                }
            }
        }

        return false;
    }
}
