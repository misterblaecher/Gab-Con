# Gab Con — initialisation
scoreboard objectives add gc_timer dummy
scoreboard objectives add gc_leave minecraft.custom:minecraft.leave_game
scoreboard objectives add gc_leave_prev dummy

bossbar add gabcon:loading {"text":"\ue001  GAB CON","color":"light_purple","bold":true}
bossbar set gabcon:loading max 100
bossbar set gabcon:loading value 0
bossbar set gabcon:loading color purple
bossbar set gabcon:loading style progress
bossbar set gabcon:loading visible false
