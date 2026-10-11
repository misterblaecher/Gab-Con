package fr.serveurminecraft.gabconget.client;

import java.util.ArrayDeque;
import java.util.Queue;
import fr.serveurminecraft.gabconget.GetNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;

/** Affiche la prochaine notification après fermeture de l'écran courant/de mort. */
public final class ClientNotices {
    private static final Queue<GetNetwork.Notice> PENDING = new ArrayDeque<>();
    private ClientNotices() {}
    public static void enqueue(GetNetwork.Notice notice) {
        if (PENDING.size() < 12) PENDING.add(notice);
    }
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.level == null || mc.screen != null || PENDING.isEmpty()) return;
        mc.setScreen(new GetNoticeScreen(PENDING.remove()));
    }
    public static void clear() { PENDING.clear(); }
}
