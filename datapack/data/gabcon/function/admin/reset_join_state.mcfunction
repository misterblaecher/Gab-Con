# Oublie l'état Gab Con des joueurs en ligne.
# Au tick suivant, l'écran d'arrivée sera relancé.
tag @a remove gabcon.seen
tag @a remove gabcon.loading
scoreboard players reset @a gc_timer
scoreboard players reset @a gc_leave_prev
bossbar set gabcon:loading visible false
