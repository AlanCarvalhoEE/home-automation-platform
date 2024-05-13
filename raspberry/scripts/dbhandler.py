# Project: HAP - Home Automation Platform
# Code: Raspberry database handler script
# Author: Alan Carvalho
# Date: 08/05/20124

# Libraries
import setup            # setup.py script
import sqlite3 as lite  # Library to work sqlite database
import json             # Library to work with json packages


# Function to create and configure the database
def createDatabase():

    # Create the database file
    connection = lite.connect(setup.dbName)
    connection.commit()

    # Create the tables
    cursor = connection.cursor()

    # Create the database tables
    for table in range(0, len(setup.dbStructure), 3):
        query = "CREATE TABLE IF NOT EXISTS "
        query += setup.dbStructure[table][0] + " ("

        for field in range(len(setup.dbStructure[table + 1])):
            query += setup.dbStructure[table + 1][field] + ' '
            query += setup.dbStructure[table + 2][field]
            if (field < (len(setup.dbStructure[table + 1]) - 1)): query += ", "
        query += ")"

        cursor.execute(query)

    # Populate the device types table if it is empty
    query = "SELECT COUNT(*) FROM "
    query += setup.dbStructure[6][0]
    cursor.execute(query)
    rows = cursor.fetchone()[0]

    if (rows == 0):
        query = "INSERT INTO "
        query += setup.dbStructure[6][0]
        query += " VALUES (null, ?, ?)"
        cursor.executemany(query, setup.dbTypes)
        connection.commit()


# Function to get the database as a JSON list
def getDatabase():
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()
    jsonList = []

    for table in range(0, len(setup.dbStructure), 3):
        query = "SELECT * FROM "
        query += setup.dbStructure[table][0]
        cursor.execute(query)
        data = cursor.fetchall()
        dataJSON = json.dumps(data, ensure_ascii = False)
        jsonList.append(dataJSON)

    connection.commit()
    return jsonList


# Function to add new room
def addRoom(roomName):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    query = "INSERT INTO Cômodos (ID, Cômodo) VALUES (null, '"
    query += roomName
    query += "')"

    cursor.execute(query)
    connection.commit()


# Function to add new devices
def addDevice(deviceName, deviceRoom, deviceType, deviceDesignator, deviceIP):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    query = "INSERT INTO Dispositivos (ID, Dispositivo, Cômodo, Tipo, Designador, IP) VALUES (null, '"
    query += deviceName
    query += "', '"
    query += deviceRoom
    query += "', '"
    query += deviceType
    query += "', '"
    query += deviceDesignator
    query += "', '"
    query += deviceIP
    query += "')"

    cursor.execute(query)
    connection.commit()



def deleteDevice(deviceName):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    query = "DELETE FROM Dispositivos WHERE Dispositivo ='"
    query += deviceName
    query += "'"

    cursor.execute(query)
    connection.commit()


# Function to get a device IP
def getIP(deviceDesignator):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    query = "SELECT IP FROM Dispositivos WHERE DESIGNADOR='"
    query += deviceDesignator
    query += "'"
    cursor.execute(query)

    ip = ''
    ip = ip + cursor.fetchone()[0]
    ip = ip.replace("[", "")

    return ip


# Function to search an IP in the database
def searchIP(deviceIP):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    query = "SELECT IP FROM Dispositivos WHERE IP="
    query += deviceIP
    cursor.execute(query)

    if (cursor.rowcount > 0): return True
    else: return False
