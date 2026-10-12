package fr.serveurminecraft.gabconget;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(GabConGet.MOD_ID)
public final class GabConGet {
    public static final String MOD_ID = "gabconget";
    public GabConGet(IEventBus modBus) {
        modBus.addListener(GetNetwork::register);
        NeoForge.EVENT_BUS.addListener(GetCommands::register);
        NeoForge.EVENT_BUS.addListener(GetServerState::onStartTracking);
        NeoForge.EVENT_BUS.addListener(GetServerState::onPlayerKilled);
        NeoForge.EVENT_BUS.addListener(GetServerState::onLogin);
        NeoForge.EVENT_BUS.addListener(GetServerState::onLogout);
    }
}
