# Project: HAP - Home Automation Platform
# Code: Raspberry main script
# Author: Alan Carvalho
# Date: 08/05/2024

# Libraries
import setup                # setup.py script
import devices              # devices.py script
import server               # server.py script
import dbhandler            # dbhandler.py script
from pubsub import pub      # Library to handle data listening from other script


# Function to listen to data from the tcp server
def listener(message = None):

    # All database data request
    if b'GET_DATABASE' in message:
        database = dbhandler.getDatabase()
        firstTable = True
        databaseMessage = "DATABASE-"
        for table in database:
            if (not firstTable): databaseMessage += '/'
            databaseMessage += table
            firstTable = False
        databaseMessage += '\r\n'
        server.connections[0].socket.sendto((databaseMessage).encode('utf-8'), server.connections[0].address)

    # Request to add room to database
    elif b'ADD_ROOM' in message:
        database = dbhandler.getDatabase()
        start = message.index(b'-') + 1
        room = message[start : len(message)-1].decode('utf-8')
        dbhandler.addRoom(room)

    # Request to add device to database
    elif b'ADD_DEVICE' in message:
        database = dbhandler.getDatabase()
        start = message.index(b'-') + 1
        data = message[start : len(message)-1].decode('utf-8')
        fields = data.split(',')
        dbhandler.addDevice(fields[0], fields[1], fields[2], fields[3], fields[4])

    # Request to set a device state
    elif b'SET' in message:
        start = message.index(b'-') + 1
        data = message[start : len(message)-1].decode('utf-8')
        fields = data.split('_')
        deviceIP = dbhandler.getIP(fields[0])
        for i in range(len(server.connections)):
            if (deviceIP in server.connections[i].address):
                server.connections[i].socket.sendto((fields[1]).encode('utf-8'), server.connections[i].address)

    # Device manual control notification
    elif b'MANUAL' in message:
        data = message.decode('utf-8') + '\n'
        for i in range(len(server.connections)):
            server.connections[i].socket.sendto(data.encode('utf-8'), server.connections[i].address)


server.main()                           # Initialize the TCP server
dbhandler.createDatabase()              # Create the database if it doesn't exist
pub.subscribe(listener, 'commands')     # Subscribe to the 'commands' topic

print('Server initialized')
