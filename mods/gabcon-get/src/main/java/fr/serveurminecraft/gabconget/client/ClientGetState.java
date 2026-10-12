package fr.serveurminecraft.gabconget.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import fr.serveurminecraft.gabconget.GetNetwork;

/** Interpolation de pose : 12 ticks à l'aller, 12 au retour. */
public final class ClientGetState {
    private static final Map<UUID, Transition> ANIMATIONS = new HashMap<>();
    private static final float STEP = 1.0F / 12.0F;

    private ClientGetState() {}

    private static final class Transition {
        float previous;
        float current;
        boolean target;
        Transition(boolean target) { this.target = target; }
    }

    public static void receive(GetNetwork.GetState packet) {
        ANIMATIONS.computeIfAbsent(packet.playerId(), id -> new Transition(false))
            .target = packet.active();
    }

    public static void clientTick() {
        if (Minecraft.getInstance().level == null) {
            ANIMATIONS.clear();
            return;
        }
        ANIMATIONS.values().forEach(animation -> {
            animation.previous = animation.current;
            animation.current = Mth.clamp(animation.current + (animation.target ? STEP : -STEP), 0, 1);
        });
        ANIMATIONS.entrySet().removeIf(entry ->
            !entry.getValue().target && entry.getValue().current == 0);
    }

    public static float progress(AbstractClientPlayer player, float partialTick) {
        Transition transition = ANIMATIONS.get(player.getUUID());
        return transition == null ? 0 : Mth.lerp(Mth.clamp(partialTick, 0, 1),
            transition.previous, transition.current);
    }

    public static void clear() { ANIMATIONS.clear(); }
}
