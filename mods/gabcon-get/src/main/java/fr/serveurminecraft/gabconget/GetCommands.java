package fr.serveurminecraft.gabconget;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /get, /get on|off ; un OP peut faire /get set <joueur> on|off. */
public final class GetCommands {
    private GetCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("get")
            .executes(ctx -> {
                GetServerState.toggle(ctx.getSource().getPlayerOrException());
                return 1;
            })
            .then(Commands.literal("on").executes(ctx -> own(ctx.getSource().getPlayerOrException(), true)))
            .then(Commands.literal("off").executes(ctx -> own(ctx.getSource().getPlayerOrException(), false)))
            .then(Commands.literal("set")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("joueur", EntityArgument.player())
                    .then(Commands.literal("on").executes(ctx -> target(
                        EntityArgument.getPlayer(ctx, "joueur"), true)))
                    .then(Commands.literal("off").executes(ctx -> target(
                        EntityArgument.getPlayer(ctx, "joueur"), false))))));
    }

    private static int own(ServerPlayer player, boolean enabled) throws CommandSyntaxException {
        GetServerState.set(player, enabled);
        return 1;
    }

    private static int target(ServerPlayer player, boolean enabled) {
        GetServerState.set(player, enabled);
        return 1;
    }
}
