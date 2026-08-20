package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.block.harbinger.EngramBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Scratch-Engraving mechanic: a player can carve a crude engram block into the
 * ground without an Engram Stamp by pressing a sharp shard against a solid
 * surface.  This is intended as an early-game or emergency option — but the
 * physical cost is real.
 *
 * <h3>Valid etching tools</h3>
 * <ul>
 *   <li>{@code hemomancy:vivianite_cluster} — vivianite is already attuned to
 *       blood-memory; the most reliable option.</li>
 *   <li>{@code minecraft:flint} — flint is sharp enough but crude; leaves the
 *       shard nicked.</li>
 *   <li>{@code minecraft:quartz} — nether quartz holds a fine edge.</li>
 *   <li>{@code hutoslib:obsidian_flakes} — near-volcanic sharpness; works
 *       perfectly but the flake may shatter.</li>
 * </ul>
 *
 * <h3>Cost</h3>
 * One heart (2 HP) per engram block carved.  Creative-mode players are exempt
 * from the damage but the engram is still placed.
 *
 * <h3>Conditions</h3>
 * <ul>
	*   <li>The player must crouch to distinguish engraving from block use.</li>
 *   <li>The clicked block must have a sturdy top face.</li>
 *   <li>The space one block above must be empty.</li>
 *   <li>The player must use their main hand (prevents offhand ghost-clicks).</li>
 * </ul>
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class ScratchEngramHandler {

    /** Damage dealt to the player per engram etched — one heart. */
    private static final float ETCH_DAMAGE = 2.0f;

    private static final ResourceLocation VIVIANITE_CLUSTER =
            ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "vivianite_cluster");
    private static final ResourceLocation OBSIDIAN_FLAKES =
            ResourceLocation.fromNamespaceAndPath("hutoslib", "obsidian_flakes");

    /** Returns true if the held item qualifies as a scratch-engraving tool. */
    private static boolean isEtchingTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item == Items.FLINT || item == Items.QUARTZ) return true;
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return VIVIANITE_CLUSTER.equals(key) || OBSIDIAN_FLAKES.equals(key);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Only trigger from the main hand to avoid duplicate calls.
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
		if (!player.isShiftKeyDown()) return;

        ItemStack held = event.getItemStack();
        if (!isEtchingTool(held)) return;

        BlockPos pos = event.getPos();
        BlockState clickedState = level.getBlockState(pos);
        Direction clickedFace = event.getFace();
        if (clickedFace == Direction.DOWN) return;

        Direction engramFacing = clickedFace.getAxis().isHorizontal() ? clickedFace : Direction.UP;
        BlockPos targetPos = clickedFace == Direction.UP ? pos.above() : pos.relative(clickedFace);

        // Require a sturdy clicked face and an empty target space for the engram.
        if (!clickedState.isFaceSturdy(level, pos, clickedFace)) return;
        if (!level.isEmptyBlock(targetPos)) return;

        // Place the engram block.
        BlockState engramState = BlockInit.engram_block.get().defaultBlockState()
                .setValue(EngramBlock.CHARACTERINDEX, EngramBlock.randomCharacterIndex())
                .setValue(EngramBlock.FACING, engramFacing)
                .setValue(EngramBlock.LIT, false);
        level.setBlockAndUpdate(targetPos, engramState);

        // Cost: one heart.  Creative players are exempt from the damage.
        if (!player.isCreative()) {
            player.hurt(player.damageSources().generic(), ETCH_DAMAGE);
        }

        // Feedback: scratch/etching sound + blood particle puff.
        level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.6f, 1.8f);
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.3f, 1.4f);
        if (level instanceof ServerLevel serverLevel) {
            com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils.spawnPoof(
                    serverLevel, targetPos,
                    BloodCellParticleFactory.createData(ParticleColor.BLOOD));
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
