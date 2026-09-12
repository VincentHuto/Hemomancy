package com.vincenthuto.hemomancy.common.damage;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import javax.annotation.Nullable;
import java.util.UUID;

/** A launch-time identity, independent of the weapon subsequently held by the owner. */
public record SchoolHitContext(ResourceLocation ability, EnumBloodTendency primary,
        @Nullable EnumBloodTendency secondary, @Nullable UUID owner, @Nullable UUID direct,
        UUID rootAttack, Kind kind, Role role, float charge, int duration, int levels) {
    public enum Kind { DIRECT, PERIODIC, REACTION }
    public enum Role { APPLY_STATE, EXPLOIT_STATE, DAMAGE_ONLY }

    public SchoolHitContext {
        if (ability == null || primary == null || rootAttack == null || kind == null || role == null)
            throw new IllegalArgumentException("School hits require an ability, school, root and role");
        charge = Math.clamp(charge, 0, 1);
        duration = Math.max(0, duration);
        levels = Math.clamp(levels, 1, 3);
    }

    public static SchoolHitContext direct(ResourceLocation ability, EnumBloodTendency primary,
            @Nullable EnumBloodTendency secondary, @Nullable Entity owner) {
        Role role = switch (ability.getPath()) {
            case "blood_aneurysm", "funeral_bell", "vitric_combustion",
                 "prismatic_reproof", "white_verdict" -> Role.EXPLOIT_STATE;
            default -> Role.APPLY_STATE;
        };
        return new SchoolHitContext(ability, primary, secondary, owner == null ? null : owner.getUUID(),
                owner == null ? null : owner.getUUID(), UUID.randomUUID(), Kind.DIRECT, role, 1, 0, 1);
    }

    public SchoolHitContext newAttack() {
        return new SchoolHitContext(ability, primary, secondary, owner, direct, UUID.randomUUID(),
                Kind.DIRECT, role, charge, duration, levels);
    }

    public SchoolHitContext withRole(Role value) {
        return new SchoolHitContext(ability, primary, secondary, owner, direct, rootAttack, kind,
                value, charge, duration, levels);
    }

    public SchoolHitContext withDirect(@Nullable Entity entity) {
        return new SchoolHitContext(ability, primary, secondary, owner,
                entity == null ? direct : entity.getUUID(), rootAttack, kind, role, charge, duration, levels);
    }

    public SchoolHitContext withCharge(float value) {
        return new SchoolHitContext(ability, primary, secondary, owner, direct, rootAttack, kind, role, value, duration, levels);
    }

    public SchoolHitContext withApplication(int ticks, int amount) {
        return new SchoolHitContext(ability, primary, secondary, owner, direct, rootAttack, kind, role, charge, ticks, amount);
    }

    public SchoolHitContext child(Kind value) {
        return new SchoolHitContext(ability, primary, secondary, owner, direct, rootAttack, value,
                Role.DAMAGE_ONLY, charge, duration, levels);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ability", ability.toString());
        tag.putString("primary", primary.name());
        if (secondary != null) tag.putString("secondary", secondary.name());
        if (owner != null) tag.putUUID("owner", owner);
        if (direct != null) tag.putUUID("direct", direct);
        tag.putUUID("root", rootAttack);
        tag.putString("kind", kind.name());
        tag.putString("role", role.name());
        tag.putFloat("charge", charge);
        tag.putInt("duration", duration);
        tag.putInt("levels", levels);
        return tag;
    }

    @Nullable
    public static SchoolHitContext load(CompoundTag tag) {
        try {
            return new SchoolHitContext(ResourceLocation.parse(tag.getString("ability")),
                    EnumBloodTendency.valueOf(tag.getString("primary")),
                    tag.contains("secondary") ? EnumBloodTendency.valueOf(tag.getString("secondary")) : null,
                    tag.hasUUID("owner") ? tag.getUUID("owner") : null,
                    tag.hasUUID("direct") ? tag.getUUID("direct") : null,
                    tag.getUUID("root"), Kind.valueOf(tag.getString("kind")), Role.valueOf(tag.getString("role")),
                    tag.getFloat("charge"), tag.getInt("duration"), tag.getInt("levels"));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
