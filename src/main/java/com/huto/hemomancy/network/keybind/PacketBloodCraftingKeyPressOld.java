package com.huto.hemomancy.network.keybind;
/*
 * package com.huto.hemomancy.network.crafting;
 * 
 * import java.util.function.Supplier;
 * 
 * import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
 * import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume; import
 * com.huto.hemomancy.network.PacketHandler; import
 * com.huto.hemomancy.network.capa.BloodVolumePacketServer; import
 * com.huto.hemomancy.recipes.BaseBloodCraftingRecipe; import
 * com.huto.hemomancy.recipes.ModBloodCraftingRecipes;
 * 
 * import net.minecraft.block.Block; import net.minecraft.block.Blocks; import
 * net.minecraft.block.pattern.BlockPattern; import
 * net.minecraft.entity.item.ItemEntity; import
 * net.minecraft.entity.player.Player; import
 * net.minecraft.entity.player.ServerPlayer; import
 * net.minecraft.item.ItemStack; import net.minecraft.network.FriendlyByteBuf;
 * import net.minecraft.util.BlockInWorld; import
 * net.minecraft.util.InteractionHand; import net.minecraft.util.SoundSource;
 * import net.minecraft.util.SoundEvents; import
 * net.minecraft.util.math.BlockPos; import
 * net.minecraft.util.math.BlockHitResult; import
 * net.minecraft.util.math.HitResult; import
 * net.minecraft.util.text.TextComponent; import
 * net.minecraft.world.server.ServerLevel; import
 * net.minecraftforge.fml.network.NetworkEvent; import
 * net.minecraftforge.fml.network.PacketDistributor;
 * 
 * public class PacketBloodCraftingKeyPressOld {
 * 
 * public ItemStack heldStack;
 * 
 * public PacketBloodCraftingKeyPressOld(ItemStack stack) { this.heldStack =
 * stack; }
 * 
 * public static PacketBloodCraftingKeyPressOld decode(final FriendlyByteBuf
 * buffer) { buffer.readByte(); return new
 * PacketBloodCraftingKeyPressOld(buffer.readItemStack()); }
 * 
 * public static void encode(final PacketBloodCraftingKeyPressOld message, final
 * FriendlyByteBuf buffer) { buffer.writeByte(0);
 * buffer.writeItemStack(message.heldStack); }
 * 
 * public static BaseBloodCraftingRecipe getMatchingRecipe(ItemStack stack) {
 * for (BaseBloodCraftingRecipe recipe : ModBloodCraftingRecipes.RECIPES) { if
 * (recipe.getHeldItem() == stack.getItem()) { return recipe; } } return null; }
 * 
 * public static void handle(final PacketBloodCraftingKeyPressOld message, final
 * Supplier<NetworkEvent.Context> ctx) { ctx.get().enqueueWork(() -> { Player
 * player = ctx.get().getSender(); if (player == null) return; IBloodVolume
 * bloodVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
 * .orElseThrow(NullPointerException::new);
 * 
 * BaseBloodCraftingRecipe targetPattern = getMatchingRecipe(message.heldStack);
 * ServerLevel sLevel = (ServerLevel) ctx.get().getSender().world; if
 * (player.getHeldItemMainhand().getItem() == targetPattern.getHeldItem()) { if
 * (bloodVolume.getBloodVolume() > targetPattern.getCost()) { HitResult rayTrace
 * = player.pick(3, 102, false); if (rayTrace.getType() == HitResult.Type.BLOCK)
 * { BlockHitResult blockResult = (BlockHitResult) rayTrace; BlockPos hitPos =
 * blockResult.getPos(); Block hitBlock =
 * sLevel.getBlockState(hitPos).getBlock(); if (hitBlock ==
 * targetPattern.getHitBlock()) { BlockPattern.PatternHelper patternHelper =
 * targetPattern.getBundledPattern() .getBlockPattern().match(sLevel, hitPos);
 * if (patternHelper != null) { for (int i = 0; i <
 * targetPattern.getBundledPattern().getBlockPattern() .getPalmLength(); ++i) {
 * for (int j = 0; j < targetPattern.getBundledPattern().getBlockPattern()
 * .getThumbLength(); ++j) { for (int k = 0; k <
 * targetPattern.getBundledPattern().getBlockPattern() .getFingerLength(); ++k)
 * { BlockInWorld cachedBlockInfo = patternHelper.translateOffset(i, j, k);
 * sLevel.setBlockState(cachedBlockInfo.getPos(), Blocks.AIR.getDefaultState(),
 * 2); sLevel.playEvent(2001, cachedBlockInfo.getPos(),
 * Block.getStateId(cachedBlockInfo.getBlockState())); } } }
 * sLevel.playSound(hitPos.getX(), hitPos.getY(), hitPos.getZ(),
 * SoundEvents.ENTITY_ENDERMAN_SCREAM, SoundSource.BLOCKS, 1, 1, true);
 * sLevel.addEntity(new ItemEntity(sLevel, hitPos.getX(), hitPos.getY(),
 * hitPos.getZ(), new ItemStack(targetPattern.getCreation()))); ItemStack
 * oldStack = player.getHeldItemMainhand().copy();
 * player.setHeldItem(InteractionHand.MAIN_HAND, new
 * ItemStack(oldStack.getItem(), oldStack.getCount() - 1));
 * bloodVolume.subtractBloodVolume(targetPattern.getCost());
 * PacketHandler.CHANNELBLOODVOLUME.send( PacketDistributor.PLAYER.with(() ->
 * (ServerPlayer) player), new
 * BloodVolumePacketServer(bloodVolume.getBloodVolume())); } } } } else {
 * player.sendStatusMessage(new
 * TextComponent("Not enough blood can be drawn for formation"), true);
 * sLevel.playSound(player.getPosition().getX(), player.getPosition().getY(),
 * player.getPosition().getZ(), SoundEvents.ENTITY_ENDERMAN_SCREAM, null, 1f,
 * 1f, false); } } }); ctx.get().setPacketHandled(true); } }
 */