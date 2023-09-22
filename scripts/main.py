import configuration        # configuration.py script
import devices              # devices.py script
import server               # server.py script
from pubsub import pub      # Library to handle data listening from other script

# Function to listen to data from the tcp server
def listener(arg1 = None):

    data = arg1.split(b'-')

    param1 = ord(data[1]) - 48
    param2 = ord(data[2]) - 48

    if data[0] == b'RELAY':
        devices.setRelay(configuration.relays[param1 - 1], param2)

server.main()                           # Initialize the TCP server
pub.subscribe(listener, 'commands')     # Subscribe to the 'commands' topic
