# Project: HAP - Home Automation Platform
# Code: Raspberry server script
# Author: Alan Carvalho
# Date: 08/05/2024

# Libraries
import credentials
import socket          
import threading       
from pubsub import pub  

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
                pub.sendMessage('commands', message = input, socket = self.socket, address = self.address)


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
    host = credentials.SERVER_HOST
    port = credentials.SERVER_PORT

    # Create new server socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((host, port))
    sock.listen(5)

    # Create new thread to wait for connections
    newConnectionsThread = threading.Thread(target = newConnections, args = (sock,))
    newConnectionsThread.start()
