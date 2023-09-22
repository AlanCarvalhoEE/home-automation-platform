import configuration
import devices
import server
from pubsub import pub
import sys

def listener(arg1=None):

    data=arg1.split(b'-')

    param1=ord(data[1])-48
    param2=ord(data[2])-48

    if data[0]==b'RELAY':
        devices.setRelay(configuration.relays[param1-1], param2)

server.main()
pub.subscribe(listener, 'commands')
