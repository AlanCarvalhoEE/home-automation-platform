# Project: HAP - Home Automation Platform
# Code: Raspberry setup script
# Author: Alan Carvalho
# Date: 11/08/2024

import credentials

# Database name
dbName = credentials.DATABASE_NAME

# Database structure
databaseStructure = {
    "Devices": {
        "columns": [
            ("ID", "TEXT PRIMARY KEY"),
            ("Name", "TEXT"),
            ("Room", "TEXT"),
            ("Type", "TEXT"),
            ("Function", "TEXT"),
            ("Topic", "TEXT"),
        ]
    },
    "Rooms": {
        "columns": [
            ("ID", "TEXT PRIMARY KEY"),
            ("Name", "TEXT"),
        ]
    },
    "Users": {
        "columns": [
            ("ID", "INTEGER PRIMARY KEY"),
            ("User", "TEXT"),
            ("Password", "TEXT"),
            ("Level", "TEXT"),
        ]
    },
    "Log": {
        "columns": [
            ("ID", "INTEGER PRIMARY KEY AUTOINCREMENT"),
            ("Timestamp", "TEXT"),
            ("Type", "TEXT"),
            ("Message", "TEXT"),
        ]
    }
}