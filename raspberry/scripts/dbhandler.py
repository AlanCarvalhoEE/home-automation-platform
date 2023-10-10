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
    query = "CREATE TABLE IF NOT EXISTS "

    for table in range(0, 3, len(setup.dbStructure)):
        query += setup.dbStructure[table][0] + " ("

        for field in range(len(setup.dbStructure[table + 1]) - 1):
            query += setup.dbStructure[table + 1][field] + ' '
            query += setup.dbStructure[table + 2][field]
            if(field < (len(setup.dbStructure[table + 1]) - 2)): query += ", "
        query += ")"

        cursor.execute(query)

    # Populate the device types table
    cursor.executemany("INSERT INTO Tipos VALUES (null, ?, ?)", setup.dbTypes)
    connection.commit()


# Function to get the database as JSON list
def getDatabase():
    connection = lite.connect("home_automation.db")
    cursor = connection.cursor()
    jsonList = []

    cursor.execute("SELECT * FROM Dispositivos")
    devicesTable = cursor.fetchall()
    devicesTableJSON = json.dumps(devicesTable, ensure_ascii = False)

    cursor.execute("SELECT * FROM Cômodos")
    roomsTable = cursor.fetchall()
    roomsTableJSON = json.dumps(roomsTable, ensure_ascii = False)

    cursor.execute("SELECT * FROM Tipos")
    typesTable = cursor.fetchall()
    typesTableJSON = json.dumps(typesTable, ensure_ascii = False)

    cursor.execute("SELECT * FROM Usuários")
    usersTable = cursor.fetchall()
    usersTableJSON = json.dumps(usersTable, ensure_ascii = False)

    jsonList.append(devicesTableJSON)
    jsonList.append(roomsTableJSON)
    jsonList.append(typesTableJSON)
    jsonList.append(usersTableJSON)

    connection.commit()
    return jsonList

# Function to get the rooms list
def getRoomsList():
    connection = lite.connect("home_automation.db")
    cursor = connection.cursor()
    cursor.execute("SELECT Cômodo FROM Cômodos")
    rooms = cursor.fetchall()
    connection.commit()
    return rooms

# Function to get the devices list
def getDevicesList(roomName):
    connection = lite.connect("home_automation.db")
    cursor = connection.cursor()
    cursor.execute("SELECT Dispositivo FROM Dispositivos WHERE Cômodo=\'" + roomName +"\'")
    devices = cursor.fetchall()
    connection.commit()
    return devices

# Function to get the types list
def getTypesList():
    connection = lite.connect("home_automation.db")
    cursor = connection.cursor()
    cursor.execute("SELECT Tipo FROM Tipos")
    types = cursor.fetchall()
    connection.commit()
    return types

# Function to get the designators list
def getDesignatorsList():
    connection = lite.connect("home_automation.db")
    cursor = connection.cursor()
    cursor.execute("SELECT Designador FROM Dispositivos")
    designators = cursor.fetchall()
    connection.commit()
    return designators

# Function to get a device type
def getDeviceType(deviceName):
    connection = lite.connect("home_automation.db")
    cursor = connection.cursor()
    cursor.execute("SELECT Tipo FROM Dispositivos WHERE Dispositivo=\'" + deviceName + "\'")
    type = cursor.fetchone()
    connection.commit()
    return type

#Function to get a device designator
def getDeviceDesignator(deviceName):
    coonnection = lite.connect("home_automation.db")
    cursor = connection.cursor()
    cursor.execute("SELECT Designador FROM Dispositivos WHERE Dispositivo=" + deviceName + '"')
    designator = cursor.fetchone()
    connection.commit()
    return designator

# Function to add new room
def addRoom(roomName):
    connection = lite.connect("home_automation.db")
    cursor = connection.cursor()
    cursor.execute("INSERT INTO Cômodos (ID, Cômodo) VALUES (null, " + roomName + ')')
    connection.commit()

# Function to add new devices
def addDevice(deviceName, deviceRoom, deviceType, deviceDesignator):
    connection = lite.connect("home_automation.db")
    cursor = connection.cursor()
    cursor.execute("INSERT INTO Dispositivos (ID, Dispositivo, Cômodo, Tipo, Designador) VALUES (null, '" + deviceName + "', '" + deviceRoom + "', '" + deviceType + "', '" + deviceDesignator + "')")
    connection.commit()
