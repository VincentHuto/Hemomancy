package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.*;

@Mixin(value = RandomState.class, remap = false)
public abstract class MixinPelagicRandomState implements PelagicRandomStateAccess {
    @Shadow @Final @Mutable private NoiseRouter router;
    @Unique private PelagicContext hemomancy$context;
    @Override public PelagicContext hemomancy$pelagic() { return hemomancy$context; }
    @Override public void hemomancy$setPelagic(PelagicContext context) {
        hemomancy$context = context;
        router = context.shape(router);
    }
}
