# Gab Con — Resource Pack & Loading Screen

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

## Faux écran de chargement Gab Con

Le dossier `datapack/` contient maintenant une séquence d'arrivée vanilla pour Minecraft Java 1.21.1 :

- logo Gab Con ;
- bossbar personnalisée ;
- barre de progression 5 → 100 % ;
- messages de chargement ;
- sons vanilla ;
- protection et ralentissement temporaires ;
- déclenchement au premier passage puis à chaque reconnexion.

Pour tester l'animation manuellement :

```mcfunction
/execute as NOM_DU_JOUEUR run function gabcon:loading/test
```

Pour tous les joueurs :

```mcfunction
/function gabcon:admin/replay_all
```

Le ZIP généré automatiquement est `GabCon-Datapack-1.21.1.zip`. Il doit être placé dans `<monde>/datapacks/`.

## Installation manuelle du resource pack

Le fichier `1.21.1-Gab Con.zip` peut être placé tel quel dans :

```text
.minecraft/resourcepacks/
```

Les fichiers de travail décompressés se trouvent dans `resource-pack/`.

## Workflow

- `Unpack Gab Con resource pack` : extrait le ZIP vers `resource-pack/` quand le ZIP est remplacé.
- `Build Gab Con resource pack` : reconstruit automatiquement le ZIP quand un fichier de `resource-pack/` change.
- `Build Gab Con datapack` : reconstruit automatiquement `GabCon-Datapack-1.21.1.zip` quand un fichier de `datapack/` change.

## Suite prévue

- glyphes/icônes Gab Con supplémentaires ;
- GUI violet/vert ;
- sons Gab Con ;
- amélioration progressive de l'écran d'arrivée et des transitions.


## Panorama & écran de connexion

Les visuels source sont à la racine du dépôt :

- `Loading-screen-panorama-1.png` → converti automatiquement en cubemap Minecraft (`panorama_0.png` à `panorama_5.png`) pour le panorama du menu principal.
- `Loading-screen.png` → intégré comme glyphe plein écran `U+E100` dans la police `gabcon:loading` et affiché par le datapack quand un joueur rejoint le serveur.

Le panorama source actuel fait **1774×887**, soit un format 2:1 adapté à une conversion equirectangulaire en cubemap. L'écran de connexion fait **1536×1024**.

> Le panorama du menu principal n'est visible avant la connexion que si le resource pack est déjà actif localement (par exemple inclus dans le modpack). Un pack téléchargé uniquement lors de la connexion au serveur ne peut pas modifier le menu affiché avant son téléchargement.

## GUI & HUD transparents

Les interfaces de conteneurs sont rendues semi-transparentes à environ **62 % d'opacité** :

- inventaire joueur ;
- coffres ;
- crafting table ;
- fours / blast furnace / smoker ;
- hopper ;
- shulker box ;
- anvil ;
- enchanting table ;
- smithing ;
- stonecutter ;
- villager ;
- beacon ;
- brewing stand ;
- interfaces créatives ;
- autres conteneurs vanilla présents dans le pack.

Le HUD est également allégé :

- hotbar : ~55 % ;
- offhand : ~55 % ;
- sélection de slot : ~78 % ;
- fonds d'effets : ~55 % ;
- fonds des barres XP / saut / indicateurs : ~62 %.

Les cœurs, la faim, l'armure, le crosshair et les remplissages de progression restent nets pour conserver une bonne lisibilité.

Le workflow `Apply Gab Con visuals` conserve une copie des textures d'origine dans `sources/gui-base/`, puis régénère les versions transparentes depuis cette base. Les modifications ne s'accumulent donc pas à chaque build.
