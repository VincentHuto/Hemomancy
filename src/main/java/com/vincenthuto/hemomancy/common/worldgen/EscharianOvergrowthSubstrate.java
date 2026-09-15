package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.Cell;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.Plan;
import net.minecraft.core.Direction;
import java.util.*;
import java.util.function.Predicate;

/** A shallow infested foundation and a mottled stone transition along the cavern skin. */
public final class EscharianOvergrowthSubstrate {
    public static final int OUTER_REACH = 4;
    public enum Material { INFESTED, VENOUS }
    private EscharianOvergrowthSubstrate() {}

    public static Map<Cell,Material> generate(long seed, Plan growth, Predicate<Cell> natural, Predicate<Cell> air) {
        if(growth.empty())return Map.of();
        Map<Cell,Material> result=new LinkedHashMap<>();
        Map<Cell,Integer> distance=new LinkedHashMap<>();
        ArrayDeque<Cell> pending=new ArrayDeque<>();
        for(Cell c:growth.backing().keySet()) {
            if(growth.ground()) {
                Cell below=c.relative(Direction.DOWN);
                if(!growth.backing().containsKey(below) && natural.test(below)) {
                    result.put(below,Material.INFESTED);
                    if(distance.putIfAbsent(below,0)==null)pending.add(below);
                }
            } else {
                if(distance.putIfAbsent(c,0)==null)pending.add(c);
                for(Direction d:Direction.values()) {
                    Cell root=c.relative(d);
                    if(!growth.backing().containsKey(root) && natural.test(root) && !exposed(root,air))
                        result.put(root,Material.INFESTED);
                }
            }
        }
        while(!pending.isEmpty()) {
            Cell c=pending.removeFirst();int depth=distance.get(c);
            if(depth==OUTER_REACH)continue;
            for(Direction d:Direction.values()) {
                Cell next=c.relative(d);
                visit(next,depth+1,growth,natural,air,distance,pending);
                // The surface also crosses edge connections at inward wall/ceiling corners.
                if(natural.test(next))for(Direction turn:Direction.values())if(turn.getAxis()!=d.getAxis())
                    visit(next.relative(turn),depth+1,growth,natural,air,distance,pending);
            }
        }
        for(var entry:distance.entrySet()) {
            Cell c=entry.getKey();int depth=entry.getValue();
            if(growth.backing().containsKey(c) || result.containsKey(c))continue;
            int variation=(int)Math.floorMod(PhlegethonticRules.seed(seed ^ 0x5355425354524154L,c.x(),c.z(),c.y()),100);
            if(depth<=1 || depth==2 && variation<80 || depth==3 && variation<20)
                result.put(c,Material.INFESTED);
            else if(depth<=3 || variation<55)result.put(c,Material.VENOUS);
        }
        return Collections.unmodifiableMap(result);
    }
    private static void visit(Cell c,int depth,Plan growth,Predicate<Cell> natural,Predicate<Cell> air,
                               Map<Cell,Integer> distance,ArrayDeque<Cell> pending) {
        if(!distance.containsKey(c) && !growth.plants().containsKey(c) && natural.test(c) && exposed(c,air)) {
            distance.put(c,depth);pending.addLast(c);
        }
    }
    private static boolean exposed(Cell c,Predicate<Cell> air) {
        for(Direction d:Direction.values())if(air.test(c.relative(d)))return true;
        return false;
    }
}
