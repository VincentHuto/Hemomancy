package com.vincenthuto.hemomancy.common.worldgen;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.Cell;
import com.vincenthuto.hemomancy.common.worldgen.config.EscharianOvergrowthConfiguration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EscharianOvergrowthPlacementTest {
    @Test void preflightRejectsAnyBackingOrPlantObstructionWithoutClipping() {
        var plan=EscharianOvergrowthLayout.patch(7,new Cell(0,0,0),EscharianOvergrowthConfiguration.DEFAULT,c -> c.z()<=0,c -> c.z()>0);
        assertTrue(EscharianOvergrowthPlacement.preflight(plan,c -> c.z()<=0,c -> c.z()>0));
        for(Cell blocked:plan.backing().keySet()) assertFalse(EscharianOvergrowthPlacement.preflight(plan,c -> c.z()<=0 && !c.equals(blocked),c -> c.z()>0));
        for(Cell blocked:plan.plants().keySet()) assertFalse(EscharianOvergrowthPlacement.preflight(plan,c -> c.z()<=0,c -> c.z()>0 && !c.equals(blocked)));
    }
    @Test void lostGroundSupportRejectsTheWholePile() {
        var plan=EscharianOvergrowthLayout.pile(3,new Cell(0,0,0),c -> c.y()<0,c -> c.y()>=0);
        assertTrue(EscharianOvergrowthPlacement.preflight(plan,c -> c.y()<0,c -> c.y()>=0));
        assertFalse(EscharianOvergrowthPlacement.preflight(plan,c -> false,c -> c.y()>=0));
    }
}
