package com.vincenthuto.hemomancy.common.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * The client halves of client-bound payloads. {@code PacketHandler} registers {@link #dispatch} for a payload whose
 * handling needs client classes, and the client fills this table at setup from {@code client/network}. A dedicated
 * server never fills it and never receives those payloads, so common code holds no reference to a client class.
 */
public final class ClientPayloadHandlers {
	private static final Map<CustomPacketPayload.Type<?>, IPayloadHandler<?>> HANDLERS = new ConcurrentHashMap<>();

	private ClientPayloadHandlers() {
	}

	public static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type,
			IPayloadHandler<T> handler) {
		if (HANDLERS.putIfAbsent(type, handler) != null) {
			throw new IllegalStateException("Client payload handler registered twice: " + type.id());
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends CustomPacketPayload> void dispatch(T payload, IPayloadContext context) {
		IPayloadHandler<T> handler = (IPayloadHandler<T>) HANDLERS.get(payload.type());
		if (handler == null) {
			Hemomancy.LOGGER.warn("No client handler for payload {}", payload.type().id());
			return;
		}
		handler.handle(payload, context);
	}
}
