package fr.serveurminecraft.gabconget;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Joueur: /get, /get imposer <joueur>, /get relever <joueur>, /get droits. */
public final class GetCommands {
    private GetCommands() {}
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("get")
            .executes(ctx -> { GetServerState.toggle(ctx.getSource().getPlayerOrException()); return 1; })
            .then(Commands.literal("on").executes(ctx -> own(ctx.getSource().getPlayerOrException(), true)))
            .then(Commands.literal("off").executes(ctx -> own(ctx.getSource().getPlayerOrException(), false)))
            .then(Commands.literal("droits").executes(ctx -> {
                ServerPlayer actor=ctx.getSource().getPlayerOrException();
                GetServerState.syncRights(actor);
                actor.sendSystemMessage(net.minecraft.network.chat.Component.literal("Ouvre le menu des droits avec G."));
                return 1;
            }))
            .then(Commands.literal("imposer").then(Commands.argument("joueur", EntityArgument.player())
                .executes(ctx -> GetServerState.impose(ctx.getSource().getPlayerOrException(),
                    EntityArgument.getPlayer(ctx, "joueur"),true))))
            .then(Commands.literal("relever").then(Commands.argument("joueur", EntityArgument.player())
                .executes(ctx -> GetServerState.impose(ctx.getSource().getPlayerOrException(),
                    EntityArgument.getPlayer(ctx, "joueur"),false))))
            .then(Commands.literal("set").requires(source -> source.hasPermission(2))
                .then(Commands.argument("joueur", EntityArgument.player())
                    .then(Commands.literal("on").executes(ctx -> admin(EntityArgument.getPlayer(ctx,"joueur"),true)))
                    .then(Commands.literal("off").executes(ctx -> admin(EntityArgument.getPlayer(ctx,"joueur"),false))))));
    }
    private static int own(ServerPlayer player,boolean enabled) { GetServerState.set(player,enabled);return 1; }
    private static int admin(ServerPlayer player,boolean enabled) { GetServerState.setAdmin(player,enabled);return 1; }
}
