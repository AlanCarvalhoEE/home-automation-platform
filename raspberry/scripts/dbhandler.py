import setup            # setup.py script
import sqlite3 as lite  # Library to work sqlite database
import json

# Function to create and configure the database
def createDatabase():

    # Create the database file
    connection = lite.connect(setup.dbName)
    connection.commit()

    # Create the tables
    cursor = connection.cursor()

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


# Function to get the database as JSON list
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
def addDevice(deviceName, deviceRoom, deviceType, deviceDesignator):
    connection = lite.connect(setup.dbName)
    cursor = connection.cursor()
    query = "INSERT INTO Dispositivos (ID, Dispositivo, Cômodo, Tipo, Designador) VALUES (null, '"
    query += deviceName
    query += "', '"
    query += deviceRoom
    query += "', '"
    query += deviceType
    query += "', '"
    query += deviceDesignator
    query += "')"
    cursor.execute(query)
    connection.commit()


