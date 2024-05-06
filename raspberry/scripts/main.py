import setup                # setup.py script
import devices              # devices.py script
import server               # server.py script
import dbhandler            # dbhandler.py script
from pubsub import pub      # Library to handle data listening from other script

# Function to listen to data from the tcp server
def listener(arg1 = None):

    if b'GET_DATABASE' in arg1:
        database = dbhandler.getDatabase()
        firstTable = True
        databaseMessage = "DATABASE-"
        for table in database:
            if (not firstTable): databaseMessage += '/'
            databaseMessage += table
            firstTable = False
        databaseMessage += '\r\n'
        server.connections[0].socket.sendto((databaseMessage).encode('utf-8'), server.connections[0].address)

    elif b'ADD_ROOM' in arg1:
        database = dbhandler.getDatabase()
        start = arg1.index(b'-') + 1
        room = arg1[start : len(arg1)-1].decode('utf-8')
        dbhandler.addRoom(room)

    elif b'ADD_DEVICE' in arg1:
        database = dbhandler.getDatabase()
        start = arg1.index(b'-') + 1
        message = arg1[start : len(arg1)-1].decode('utf-8')
        fields = message.split(',')
        dbhandler.addDevice(fields[0], fields[1], fields[2], fields[3], fields[4])

    elif b'CONTROL' in arg1:
        start = arg1.index(b'-') + 1
        message = arg1[start : len(arg1)-1].decode('utf-8')
        fields = message.split('_')
        print(fields[1])
        server.connections[1].socket.sendto((fields[1]).encode('utf-8'), server.connections[1].address)

    elif b'AIR' in arg1:
        data = arg1.decode('utf-8').replace("\n" ,"").split('-')
        address = server.connections[0].address
        command = data[1]
        devices.controlAirConditioner(address, command)

server.main()                           # Initialize the TCP server
dbhandler.createDatabase()              # Create the database if it doesn't exist
pub.subscribe(listener, 'commands')     # Subscribe to the 'commands' topic
