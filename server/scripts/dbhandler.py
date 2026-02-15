# Project: HAP - Home Automation Platform
# Code: Raspberry database handler script
# Author: Alan Carvalho
# Date: 12/02/2026

# Libraries
import setup            # setup.py script
import sqlite3 as lite  # Library to work with sqlite database
import json             # Library to work with json packages


# Function to create and configure the database
def createDatabase():

    # Create the database file
    connection = lite.connect(setup.dbName)
    connection.commit()

    # Create the tables
    cursor = connection.cursor()

    try:
        # Create the database tables
        for table in range(0, len(setup.dbStructure), 3):
            query = "CREATE TABLE IF NOT EXISTS "
            query += setup.dbStructure[table][0] + " ("

            for field in range(len(setup.dbStructure[table + 1])):
                query += setup.dbStructure[table + 1][field] + ' '
                query += setup.dbStructure[table + 2][field]
                if (field < (len(setup.dbStructure[table + 1]) - 1)): 
                    query += ", "
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
    except lite.Error as e:
        connection.rollback()
        raise e
    finally:
        cursor.close()
        connection.close()


# Function to get the database as a JSON list
def getDatabase():
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()
    jsonList = []

    try:
        for table in range(0, len(setup.dbStructure), 3):
            query = "SELECT * FROM "
            query += setup.dbStructure[table][0]
            cursor.execute(query)
            data = cursor.fetchall()
            
            dataList = [list(row) for row in data]
            dataJSON = json.dumps(dataList, ensure_ascii=False)
            jsonList.append(dataJSON)
    except lite.Error as e:
        raise e
    finally:
        cursor.close()
        connection.close()

    return jsonList


# Function to get the list of rooms
def getRooms():
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        query = "SELECT " + setup.dbStructure[4][0] + ", " + setup.dbStructure[4][1] + " FROM " + setup.dbStructure[3][0]
        cursor.execute(query)
        rooms = [{"id": row[0], "name": row[1]} for row in cursor.fetchall()]
    except lite.Error as e:
        raise e
    finally:
        cursor.close()
        connection.close()

    return rooms


# Function to add a new room
def addRoom(roomID, roomName, roomTopic):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        query = "INSERT INTO " + setup.dbStructure[3][0] + " (" + setup.dbStructure[4][0] + ", " + setup.dbStructure[4][1] + ", " + setup.dbStructure[4][2] + ") VALUES (?, ?, ?)"
        cursor.execute(query, (roomID, roomName, roomTopic))
        connection.commit()
    except lite.Error as e:
        connection.rollback()
        raise e
    finally:
        cursor.close()
        connection.close()

# Function to update a room
def updateRoom(roomID, roomName, roomTopic):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        query = "UPDATE " + setup.dbStructure[3][0] + " SET " + setup.dbStructure[4][1] + " = ?, " + setup.dbStructure[4][2] + " = ? WHERE " + setup.dbStructure[4][0] + " = ?"
        cursor.execute(query, (roomName, roomTopic, roomID))
        connection.commit()
    except lite.Error as e:
        connection.rollback()
        raise e
    finally:
        cursor.close()
        connection.close()


#Function to delete a room
def deleteRoom(roomID):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        query = "DELETE FROM " + setup.dbStructure[3][0] + " WHERE " + setup.dbStructure[4][0] + " = ?"
        cursor.execute(query, (roomID,))
        connection.commit()
    except lite.Error as e:
        connection.rollback()
        raise e
    finally:
        cursor.close()
        connection.close()


# Function to add new devices
def addDevice(deviceID, deviceName, deviceRoom, deviceType, deviceTopic):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        query = "INSERT INTO " + setup.dbStructure[0][0] + " (" + setup.dbStructure[1][0] + ", " + setup.dbStructure[1][1] + ", " + setup.dbStructure[1][2] + ", " + setup.dbStructure[1][3] + ", " + setup.dbStructure[1][4] + ") VALUES (?, ?, ?, ?, ?)"
        cursor.execute(query, (deviceID, deviceName, deviceRoom, deviceType, deviceTopic))
        connection.commit()
    except lite.Error as e:
        connection.rollback()
        raise e
    finally:
        cursor.close()
        connection.close()


# Function to delete a device
def deleteDevice(deviceID):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        query = "DELETE FROM " + setup.dbStructure[0][0] + " WHERE " + setup.dbStructure[1][0] + " = ?"
        cursor.execute(query, (deviceID,))
        connection.commit()
    except lite.Error as e:
        connection.rollback()
        raise e
    finally:
        cursor.close()
        connection.close()