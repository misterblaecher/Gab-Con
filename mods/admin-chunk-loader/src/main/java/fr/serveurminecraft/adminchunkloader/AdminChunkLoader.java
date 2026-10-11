package fr.serveurminecraft.adminchunkloader;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Petit outil d'administration serveur. Aucun écran, item ou paquet personnalisé :
 * les clients standards voient un menu de coffre vanilla.
 */
@Mod(AdminChunkLoader.MOD_ID)
public final class AdminChunkLoader {
    public static final String MOD_ID = "adminchunkloader";

    public AdminChunkLoader() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("chunkloader")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    return openMenu(context.getSource(), player.getBlockX(), player.getBlockZ());
                })
                .then(Commands.literal("at")
                    .then(Commands.argument("x", IntegerArgumentType.integer(-29_999_984, 29_999_984))
                        .then(Commands.argument("z", IntegerArgumentType.integer(-29_999_984, 29_999_984))
                            .executes(context -> openMenu(
                                context.getSource(),
                                IntegerArgumentType.getInteger(context, "x"),
                                IntegerArgumentType.getInteger(context, "z")
                            )))))
        );
    }

    private static int openMenu(CommandSourceStack source, int blockX, int blockZ)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int chunkX = Math.floorDiv(blockX, 16);
        int chunkZ = Math.floorDiv(blockZ, 16);

        Component title = Component.literal("Force-load | " +
            player.serverLevel().dimension().location().getPath());

        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, menuPlayer) ->
                new AdminChunkMenu(containerId, inventory, player.serverLevel(), chunkX, chunkZ),
            title
        ));
        return 1;
    }
}
