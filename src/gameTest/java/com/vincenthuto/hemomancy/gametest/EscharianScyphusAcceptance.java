package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.EscharianScyphusBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

/** Exercises real BlockItem placement, empty-hand drops and neighbor updates in a disposable level. */
final class EscharianScyphusAcceptance {
    static void run(Player player, BlockPos origin) {
        ServerLevel level=(ServerLevel)player.level();
        var block=BlockInit.escharian_scyphus.get();
        for(var material:new Block[]{Blocks.STONE,BlockInit.escharian_overgrowth.get()})
        for(Direction face:Direction.values())for(boolean wet:new boolean[]{false,true}) {
            BlockPos support=origin.offset(face.ordinal()*5,0,wet?5:0), pos=support.relative(face);
            level.setBlock(support,BlockInit.escharian_overgrowth_rim.get().defaultBlockState(),3);
            level.setBlock(pos,wet?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState(),3);
            clearDrops(level,pos);
            ItemStack stack=new ItemStack(block);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var context=new BlockPlaceContext(player,InteractionHand.MAIN_HAND,stack,
                    new BlockHitResult(Vec3.atCenterOf(support).add(Vec3.atLowerCornerOf(face.getNormal()).scale(.5)),face,support,false));
            require(!((BlockItem)block.asItem()).place(context).consumesAction(),"White rim must reject placement "+face);
            require(stack.getCount()==1 && !level.getBlockState(pos).is(block),"Rejected rim placement must not consume the item");
            level.setBlock(support,material.defaultBlockState(),3);
            require(((BlockItem)block.asItem()).place(context).consumesAction(),"BlockItem placement "+face);
            var state=level.getBlockState(pos);
            require(state.is(block) && state.getValue(EscharianScyphusBlock.FACING)==face,"Orientation "+face);
            require(state.getValue(EscharianScyphusBlock.COUNT)==1,"Initial count "+face);
            require(state.getValue(EscharianScyphusBlock.WATERLOGGED)==wet,"Waterlogging "+face);
            require(state.getFluidState().isSource()==wet,"Fluid state "+face);
            require(state.getCollisionShape(level,pos).isEmpty(),"Movement collision");
            for(Rotation rotation:Rotation.values())require(block.rotate(state,rotation).getValue(EscharianScyphusBlock.FACING)==rotation.rotate(face),"Rotation");
            for(Mirror mirror:Mirror.values())require(block.mirror(state,mirror).getValue(EscharianScyphusBlock.FACING)==mirror.mirror(face),"Mirror");
            ItemStack additions=new ItemStack(block,4);
            player.setItemInHand(InteractionHand.MAIN_HAND,additions);
            var stackContext=new BlockPlaceContext(player,InteractionHand.MAIN_HAND,additions,
                    new BlockHitResult(Vec3.atCenterOf(pos),face,pos,false));
            for(int count=2;count<=5;count++) {
                require(((BlockItem)block.asItem()).place(stackContext).consumesAction(),"Stack count "+count+" "+face);
                state=level.getBlockState(pos);
                require(state.getValue(EscharianScyphusBlock.COUNT)==count,"Placed count "+count+" "+face);
                require(state.getValue(EscharianScyphusBlock.FACING)==face,"Stacking preserves facing "+face);
                require(state.getValue(EscharianScyphusBlock.WATERLOGGED)==wet,"Stacking preserves water "+face);
                var bounds=state.getShape(level,pos).bounds();
                double thickness=bounds.max(face.getAxis())-bounds.min(face.getAxis());
                double expected=count==5?3.25/16:1.625/16;
                require(Math.abs(thickness-expected)<.00001,"Selection thickness count="+count+" "+face);
                require(face.getAxisDirection()==Direction.AxisDirection.POSITIVE
                        ? bounds.min(face.getAxis())==0 : bounds.max(face.getAxis())==1,"Selection attachment "+face);
            }
            ItemStack excess=new ItemStack(block);
            player.setItemInHand(InteractionHand.MAIN_HAND,excess);
            require(!((BlockItem)block.asItem()).place(new BlockPlaceContext(player,InteractionHand.MAIN_HAND,excess,
                    new BlockHitResult(Vec3.atCenterOf(pos),face,pos,false))).consumesAction(),"Count five rejects another Scyphus");
            require(excess.getCount()==1 && level.getBlockState(pos).getValue(EscharianScyphusBlock.COUNT)==5,
                    "Rejected sixth Scyphus must remain in hand");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            level.removeBlock(pos,false);
            block.playerDestroy(level,player,pos,state,null,ItemStack.EMPTY);
            require(dropCount(level,pos)==5,"Empty hand harvest "+face+" wet="+wet+" drops="+dropCount(level,pos));
            clearDrops(level,pos);
            level.setBlock(pos,wet?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState(),3);
            ItemStack replanted=new ItemStack(block);
            player.setItemInHand(InteractionHand.MAIN_HAND,replanted);
            require(((BlockItem)block.asItem()).place(new BlockPlaceContext(player,InteractionHand.MAIN_HAND,replanted,
                    new BlockHitResult(Vec3.atCenterOf(support),face,support,false))).consumesAction(),"Replant");
            ItemStack supportLossGroup=new ItemStack(block,4);
            player.setItemInHand(InteractionHand.MAIN_HAND,supportLossGroup);
            var supportLossContext=new BlockPlaceContext(player,InteractionHand.MAIN_HAND,supportLossGroup,
                    new BlockHitResult(Vec3.atCenterOf(pos),face,pos,false));
            for(int count=2;count<=5;count++)
                require(((BlockItem)block.asItem()).place(supportLossContext).consumesAction(),"Support-loss stack "+count);
            if(material==Blocks.STONE)level.removeBlock(support,false);
            else level.setBlock(support,BlockInit.escharian_overgrowth_rim.get().defaultBlockState(),3);
            require(!level.getBlockState(pos).is(block),"Unsupported plant must break");
            require(dropCount(level,pos)==5,"Support loss "+face+" wet="+wet+" drops="+dropCount(level,pos));
            require(level.getFluidState(pos).isSource()==wet,"Support loss preserves water");
            clearDrops(level,pos);level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);level.removeBlock(support,false);
        }
        com.mojang.logging.LogUtils.getLogger().info("ESCHARIAN_SCYPHUS_ACCEPTANCE passed: six faces, counts 1-5, dry/wet placement, shapes, rotation/mirror, count drops, replant, support drops, white-rim rejection");
    }
    private static int dropCount(ServerLevel level,BlockPos pos) {
        return level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1)).stream()
                .filter(e -> e.getItem().is(BlockInit.escharian_scyphus.get().asItem())).mapToInt(e -> e.getItem().getCount()).sum();
    }
    private static void clearDrops(ServerLevel level,BlockPos pos) {
        level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1)).forEach(ItemEntity::discard);
    }
    private static void require(boolean condition,String message) { if(!condition)throw new IllegalStateException(message); }
}
