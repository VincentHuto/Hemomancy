package com.vincenthuto.hemomancy.common.worldgen;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PhlegethonticGeometryTest {
    @Test void basinShorelineFormsPronouncedConnectedFingersAndCoves() {
        var layout=new PhlegethonticBasinLayout(17,0,0,List.of(
                new PhlegethonticBasinLayout.Basin(0,0,32,32,48)),List.of());
        DoubleSummaryStatistics crossings=new DoubleSummaryStatistics();
        for(int ray=0;ray<64;ray++) {
            double angle=ray*Math.PI*2/64;
            int transitions=0;
            boolean inside=true;
            double crossing=Double.NaN;
            for(double radius=0;radius<=52;radius+=.25) {
                boolean nextInside=layout.sample(Math.cos(angle)*radius,Math.sin(angle)*radius).distance()<=0;
                if(nextInside!=inside) {
                    transitions++;
                    if(!nextInside && Double.isNaN(crossing))crossing=radius;
                }
                inside=nextInside;
            }
            assertEquals(1,transitions,"Each angular slice must keep one connected basin boundary");
            crossings.accept(crossing);
        }
        assertTrue(crossings.getMin()<=26,"Inward coves must recede at least six blocks; got "+crossings.getMin());
        assertTrue(crossings.getMax()>=38,"Outward fingers must reach at least six blocks; got "+crossings.getMax());
        assertTrue(crossings.getMax()-crossings.getMin()>=12,
                "The shoreline needs a clearly readable finger-and-cove silhouette: "+crossings);
        assertEquals(layout.sample(-37.5,4.25),layout.sample(-37.5,4.25),"Sampling must remain deterministic");
    }

    @Test void sentinelCandidatesFollowTheRoughRelievedShoreline() {
        var layout=new PhlegethonticBasinLayout(17,0,0,List.of(
                new PhlegethonticBasinLayout.Basin(0,0,32,32,48)),List.of());
        Set<Integer> heightOffsets=new HashSet<>();
        int valid=0;
        for(var candidate:layout.sentinelCandidates()) {
            var column=PhlegethonticTerrainPlan.column(List.of(layout),candidate.x(),candidate.z());
            if(column.distance()>=1 && column.distance()<=9)valid++;
            heightOffsets.add(candidate.y()-column.surface());
            Set<Integer> footprint=new HashSet<>();
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++) {
                var support=PhlegethonticTerrainPlan.column(List.of(layout),candidate.x()+dx,candidate.z()+dz);
                footprint.add(support.surface()+1+PhlegethonticTerrainPlan.bankRelief(candidate.x()+dx,candidate.z()+dz));
            }
            assertEquals(Set.of(candidate.y()-1),footprint,"A sentinel candidate needs one level 3x3 footing");
        }
        assertTrue(valid>=24,"At least three quarters of sentinel probes must follow the deformed shore; got "+valid);
        assertEquals(Set.of(2,3,4),heightOffsets,"Sentinel probes must follow all three bank-relief levels");
    }

    @Test void overlappingReservoirsDoNotEraseAConnectingChannel() {
        var river=new PhlegethonticBasinLayout(0,0,0,List.of(),List.of(
                new PhlegethonticBasinLayout.Channel(0,1,5,List.of(
                        new PhlegethonticBasinLayout.Point(0,8,56),
                        new PhlegethonticBasinLayout.Point(32,8,56)),true)));
        var reservoir=new PhlegethonticBasinLayout(1,0,0,List.of(
                new PhlegethonticBasinLayout.Basin(16,8,42,42,46)),List.of());
        var column=PhlegethonticTerrainPlan.column(List.of(river,reservoir),16,8);
        assertTrue(column.channel() && column.raised(),"A neighboring bowl must preserve the connecting trough");
        assertEquals(column,PhlegethonticTerrainPlan.column(List.of(reservoir,river),16,8));
    }
    @Test void theLargestReservoirKeepsRaisedPrimaryConnections() {
        for(int x=-10;x<10;x++) {
            var layout=PhlegethonticBasinLayout.create(8675309,x,0,32);
            int primary=layout.basins().indexOf(layout.largestBasin());
            for(var channel:layout.channels())if(channel.from()==primary || channel.to()==primary)
                assertTrue(channel.raised(),"The primary reservoir needs a raised connecting course");
        }
    }
    @Test void aBasinOutletRetainsItsRaisedChannelIdentity() {
        var layout=new PhlegethonticBasinLayout(0,0,0,List.of(
                new PhlegethonticBasinLayout.Basin(0,0,42,42,56),
                new PhlegethonticBasinLayout.Basin(110,0,18,18,46)),List.of(
                new PhlegethonticBasinLayout.Channel(0,1,7,List.of(
                        new PhlegethonticBasinLayout.Point(0,0,56),
                        new PhlegethonticBasinLayout.Point(110,0,46)),true)));
        var outlet=layout.sample(32,0);
        assertTrue(outlet.channel() && outlet.raised(),"The outer bowl must not swallow its outgoing aqueduct");
        assertTrue(outlet.surface()<56,"The outlet must descend toward the connected lower basin");
    }
    @Test void raisedChannelsRemainVisibleBetweenTheBowls() {
        int visible=0;
        for(int x=-10;x<10;x++) {
            var layout=PhlegethonticBasinLayout.create(8675309,x,0,32);
            boolean raised=false;
            for(var channel:layout.channels()) {
                double span=0;
                for(int i=1;i<channel.points().size();i++) {
                    var a=channel.points().get(i-1);var b=channel.points().get(i);
                    if(layout.sample((a.x()+b.x())/2,(a.z()+b.z())/2).raised())span+=Math.hypot(b.x()-a.x(),b.z()-a.z());
                    else span=0;
                    if(span>=22)raised=true;
                }
            }
            if(raised)visible++;
        }
        assertTrue(visible>=12,"Two-arch spans must survive bowl overlap in at least 60% of unclipped sample layouts; got "+visible);
    }
    @Test void basinNetworksRemainConnectedAcrossNegativeCells() {
        for (int x = -3; x <= 3; x++) {
            var layout = PhlegethonticBasinLayout.create(42L, x, -1, 32);
            assertTrue(layout.basins().size() >= 2 && layout.basins().size() <= 4);
            Set<Integer> reached = new HashSet<>(Set.of(0));
            for (int pass = 0; pass < 4; pass++) for (var channel : layout.channels()) {
                if (reached.contains(channel.from())) reached.add(channel.to());
                if (reached.contains(channel.to())) reached.add(channel.from());
            }
            assertEquals(layout.basins().size(), reached.size());
            assertEquals(layout, PhlegethonticBasinLayout.create(42L, x, -1, 32));
            for (var basin : layout.basins()) {
                assertTrue(basin.surface() >= 40 && basin.surface() <= 68);
                assertTrue(basin.radiusX() >= 18 && basin.radiusX() <= 42);
                assertTrue(basin.radiusZ() >= 18 && basin.radiusZ() <= 42);
            }
        }
    }

    @Test void veinEnvelopePreservesBedrockEvenWithTranslatedDimensionBottom() {
        int activated = 0;
        for (int x = -80; x < 80; x++) {
            var nodes = PhlegethonticVeinPath.fromChunk(29L, x, 0, -64);
            if (nodes.isEmpty()) continue;
            activated++;
            assertTrue(nodes.size() >= 32 && nodes.size() <= 72);
            for (var node : nodes) {
                assertTrue(node.y() - node.radius() - 2 >= -59);
                assertTrue(node.y() + node.radius() + 2 <= -44);
            }
            assertEquals(nodes, PhlegethonticVeinPath.fromChunk(29L, x, 0, -64));
        }
        assertTrue(activated > 2 && activated < 20);
    }

    @Test void chunksReconstructTheSameCrossingVein() {
        boolean found = false;
        for (int x = -30; x < 30 && !found; x++) {
            var west = PhlegethonticVeinPath.nearChunk(17, x, 0, 0);
            var east = PhlegethonticVeinPath.nearChunk(17, x + 1, 0, 0);
            for (var node : west) if (Math.abs(node.x() - (x + 1) * 16) < 2 && node.z() >= 0 && node.z() < 16) {
                assertTrue(east.contains(node));
                found = true;
            }
        }
        assertTrue(found, "sample must exercise a real crossing");
    }

    @Test void pulseAndSoundingHaveExplicitTransitions() {
        assertTrue(PhlegethonticRules.pulsing(0));
        assertTrue(PhlegethonticRules.pulsing(29));
        assertFalse(PhlegethonticRules.pulsing(30));
        assertTrue(PhlegethonticRules.pulsing(160));
        assertFalse(PhlegethonticRules.pullToRiver(.5));
        assertTrue(PhlegethonticRules.pullToRiver(.51));
        assertEquals(50, PhlegethonticRules.severCost(5000));
        assertEquals(100, PhlegethonticRules.severCost(20000));
    }
}
