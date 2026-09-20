# Premier passage sur le serveur
execute as @a[tag=!gabcon.seen] run function gabcon:loading/start

# Reconnexion : leave_game augmente lorsqu'un joueur quitte le serveur.
execute as @a[tag=gabcon.seen,tag=!gabcon.loading] if score @s gc_leave matches 0.. unless score @s gc_leave = @s gc_leave_prev run function gabcon:loading/start

# Affichage de la bossbar uniquement aux joueurs en chargement.
execute if entity @a[tag=gabcon.loading] run bossbar set gabcon:loading players @a[tag=gabcon.loading]
execute if entity @a[tag=gabcon.loading] run bossbar set gabcon:loading visible true
execute unless entity @a[tag=gabcon.loading] run bossbar set gabcon:loading visible false
execute unless entity @a[tag=gabcon.loading] run bossbar set gabcon:loading value 0

# Chronomètre individuel.
scoreboard players add @a[tag=gabcon.loading] gc_timer 1

execute as @a[tag=gabcon.loading,scores={gc_timer=20}] at @s run function gabcon:loading/stage_20
execute as @a[tag=gabcon.loading,scores={gc_timer=40}] at @s run function gabcon:loading/stage_40
execute as @a[tag=gabcon.loading,scores={gc_timer=60}] at @s run function gabcon:loading/stage_60
execute as @a[tag=gabcon.loading,scores={gc_timer=80}] at @s run function gabcon:loading/stage_80
execute as @a[tag=gabcon.loading,scores={gc_timer=100}] at @s run function gabcon:loading/finish
