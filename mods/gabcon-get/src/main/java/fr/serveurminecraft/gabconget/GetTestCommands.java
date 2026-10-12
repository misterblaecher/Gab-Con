package fr.serveurminecraft.gabconget;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

/** Tests OP niveau 2 : apercus sans effets, ou simulations PvP volontaires. */
public final class GetTestCommands {
    private GetTestCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("test")
            .requires(source -> source.hasPermission(2))
            .executes(ctx -> help(ctx.getSource()))
            .then(Commands.literal("aide").executes(ctx -> help(ctx.getSource())))
            .then(Commands.literal("ecran")
                .then(screen("obtenu", GetNetwork.NOTICE_GRANTED))
                .then(screen("impose", GetNetwork.NOTICE_IMPOSED))
                .then(screen("annule", GetNetwork.NOTICE_CANCELLED))
                .then(screen("releve", GetNetwork.NOTICE_RELEASED))
                .then(screen("tous", -1)))
            .then(Commands.literal("combat")
                .then(Commands.argument("tueur", EntityArgument.player())
                    .then(Commands.argument("victime", EntityArgument.player())
                        .executes(ctx -> combat(ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "tueur"),
                            EntityArgument.getPlayer(ctx, "victime"))))))
            .then(Commands.literal("reset")
                .then(Commands.argument("detenteur", EntityArgument.player())
                    .then(Commands.argument("cible", EntityArgument.player())
                        .executes(ctx -> reset(ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "detenteur"),
                            EntityArgument.getPlayer(ctx, "cible"))))))
            .then(Commands.literal("etat")
                .then(Commands.argument("joueur", EntityArgument.player())
                    .executes(ctx -> status(ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "joueur")))))
            .then(Commands.literal("pose")
                .then(Commands.argument("joueur", EntityArgument.player())
                    .then(Commands.literal("on")
                        .executes(ctx -> pose(ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "joueur"), true)))
                    .then(Commands.literal("off")
                        .executes(ctx -> pose(ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "joueur"), false)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> screen(String name, int kind) {
        return Commands.literal(name)
            .executes(ctx -> preview(ctx.getSource(), ctx.getSource().getPlayerOrException(), kind))
            .then(Commands.argument("joueur", EntityArgument.player())
                .executes(ctx -> preview(ctx.getSource(),
                    EntityArgument.getPlayer(ctx, "joueur"), kind)));
    }

    private static int preview(CommandSourceStack source, ServerPlayer recipient, int kind) {
        int[] kinds = kind == -1
            ? new int[]{GetNetwork.NOTICE_GRANTED, GetNetwork.NOTICE_IMPOSED,
                GetNetwork.NOTICE_CANCELLED, GetNetwork.NOTICE_RELEASED}
            : new int[]{kind};
        for (int entry : kinds) GetServerState.notice(recipient, entry, "Joueur_DEMO");
        recipient.sendSystemMessage(Component.literal(
            "[GabCon TEST] Apercu uniquement : aucun droit ni aucune pose modifies.")
            .withStyle(ChatFormatting.YELLOW));
        source.sendSuccess(() -> Component.literal(
            "GabCon : " + kinds.length + " ecran(s) envoyes a " +
            recipient.getGameProfile().getName() + ". Ferme-les pour voir le suivant."), false);
        return kinds.length;
    }

    private static int combat(CommandSourceStack source, ServerPlayer killer, ServerPlayer victim) {
        if (killer.getUUID().equals(victim.getUUID())) {
            source.sendFailure(Component.literal("Le tueur et la victime doivent etre differents."));
            return 0;
        }
        int result = GetServerState.simulateCombat(killer, victim);
        String resultText = result == 2 ? "Revanche : droit oppose annule, aucun droit inverse."
            : result == 1 ? "Victoire : droit attribue au tueur."
            : "Aucun changement : droit deja detenu.";
        source.sendSuccess(() -> Component.literal(
            "[SIMULATION sans degats] " + killer.getGameProfile().getName()
            + " bat " + victim.getGameProfile().getName() + ". " + resultText), false);
        return 1;
    }

    private static int reset(CommandSourceStack source, ServerPlayer holder, ServerPlayer target) {
        if (holder.getUUID().equals(target.getUUID())) {
            source.sendFailure(Component.literal("Pas de droit de GET sur soi-meme."));
            return 0;
        }
        boolean changed = GetServerState.resetRight(holder, target);
        source.sendSuccess(() -> Component.literal("GabCon TEST : "
            + (changed ? "droit supprime" : "aucun droit existant") + " pour "
            + holder.getGameProfile().getName() + " -> "
            + target.getGameProfile().getName() + "."), false);
        return 1;
    }

    private static int pose(CommandSourceStack source, ServerPlayer player, boolean active) {
        GetServerState.setAdmin(player, active);
        source.sendSuccess(() -> Component.literal("GabCon TEST : pose "
            + (active ? "activee" : "desactivee") + " pour "
            + player.getGameProfile().getName() + ". Aucun droit cree."), false);
        return 1;
    }

    private static int status(CommandSourceStack source, ServerPlayer player) {
        GetRightsData rights = GetRightsData.of(player.server);
        UUID forced = GetServerState.forcedBy(player);
        source.sendSuccess(() -> Component.literal(
            "[GabCon TEST] " + player.getGameProfile().getName()
            + " : GET=" + GetServerState.active(player)
            + ", imposee_par=" + (forced == null ? "personne" : forced)
            + ", droits_detenus=" + rights.targets(player.getUUID()).size()), false);
        for (UUID id : rights.targets(player.getUUID())) {
            ServerPlayer target = player.server.getPlayerList().getPlayer(id);
            String label = target == null ? id + " (hors ligne)" : target.getGameProfile().getName();
            String imposed = target != null && player.getUUID().equals(GetServerState.forcedBy(target))
                ? " [GET imposee]" : "";
            source.sendSuccess(() -> Component.literal(" - " + label + imposed), false);
        }
        return 1;
    }

    private static int help(CommandSourceStack source) {
        String[] lines = {
            "=== GabCon GET : commandes de test OP ===",
            "/get test ecran obtenu|impose|annule|releve|tous [joueur]",
            "Apercus sans effet sur les droits (fermer chaque ecran).",
            "/get test pose <joueur> on|off : animation de pose",
            "/get test combat <tueur> <victime> : PvP simule SANS mort",
            "/get test etat <joueur> : affiche la pose et les droits",
            "/get test reset <detenteur> <cible> : efface un droit precis",
            "Pour tester les controles reels : G, /get imposer, /get relever."
        };
        for (String line : lines) {
            source.sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }
}
