# Project: HAP - Home Automation Platform
# Code: Raspberry main script
# Author: Alan Carvalho
# Date: 04/08/2025

# Libraries
import setup                # setup.py script
import mqttclient           # mqttclient.py script
import dbhandler            # dbhandler.py script
from pubsub import pub      # Library to handle data listening from other script

dbhandler.createDatabase()
mqttclient.start()

print('MQTT Server initialized and running')
