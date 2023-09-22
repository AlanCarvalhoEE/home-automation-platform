# File to configure GPIO, network and others

import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
relays=[4, 17, 27, 22, 5, 6, 26, 23, 24, 36]

for i in range(9):
    GPIO.setup(relays[i], GPIO.OUT)
