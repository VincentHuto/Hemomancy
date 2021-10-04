package com.vincenthuto.hemomancy.item.tool.living;

import java.util.List;
import java.util.function.Consumer;

import com.vincenthuto.hemomancy.ClientProxy;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.player.tendency.BloodTendencyProvider;
import com.vincenthuto.hemomancy.capa.player.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.vincenthuto.hemomancy.render.item.RenderItemCellHand;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class ItemBloodAbsorption extends Item implements IDispellable, ICellHand {

	public ItemBloodAbsorption(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
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
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		List<Entity> targets = player.level.getEntities(player, player.getBoundingBox().inflate(5.0));
		if (!targets.isEmpty()) {
			targets.forEach((t) -> {
				if (t instanceof LivingEntity) {
					LivingEntity livingTarget = (LivingEntity) t;
					float dam = 3f / targets.size();
					livingTarget.hurt(ItemInit.bloodLoss, dam);
					if (!worldIn.isClientSide) {
						volume.addBloodVolume(dam);
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
								new PacketBloodVolumeServer(volume));
					}
				}
			});

		}
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public BakedModel getBakedModel() {
		// TODO Auto-generated method stub
		return ClientProxy.bloodAbsorptionModel;
	}

}
