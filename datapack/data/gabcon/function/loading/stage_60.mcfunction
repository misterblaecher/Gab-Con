bossbar set gabcon:loading value 65
bossbar set gabcon:loading name {"text":"\ue001  ","color":"white","extra":[{"text":"Injection de CON...","color":"green","bold":true}]}

title @s subtitle {"text":"Injection de CON...","color":"green"}
title @s actionbar {"text":"█████████████","color":"green","extra":[{"text":"███████  ","color":"dark_gray"},{"text":"65%","color":"white"}]}

playsound minecraft:entity.experience_orb.pickup master @s ~ ~ ~ 0.45 0.85
