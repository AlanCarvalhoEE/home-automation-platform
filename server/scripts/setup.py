# Project: HAP - Home Automation Platform
# Code: Raspberry setup script
# Author: Alan Carvalho
# Date: 11/08/2024


# Database name
dbName = "home_automation_db.db"

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