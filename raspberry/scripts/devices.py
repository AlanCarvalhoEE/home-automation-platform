import RPi.GPIO as GPIO     # Library for GPIO manipulation

# Function to control relays
# parameter 1 = relay number (1 to 10)
# parameter 2 = relay state (0 = OFF, 1 = ON)
def setRelay(number, state):
    if state:
        print("HIGH")
        GPIO.output(number, GPIO.HIGH)
    else:
        print("LOW")
        GPIO.output(number, GPIO.LOW)
