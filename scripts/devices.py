import RPi.GPIO as GPIO

def setRelay(number, state):
        if state:
                print("HIGH")
                GPIO.output(number, GPIO.HIGH)
        else:
                print("LOW")
                GPIO.output(number, GPIO.LOW)
