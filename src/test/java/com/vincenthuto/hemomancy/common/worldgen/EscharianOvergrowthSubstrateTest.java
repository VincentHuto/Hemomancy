package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.*;
import com.vincenthuto.hemomancy.common.worldgen.config.EscharianOvergrowthConfiguration;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.*;

class EscharianOvergrowthSubstrateTest {
    @Test void flatSurfacesHaveInfestedRootsAndTwoBlendedBandsInEveryOrientation() {
        for(Direction face:Direction.values())for(int seed=0;seed<32;seed++) {
            Predicate<Cell> natural=c -> dot(c,face)<=0;
            var growth=EscharianOvergrowthLayout.patch(seed,new Cell(0,0,0),EscharianOvergrowthConfiguration.DEFAULT,natural,natural.negate());
            var skin=EscharianOvergrowthSubstrate.generate(seed,growth,natural,natural.negate());
            assertFalse(skin.isEmpty());assertTrue(skin.containsValue(EscharianOvergrowthSubstrate.Material.INFESTED));
            assertTrue(skin.containsValue(EscharianOvergrowthSubstrate.Material.VENOUS));
            assertEquals(skin,EscharianOvergrowthSubstrate.generate(seed,growth,natural,natural.negate()));
            for(Cell c:growth.backing().keySet())assertEquals(EscharianOvergrowthSubstrate.Material.INFESTED,skin.get(c.relative(face.getOpposite())));
            skin.forEach((c,m) -> {assertTrue(natural.test(c));assertFalse(growth.backing().containsKey(c));assertFalse(growth.plants().containsKey(c));
                assertTrue(dot(c,face)>=-1,"No deep rock conversion");});
        }
    }
    @Test void groundFootprintIsEntirelyRootedInInfestedStone() {
        Predicate<Cell> natural=c -> c.y()<0;
        var growth=EscharianOvergrowthLayout.pile(0,new Cell(0,0,0),natural,natural.negate());
        var skin=EscharianOvergrowthSubstrate.generate(0,growth,natural,natural.negate());
        for(Cell c:growth.backing().keySet())if(c.y()==0)
            assertEquals(EscharianOvergrowthSubstrate.Material.INFESTED,skin.get(c.relative(Direction.DOWN)));
        assertTrue(skin.keySet().stream().allMatch(c -> c.y()==-1));
        assertTrue(skin.size()>growth.backing().size());
    }
    @Test void substrateFollowsStepsAndNeverReplacesProtectedCellsOrAir() {
        Predicate<Cell> rock=c -> c.z()<=Math.floorDiv(c.x(),2) || c.y()>=4;
        Predicate<Cell> allowed=c -> rock.test(c) && c.x()!=3;
        var growth=EscharianOvergrowthLayout.patch(7,new Cell(0,2,0),EscharianOvergrowthConfiguration.DEFAULT,allowed,rock.negate());
        var skin=EscharianOvergrowthSubstrate.generate(7,growth,allowed,rock.negate());
        assertTrue(skin.keySet().stream().allMatch(allowed));
        assertTrue(skin.keySet().stream().map(Cell::z).distinct().count()>3);
    }
    @Test void innerTwoBlocksStayContinuousAndOuterEdgeIsMottled() {
        var origin=new Cell(0,0,0);
        var growth=new Plan(false,Map.of(origin,Layer.CENTER),Map.of());
        var skin=EscharianOvergrowthSubstrate.generate(17,growth,c -> c.y()<=0,c -> c.y()>0);
        int outerStone=0,outerGaps=0;
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++) {
            int radius=Math.max(Math.abs(x),Math.abs(z));var material=skin.get(new Cell(x,0,z));
            if(radius==1)assertEquals(EscharianOvergrowthSubstrate.Material.INFESTED,material);
            if(radius==2 || radius==3)assertNotNull(material);
            if(radius==4) {if(material==null)outerGaps++;else outerStone++;}
        }
        assertTrue(outerStone>0 && outerGaps>0);
    }
    private static int dot(Cell c,Direction d) {return c.x()*d.getStepX()+c.y()*d.getStepY()+c.z()*d.getStepZ();}
}
