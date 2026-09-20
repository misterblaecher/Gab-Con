# Gab Con Resource Pack

Resource pack personnalisé du serveur **Gab Con** pour **Minecraft Java 1.21.1**.

## État actuel

Première base de personnalisation Gab Con :

- identité visuelle violet + vert ;
- icône de pack personnalisée ;
- métadonnées propres à Minecraft 1.21.1 (`pack_format: 34`) ;
- logo Gab Con utilisable directement dans les textes Minecraft avec le glyphe privé `U+E001` ;
- bossbar violette personnalisée Gab Con (progression violet → vert) ;
- sources du pack décompressées dans `resource-pack/` ;
- reconstruction automatique de `1.21.1-Gab Con.zip` à chaque modification des sources.

## Tester le logo en jeu

Le glyphe `U+E001` est ajouté à la police Minecraft par défaut.

```mcfunction
/title @a title {"text":"\\uE001","color":"white"}
```

## Tester la bossbar Gab Con

La couleur `purple` utilise maintenant le style Gab Con.

```mcfunction
/bossbar add gabcon:server {"text":"\\uE001"}
/bossbar set gabcon:server color purple
/bossbar set gabcon:server max 100
/bossbar set gabcon:server value 73
/bossbar set gabcon:server players @a
```

## Installation manuelle

Le fichier `1.21.1-Gab Con.zip` peut être placé tel quel dans :

```text
.minecraft/resourcepacks/
```

Les fichiers de travail décompressés se trouvent dans `resource-pack/`.

## Workflow

- `Unpack Gab Con resource pack` : extrait le ZIP vers `resource-pack/` quand le ZIP est remplacé.
- `Build Gab Con resource pack` : reconstruit automatiquement le ZIP quand un fichier de `resource-pack/` change.

## Suite prévue

- glyphes/icônes Gab Con supplémentaires ;
- GUI violet/vert ;
- sons Gab Con ;
- écran d'arrivée simulé avec datapack + `/title`.
