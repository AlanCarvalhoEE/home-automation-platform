# Project: HAP - Home Automation Platform
# Code: Raspberry setup script
# Author: Alan Carvalho
# Date: 08/05/2024


# Database name
dbName = "home_automation_db.db"

# Database schema
dbStructure = [
               ["Dispositivos"],                                                     # Table 1 name
               ["ID", "Dispositivo", "Cômodo", "Tipo", "Designador", "IP"],          # Table 1 columns
               ["INTEGER_PRIMARY_KEY", "TEXT", "TEXT", "TEXT", "TEXT", "TEXT"],      # Table 1 types
               ["Cômodos"],                                                          # Table 2 name
               ["ID", "Cômodo"],                                                     # Table 2 columns
               ["INTEGER PRIMARY KEY", "TEXT"],                                      # Table 2 types
               ["Tipos"],                                                            # Table 3 name
               ["ID", "Tipo", "Prefixo"],                                            # Table 3 columns
               ["INTEGER PRIMARY KEY", "TEXT", "TEXT"],                              # Table 3 types
               ["Usuários"],                                                         # Table 4 name
               ["ID", "Usuário", "Senha", "Nível"],                                  # Table 4 columns
               ["INTEGER PRIMARY KEY", "TEXT", "TEXT", "TEXT"]                       # Table 4 types
              ]

# Database types
dbTypes = [("Lâmpada", "LAMP"),
           ("Tomada", "SOCKET"),
           ("Ar condicionado", "AIR"),
           ("Cortina", "CURTAIN"),
           ("Porta", "DOOR")]
