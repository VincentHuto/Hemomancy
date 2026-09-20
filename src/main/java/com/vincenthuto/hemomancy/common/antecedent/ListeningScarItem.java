package com.vincenthuto.hemomancy.common.antecedent;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;

public final class ListeningScarItem extends Item {
    public ListeningScarItem() { super(new Properties().stacksTo(1).rarity(Rarity.RARE)); }
    public static int state(ItemStack stack) { int state=stack.getOrDefault(DataComponentInit.LISTENING_SCAR_STATE.get(),0);return state>=0 && state<=2?state:0; }
    public static boolean awake(ItemStack stack) { return stack.is(ItemInit.listening_scar.get()) && state(stack)==2; }
    @Override public Component getName(ItemStack stack) { return Component.translatable("item.hemomancy.listening_scar."+state(stack)); }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flags) {
        lines.add(Component.translatable("hemomancy.antecedent.scar.instructions."+state(stack)));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        var housing=player.getItemInHand(hand);
        var other=hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND;
        var vial=player.getItemInHand(other);
        if(state(housing)!=0 || !AntecedentPlayback.analyzed(vial) || vial.getCount()!=1) return InteractionResultHolder.pass(housing);
        if(!level.isClientSide) {
            if (vial.getItem() instanceof com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem)
                vial=AhaematicSample.migrate(vial);
            housing.set(DataComponentInit.LISTENING_SCAR_STATE.get(),1);
            player.setItemInHand(other,BloodSampleData.emptyVessel(vial));
        }
        return InteractionResultHolder.sidedSuccess(housing,level.isClientSide);
    }
    public static void awaken(Player player) {
        for(var hand:InteractionHand.values()) {
            var stack=player.getItemInHand(hand);
            if(stack.is(ItemInit.listening_scar.get()) && state(stack)==1) {
                stack.set(DataComponentInit.LISTENING_SCAR_STATE.get(),2);
                player.displayClientMessage(Component.translatable("hemomancy.antecedent.scar.awakened"),true);
                player.getInventory().setChanged(); player.containerMenu.broadcastChanges();
            }
        }
    }
    public static boolean reconstruct(ServerPlayer player) {
        var inventory=player.getInventory();
        if(inventory.countItem(Items.IRON_NUGGET)<4 || inventory.countItem(Items.ECHO_SHARD)<1) {
            player.displayClientMessage(Component.translatable("hemomancy.antecedent.housing.cost"),true); return false;
        }
        int iron=4,echo=1;
        for(int slot=0;slot<inventory.getContainerSize();slot++) {
            var stack=inventory.getItem(slot);
            if(stack.is(Items.IRON_NUGGET)) {int take=Math.min(iron,stack.getCount());stack.shrink(take);iron-=take;}
            if(stack.is(Items.ECHO_SHARD)) {int take=Math.min(echo,stack.getCount());stack.shrink(take);echo-=take;}
        }
        var result=new ItemStack(ItemInit.listening_scar.get());
        if(!inventory.add(result)) player.drop(result,false);
        inventory.setChanged(); player.containerMenu.broadcastChanges(); return true;
    }
}
