import RPi.GPIO as GPIO     # Library for GPIO manipulation

# Function to control relays
# parameter 1 = relay port
# parameter 2 = relay state (0 = OFF, 1 = ON)
def setRelay(port, state):
    if state:
        GPIO.output(port, GPIO.HIGH)
    else:
        GPIO.output(port, GPIO.LOW)
