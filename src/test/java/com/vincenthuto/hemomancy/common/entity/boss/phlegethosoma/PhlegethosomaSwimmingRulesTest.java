package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhlegethosomaSwimmingRulesTest {
    @Test void exportedBodyHasSerpentineProportions() throws Exception {
        var model=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(java.nio.file.Path.of(
            "src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/PhlegethosomaModel.bbmodel"))).getAsJsonObject();
        double minX=Double.POSITIVE_INFINITY,maxX=Double.NEGATIVE_INFINITY,minZ=minX,maxZ=maxX;
        for(var element:model.getAsJsonArray("elements")) {
            var box=element.getAsJsonObject();var from=box.getAsJsonArray("from");var to=box.getAsJsonArray("to");
            for(int axis=0;axis<3;axis++) {
                double low=from.get(axis).getAsDouble(),high=to.get(axis).getAsDouble();
                assertEquals(Math.rint(low*4),low*4,.001,"Cube bounds use quarter units");
                assertEquals(Math.rint(high*4),high*4,.001,"Cube bounds use quarter units");
                assertTrue(high-low>=.25,"No paper-thin zero-sized cubes");
            }
            minX=Math.min(minX,from.get(0).getAsDouble());maxX=Math.max(maxX,to.get(0).getAsDouble());
            minZ=Math.min(minZ,from.get(2).getAsDouble());maxZ=Math.max(maxZ,to.get(2).getAsDouble());
        }
        assertTrue((maxZ-minZ)/(maxX-minX)>4.5,"The exported animal must have a long, narrow fluke silhouette");
    }
    @Test void travellingWaveBendsAdjacentSegmentsDifferently() {
        float first=PhlegethosomaSwimmingRules.segmentYaw(1,8,1);
        float tail=PhlegethosomaSwimmingRules.segmentYaw(9,8,1);
        assertNotEquals(first,tail);
        assertNotEquals(first,PhlegethosomaSwimmingRules.segmentYaw(1,12,1));
    }
    @Test void fastSwimmingHasMoreTailMotionThanRest() {
        float rest=0,swim=0;
        for(int t=0;t<50;t++) {
            rest+=Math.abs(PhlegethosomaSwimmingRules.segmentYaw(9,t,0));
            swim+=Math.abs(PhlegethosomaSwimmingRules.segmentYaw(9,t,1));
        }
        assertTrue(swim>rest*2);
    }
    @Test void pursuitIsFastButCappedAndRuptureDoesNotSlowIt() {
        assertTrue(PhlegethosomaSwimmingRules.speed(1)>.3);
        assertTrue(PhlegethosomaSwimmingRules.speed(3)>PhlegethosomaSwimmingRules.speed(1));
        assertTrue(PhlegethosomaSwimmingRules.speed(3)<=.6);
    }
    @Test void bodyIsLongAndSegmentYawRemainsBounded() {
        assertTrue(PhlegethosomaSwimmingRules.SEGMENTS>=12);
        for(int i=0;i<PhlegethosomaSwimmingRules.SEGMENTS;i++)for(int t=0;t<50;t++)
            assertTrue(Math.abs(PhlegethosomaSwimmingRules.segmentYaw(i,t,1))<=.25);
    }
}
