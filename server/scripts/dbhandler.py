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
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        for tableName, tableData in setup.databaseStructure.items():
            columns = tableData["columns"]

            query = f"CREATE TABLE IF NOT EXISTS {tableName} ("
            query += ", ".join([f"{name} {type_}" for name, type_ in columns])
            query += ")"

            cursor.execute(query)

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
    dbDict = {}

    try:
        for tableName in setup.databaseStructure.keys():
            cursor.execute(f"SELECT * FROM {tableName}")
            data = cursor.fetchall()

            dataList = [list(row) for row in data]
            dbDict[tableName] = dataList

    finally:
        cursor.close()
        connection.close()

    return json.dumps(dbDict, ensure_ascii=False)


# Function to get the list of rooms
def getRooms():
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        cursor.execute("SELECT ID, Name FROM Rooms")
        rooms = [{"id": row[0], "name": row[1]} for row in cursor.fetchall()]
    finally:
        cursor.close()
        connection.close()

    return rooms


# Function to add a new room
def addRoom(roomID, roomName):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        cursor.execute(
            "INSERT INTO Rooms (ID, Name) VALUES (?, ?)",
            (roomID, roomName)
        )
        connection.commit()
    except lite.Error as e:
        connection.rollback()
        print("ERROR addRoom:", e)
        raise e
    finally:
        cursor.close()
        connection.close()

# Function to update a room
def updateRoom(roomID, roomName):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        cursor.execute(
            "UPDATE Rooms SET Name = ? WHERE ID = ?",
            (roomName, roomID)
        )
        connection.commit()
    except lite.Error as e:
        connection.rollback()
        print("ERROR updateRoom:", e)
        raise e
    finally:
        cursor.close()
        connection.close()


#Function to delete a room
def deleteRoom(roomID):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        cursor.execute(
            "DELETE FROM Rooms WHERE ID = ?",
            (roomID,)
        )
        connection.commit()
    except lite.Error as e:
        connection.rollback()
        print("ERROR deleteRoom:", e)
        raise e
    finally:
        cursor.close()
        connection.close()


# Function to add new devices
def addDevice(deviceID, name, room, type_, function, topic):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        query = """
        INSERT INTO Devices (ID, Name, Room, Type, Function, Topic)
        VALUES (?, ?, ?, ?, ?, ?)
        """
        cursor.execute(query, (
            deviceID,
            name,
            room,
            type_.lower(),
            function.lower(),
            topic
        ))
        connection.commit()
    except lite.Error as e:
        connection.rollback()
        raise e
    finally:
        cursor.close()
        connection.close()


# Function to update a devices
def updateDevice(deviceID, name, room, type_, function, topic):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        query = """
        UPDATE Devices
        SET Name = ?, Room = ?, Type = ?, Function = ?, Topic = ?
        WHERE ID = ?
        """
        cursor.execute(query, (
            name,
            room,
            type_.lower(),
            function.lower(),
            topic,
            deviceID
        ))
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
        cursor.execute("DELETE FROM Devices WHERE ID = ?", (deviceID,))
        connection.commit()
    except lite.Error as e:
        connection.rollback()
        raise e
    finally:
        cursor.close()
        connection.close()

#Function to add a log entry
def addLog(timestamp, logType, message):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()

    try:
        cursor.execute(
            "INSERT INTO Log (Timestamp, Type, Message) VALUES (?, ?, ?)",
            (timestamp, logType, message)
        )
        connection.commit()
    except lite.Error as e:
        connection.rollback()
        raise e
    finally:
        cursor.close()
        connection.close()