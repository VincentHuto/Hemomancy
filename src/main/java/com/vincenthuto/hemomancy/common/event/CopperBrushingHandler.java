package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Copper Brushing mechanic: right-clicking an unwaxed oxidized copper block
 * with a vanilla brush strips one oxidation step and drops one Virid Salis.
 *
 * <h3>Oxidation ladder (downward)</h3>
 * <ul>
 *   <li>oxidized_* -&gt; weathered_*</li>
 *   <li>weathered_* -&gt; exposed_*</li>
 *   <li>exposed_* -&gt; base copper_* / cut_copper_*</li>
 * </ul>
 *
 * Block-state properties (facing, half, shape, type, waterlogged) are
 * preserved from the old state where the target block supports them.
 * Waxed copper is not affected.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CopperBrushingHandler {

    /** Maps each scrape-eligible block to the block it degrades into. */
    private static final Map<Block, Block> DOWNGRADE = createDowngradeMap();

    private static Map<Block, Block> createDowngradeMap() {
        Map<Block, Block> map = new HashMap<>();
        // Plain copper
        map.put(Blocks.OXIDIZED_COPPER,               Blocks.WEATHERED_COPPER);
        map.put(Blocks.WEATHERED_COPPER,              Blocks.EXPOSED_COPPER);
        map.put(Blocks.EXPOSED_COPPER,                Blocks.COPPER_BLOCK);
        // Cut copper
        map.put(Blocks.OXIDIZED_CUT_COPPER,           Blocks.WEATHERED_CUT_COPPER);
        map.put(Blocks.WEATHERED_CUT_COPPER,          Blocks.EXPOSED_CUT_COPPER);
        map.put(Blocks.EXPOSED_CUT_COPPER,            Blocks.CUT_COPPER);
        // Cut copper stairs
        map.put(Blocks.OXIDIZED_CUT_COPPER_STAIRS,    Blocks.WEATHERED_CUT_COPPER_STAIRS);
        map.put(Blocks.WEATHERED_CUT_COPPER_STAIRS,   Blocks.EXPOSED_CUT_COPPER_STAIRS);
        map.put(Blocks.EXPOSED_CUT_COPPER_STAIRS,     Blocks.CUT_COPPER_STAIRS);
        // Cut copper slabs
        map.put(Blocks.OXIDIZED_CUT_COPPER_SLAB,      Blocks.WEATHERED_CUT_COPPER_SLAB);
        map.put(Blocks.WEATHERED_CUT_COPPER_SLAB,     Blocks.EXPOSED_CUT_COPPER_SLAB);
        map.put(Blocks.EXPOSED_CUT_COPPER_SLAB,       Blocks.CUT_COPPER_SLAB);
        return map;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        ItemStack held = event.getItemStack();
        if (held.getItem() != Items.BRUSH) return;

        BlockPos pos = event.getPos();
        BlockState oldState = level.getBlockState(pos);
        Block targetBlock = DOWNGRADE.get(oldState.getBlock());
        if (targetBlock == null) return;

        // Build the new state, copying matching block-state properties
        BlockState newState = copyMatchingProperties(oldState, targetBlock.defaultBlockState());
        level.setBlock(pos, newState, Block.UPDATE_ALL);

        // Drop one Virid Salis at the block centre
        Block.popResource(level, pos, new ItemStack(BlockInit.virid_salis_trail.get()));

        // Spend one durability for survival players
        if (!serverPlayer.isCreative()) {
            InteractionHand hand = event.getHand();
            EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND
                    : EquipmentSlot.OFFHAND;
            held.hurtAndBreak(1, serverPlayer, slot);
        }

        // Feedback
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SCRAPE,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    8, 0.3, 0.3, 0.3, 0.02);
        }
        level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    /**
     * Returns {@code target} with every property that it shares with {@code source}
     * set to the value from {@code source}.
     */
    private static BlockState copyMatchingProperties(BlockState source, BlockState target) {
        for (Property<?> prop : source.getBlock().getStateDefinition().getProperties()) {
            if (target.hasProperty(prop)) {
                target = copyProperty(source, target, prop);
            }
        }
        return target;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyProperty(
            BlockState source, BlockState target, Property<T> prop) {
        return target.setValue(prop, source.getValue(prop));
    }
}
