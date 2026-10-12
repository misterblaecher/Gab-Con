# GabCon GET 0.2.1 — droits PvP et interfaces

Minecraft Java **1.21.1**, **NeoForge 21.1.251**, Java 21. Installer le **même JAR** côté serveur et chez les joueurs.

## Règles des droits

- Quand **A tue B en PvP**, **A gagne le droit de mettre B en GET**, sans que B ne gagne le même droit.
- Si **B tue ensuite A**, le droit détenu par A sur B **disparaît** ; B **n'en reçoit pas un nouveau**. Les deux redeviennent à égalité.
- Un joueur peut détenir des droits sur plusieurs autres joueurs. Les droits sont enregistrés en UUID dans le **SavedData** du monde principal et conservés après redémarrage/reconnexion.
- Le joueur détenteur peut **imposer** et **lever** une GET. Le joueur en GET imposée ne peut pas se relever tout seul avec H ou `/get off`. La revanche annule le droit et lève toute GET encore imposée par ce détenteur.
- L'emote n'affecte pas encore la hitbox ni le mouvement : c'est une position visuelle. L'administration garde `/get set <joueur> on|off`.
- Les GET forcées ne persistent pas après mort, déconnexion ou redémarrage. **Seuls les droits** persistent.
- Les kills par mobs, l'environnement et les suicides ne créent pas de droit.

## Commandes / touches

- **H** : GET volontaire, si non contraint.
- **G** : menu interactif des **joueurs connectés** sur qui on a des droits, boutons `IMPOSER` / `RELEVER`.
- `/get` / `/get on` / `/get off` : emote volontaire.
- `/get imposer <joueur>` / `/get relever <joueur>` : exercer son droit sur la cible connectée.
- `/get droits` : demande un rafraîchissement des droits (l'interface s'ouvre avec G).
- `/get set <joueur> on|off` : override administrateur OP niveau 2.

Toutes les actions venant du GUI passent par le serveur et sont **revérifiées** contre les droits enregistrés : un client ne peut pas inventer un droit.

## Trois interfaces en image

Les vrais titres, messages et boutons sont ajoutés par le mod ; les PNG ne doivent contenir que les illustrations et les emplacements graphiques.

| Déclencheur | Image du message utilisateur | Destination dans le repo |
|---|---|---|
| GET imposée à la victime | `Tribunal steampunk au clair de lune.png` | `src/main/resources/assets/gabconget/textures/gui/get_imposed.png` |
| Droit gagné par le tueur | `Cérémonie voxel festive au château.png` | `src/main/resources/assets/gabconget/textures/gui/get_granted.png` |
| Revanche, droit annulé | `Atelier steampunk de redressement voxel enchanté.png` | `src/main/resources/assets/gabconget/textures/gui/get_cancelled.png` |

Les trois illustrations originales, déposées sur GitHub, sont directement incluses sous leurs noms cibles dans le mod. Les originaux déposés à la racine du dépôt restent également conservés. Les PNG ne contiennent pas le texte fonctionnel, dessiné dynamiquement par Minecraft.

Les boutons `X` (48x48 pixels source, coin supérieur droit) et la validation (320x64, bas centré) sont de vrais widgets Minecraft, mis à l'échelle pour la résolution de la fenêtre. Le texte est dynamique selon le nom de l'autre joueur. La notification attend la fermeture de l'écran de mort ou d'une interface déjà ouverte.

## Compilation

`gradle build` depuis ce dossier avec Gradle 8.11+ et Java 21 ; sinon utiliser le workflow GitHub Actions du dépôt parent. JAR : `build/libs/gabcon-get-0.2.1.jar`.

## Tests en jeu recommandés

1. A tue B : vérifier l'écran de succès chez A et que B ne peut pas imposer A.
2. A `/get imposer B` : B reçoit la sentence ; H et `/get off` ne permettent pas de sortir.
3. A `/get relever B` puis peut encore réimposer, car le droit subsiste.
4. B tue A : les deux reçoivent l'annulation, le droit de A est supprimé et la GET imposée par A, si présente, est levée.
5. Déconnexion/redémarrage : les droits restent mais les poses imposées sont annulées.
6. Tester deux clients et les vues Steve/Alex avec mods et armures.

## Commandes de test (OP niveau 2 uniquement)

Toutes les commandes de ce paragraphe sont sous `/get test`, protegees via Brigadier `requires(source -> source.hasPermission(2))`.
Les non-OP n'y ont pas acces, meme via des paquets reseau.

| Commande | Action | Etat du monde |
| --- | --- | --- |
| `/get test` ou `/get test aide` | Aide en jeu | Aucun changement |
| `/get test ecran obtenu` | Affiche le succes (image ceremonie) | Aucun changement |
| `/get test ecran impose` | Affiche la sentence (tribunal) | Aucun changement |
| `/get test ecran annule` | Affiche l'annulation (atelier) | Aucun changement |
| `/get test ecran releve` | Affiche la relevee (atelier, autre titre) | Aucun changement |
| `/get test ecran tous` | Enfile les 4 ecrans dans l'ordre | Aucun changement |
| `/get test ecran tous <joueur>` | Envoie les 4 ecrans a ce joueur | Aucun changement |
| `/get test pose <joueur> on/off` | Force/libere visuellement la pose | Modifie la pose temporaire |
| `/get test combat <tueur> <victime>` | Simule le kill PvP : droit ou revanche, notifications | Modifie les droits sauvegardes |
| `/get test etat <joueur>` | Affiche la pose, qui la force, et les droits detenus | Aucun changement |
| `/get test reset <detenteur> <cible>` | Retire un droit precis et la pose imposee associee | Modifie les droits sauvegardes |

Les noms apres `ecran` peuvent tous etre suivis du nom du joueur cible (OP comme non-OP).
Sans cible, le joueur OP recoit lui-meme la fenetre. Depuis la console, preciser la cible.
Les apercus utilisent le nom fictif `Joueur_DEMO`, sans creer de droit.

Exemple de test avec deux personnes connectees : `/get test reset Steve Alex`,
`/get test combat Steve Alex`, Steve utilise `G` ou `/get imposer Alex`,
`/get test etat Alex`, puis `/get test combat Alex Steve` pour annuler la dette.
La simulation combat n'inflige **aucun degat**, mais gere les droits comme une elimination.
Les droits crees par ces tests restent sauvegardes tant qu'ils ne sont pas annules ou reset.

Tous les ecrans de preview restent dans la file d'attente Minecraft ; il faut fermer chaque ecran
pour afficher le suivant. Utiliser `/get test ecran tous` en dehors d'autres GUI pour un test immediat.
