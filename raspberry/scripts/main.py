import configuration        # configuration.py script
import devices              # devices.py script
import server               # server.py script
from pubsub import pub      # Library to handle data listening from other script

# Function to listen to data from the tcp server
def listener(arg1 = None):

    if b'RELAY' in arg1:

        data = arg1.decode('utf-8').split('-')
        number = int(data[0][-1])
        state = int(data[1][0])

        devices.setRelay(configuration.relays[number - 1], state);


server.main()                           # Initialize the TCP server
pub.subscribe(listener, 'commands')     # Subscribe to the 'commands' topic
