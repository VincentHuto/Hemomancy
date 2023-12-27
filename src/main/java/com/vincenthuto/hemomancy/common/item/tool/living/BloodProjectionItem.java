package com.vincenthuto.hemomancy.common.item.tool.living;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.event.ClientEvents.ClientModBusEvents;
import com.vincenthuto.hemomancy.client.render.item.CellHandItemRenderer;
import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;
import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.PacketDistributor;

public class BloodProjectionItem extends Item implements IDispellable, ICellHand {

	public BloodProjectionItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public BakedModel getBakedModel() {
		return ClientModBusEvents.bloodProjectionModel;
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			final BlockEntityWithoutLevelRenderer myRenderer = new CellHandItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return myRenderer;
			}
		});
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public void onUseTick(Level worldIn, LivingEntity player, ItemStack stack, int count) {
		IBloodVolume playerVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);

		HitResult trace = player.pick(5.5, HLClientUtils.getPartialTicks(), true);
		if (trace.getType() == Type.BLOCK) {
			BlockEntity be = worldIn.getBlockEntity(new BlockPos(new Vec3i((int) trace.getLocation().x - 1,
					(int) trace.getLocation().y, (int) trace.getLocation().z - 1)));
			if (be != null) {
				if (be.getCapability(BloodVolumeProvider.VOLUME_CAPA).isPresent()) {

					IBloodVolume tileVolume = be.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(IllegalStateException::new);
					tileVolume.fillFromSource(playerVolume, 100f);
					if(be instanceof IBloodTile bt ) {
						bt.sendUpdates();
					}
				}
			}

		}
		if (!worldIn.isClientSide) {

			PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
					new BloodVolumeServerPacket(playerVolume));
		}
	}

	@SuppressWarnings("unused")
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		IKnownManipulations known = playerIn.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);
		IBloodTendency tendency = playerIn.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
				.orElseThrow(NullPointerException::new);
		IBloodVolume volume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		if (volume.isActive()) {
			if (volume.getBloodVolume() < volume.getMaxBloodVolume()) {
				playerIn.startUsingItem(handIn);
				new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
			}
		} else {
			playerIn.displayClientMessage(
					Component.literal("You lack the skill to manifest this power!").withStyle(ChatFormatting.RED),
					true);
		}

		return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
	}

}
