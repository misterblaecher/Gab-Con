package fr.serveurminecraft.adminchunkloader.client;

import fr.serveurminecraft.adminchunkloader.ChunkNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** État UI purement client ; la vérité et les permissions restent toujours sur le serveur. */
public final class ClientChunkState {
    private static boolean mode;
    private static boolean known;
    private static String dimension = "";
    private static Set<Long> forced = Set.of();
    private ClientChunkState() {}

    public static boolean mode() { return mode; }

    public static void toggleMode() {
        mode = !mode;
        if (mode) request();
    }

    public static void request() {
        known = false;
        forced = Set.of();
        PacketDistributor.sendToServer(ChunkNetwork.Query.INSTANCE);
    }

    public static void toggle(int x, int z) {
        if (!mode) return;
        PacketDistributor.sendToServer(new ChunkNetwork.Toggle(x, z));
    }

    public static void accept(ChunkNetwork.Snapshot snapshot) {
        if (!mode) return;
        if (!snapshot.allowed()) {
            clear();
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null ||
            !snapshot.dimension().equals(minecraft.level.dimension().location().toString())) return;
        dimension = snapshot.dimension();
        forced = new HashSet<>();
        for (long pos : snapshot.forced()) forced.add(pos);
        known = true;
    }

    public static boolean known() {
        Minecraft minecraft = Minecraft.getInstance();
        return mode && known && minecraft.level != null &&
            dimension.equals(minecraft.level.dimension().location().toString());
    }

    public static boolean forced(int x, int z) {
        return known() && forced.contains(ChunkPos.asLong(x, z));
    }

    public static void clear() {
        mode = false;
        known = false;
        dimension = "";
        forced = Set.of();
    }
}
