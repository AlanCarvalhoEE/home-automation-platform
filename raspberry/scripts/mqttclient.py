# Project: HAP - Home Automation Platform
# Code: Raspberry MQTT client script
# Author: Alan Carvalho
# Date: 03/08/2025
import paho.mqtt.client as mqtt
import dbhandler
import time
import json


ip = "localhost"
port = 1883

client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)

def on_connect(client, userdata, flags, rc, properties):
    print(f"Connected with result code {rc}")

    database = dbhandler.getDatabase()
    database_str = json.dumps(database, ensure_ascii=False)

    client.publish("hap/main/database/data", database_str, qos=1, retain=True)

    client.subscribe("hap/main/database/add_room")
    client.subscribe("hap/main/database/delete_room")
    client.subscribe("hap/main/database/add_device")
    client.subscribe("hap/main/database/delete_device")
    client.subscribe("hap/main/database/update_device")

def on_message(client, userdata, message):
    topic = message.topic
    payload = message.payload.decode()

    if topic == "hap/main/database/add_room":
        data = payload.split(',')
        dbhandler.addRoom(data[0], data[1])

    elif topic == "hap/main/database/delete_room":
        dbhandler.deleteRoom(payload)

    elif topic == "hap/main/database/add_device":
        data = payload.split(',')
        dbhandler.addDevice(data[0], data[1], data[2], data[3], data[4])

    elif topic == "hap/main/database/delete_device":
        dbhandler.deleteDevice(payload)

    elif topic == "hap/main/database/update_device":
        data = payload.split(',')
        dbhandler.updateDevice(data[0], data[1], data[2], data[3], data[4])

def start():
    client.on_connect = on_connect
    client.on_message = on_message
    client.connect(ip, port, 60)
    client.loop_forever()
