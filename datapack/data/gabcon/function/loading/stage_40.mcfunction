bossbar set gabcon:loading value 45
bossbar set gabcon:loading name {"text":"\ue001  ","color":"white","extra":[{"text":"Connexion aux blocs...","color":"light_purple","bold":true}]}

title @s subtitle {"text":"Connexion aux blocs...","color":"gray"}
title @s actionbar {"text":"█████████","color":"light_purple","extra":[{"text":"███████████  ","color":"dark_gray"},{"text":"45%","color":"white"}]}

playsound minecraft:block.note_block.pling master @s ~ ~ ~ 0.5 1.15
