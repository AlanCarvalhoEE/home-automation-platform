import setup                # setup.py script
import devices              # devices.py script
import server               # server.py script
import dbhandler            # dbhandler.py script
from pubsub import pub      # Library to handle data listening from other script

# Function to listen to data from the tcp server
def listener(arg1 = None):

    if b'GET_DATABASE' in arg1:
        database = dbhandler.getDatabase()
        databaseMessage = "DATABASE-"
        for table in database:
            databaseMessage += table
        databaseMessage += '\r\n'
        server.connections[0].socket.sendto((databaseMessage).encode('utf-8'), server.connections[0].address)

    elif b'RELAY' in arg1:
        data = arg1.decode('utf-8').replace("\n", "").split('-')
        number = int(data[0][-1])
        state = data[1]

        devices.controlRelay(setup.relays[number - 1], state)

    elif b'AIR' in arg1:

        data = arg1.decode('utf-8').replace("\n" ,"").split('-')
        address = server.connections[0].address
        command = data[1]

        devices.controlAirConditioner(address, command)

server.main()                           # Initialize the TCP server
dbhandler.createDatabase()              # Create the database if it doesn't exist
pub.subscribe(listener, 'commands')     # Subscribe to the 'commands' topic
