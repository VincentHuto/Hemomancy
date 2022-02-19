package com.vincenthuto.hemomancy.item.tool.living;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.ClientProxy;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.player.tendency.BloodTendencyProvider;
import com.vincenthuto.hemomancy.capa.player.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.vincenthuto.hemomancy.render.item.RenderItemCellHand;
import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.network.PacketDistributor;

public class ItemBloodProjection extends Item implements IDispellable, ICellHand {

	public ItemBloodProjection(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(new IItemRenderProperties() {
			final BlockEntityWithoutLevelRenderer myRenderer = new RenderItemCellHand(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
				return myRenderer;
			}
		});
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
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
					new TextComponent("You lack the skill to manifest this power!").withStyle(ChatFormatting.RED),
					true);
		}

		return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
	}

	@Override
	public void onUseTick(Level worldIn, LivingEntity player, ItemStack stack, int count) {
		IBloodVolume playerVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);

		HitResult trace = player.pick(5.5, HLClientUtils.getPartialTicks(), true);
		if (trace.getType() == Type.BLOCK) {
			if (worldIn.getBlockEntity(new BlockPos(trace.getLocation())) != null) {
				if (worldIn.getBlockEntity(new BlockPos(trace.getLocation()))
						.getCapability(BloodVolumeProvider.VOLUME_CAPA).isPresent()) {
					System.out.println("hit fillable tile");
					IBloodVolume tileVolume = worldIn.getBlockEntity(new BlockPos(trace.getLocation()))
							.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);
					tileVolume.fillFromSource(playerVolume, 100f);

				}
			}
			if (!worldIn.isClientSide) {

				PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
						new PacketBloodVolumeServer(playerVolume));
			}
		}

	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public BakedModel getBakedModel() {
		return ClientProxy.bloodProjectionModel;
	}
}
