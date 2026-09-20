package com.vincenthuto.hemomancy.common.antecedent;

import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class VigilLayoutTest {
    @Test void compactPassageKeepsWoolAndListeningThresholdsInOrder() {
        var plan=VigilLayout.COMPACT;
        assertTrue(plan.passageDepth(new BlockPos(4,11,5))<8);
        assertEquals(8,plan.passageDepth(new BlockPos(4,11,6)));
        assertEquals(10,plan.passageDepth(new BlockPos(4,11,8)));
        assertEquals(14,plan.passageDepth(new BlockPos(4,10,9)));
        assertTrue(plan.passageDepth(new BlockPos(4,9,12))>=19);
        assertEquals(34,plan.passageDepth(new BlockPos(10,8,18)));
    }

    @Test void ambienceReturnsBelowBothBalconiesAndOutsideTheSite() {
        for(var plan:new VigilLayout.Plan[]{VigilLayout.LEGACY,VigilLayout.COMPACT}) {
            var position=plan.gallery().getCenter().atY(plan.balcony()+1);
            assertEquals(.15F,plan.ambience(position));
            assertEquals(1F,plan.ambience(position.atY(1)));
            assertEquals(1F,plan.ambience(position.offset(100,0,0)));
        }
    }

    @Test void moduleApproachesKeepEncounterOrderRegardlessOfSocketOrientation() {
        assertEquals(6,VigilModules.ALL.size());
        for(var layout:VigilModules.ALL) {
            var plan=layout.plan();var approach=plan.approach();
            assertSame(plan,VigilLayout.forVersion(plan.version()));
            for(int z=-5;z<9;z++) {
                var local=new BlockPos(2,z<5?2:1,z);
                var at=approach.origin().offset(local.rotate(approach.rotation()));
                assertTrue(plan.contains(at));
                assertEquals(z<0?z+5:z<3?z+8:14+(z-3)*3,plan.passageDepth(at));
            }
            assertEquals(34,plan.passageDepth(plan.gallery().getCenter()));
            assertEquals(1F,plan.ambience(plan.vessel()));
        }
        assertSame(VigilLayout.LEGACY,VigilLayout.forVersion(0));
        assertSame(VigilLayout.COMPACT,VigilLayout.forVersion(2));
    }

    @Test void naturalCityBaselineCannotBeModifiedByTheOldReservation() throws Exception {
        var config=JsonParser.parseString(Files.readString(Path.of("src/main/resources/hemomancy.mixins.json"))).getAsJsonObject();
        for(var entry:config.getAsJsonArray("mixins")) assertNotEquals("MixinVigilReservation",entry.getAsString());
    }
}
