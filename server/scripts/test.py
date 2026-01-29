# Project: HAP - Home Automation Platform
# Code: Raspberry database populating test code
# Author: Alan Carvalho
# Date: 08/05/2024

# Libraries
import dbhandler    # dbhandler.py script

# Create the database
dbhandler.createDatabase()

# Populate the database
dbhandler.addRoom('Office')
dbhandler.addRoom('Bedroom')
dbhandler.addRoom('Kitchen')
dbhandler.addDevice('21', 'Lampada', 'Office', 'Lamp')
dbhandler.addDevice('22', 'Lampada', 'Bedroom', 'Lamp')
dbhandler.addDevice('23', 'Lampada', 'Kitchen', 'Lamp')
