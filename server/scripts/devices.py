import RPi.GPIO as GPIO     # Library for GPIO manipulation
import socket
import server

# Function to control relays
# parameter 1 = relay port
# parameter 2 = relay command
def controlRelay(port, command):
    if command == 'ON':
        GPIO.output(port, GPIO.HIGH)
    elif command == 'OFF':
        GPIO.output(port, GPIO.LOW)

# Function to turn air conditioners ON or OFF
# parameter 1 = air conditioner module adress (IP and port)
# parameter 2 = air conditioner command
def controlAirConditioner(address, command):
    server.connections[0].socket.sendto(bytes(command, 'utf-8'), address)
