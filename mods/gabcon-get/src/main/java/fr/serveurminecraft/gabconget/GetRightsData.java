package fr.serveurminecraft.gabconget;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Droits directionnels persistants dans la sauvegarde du monde (UUID, pas noms). */
public final class GetRightsData extends SavedData {
    private final Map<UUID, Set<UUID>> rights = new HashMap<>();

    public static GetRightsData of(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(GetRightsData::new, GetRightsData::load), "gabcon_get_rights");
    }

    private static GetRightsData load(CompoundTag tag, HolderLookup.Provider provider) {
        GetRightsData data = new GetRightsData();
        ListTag entries = tag.getList("Rights", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag item = entries.getCompound(i);
            if (!item.hasUUID("Holder") || !item.hasUUID("Target")) continue;
            UUID holder = item.getUUID("Holder");
            UUID target = item.getUUID("Target");
            if (!holder.equals(target)) data.rights.computeIfAbsent(holder, unused -> new java.util.HashSet<>()).add(target);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag entries = new ListTag();
        rights.forEach((holder, targets) -> targets.forEach(target -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Holder", holder);
            entry.putUUID("Target", target);
            entries.add(entry);
        }));
        tag.put("Rights", entries);
        return tag;
    }

    public boolean has(UUID holder, UUID target) {
        return rights.getOrDefault(holder, Set.of()).contains(target);
    }

    public boolean grant(UUID holder, UUID target) {
        if (holder.equals(target)) return false;
        if (rights.computeIfAbsent(holder, unused -> new java.util.HashSet<>()).add(target)) {
            setDirty();
            return true;
        }
        return false;
    }

    public boolean revoke(UUID holder, UUID target) {
        Set<UUID> targets = rights.get(holder);
        if (targets != null && targets.remove(target)) {
            if (targets.isEmpty()) rights.remove(holder);
            setDirty();
            return true;
        }
        return false;
    }

    public Set<UUID> targets(UUID holder) {
        return Set.copyOf(rights.getOrDefault(holder, Set.of()));
    }
}
