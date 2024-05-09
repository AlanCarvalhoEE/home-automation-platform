# Project: HAP - Home Automation Platform
# Code: Raspberry database populating test code
# Author: Alan Carvalho
# Date: 08/05/2024

# Libraries
import dbhandler    # dbhandler.py script

# Create the database
dbhandler.createDatabase()

# Populate the database
dbhandler.addRoom('Sala')
dbhandler.addRoom('Quarto 1')
dbhandler.addRoom('Quarto 2')
dbhandler.addRoom('Corredor')
dbhandler.addDevice('Lâmpada 1', 'Sala', 'Lâmpada', 'LAMP1')
dbhandler.addDevice('Lâmpada 2', 'Quarto 1', 'Lâmpada', 'LAMP2')
dbhandler.addDevice('Lâmpada 3', 'Quarto 2', 'Lâmpada', 'LAMP3')
dbhandler.addDevice('Lâmpada 4', 'Corredor', 'Lâmpada', 'LAMP4')
dbhandler.addDevice('Ar 1', 'Quarto 1', 'Ar condicionado', 'AIR1')
dbhandler.addDevice('Porta 1', 'Sala', 'Porta', 'DOOR1')
