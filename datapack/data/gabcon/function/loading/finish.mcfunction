bossbar set gabcon:loading value 100
bossbar set gabcon:loading name {"text":"\ue001  ","color":"white","extra":[{"text":"Prêt !","color":"green","bold":true}]}

title @s times 0 30 10
title @s title {"text":"\ue001","color":"white"}
title @s subtitle {"text":"Bienvenue, ","color":"gray","extra":[{"selector":"@s","color":"green","bold":true},{"text":" !","color":"gray"}]}
title @s actionbar {"text":"████████████████████  ","color":"green","extra":[{"text":"100%  ✔","color":"white","bold":true}]}

playsound minecraft:entity.player.levelup master @s ~ ~ ~ 0.75 1.1

# Enregistre la valeur de leave_game utilisée pour détecter la prochaine reconnexion.
execute if score @s gc_leave matches 0.. run scoreboard players operation @s gc_leave_prev = @s gc_leave
scoreboard players set @s gc_timer 0
tag @s remove gabcon.loading
