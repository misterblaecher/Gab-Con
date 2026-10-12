package fr.serveurminecraft.gabconget.client;

import java.util.List;
import fr.serveurminecraft.gabconget.GetNetwork;
import net.minecraft.client.Minecraft;

public final class ClientRights {
    private static List<GetNetwork.RightEntry> entries = List.of();
    private ClientRights() {}
    public static void receive(GetNetwork.RightsSnapshot p) {
        entries = List.copyOf(p.entries());
        if (Minecraft.getInstance().screen instanceof GetRightsScreen screen) screen.refresh();
    }
    public static List<GetNetwork.RightEntry> entries() { return entries; }
    public static void clear() { entries=List.of(); }
}
