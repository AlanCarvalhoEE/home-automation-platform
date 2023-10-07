import RPi.GPIO as GPIO     # Library for GPIO manipulation
import socket
import server

# Function to control relays
# parameter 1 = relay port
# parameter 2 = relay state (ON or OFF)
def setRelayState(port, state):
    if state == 'ON':
        GPIO.output(port, GPIO.HIGH)
    elif state == 'OFF':
        GPIO.output(port, GPIO.LOW)

# Function to turn air conditioners ON or OFF
# parameter 1 = air conditioner module adress (IP and port)
# parameter 2 = air conditioner state (ON or OFF)
def setAirConditionerState(address, state):
    if state == 'ON':
        server.connections[0].socket.sendto(b'AIR-ON', address)
    elif state == 'OFF':
        server.connections[0].socket.sendto(b'AIR-OFF', address)
