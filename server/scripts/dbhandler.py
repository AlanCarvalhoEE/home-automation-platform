# Project: HAP - Home Automation Platform
# Code: Raspberry database handler script
# Author: Alan Carvalho
# Date: 11/08/20124

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
        query += " VALUES (null, ?)"
        cursor.executemany(query, setup.deviceTypes)
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


# Function to get the list of rooms
def getRooms():
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    query = "SELECT Room FROM Rooms"
    cursor.execute(query)

    rooms = [row[0] for row in cursor.fetchall()]
    return rooms


# Function to add a new room
def addRoom(roomName, roomTopic):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    query = "INSERT INTO Rooms (ID, Name, Topic) VALUES (null, '"
    query += roomName
    query += "', '"
    query += roomTopic
    query += "')"

    cursor.execute(query)
    connection.commit()


#Function to delete a room
def deleteRoom(roomName):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    query = "DELETE FROM Rooms WHERE Name ='"
    query += roomName
    query += "'"

    cursor.execute(query)
    connection.commit()


# Function to add new devices
def addDevice(deviceID, deviceName, deviceRoom, deviceType, deviceTopic):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    query = "INSERT INTO Devices (ID, Name, Room, Type, Topic) VALUES ('"
    query += deviceID
    query += "', '"
    query += deviceName
    query += "', '"
    query += deviceRoom
    query += "', '"
    query += deviceType
    query += "', '"
    query += deviceTopic
    query += "')"

    cursor.execute(query)
    connection.commit()


# Function to delete a device
def deleteDevice(deviceID):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()
    
    query = "DELETE FROM Devices WHERE ID ='"
    query += deviceID
    query += "'"
    print(query)
    cursor.execute(query)
    connection.commit()
