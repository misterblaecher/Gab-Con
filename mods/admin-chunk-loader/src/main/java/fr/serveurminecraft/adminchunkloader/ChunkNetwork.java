package fr.serveurminecraft.adminchunkloader;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Communication entre l'UI FTB client et la sauvegarde forceload vanilla du serveur. */
public final class ChunkNetwork {
    private static final int MAX_FORCED = 256;
    private static final int MAX_SNAPSHOT = 16_384;

    private ChunkNetwork() {}

    public record Query() implements CustomPacketPayload {
        public static final Query INSTANCE = new Query();
        public static final Type<Query> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AdminChunkLoader.MOD_ID, "query"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Query> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> INSTANCE);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record Toggle(int x, int z) implements CustomPacketPayload {
        public static final Type<Toggle> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AdminChunkLoader.MOD_ID, "toggle"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Toggle> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Toggle::x,
            ByteBufCodecs.VAR_INT, Toggle::z,
            Toggle::new
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record Snapshot(String dimension, long[] forced, boolean allowed) implements CustomPacketPayload {
        public static final Type<Snapshot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AdminChunkLoader.MOD_ID, "snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUtf(pkt.dimension());
                buf.writeBoolean(pkt.allowed());
                buf.writeVarInt(pkt.forced().length);
                for (long pos : pkt.forced()) buf.writeLong(pos);
            },
            buf -> {
                String dim = buf.readUtf(128);
                boolean allowed = buf.readBoolean();
                int n = buf.readVarInt();
                if (n < 0 || n > MAX_SNAPSHOT) throw new IllegalArgumentException("Invalid chunk snapshot");
                long[] chunks = new long[n];
                for (int i = 0; i < n; i++) chunks[i] = buf.readLong();
                return new Snapshot(dim, chunks, allowed);
            }
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(Query.TYPE, Query.CODEC, ChunkNetwork::handleQuery);
        registrar.playToServer(Toggle.TYPE, Toggle.CODEC, ChunkNetwork::handleToggle);
        registrar.playToClient(Snapshot.TYPE, Snapshot.CODEC, ChunkNetwork::handleSnapshot);
    }

    private static void handleQuery(Query query, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) sendSnapshot(player);
    }

    private static void handleToggle(Toggle payload, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        if (!player.hasPermissions(2)) return;
        int x = payload.x(), z = payload.z();
        // La grille de FTB Chunks est centrée sur le joueur (15 x 15).
        ChunkPos playerPos = player.chunkPosition();
        if (Math.abs((long) x - playerPos.x) > 7 || Math.abs((long) z - playerPos.z) > 7) return;
        if (Math.abs((long) x) > 1_874_999 || Math.abs((long) z) > 1_874_999) return;

        ServerLevel level = player.serverLevel();
        boolean alreadyForced = level.getForcedChunks().contains(ChunkPos.asLong(x, z));
        if (!alreadyForced && level.getForcedChunks().size() >= MAX_FORCED) {
            player.displayClientMessage(Component.literal(
                "GabCon : limite de " + MAX_FORCED + " chunks forcés atteinte."
            ).withStyle(ChatFormatting.RED), true);
            sendSnapshot(player);
            return;
        }
        if (level.setChunkForced(x, z, !alreadyForced)) {
            player.displayClientMessage(Component.literal(
                "GabCon : chunk [" + x + ", " + z + "] " +
                (alreadyForced ? "libéré" : "force-load activé")
            ).withStyle(alreadyForced ? ChatFormatting.YELLOW : ChatFormatting.GREEN), true);
        }
        sendSnapshot(player);
    }

    private static void sendSnapshot(ServerPlayer player) {
        boolean allowed = player.hasPermissions(2);
        ServerLevel level = player.serverLevel();
        // Limite la taille du paquet si d'autres commandes ont forcé énormément de chunks.
        long[] forced = allowed ? level.getForcedChunks().longStream().limit(MAX_SNAPSHOT).toArray() : new long[0];
        PacketDistributor.sendToPlayer(player, new Snapshot(level.dimension().location().toString(), forced, allowed));
    }

    private static void handleSnapshot(Snapshot packet, IPayloadContext ctx) {
        // Cette méthode n'est invoquée que côté client pour les paquets S2C.
        fr.serveurminecraft.adminchunkloader.client.ClientChunkState.accept(packet);
    }
}
