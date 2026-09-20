# Gab Con Resource Pack

Resource pack personnalisé du serveur **Gab Con** pour **Minecraft Java 1.21.1**.

## État actuel

Première base de personnalisation Gab Con :

- identité visuelle violet + vert ;
- icône de pack personnalisée ;
- métadonnées propres à Minecraft 1.21.1 (`pack_format: 34`) ;
- logo Gab Con utilisable directement dans les textes Minecraft avec le glyphe privé `U+E001` ;
- sources du pack décompressées dans `resource-pack/`.

## Tester le logo en jeu

Le glyphe `U+E001` est ajouté à la police Minecraft par défaut.

Exemple avec une commande :

```mcfunction
/title @a title {"text":"\\uE001","color":"white"}
```

Ou dans une bossbar :

```mcfunction
/bossbar add gabcon:server {"text":"\\uE001"}
/bossbar set gabcon:server players @a
```

## Installation manuelle

Le fichier `1.21.1-Gab Con.zip` peut être placé tel quel dans :

```text
.minecraft/resourcepacks/
```

Les fichiers de travail décompressés se trouvent dans `resource-pack/`.

## Suite prévue

- glyphes/icônes Gab Con supplémentaires ;
- GUI violet/vert ;
- bossbar personnalisée ;
- sons Gab Con ;
- écran d'arrivée simulé avec datapack + `/title` ;
- génération automatique d'un ZIP à jour depuis les sources.
