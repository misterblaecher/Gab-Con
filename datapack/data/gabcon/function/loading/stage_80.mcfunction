bossbar set gabcon:loading value 85
bossbar set gabcon:loading name {"text":"\ue001  ","color":"white","extra":[{"text":"Synchronisation du serveur...","color":"green","bold":true}]}

title @s subtitle {"text":"Synchronisation du chaos...","color":"green"}
title @s actionbar {"text":"█████████████████","color":"green","extra":[{"text":"███  ","color":"dark_gray"},{"text":"85%","color":"white"}]}

playsound minecraft:block.amethyst_block.chime master @s ~ ~ ~ 0.55 1.35
