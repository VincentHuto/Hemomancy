package com.vincenthuto.hemomancy.common.antecedent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Generated socket arrangements and controller coordinates, shared with the NBT authoring tool. */
public final class VigilModules {
    public record Part(String name, BlockPos offset) {}
    public record Layout(VigilLayout.Plan plan, List<Part> parts, BlockPos entry,
                         Rotation entryRotation, String composite) {}
    public static final List<Layout> ALL=load();
    private VigilModules() {}

    private static List<Layout> load() {
        try(var stream=VigilModules.class.getResourceAsStream("/data/hemomancy/antecedent/vigil_layouts.json")) {
            if(stream==null)throw new IllegalStateException("Missing Vigil module manifest");
            var layouts=new ArrayList<Layout>();
            for(var value:JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonArray()) {
                var json=value.getAsJsonObject();
                var parts=new ArrayList<Part>();
                for(var part:json.getAsJsonArray("parts")) {
                    var object=part.getAsJsonObject();parts.add(new Part(object.get("name").getAsString(),pos(object,"offset")));
                }
                var rooms=new ArrayList<BoundingBox>();
                for(var room:json.getAsJsonArray("rooms"))rooms.add(box(room.getAsJsonArray()));
                var sensors=new ArrayList<BlockPos>();
                for(var sensor:json.getAsJsonArray("sensors"))sensors.add(pos(sensor.getAsJsonArray()));
                var approach=new VigilLayout.Approach(pos(json,"passageOrigin"),rotation(json,"passageRotation"),List.copyOf(rooms));
                var plan=new VigilLayout.Plan(json.get("version").getAsInt(),pos(json,"vessel"),List.copyOf(sensors),
                        pos(json,"catalyst"),pos(json,"shrieker"),pos(json,"record"),pos(json,"chest"),box(json.getAsJsonArray("gallery")),
                        pos(json,"size"),7,pos(json,"bed"),8,11,pos(json,"demonstration"),pos(json,"firstClick"),pos(json,"secondClick"),approach);
                layouts.add(new Layout(plan,List.copyOf(parts),pos(json,"entry"),rotation(json,"entryRotation"),json.get("composite").getAsString()));
            }
            return List.copyOf(layouts);
        } catch(java.io.IOException failure) {throw new IllegalStateException("Cannot read Vigil module manifest",failure);}
    }
    private static Rotation rotation(JsonObject json,String key) {return Rotation.values()[json.get(key).getAsInt()];}
    private static BlockPos pos(JsonObject json,String key) {return pos(json.getAsJsonArray(key));}
    private static BlockPos pos(JsonArray a) {return new BlockPos(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt());}
    private static BoundingBox box(JsonArray a) {
        return new BoundingBox(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt(),a.get(3).getAsInt(),a.get(4).getAsInt(),a.get(5).getAsInt());
    }
}
