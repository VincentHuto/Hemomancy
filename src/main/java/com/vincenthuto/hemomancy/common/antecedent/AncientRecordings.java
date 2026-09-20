package com.vincenthuto.hemomancy.common.antecedent;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public final class AncientRecordings {
    public record Caption(int start, int end, String key) {}
    public record Program(String id, int duration, List<Caption> captions) {
        public String sound() { return "hemomancy:antecedent." + id; }
        public String caption(int tick) { return captions.stream().filter(c->tick>=c.start && tick<c.end).map(Caption::key).findFirst().orElse(""); }
        public boolean unresolved(int tick) { return id.equals("severed_record") && tick>=1100 && tick<1360; }
    }
    private static Caption cue(int start,int end,String key) { return new Caption(start*20,end*20,"hemomancy.antecedent.recording."+key); }
    public static final Program SEVERED = new Program("severed_record",1460,List.of(
            cue(0,8,"crackle"),cue(8,11,"severed.1"),cue(11,13,"severed.2"),cue(13,14,"severed.3"),
            cue(14,16,"severed.4"),cue(16,19,"severed.5"),cue(19,23,"severed.6"),cue(26,29,"shriek"),
            cue(30,35,"heartbeat"),cue(35,38,"severed.7"),cue(38,43,"crash"),cue(43,45,"severed.8"),
            cue(45,48,"severed.9"),cue(48,50,"severed.10"),cue(50,52,"severed.11"),cue(52,55,"severed.12"),
            cue(55,68,"silence"),cue(68,72,"crackle"),cue(72,73,"behind")));
    public static final Program SURVEY = new Program("lower_district_survey",600,List.of(cue(0,5,"crackle"),cue(5,14,"survey.1"),cue(14,23,"survey.2"),cue(23,30,"crackle")));
    public static final Program QUIETING = new Program("the_quieting",600,List.of(cue(0,5,"crackle"),cue(5,14,"quieting.1"),cue(14,23,"quieting.2"),cue(23,30,"crackle")));
    public static final List<Program> ALL = List.of(SEVERED,SURVEY,QUIETING);
    private AncientRecordings() {}
    public static Program byId(String id) { return ALL.stream().filter(p->p.id.equals(id)).findFirst().orElse(null); }
    public static Program get(ItemStack cylinder) { return cylinder.is(ItemInit.ambergris_cylinder.get()) ? byId(cylinder.getOrDefault(DataComponentInit.ANCIENT_RECORDING.get(),"")) : null; }
    public static ItemStack cylinder(Program program) { var stack=new ItemStack(ItemInit.ambergris_cylinder.get()); stack.set(DataComponentInit.ANCIENT_RECORDING.get(),program.id); return stack; }
}
