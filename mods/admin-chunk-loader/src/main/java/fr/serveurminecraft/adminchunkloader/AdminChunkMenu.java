package fr.serveurminecraft.adminchunkloader;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ChunkPos;

import java.util.Arrays;
import java.util.List;

/**
 * Interface de 9 x 6 cases (coffre vanilla).
 * La 1re ligne contient les boutons ; les 5 autres contiennent une grille 7 x 5.
 * La carte porte sur la dimension dans laquelle la commande a été exécutée.
 */
public final class AdminChunkMenu extends ChestMenu {
    // Plafond volontaire pour éviter les milliers de force-load accidentels.
    private static final int MAX_FORCED_CHUNKS = 256;
    private static final int WORLD_LIMIT_CHUNK = 1_874_999;
    private static final int GRID_WIDTH = 7;
    private static final int GRID_HEIGHT = 5;
    private static final int SLOT_COUNT = 54;

    private final SimpleContainer display;
    private final ServerLevel level;
    private int centerX;
    private int centerZ;

    public AdminChunkMenu(int containerId, Inventory inventory, ServerLevel level,
                          int centerX, int centerZ) {
        this(containerId, inventory, level, centerX, centerZ, new SimpleContainer(SLOT_COUNT));
    }

    private AdminChunkMenu(int containerId, Inventory inventory, ServerLevel level,
                           int centerX, int centerZ, SimpleContainer display) {
        super(MenuType.GENERIC_9x6, containerId, inventory, display, 6);
        this.display = display;
        this.level = level;
        this.centerX = centerX;
        this.centerZ = centerZ;
        refresh();
    }

    @Override
    public boolean stillValid(Player player) {
        return player instanceof ServerPlayer serverPlayer
            && serverPlayer.createCommandSourceStack().hasPermission(2)
            && serverPlayer.serverLevel() == level;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        // Les icônes du menu sont virtuelles : elles ne doivent jamais pouvoir être prises.
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slot, int button, ClickType clickType, Player player) {
        // Bloque absolument toutes les manipulations vanilla de l'inventaire.
        // Autorise uniquement les clics gauche/droite sur les cases de commande.
        if (!(player instanceof ServerPlayer serverPlayer) || !stillValid(player)) {
            return;
        }
        if (clickType != ClickType.PICKUP || slot < 0 || slot >= SLOT_COUNT ||
            (button != 0 && button != 1)) {
            broadcastFullState();
            return;
        }

        if (slot < 9) {
            if (button == 0) {
                handleToolbarClick(serverPlayer, slot);
            }
        } else {
            int row = slot / 9;
            int col = slot % 9;
            if (col >= 1 && col <= GRID_WIDTH && row >= 1 && row <= GRID_HEIGHT) {
                int chunkX = centerX + col - 4;
                int chunkZ = centerZ + row - 3;
                if (button == 0) {
                    toggleChunk(serverPlayer, chunkX, chunkZ);
                } else {
                    // Clic droit : recentre la carte sans modifier la force-load.
                    centerX = chunkX;
                    centerZ = chunkZ;
                }
            }
        }
        refresh();
        broadcastFullState();
    }

    private void handleToolbarClick(ServerPlayer player, int slot) {
        switch (slot) {
            case 0 -> centerX = Math.max(-WORLD_LIMIT_CHUNK, centerX - GRID_WIDTH);
            case 1 -> centerX = Math.min(WORLD_LIMIT_CHUNK, centerX + GRID_WIDTH);
            case 2 -> centerZ = Math.max(-WORLD_LIMIT_CHUNK, centerZ - GRID_HEIGHT);
            case 3 -> centerZ = Math.min(WORLD_LIMIT_CHUNK, centerZ + GRID_HEIGHT);
            case 4 -> {
                centerX = player.chunkPosition().x;
                centerZ = player.chunkPosition().z;
            }
            case 5 -> player.sendSystemMessage(Component.literal(
                "Chunks forcés dans " + level.dimension().location() + " : " +
                level.getForcedChunks().size() + "/" + MAX_FORCED_CHUNKS
            ).withStyle(ChatFormatting.GOLD));
            case 6 -> { /* bouton actualiser : refresh() est appelé ensuite */ }
            case 8 -> player.closeContainer();
            default -> { /* aide */ }
        }
    }

