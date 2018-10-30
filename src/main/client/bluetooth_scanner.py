#!python3

import bluetooth._bluetooth as bluez
import sys
import os
import blescan
from beacon_scan import BeaconScan


class BluetoothScanner:
  def __init__(self):
    self.dev_id = 0
    self.sock = None
    self.debug = (os.getenv('TEKNICHRONO_BT_DEBUG', 'false') == 'true')

  def init(self):
    try:
      self.sock = bluez.hci_open_dev(self.dev_id)
      print("Loading Scanner ...")
    except:
      print("Not bluetooth device found ...")
      sys.exit(1)
    blescan.hci_le_set_scan_parameters(self.sock)
    blescan.hci_enable_le_scan(self.sock)

  def scan(self):
    current = blescan.parse_events(self.sock, 1)
    if len(current) > 0:
      scanned = BeaconScan()
      scanned.init(current[0])
      if self.debug:
        print('Saw : ' + str(scanned))
      if isinstance(scanned.major, int) and isinstance(scanned.minor, int) and scanned.minor == scanned.major and scanned.minor > 0:
        return scanned
    return None