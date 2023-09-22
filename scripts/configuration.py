import RPi.GPIO as GPIO     # Library for GPIO manipulation

GPIO.setmode(GPIO.BCM)      # Use pin numbering based on the Broadcom chip

relays=[4, 17, 27, 22, 5, 6, 26, 23, 24, 36]    # Map the relay number (1 to 10) to the pin numbering

for i in range(9):      # Configure all the relay pins as outputs
    GPIO.setup(relays[i], GPIO.OUT)
