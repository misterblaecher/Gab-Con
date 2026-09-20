# Gab Con Loading Screen Datapack

Datapack **Minecraft Java 1.21.1** (`pack_format: 48`) qui simule un écran de chargement Gab Con en vanilla.

## Ce qu'il fait

- déclenche l'animation au premier passage d'un joueur ;
- la rejoue à chaque reconnexion grâce à `minecraft.custom:minecraft.leave_game` ;
- affiche le logo Gab Con `U+E001` fourni par le resource pack ;
- anime une bossbar ;
- affiche titres, sous-titres et barre de progression ;
- joue plusieurs sons vanilla ;
- applique brièvement Blindness, Slowness et Resistance pendant l'animation.

La séquence dure environ **5 secondes**.

## Installation

Copier `GabCon-Datapack-1.21.1.zip` dans :

```text
<dossier-du-monde>/datapacks/
```

Puis lancer :

```mcfunction
/reload
```

Le resource pack Gab Con doit également être actif pour afficher correctement le logo `U+E001`.

## Test

Pour rejouer l'écran sur un joueur :

```mcfunction
/execute as NOM_DU_JOUEUR run function gabcon:loading/test
```

Pour le rejouer sur tous les joueurs connectés :

```mcfunction
/function gabcon:admin/replay_all
```

Pour effacer l'état de connexion des joueurs en ligne :

```mcfunction
/function gabcon:admin/reset_join_state
```

## Détails

La bossbar utilise la couleur `purple`. Le resource pack Gab Con remplace justement cette bossbar par le style violet → vert personnalisé.