    private void toggleChunk(ServerPlayer player, int chunkX, int chunkZ) {
        if (Math.abs((long) chunkX) > WORLD_LIMIT_CHUNK ||
            Math.abs((long) chunkZ) > WORLD_LIMIT_CHUNK) {
            player.sendSystemMessage(Component.literal("Chunk hors des limites du monde.")
                .withStyle(ChatFormatting.RED));
            return;
        }
        boolean forced = level.getForcedChunks().contains(ChunkPos.asLong(chunkX, chunkZ));
        if (!forced && level.getForcedChunks().size() >= MAX_FORCED_CHUNKS) {
            player.sendSystemMessage(Component.literal(
                "Limite de " + MAX_FORCED_CHUNKS + " chunks forcés atteinte dans cette dimension."
            ).withStyle(ChatFormatting.RED));
            return;
        }
        if (level.setChunkForced(chunkX, chunkZ, !forced)) {
            player.sendSystemMessage(Component.literal(
                (forced ? "Force-load retiré" : "Force-load activé") +
                " pour le chunk [" + chunkX + ", " + chunkZ + "] dans " +
                level.dimension().location()
            ).withStyle(forced ? ChatFormatting.YELLOW : ChatFormatting.GREEN));
        }
    }

    private void refresh() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            display.setItem(slot, icon(Items.GRAY_STAINED_GLASS_PANE,
                " ", ChatFormatting.DARK_GRAY));
        }

        display.setItem(0, icon(Items.ARROW, "◀ Ouest (-7 chunks)", ChatFormatting.YELLOW,
            "Décaler la grille vers X négatif"));
        display.setItem(1, icon(Items.ARROW, "Est (+7 chunks) ▶", ChatFormatting.YELLOW,
            "Décaler la grille vers X positif"));
        display.setItem(2, icon(Items.COMPASS, "▲ Nord (-5 chunks)", ChatFormatting.YELLOW,
            "Décaler la grille vers Z négatif"));
        display.setItem(3, icon(Items.COMPASS, "▼ Sud (+5 chunks)", ChatFormatting.YELLOW,
            "Décaler la grille vers Z positif"));
        display.setItem(4, icon(Items.ENDER_EYE, "Se centrer sur moi", ChatFormatting.AQUA,
            "Centre actuel : " + centerX + ", " + centerZ,
            "Dimension : " + level.dimension().location()));
        display.setItem(5, icon(Items.CLOCK, "Chunks forcés : " + level.getForcedChunks().size(),
            ChatFormatting.GOLD,
            "Limite de sécurité : " + MAX_FORCED_CHUNKS + " par dimension",
            "Cliquer pour afficher dans le chat"));
        display.setItem(6, icon(Items.SLIME_BALL, "Actualiser", ChatFormatting.GREEN,
            "Rafraîchir l'état des chunks"));
        display.setItem(7, icon(Items.BOOK, "Aide", ChatFormatting.AQUA,
            "Vert = force-load actif (vanilla)",
            "Rouge = force-load inactif (vanilla)",
            "Clic gauche : activer / retirer",
            "Clic droit : recentrer sur ce chunk",
            "Case brillante = centre de la carte"));
        display.setItem(8, icon(Items.BARRIER, "Fermer", ChatFormatting.RED));

        for (int row = 1; row <= GRID_HEIGHT; row++) {
            for (int col = 1; col <= GRID_WIDTH; col++) {
                int chunkX = centerX + col - 4;
                int chunkZ = centerZ + row - 3;
                int slot = row * 9 + col;
                boolean outOfWorld = Math.abs((long) chunkX) > WORLD_LIMIT_CHUNK ||
                    Math.abs((long) chunkZ) > WORLD_LIMIT_CHUNK;
                if (outOfWorld) {
                    display.setItem(slot, icon(Items.BARRIER, "Hors limites", ChatFormatting.RED));
                    continue;
                }
                boolean forced = level.getForcedChunks().contains(ChunkPos.asLong(chunkX, chunkZ));
                ItemStack tile = icon(forced ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE,
                    "Chunk [" + chunkX + ", " + chunkZ + "]" +
                    (chunkX == centerX && chunkZ == centerZ ? " ★" : ""),
                    forced ? ChatFormatting.GREEN : ChatFormatting.RED,
                    "Blocs X: " + (chunkX * 16) + " à " + (chunkX * 16 + 15),
                    "Blocs Z: " + (chunkZ * 16) + " à " + (chunkZ * 16 + 15),
                    forced ? "État : CHARGÉ" : "État : NON FORCÉ",
                    "Gauche : " + (forced ? "retirer" : "forcer"),
                    "Droite : recentrer la grille");
                if (chunkX == centerX && chunkZ == centerZ) {
                    tile.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                }
                display.setItem(slot, tile);
            }
        }
    }

    private static ItemStack icon(Item item, String name, ChatFormatting color, String... description) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(color));
        if (description.length > 0) {
            List<Component> lore = Arrays.stream(description)
                .map(line -> (Component) Component.literal(line).withStyle(ChatFormatting.GRAY))
                .toList();
            stack.set(DataComponents.LORE, new ItemLore(lore));
        }
        return stack;
    }
}
