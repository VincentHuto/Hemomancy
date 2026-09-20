package com.vincenthuto.hemomancy.common.antecedent;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.*;
import static com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch.Evidence.*;

public final class AntecedentDialogue {
    private AntecedentDialogue() {}
    public static DialogueTree epilogue() {
        var node=new DialogueNode("antecedent",List.of("hemomancy.antecedent.epilogue"),List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave",null,null)));
        return new DialogueTree("hemomancy.whisper.speaker_name",com.vincenthuto.hemomancy.Hemomancy.rloc("textures/gui/mystery_speaker.png"),"antecedent",Map.of("antecedent",node),0,DialogueTheme.FUNGAL);
    }
    public static DialogueTree append(DialogueTree tree, Player player, String speaker) {
        int degree=HemoCapabilityAccess.getPlayerDegreeNumber(player);
        var research=HemoCapabilityAccess.antecedent(player);
        if(degree<3 || !(speaker.equals("alchemist") || speaker.equals("vicar"))) return tree;
        var nodes=new LinkedHashMap<>(tree.nodes());
        var root=tree.getStartNode();
        var options=new ArrayList<>(root.options());
        if(research.has(SAMPLE_ANALYZED)) add(nodes,options,"initial_"+speaker,null,4);
        if(speaker.equals("alchemist") && research.has(SEVERED_RECORD_HEARD))
            add(nodes,options,research.has(SAMPLE_RESPONSE)?"replay":"recording","antecedent_replay",4);
        if(speaker.equals("alchemist") && research.has(CONTROLLED_REPLAY)) add(nodes,options,"conclusion",null,4);
        if(speaker.equals("vicar") && degree>=4 && research.has(CONTROLLED_REPLAY)) {
            add(nodes,options,"recognition","antecedent_recognition",7);
            if(research.has(VICAR_RECOGNITION)) add(nodes,options,"clue",null,4);
        }
        if(speaker.equals("vicar") && degree>=5 && research.has(VICAR_RECOGNITION)) add(nodes,options,"comparison",null,4);
        if(speaker.equals("vicar") && research.complete()) add(nodes,options,"housing","antecedent_housing",2);
        nodes.put(root.id(),new DialogueNode(root.id(),root.lines(),options));
        return new DialogueTree(tree.speakerName(),tree.speakerIcon(),tree.startNodeId(),nodes,tree.entityId(),tree.theme(),tree.presentation());
    }
    private static void add(Map<String,DialogueNode> nodes,List<DialogueOption> options,String name,String event,int count) {
        String id="antecedent_"+name;
        options.add(new DialogueOption("hemomancy.antecedent.dialogue."+name+".title",id,event));
        var lines=new ArrayList<String>();
        for(int i=1;i<=count;i++) lines.add("hemomancy.antecedent.dialogue."+name+"."+i);
        nodes.put(id,new DialogueNode(id,lines,List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave",null,null))));
    }
    public static Optional<List<String>> inquiry(Player player,String speaker,ItemStack stack) {
        if(!(speaker.equals("alchemist") || speaker.equals("vicar"))) return Optional.empty();
        if(AhaematicSample.is(stack)) return Optional.of(List.of("hemomancy.antecedent.sample."+(AntecedentPlayback.analyzed(stack)?"identified":"unknown")));
        if(AncientRecordings.get(stack)!=null) return Optional.of(List.of("hemomancy.antecedent.cylinder.description"));
        return Optional.empty();
    }
    public static boolean handle(DialogueEvent event) {
        var player=event.getPlayer();
        var npc=player.level().getEntity(event.getEntityId());
        if(npc==null || !npc.isAlive() || npc.distanceToSqr(player)>64 || HemoCapabilityAccess.getPlayerDegreeNumber(player)<3) return false;
        var research=HemoCapabilityAccess.antecedent(player);
        return switch(event.getEventId()) {
            case "antecedent_replay" -> npc instanceof HarbingerAlchemistEntity && research.has(SEVERED_RECORD_HEARD)
                    && AntecedentKnowledge.record(player,REPLAY_REQUESTED);
            case "antecedent_recognition" -> npc instanceof HarbingerVicarEntity && HemoCapabilityAccess.getPlayerDegreeNumber(player)>=4
                    && research.has(CONTROLLED_REPLAY) && AntecedentKnowledge.record(player,VICAR_RECOGNITION);
            case "antecedent_housing" -> npc instanceof HarbingerVicarEntity && research.complete() && ListeningScarItem.reconstruct(player);
            default -> false;
        };
    }
}
