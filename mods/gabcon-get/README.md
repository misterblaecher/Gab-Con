# GabCon GET - Gueule en Terre

Mod autonome **Minecraft Java 1.21.1 / NeoForge 21.1.251 / Java 21**.

## Fonctionnalités

- Emote déclenchée par **H** (modifiable dans les contrôles) ou `/get`.
- `/get on`, `/get off` et `/get set <joueur> on|off` (commande administrateur, niveau 2).
- Entrée et sortie en 12 ticks, avec interpolation lissée.
- Pose théâtrale : buste fortement penché en avant, tête basse, bassin haut, bras tendus vers l'arrière.
- Cinq doigts mobiles par main, dix animations déphasées, et deux mollets repliés supplémentaires, utilisant la texture du skin Minecraft.
- Synchronisation client/serveur par paquets NeoForge, état uniquement en mémoire (réinitialisé à la déconnexion).
- Aucun besoin de GeckoLib, Player Animator ou autre mod tiers.

## Installation

1. Installer **exactement le même JAR** sur le serveur NeoForge et sur chaque client (ce mod synchronise l'emote, ce n'est pas un resource pack).
2. Serveur : `A:\MinecraftServer\mods\gabcon-get-0.1.0.jar`
3. Client : `A:\MinecraftClient\mods\gabcon-get-0.1.0.jar`
4. Relancer le serveur et les clients, entrer dans le monde et appuyer sur **H** ou taper `/get`.

## Construction

Sous Java 21, avec Gradle 8.11+ installé :

```shell
gradle build
```

JAR créé : `build/libs/gabcon-get-0.1.0.jar`.

Un workflow GitHub Actions est également proposé dans le dépôt parent pour automatiser la compilation et rendre le JAR téléchargeable comme artefact.

## Limites connues de cette première version

- **Animation visuelle seulement** : hitbox, hauteur réelle des yeux et mécanique du déplacement vanilla inchangées. Pour inspecter la pose, passer en vue F5.
- Les mains Minecraft n'ont aucun doigt anatomique : les dix doigts sont de très petits cubes supplémentaires avec la texture du skin.
- Les pièces d'armure, items tenus, elytras et modificateurs de poses d'autres mods peuvent se superposer imparfaitement à cette pose.
- Les rotations et offsets devront faire l'objet d'un test visuel en jeu (Steve et Alex) et d'un test multijoueur avant de considérer ce mod comme une version stable.
- Une compilation Java seule n'assure pas la compatibilité de tous les mixins au lancement.

## Structure

- `GetServerState` : état autoritaire et synchronisation des joueurs suivis.
- `GetNetwork` : protocole des deux payloads C2S/S2C.
- `GetCommands` : commandes joueur/admin.
- `ClientGetState` : interpolation (entrée et sortie).
- `HumanoidModelMixin` : inclinaison du buste, de la tête, des bras et cuisses.
- `GetDetailLayer` : rendu des dix doigts animés et des deux mollets pliés.

## Licence

MIT.
