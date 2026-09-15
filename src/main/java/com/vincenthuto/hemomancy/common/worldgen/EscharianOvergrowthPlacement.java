package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.Cell;
import java.util.function.Predicate;

public final class EscharianOvergrowthPlacement {
    private EscharianOvergrowthPlacement() {}
    public static boolean preflight(EscharianOvergrowthLayout.Plan plan, Predicate<Cell> natural, Predicate<Cell> air) {
        if (plan.empty() || plan.plants().size()<3) return false;
        for (Cell c : plan.backing().keySet()) {
            if (!(plan.ground() ? air : natural).test(c)) return false;
            if (plan.ground() && !plan.backing().containsKey(c.relative(net.minecraft.core.Direction.DOWN))
                    && !natural.test(c.relative(net.minecraft.core.Direction.DOWN))) return false;
        }
        return plan.plants().entrySet().stream().allMatch(e -> air.test(e.getKey())
                && !plan.backing().containsKey(e.getKey())
                && plan.backing().get(e.getKey().relative(e.getValue().facing().getOpposite())) == EscharianOvergrowthLayout.Layer.CENTER);
    }
}
