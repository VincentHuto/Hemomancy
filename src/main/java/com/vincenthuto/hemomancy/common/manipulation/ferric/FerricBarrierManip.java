package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.summon.*;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class FerricBarrierManip extends BloodManipulation {
    private final Kind kind;
    public FerricBarrierManip(String name, Kind kind) {
        super(name, 450, 20, 0, EnumManipulationType.QUICK, EnumManipulationRank.SUMMA, EnumBloodTendency.FERRIC, EnumVeinSections.BODY);
        this.kind = kind;
    }

    @Override protected boolean canPerformAction(Player player, ItemStack stack, float ticks) {
        return FerricPlacement.aimed(player, kind, 18 * SkillPointHelper.getSanguineReachMultiplier(player)) != null
                && super.canPerformAction(player, stack, ticks);
    }

    @Override public void getAction(Player player, Level world, ItemStack stack, BlockPos position) {
        if (!(world instanceof ServerLevel level)) return;
        var placement = FerricPlacement.aimed(player, kind, 18 * SkillPointHelper.getSanguineReachMultiplier(player));
        if (placement == null) return;
        for (var at : placement.origins()) {
            FerricConstructEntity entity = kind == Kind.WALL ? new EntityIronWall(EntityInit.iron_wall.get(), level, player)
                    : new EntityIronSpike(EntityInit.iron_spike.get(), level, player);
            entity.moveTo(at.x, at.y, at.z, placement.facing().toYRot(), 0);
            entity.configure(player, kind, placement.facing(), 120);
            level.addFreshEntity(entity);
        }
        level.playSound(null, BlockPos.containing(placement.origins().getFirst()), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, .65f, .72f);
    }
}
