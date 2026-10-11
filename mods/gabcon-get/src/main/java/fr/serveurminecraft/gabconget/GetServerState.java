package fr.serveurminecraft.gabconget;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Le serveur seul attribue les droits et valide les GET imposées. */
public final class GetServerState {
    private static final Set<UUID> ACTIVE = new HashSet<>();
    private static final Map<UUID, UUID> FORCED_BY = new HashMap<>();
    private GetServerState() {}

    public static boolean active(ServerPlayer p) { return ACTIVE.contains(p.getUUID()); }

    public static void toggle(ServerPlayer p) { set(p, !active(p), false); }

    public static void set(ServerPlayer p, boolean requested) { set(p, requested, false); }

    public static void setAdmin(ServerPlayer p, boolean requested) {
        FORCED_BY.remove(p.getUUID());
        set(p, requested, true);
    }

    private static void set(ServerPlayer player, boolean requested, boolean admin) {
        if (!admin && !requested && FORCED_BY.containsKey(player.getUUID())) {
            player.displayClientMessage(Component.literal("GET imposée : seul le détenteur du droit peut te relever, ou prends ta revanche.")
                .withStyle(ChatFormatting.RED), true);
            return;
        }
        if (requested && (player.isSpectator() || !player.isAlive() || player.isPassenger()
                || player.isFallFlying() || player.isSwimming())) {
            player.displayClientMessage(Component.literal("GET impossible pendant cet état du joueur.")
                .withStyle(ChatFormatting.RED), true);
            return;
        }
        if (active(player) == requested) return;
        if (requested) ACTIVE.add(player.getUUID());
        else ACTIVE.remove(player.getUUID());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
            new GetNetwork.GetState(player.getUUID(), requested));
    }

    /** Utilisable via une commande ou depuis l'interface, avec revalidation serveur. */
    public static int impose(ServerPlayer actor, ServerPlayer target, boolean requested) {
        if (actor.getUUID().equals(target.getUUID()) || !GetRightsData.of(actor.server).has(actor.getUUID(), target.getUUID())) {
            actor.displayClientMessage(Component.literal("Tu n'as pas le droit de GET sur ce joueur.").withStyle(ChatFormatting.RED), true);
            return 0;
        }
        UUID current = FORCED_BY.get(target.getUUID());
        if (requested) {
            if (current != null && !current.equals(actor.getUUID())) {
                actor.displayClientMessage(Component.literal("Ce joueur est déjà en GET imposée par quelqu'un d'autre.").withStyle(ChatFormatting.RED), true);
                return 0;
            }
            if (target.isSpectator() || !target.isAlive() || target.isPassenger() || target.isFallFlying() || target.isSwimming()) return 0;
            FORCED_BY.put(target.getUUID(), actor.getUUID());
            set(target, true, true);
            notice(target, GetNetwork.NOTICE_IMPOSED, actor.getGameProfile().getName());
        } else {
            if (!actor.getUUID().equals(current)) return 0;
            FORCED_BY.remove(target.getUUID());
            set(target, false, true);
        }
        actor.displayClientMessage(Component.literal((requested ? "GET imposée à " : "GET levée pour ") + target.getGameProfile().getName())
            .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        syncRights(actor);
        return 1;
    }

    public static void onPlayerKilled(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        UUID priorHolder = FORCED_BY.remove(victim.getUUID());
        if (priorHolder != null) {
            ServerPlayer holder = victim.server.getPlayerList().getPlayer(priorHolder);
            if (holder != null) syncRights(holder);
        }
        ACTIVE.remove(victim.getUUID());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(victim, new GetNetwork.GetState(victim.getUUID(), false));
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer) || killer == victim) return;

        GetRightsData rights = GetRightsData.of(victim.server);
        UUID killerId = killer.getUUID(), victimId = victim.getUUID();
        // Revanche : on efface la dette antérieure, sans en créer une nouvelle.
        if (rights.revoke(victimId, killerId)) {
            if (victimId.equals(FORCED_BY.get(killerId))) {
                FORCED_BY.remove(killerId);
                set(killer, false, true);
            }
            notice(killer, GetNetwork.NOTICE_CANCELLED, victim.getGameProfile().getName());
            notice(victim, GetNetwork.NOTICE_CANCELLED, killer.getGameProfile().getName());
            syncRights(victim);
            syncRights(killer);
        } else if (rights.grant(killerId, victimId)) {
            notice(killer, GetNetwork.NOTICE_GRANTED, victim.getGameProfile().getName());
            syncRights(killer);
        }
    }

    public static void syncRights(ServerPlayer actor) {
        GetRightsData data = GetRightsData.of(actor.server);
        java.util.List<GetNetwork.RightEntry> list = new java.util.ArrayList<>();
        for (UUID id : data.targets(actor.getUUID())) {
            ServerPlayer target = actor.server.getPlayerList().getPlayer(id);
            // Offline players remain in storage, but are not actionable via this GUI.
            if (target != null) list.add(new GetNetwork.RightEntry(id, target.getGameProfile().getName(),
                actor.getUUID().equals(FORCED_BY.get(id))));
        }
        list.sort(java.util.Comparator.comparing(GetNetwork.RightEntry::name));
        PacketDistributor.sendToPlayer(actor, new GetNetwork.RightsSnapshot(list));
    }

    public static void notice(ServerPlayer p, int kind, String other) {
        PacketDistributor.sendToPlayer(p, new GetNetwork.Notice(kind, other));
    }

    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer observer
                && event.getTarget() instanceof ServerPlayer target
                && ACTIVE.contains(target.getUUID())) {
            PacketDistributor.sendToPlayer(observer, new GetNetwork.GetState(target.getUUID(), true));
        }
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncRights(player);
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        FORCED_BY.remove(player.getUUID());
        if (ACTIVE.remove(player.getUUID())) {
            PacketDistributor.sendToPlayersTrackingEntity(player, new GetNetwork.GetState(player.getUUID(), false));
        }
    }
}
