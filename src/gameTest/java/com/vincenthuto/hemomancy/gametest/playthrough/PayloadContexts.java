package com.vincenthuto.hemomancy.gametest.playthrough;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A serverbound payload context for a test player, so a payload's own {@code handle} runs exactly as the network
 * would run it: work is done at once on the server thread, replies go through the player's connection.
 */
public final class PayloadContexts {
	private PayloadContexts() {
	}

	/** The context a serverbound payload from {@code player} is handled in. */
	public static IPayloadContext of(ServerPlayer player) {
		return (IPayloadContext) Proxy.newProxyInstance(PayloadContexts.class.getClassLoader(),
				new Class<?>[]{IPayloadContext.class}, (proxy, method, args) -> switch (method.getName()) {
					case "player" -> player;
					case "listener" -> player.connection;
					case "flow" -> PacketFlow.SERVERBOUND;
					case "enqueueWork" -> {
						if (args[0] instanceof Runnable task) {
							task.run();
							yield CompletableFuture.completedFuture(null);
						}
						yield CompletableFuture.completedFuture(((Supplier<?>) args[0]).get());
					}
					case "toString" -> "PayloadContexts[" + player.getGameProfile().getName() + "]";
					case "hashCode" -> System.identityHashCode(proxy);
					case "equals" -> proxy == args[0];
					default -> {
						if (method.isDefault()) yield InvocationHandler.invokeDefault(proxy, method, args);
						throw new UnsupportedOperationException("PayloadContexts does not provide " + method.getName());
					}
				});
	}
}
