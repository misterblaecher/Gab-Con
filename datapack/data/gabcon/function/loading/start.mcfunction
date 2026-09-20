# Marque le joueur et initialise sa session.
tag @s add gabcon.seen
tag @s add gabcon.loading
scoreboard players set @s gc_timer 0
scoreboard players set @s gc_leave_prev 0
execute if score @s gc_leave matches 0.. run scoreboard players operation @s gc_leave_prev = @s gc_leave

# Protection pendant l'animation.
effect give @s minecraft:blindness 5 0 true
effect give @s minecraft:slowness 5 10 true
effect give @s minecraft:resistance 5 4 true

# Logo + première étape.
title @s times 0 110 10
title @s title {"text":"\ue100","font":"gabcon:loading","color":"white"}
title @s subtitle {"text":"Initialisation de Gab Con...","color":"light_purple"}
title @s actionbar {"text":"█","color":"light_purple","extra":[{"text":"███████████████████  ","color":"dark_gray"},{"text":"5%","color":"white"}]}

bossbar set gabcon:loading value 5
bossbar set gabcon:loading name {"text":"\ue001  ","color":"white","extra":[{"text":"Initialisation...","color":"light_purple","bold":true}]}

playsound minecraft:block.beacon.activate master @s ~ ~ ~ 0.65 1.25
