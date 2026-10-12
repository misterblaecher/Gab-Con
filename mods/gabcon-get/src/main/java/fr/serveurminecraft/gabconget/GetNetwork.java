package fr.serveurminecraft.gabconget;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class GetNetwork {
    private GetNetwork() {}
    public static final int NOTICE_GRANTED = 0;
    public static final int NOTICE_CANCELLED = 1;
    public static final int NOTICE_IMPOSED = 2;
    public static final int NOTICE_RELEASED = 3;

    public record ToggleRequest() implements CustomPacketPayload {
        public static final ToggleRequest INSTANCE = new ToggleRequest();
        public static final Type<ToggleRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "toggle"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ToggleRequest> CODEC =
            StreamCodec.of((buf, payload) -> {}, buf -> INSTANCE);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record GetState(UUID playerId, boolean active) implements CustomPacketPayload {
        public static final Type<GetState> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, GetState> CODEC = StreamCodec.of(
            (buf, p) -> { buf.writeUUID(p.playerId()); buf.writeBoolean(p.active()); },
            buf -> new GetState(buf.readUUID(), buf.readBoolean()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record RightsQuery() implements CustomPacketPayload {
        public static final RightsQuery INSTANCE = new RightsQuery();
        public static final Type<RightsQuery> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "query_rights"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RightsQuery> CODEC = StreamCodec.of((b,p)->{}, b->INSTANCE);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ImposeRequest(UUID target, boolean enabled) implements CustomPacketPayload {
        public static final Type<ImposeRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "impose"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ImposeRequest> CODEC = StreamCodec.of(
            (b,p) -> { b.writeUUID(p.target()); b.writeBoolean(p.enabled()); },
            b -> new ImposeRequest(b.readUUID(), b.readBoolean()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record RightEntry(UUID id, String name, boolean imposed) {}
    public record RightsSnapshot(List<RightEntry> entries) implements CustomPacketPayload {
        public static final Type<RightsSnapshot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "rights"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RightsSnapshot> CODEC = StreamCodec.of(
            (b,p) -> { b.writeVarInt(p.entries().size()); for (RightEntry e:p.entries()) {
                b.writeUUID(e.id()); b.writeUtf(e.name(), 48); b.writeBoolean(e.imposed());
            } },
            b -> { int n=b.readVarInt(); if(n<0 || n>1024) throw new IllegalArgumentException("Too many GET rights");
                List<RightEntry> list = new ArrayList<>();
                for(int i=0;i<n;i++) list.add(new RightEntry(b.readUUID(),b.readUtf(48),b.readBoolean()));
                return new RightsSnapshot(list); });
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record Notice(int kind, String other) implements CustomPacketPayload {
        public static final Type<Notice> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "notice"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Notice> CODEC = StreamCodec.of(
            (b,p) -> { b.writeVarInt(p.kind()); b.writeUtf(p.other(), 48); },
            b -> new Notice(b.readVarInt(),b.readUtf(48)));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("2");
        r.playToServer(ToggleRequest.TYPE, ToggleRequest.CODEC, (p,c)-> {
            if (c.player() instanceof ServerPlayer sp) GetServerState.toggle(sp); });
        r.playToServer(RightsQuery.TYPE, RightsQuery.CODEC, (p,c)-> {
            if (c.player() instanceof ServerPlayer sp) GetServerState.syncRights(sp); });
        r.playToServer(ImposeRequest.TYPE, ImposeRequest.CODEC, (p,c)-> {
            if (c.player() instanceof ServerPlayer sp) {
                ServerPlayer target = sp.server.getPlayerList().getPlayer(p.target());
                if (target != null) GetServerState.impose(sp,target,p.enabled());
            }
        });
        r.playToClient(GetState.TYPE, GetState.CODEC, (p,c)->
            fr.serveurminecraft.gabconget.client.ClientGetState.receive(p));
        r.playToClient(RightsSnapshot.TYPE, RightsSnapshot.CODEC, (p,c)->
            fr.serveurminecraft.gabconget.client.ClientRights.receive(p));
        r.playToClient(Notice.TYPE, Notice.CODEC, (p,c)->
            fr.serveurminecraft.gabconget.client.ClientNotices.enqueue(p));
    }
}
