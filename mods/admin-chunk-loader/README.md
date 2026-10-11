# Admin Chunk Loader — NeoForge 1.21.1

Mod **côté serveur uniquement**, sans dépendance à FTB Chunks et sans installation sur les clients.

## Fonctionnement

- `/chunkloader` : ouvre une interface coffre sur les chunks autour de l'admin.
- `/chunkloader at -1544 710` : ouvre une interface centrée sur ces coordonnées **en blocs**.
- **OP niveau 2 minimum**, vérifié à l'ouverture **et à chaque clic**.
- **Grille de 7 × 5 chunks** : **vert** = force-load vanilla actif, **rouge** = inactif ; la case brillante est le centre.
- **Clic gauche** : active / retire le force-load.
- **Clic droit** : recentre la carte sur la case cliquée.
- Barre du haut : déplacement Ouest/Est/Nord/Sud, recentrage joueur, total, actualisation, aide, fermeture.
- Le force-load est géré par Minecraft (`ServerLevel#setChunkForced`) et conservé à travers les redémarrages.
- Limite de sécurité : **256 chunks forcés par dimension** pour les ajouts via cette interface (les chunks déjà forcés peuvent toujours être libérés).
- L'interface agit sur **la dimension du joueur au moment de l'ouverture** (Overworld, Nether, End).
- Aucun objet virtuel du menu ne peut être récupéré dans l'inventaire.

**Attention** : FTB Chunks maintient ses propres claims et tickets de chargement ; cette interface pilote le `/forceload` **vanilla** et ne modifie pas les claims FTB.

## Installation de développement / compilation Windows

1. Disposer de **Java JDK 21** (`java -version` et `javac -version`).
2. Ouvrir ce dossier sur Windows.
3. Double-cliquer sur **`build.bat`**. Le script télécharge Gradle 9.2.1 depuis le site officiel dans `.build-tools` s'il n'est pas disponible (connexion internet requise), puis exécute `gradle build`.
4. Le mod compilé est créé dans **`build\libs\adminchunkloader-1.0.0.jar`**.
5. Arrêter le serveur, copier le `.jar` dans **`A:\MinecraftServer\mods\`**, puis redémarrer.
6. Depuis un compte OP niveau 2, utiliser `/chunkloader` ou `/chunkloader at -1544 710`.

**IMPORTANT :** Ce dossier contient **le projet source**, pas un `.jar` prêt à jouer. Le projet n'a pas encore été compilé ni testé automatiquement, faute d'accès aux dépendances Gradle/NeoForge. Compiler et tester sur une copie du serveur avant production.

## Sécurité et charge

- Ne pas utiliser en même temps des milliers de chunks forcés : cela augmente la RAM, la charge CPU et le travail des machines moddés (notamment Create).
- Ne pas oublier de retirer les chunks devenus inutiles.
- Les admins doivent être OP niveau 2 : ex. `/op NomDuJoueur`, en tenant compte des permissions de ton serveur.
- Le menu est visuel, aucun inventaire réel n'est utilisé et les clics Shift / glisser-déposer sont ignorés.
