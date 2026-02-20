# Project: HAP - Home Automation Platform
# Code: Raspberry setup script
# Author: Alan Carvalho
# Date: 11/08/2024


# Database name
dbName = "home_automation_db.db"

# Database structure
dbStructure = [
               ["Devices"],                                              # Table 1 name
               ["ID", "Name", "Room", "Type", "Topic"],                  # Table 1 columns
               ["TEXT PRIMARY KEY", "TEXT", "TEXT", "TEXT", "TEXT"],     # Table 1 types
               ["Rooms"],                                                # Table 2 name
               ["ID", "Name"],                                           # Table 2 columns
               ["TEXT PRIMARY KEY", "TEXT"],                             # Table 2 types
               ["Types"],                                                # Table 3 name
               ["ID", "Type"],                                           # Table 3 columns
               ["INTEGER PRIMARY KEY", "TEXT"],                          # Table 3 types
               ["Users"],                                                # Table 4 name
               ["ID", "User", "Password", "Level"],                      # Table 4 columns
               ["INTEGER PRIMARY KEY", "TEXT", "TEXT", "TEXT"]           # Table 4 types
              ]

#Device types
deviceTypes = [("Lamp",),
               ("LampLDR",),
               ("WallSocket",),
               ("AirConditioner",),
               ("Curtain",),
               ("Door",)]

