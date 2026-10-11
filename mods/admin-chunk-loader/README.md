# GabCon Admin Chunk Loader — NeoForge 1.21.1

Mod **côté serveur uniquement**, sans dépendance à FTB Chunks et sans installation sur les clients.
Le nom affiché dans Minecraft est **GabCon Admin Chunk Loader** et le fichier compilé s'appelle
`gabcon-admin-chunk-loader-1.0.0.jar`. L'identifiant technique `adminchunkloader` est conservé.

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

## Compilation automatique : GitHub Actions

Le workflow [Build GabCon Admin Chunk Loader](../../.github/workflows/build-gabcon-admin-chunk-loader.yml)
compile ce projet automatiquement sur chaque `push` vers `main` et sur les pull requests qui
modifient le mod ou le workflow. Il peut aussi être lancé manuellement via `Actions → Build GabCon Admin Chunk Loader → Run workflow`.

Il utilise **Java 21** et **Gradle 9.2.1** pour NeoForge 1.21.1. Après une compilation réussie :

1. Ouvrir l'onglet **Actions** du dépôt.
2. Ouvrir l'exécution **Build GabCon Admin Chunk Loader** terminée avec succès.
3. Dans **Artifacts**, télécharger `gabcon-admin-chunk-loader-1.0.0` (archive ZIP fournie par GitHub).
4. Extraire `gabcon-admin-chunk-loader-1.0.0.jar` du ZIP.
5. Arrêter le serveur, copier le `.jar` dans `A:\MinecraftServer\mods\`, puis redémarrer.

Le ZIP des artefacts est conservé **30 jours**. Le workflow ne publie pas de GitHub Release et
n'installe pas le mod sur ton serveur : il compile uniquement et fournit le `.jar` à télécharger.

## Compilation manuelle sur Windows

1. Disposer du **JDK Java 21** (`java -version` et `javac -version`).
2. Ouvrir ce dossier sur Windows.
3. Double-cliquer sur **`build.bat`**. Le script télécharge Gradle 9.2.1 depuis le site officiel dans `.build-tools` si nécessaire, puis exécute `gradle build`.
4. Récupérer **`build\libs\gabcon-admin-chunk-loader-1.0.0.jar`**.
5. Arrêter le serveur, copier le `.jar` dans **`A:\MinecraftServer\mods\`**, puis redémarrer.
6. Depuis un compte OP niveau 2, utiliser `/chunkloader` ou `/chunkloader at -1544 710`.

**Important :** il s'agit d'un projet source. Les builds automatisés servent à vérifier la compilation,
mais le gameplay doit être testé sur une copie du serveur avant installation en production.

## Sécurité et charge

- Ne pas utiliser en même temps des milliers de chunks forcés : cela augmente la RAM, la charge CPU et le travail des machines moddés (notamment Create).
- Ne pas oublier de retirer les chunks devenus inutiles.
- Les admins doivent être OP niveau 2 : ex. `/op NomDuJoueur`, en tenant compte des permissions de ton serveur.
- Le menu est visuel, aucun inventaire réel n'est utilisé et les clics Shift / glisser-déposer sont ignorés.
