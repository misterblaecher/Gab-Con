package fr.serveurminecraft.gabconget;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Le client demande un toggle ; seul le serveur décide et diffuse l'état. */
public final class GetNetwork {
    private GetNetwork() {}

    public record ToggleRequest() implements CustomPacketPayload {
        public static final ToggleRequest INSTANCE = new ToggleRequest();
        public static final Type<ToggleRequest> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "toggle"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ToggleRequest> CODEC =
            StreamCodec.of((buf, payload) -> {}, buf -> INSTANCE);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record GetState(UUID playerId, boolean active) implements CustomPacketPayload {
        public static final Type<GetState> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, GetState> CODEC =
            StreamCodec.of(
                (buf, payload) -> {
                    buf.writeUUID(payload.playerId());
                    buf.writeBoolean(payload.active());
                },
                buf -> new GetState(buf.readUUID(), buf.readBoolean()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ToggleRequest.TYPE, ToggleRequest.CODEC, GetNetwork::onToggle);
        registrar.playToClient(GetState.TYPE, GetState.CODEC, GetNetwork::onState);
    }

    private static void onToggle(ToggleRequest packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) GetServerState.toggle(player);
    }

    /** Chargé seulement quand un paquet côté client est effectivement traité. */
    private static void onState(GetState packet, IPayloadContext context) {
        fr.serveurminecraft.gabconget.client.ClientGetState.receive(packet);
    }
}
