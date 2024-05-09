# Project: HAP - Home Automation Platform
# Code: Raspberry server script
# Author: Alan Carvalho
# Date: 08/05/2024

# Libraries
import socket           # Library for sockets implementation
import threading        # Library to allow concurrent processes
from pubsub import pub  # Library to handle data publishing to another script 

# Client connections variables
connections = []
total_connections = 0


# Client class
class Client(threading.Thread):
    def __init__(self, socket, address, id, name, signal):
        threading.Thread.__init__(self)
        self.socket = socket
        self.address = address
        self.id = id
        self.name = name
        self.signal = signal

    def __str__(self):
        return str(self.id) + " " + str(self.address)

    # Check for data from clients
    def run(self):
        while self.signal:
            try:
                input = self.socket.recv(80)
            except:
                print("Client " + str(self.address) + " has disconnected")
                self.signal = False
                connections.remove(self)
                break
            if input != "":
                pub.sendMessage('commands', message = input)

# Wait for new connections
def newConnections(socket):
    while True:
        sock, address = socket.accept()
        global total_connections
        connections.append(Client(sock, address, total_connections, "Name", True))
        connections[len(connections) - 1].start()
        print("New connection at ID " + str(connections[len(connections) - 1]))
        total_connections += 1

def main():
    # Server host and port
    host = '192.168.88.11'
    port = 5560

    # Create new server socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.bind((host, port))
    sock.listen(5)

    # Create new thread to wait for connections
    newConnectionsThread = threading.Thread(target = newConnections, args = (sock,))
    newConnectionsThread.start()
