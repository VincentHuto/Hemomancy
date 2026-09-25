package com.vincenthuto.hemomancy.common.item.harbinger;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
import java.util.List;
public class AmbergrisCylinderItem extends Item implements HemoClientItemExtensionsProvider {
    public AmbergrisCylinderItem(){super(new Properties().stacksTo(16));}
    @Override public net.neoforged.neoforge.client.extensions.common.IClientItemExtensions hemomancy$getClientItemExtensions(){return com.vincenthuto.hemomancy.client.render.item.harbinger.AmbergrisCylinderRenderer.EXTENSIONS;}
    @Override public Component getName(ItemStack stack) {
        var pattern=stack.get(DataComponentInit.RESONANT_PATTERN.get());
        if(pattern!=null) return Component.translatable(pattern.master()
                ? "item.hemomancy.ambergris_cylinder.master" : "item.hemomancy.ambergris_cylinder.pattern");
        var program=com.vincenthuto.hemomancy.common.antecedent.AncientRecordings.get(stack);
        return program==null?super.getName(stack):Component.translatable("hemomancy.antecedent.cylinder."+program.id());
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flags){
        var pattern=stack.get(DataComponentInit.RESONANT_PATTERN.get());
        if(pattern!=null){
            lines.add(Component.translatable(pattern.master()
                    ? "tooltip.hemomancy.resonant_pattern.master" : "tooltip.hemomancy.resonant_pattern.single_use"));
            for(var entry:pattern.enchantments()){
                var id=net.minecraft.resources.ResourceLocation.tryParse(entry.enchantment());
                var enchantment=id==null?java.util.Optional.<net.minecraft.core.Holder.Reference<net.minecraft.world.item.enchantment.Enchantment>>empty()
                        : context.registries().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                        .get(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,id));
                lines.add(enchantment.map(holder -> net.minecraft.world.item.enchantment.Enchantment.getFullname(holder,entry.level()))
                        .orElse(Component.literal(entry.enchantment()+" "+entry.level())));
            }
            if(!pattern.curse().isBlank()) lines.add(Component.translatable("hemomancy.scriptorium.curse."+pattern.curse())
                    .append(" "+pattern.curseSeverity()));
            return;
        }
        if(stack.has(DataComponentInit.ANCIENT_RECORDING.get())) {
            var program=com.vincenthuto.hemomancy.common.antecedent.AncientRecordings.get(stack);
            lines.add(Component.translatable(program==null?"gui.hemomancy.clairaudiograph.unreadable":"hemomancy.antecedent.cylinder.description"));
            return;
        }
        var r=stack.get(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get());
        if(r==null){lines.add(Component.translatable("gui.hemomancy.clairaudiograph.blank"));return;}
        if(!r.readable()){lines.add(Component.translatable("gui.hemomancy.clairaudiograph.unreadable"));return;}
        var id=net.minecraft.resources.ResourceLocation.parse(r.source());
        lines.add(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(t->t.getDescription()).orElse(Component.translatable("gui.hemomancy.clairaudiograph.unknown")));
        lines.add(Component.translatable("sound_kind.hemomancy."+r.kind()));
        if(flags.isAdvanced()){lines.add(Component.literal(r.source()));lines.add(Component.literal(r.sound()));}
    }
}
