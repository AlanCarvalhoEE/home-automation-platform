#!/bin/bash

set -e
cd "$(dirname "$0")"

echo "Updating system..."
sudo apt update

echo "Installing APT packages..."
sudo xargs -a installed_packages.txt apt install -y

echo "Creating Python virtual environment..."
python3 -m venv venv

echo "Activating virtual environment..."
source venv/bin/activate

echo "Installing Python requirements..."
pip install --upgrade pip
pip3 install -r requirements.txt

echo "Installing systemd services..."
sudo cp services/*.service /etc/systemd/system/

echo "Enabling services..."
sudo systemctl daemon-reload
sudo systemctl enable server-auto-start.service
sudo systemctl start server-auto-start.service

echo "Setup complete."