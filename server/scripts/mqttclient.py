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

    try:
        database = dbhandler.getDatabase()
        database_str = json.dumps(database, ensure_ascii=False)
        client.publish("hap/main/database/data", database_str, qos=1, retain=True)
        print("Published initial database")
    except Exception as e:
        print(f"Error publishing initial database: {e}")

    client.subscribe("hap/main/database/add_room")
    client.subscribe("hap/main/database/delete_room")
    client.subscribe("hap/main/database/update_room")
    client.subscribe("hap/main/database/add_device")
    client.subscribe("hap/main/database/delete_device")
    client.subscribe("hap/main/database/update_device")

def on_message(client, userdata, message):
    topic = message.topic
    payload = message.payload.decode()

    try:
        if topic == "hap/main/database/add_room":
            data = payload.split(',')
            dbhandler.addRoom(data[0], data[1], data[2])

        elif topic == "hap/main/database/update_room":
            data = payload.split(',')
            dbhandler.updateRoom(data[0], data[1], data[2])

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

        database = dbhandler.getDatabase()
        database_str = json.dumps(database, ensure_ascii=False)
        client.publish("hap/main/database/data", database_str, qos=1, retain=True)

    except Exception as e:
        print(f"✗ ERROR processing MQTT message on topic {topic}:")
        print(f"  Exception: {e}")
        print(f"  Exception Type: {type(e).__name__}")
        import traceback
        traceback.print_exc()

def start():
    client.on_connect = on_connect
    client.on_message = on_message
    client.connect(ip, port, 60)
    print("MQTT Client connecting to broker...")
    client.loop_forever()