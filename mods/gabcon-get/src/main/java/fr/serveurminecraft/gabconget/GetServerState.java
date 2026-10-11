package fr.serveurminecraft.gabconget;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Autorité serveur. L'emote est volontairement éphémère (non sauvegardée). */
public final class GetServerState {
    private static final Set<UUID> ACTIVE = new HashSet<>();

    private GetServerState() {}

    public static boolean active(ServerPlayer player) {
        return ACTIVE.contains(player.getUUID());
    }

    public static void toggle(ServerPlayer player) {
        set(player, !active(player));
    }

    public static void set(ServerPlayer player, boolean requested) {
        if (requested && (player.isSpectator() || !player.isAlive() || player.isPassenger()
                || player.isFallFlying() || player.isSwimming())) {
            player.displayClientMessage(Component.literal(
                "GET impossible en mode spectateur, en vol, en nageant ou sur une monture.")
                .withStyle(ChatFormatting.RED), true);
            return;
        }

        if (active(player) == requested) return;
        if (requested) ACTIVE.add(player.getUUID());
        else ACTIVE.remove(player.getUUID());

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
            player, new GetNetwork.GetState(player.getUUID(), requested));
        player.displayClientMessage(Component.literal(
            requested ? "GET : gueule en terre !" : "GET : position normale.")
            .withStyle(requested ? ChatFormatting.GREEN : ChatFormatting.YELLOW), true);
    }

    /** Un joueur rejoignant la distance de rendu reçoit la pose des joueurs suivis. */
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer observer
                && event.getTarget() instanceof ServerPlayer target
                && ACTIVE.contains(target.getUUID())) {
            PacketDistributor.sendToPlayer(observer,
                new GetNetwork.GetState(target.getUUID(), true));
        }
    }

    /** Pas de joueur fantôme, ni de persistance surprise après reconnexion. */
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (ACTIVE.remove(player.getUUID())) {
                PacketDistributor.sendToPlayersTrackingEntity(player,
                    new GetNetwork.GetState(player.getUUID(), false));
            }
        }
    }
}
