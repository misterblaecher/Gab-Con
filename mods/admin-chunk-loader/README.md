# GabCon Admin Chunk Loader 1.1.0 — NeoForge 1.21.1

## Nouveau : directement sur la carte FTB Chunks

Cette version intègre le mode **GabCon Force-load** à la **carte de revendication** FTB Chunks
(`ftb-chunks-neoforge-2101.1.22`), sans modifier les fichiers du mod FTB d'origine.

### Installation

**Installer le même JAR sur le serveur ET sur les clients administrateurs.**
C'est désormais nécessaire car les boutons et overlays de la carte sont dessinés côté client.
Les joueurs sans mod client ne verront pas cette fonctionnalité ; la connexion peut dépendre
de la négociation de paquets NeoForge (recommandé : distribuer à tous les clients du modpack).

- Supprimer l'ancienne version `gabcon-admin-chunk-loader-1.0.0.jar` sur le serveur.
- Ajouter `gabcon-admin-chunk-loader-1.1.0.jar` dans `A:\MinecraftServer\mods\`.
- Ajouter ce même JAR dans le dossier `mods` du client GabCon (et de tous les clients moddés).
- Redémarrer le serveur et relancer le jeu.

**Dépendances fixes :** Minecraft 1.21.1, NeoForge 21.1.251,
FTB Chunks 2101.1.22, FTB Library 2101.1.36, FTB Teams 2101.1.11.

### Utilisation (OP niveau 2+)

1. Ouvrir la carte FTB Chunks (`M` par défaut), puis cliquer sur l'icône des chunks revendiqués
   afin d'ouvrir la **carte des claims** (grille de 15×15 chunks autour du joueur).
2. Dans la barre supérieure, cliquer sur la petite icône **balise/Beacon GabCon**
   (3e bouton à gauche). Le mode **GabCon Force-load** passe sur ON.
3. Les chunks vanilla forcés sont surlignés en **vert** ; les autres en **rouge**.
4. **Clic gauche** sur une case : force-load du chunk ou libération.
5. Cliquer à nouveau sur l'icône pour revenir au mode FTB classique. Fermer la carte
   désactive aussi le mode.
6. Le serveur confirme chaque modification (actionbar), puis synchronise l'état réel.

En mode GabCon, le clic ne fait **pas de claims FTB**. Le mod pilote le force-load
**vanilla** (`ServerLevel#setChunkForced`) : il n'ajoute ni ne retire les tickets
ou claims propres à FTB Chunks. **256 chunks max par dimension** pour les activations
faites depuis cette interface. Les chunks restent forcés après redémarrage.

### Dépannage : ancienne interface admin

`/chunkloader` ou `/chunkloader at -1544 710` ouvrent toujours l'interface coffre
du serveur si le mode FTB pose problème.

### Compilation automatique

Workflow : [Build GabCon Admin Chunk Loader](../../.github/workflows/build-gabcon-admin-chunk-loader.yml).

Sur chaque push dans `mods/admin-chunk-loader/**` sur `main`, GitHub Actions compile avec
Java 21 / Gradle 9.2.1. Dans **Actions → Build GabCon Admin Chunk Loader → Artifacts**,
récupérer `gabcon-admin-chunk-loader-1.1.0`, puis extraire
`gabcon-admin-chunk-loader-1.1.0.jar` du ZIP GitHub. Un build réussi ne remplace
pas un test manuel d'ouverture et de clic dans le jeu.

Version cible de l'intégration UI : **FTB Chunks 2101.1.22**. Une mise à jour
du mod FTB peut nécessiter d'adapter les points d'injection Mixin.
