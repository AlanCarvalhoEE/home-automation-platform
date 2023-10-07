import configuration        # configuration.py script
import devices              # devices.py script
import server               # server.py script
from pubsub import pub      # Library to handle data listening from other script

# Function to listen to data from the tcp server
def listener(arg1 = None):

    if b'RELAY' in arg1:

        data = arg1.decode('utf-8').replace("\n", "").split('-')
        number = int(data[0][-1])
        state = data[1]

        devices.setRelayState(configuration.relays[number - 1], state)

    elif b'AIR' in arg1:

        data = arg1.decode('utf-8').replace("\n" ,"").split('-')
        address = server.connections[0].address
        state = data[1]

        devices.setAirConditionerState(address, state)

server.main()                           # Initialize the TCP server
pub.subscribe(listener, 'commands')     # Subscribe to the 'commands' topic
