# Project: HAP - Home Automation Platform
# Code: Raspberry MQTT client script
# Author: Alan Carvalho
# Date: 01/05/2026

import paho.mqtt.client as mqtt
import credentials
import dbhandler
import time
import json

ip = credentials.MQTT_BROKER_IP
port = credentials.MQTT_BROKER_PORT

client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)

def on_connect(client, userdata, flags, rc, properties):
    print(f"Connected with result code {rc}")

    try:
        database = dbhandler.getDatabase()
        client.publish("hap/main/database/data", database, qos=1, retain=True)
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
            dbhandler.addRoom(data[0], data[1])
            dbhandler.addLog("ROOM_ADDED", f"Room {data[1]} added.")

        elif topic == "hap/main/database/update_room":
            data = payload.split(',')
            oldName = dbhandler.getRoomName(data[0])
            dbhandler.updateRoom(data[0], data[1])
            dbhandler.addLog("ROOM_UPDATED", f"Room {oldName} updated as {data[1]}.")

        elif topic == "hap/main/database/delete_room":
            roomName = dbhandler.getRoomName(payload)
            dbhandler.deleteRoom(payload)
            dbhandler.addLog("ROOM_DELETED", f"Room {roomName} deleted.")

        elif topic == "hap/main/database/add_device":
            data = payload.split(',')
            dbhandler.addDevice(data[0], data[1], data[2], data[3], data[4], data[5])
            dbhandler.addLog("DEVICE_ADDED", f"Device {data[1]} added to {data[2]}.")

        elif topic == "hap/main/database/delete_device":
            oldName = dbhandler.getDeviceName(payload)
            dbhandler.deleteDevice(payload)
            dbhandler.addLog("DEVICE_DELETED", f"Device {oldName} deleted.")

        elif topic == "hap/main/database/update_device":
            data = payload.split(',')
            oldName = dbhandler.getDeviceName(data[0])
            dbhandler.updateDevice(data[0], data[1], data[2], data[3], data[4], data[5])
            dbhandler.addLog("DEVICE_UPDATED", f"Device {oldName} updated as {data[1]} at {data[2]}.")

        database = dbhandler.getDatabase()
        client.publish("hap/main/database/data", database, qos=1, retain=True)

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