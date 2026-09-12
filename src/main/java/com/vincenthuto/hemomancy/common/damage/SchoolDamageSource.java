package com.vincenthuto.hemomancy.common.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import javax.annotation.Nullable;

public final class SchoolDamageSource extends DamageSource {
    private final SchoolHitContext context;

    public SchoolDamageSource(DamageSource original, SchoolHitContext context, @Nullable Entity owner) {
        super(original.typeHolder(), original.getDirectEntity() == null ? owner : original.getDirectEntity(),
                original.getEntity() == null ? owner : original.getEntity(), original.sourcePositionRaw());
        this.context = context;
    }

    public SchoolHitContext context() {
        return context;
    }
}

