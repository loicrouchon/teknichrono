#!python3

from datetime import datetime

from api.beacons import addBeacon

from api.category import addCategory, addPilotToCategory
from api.chronometer import addChronometer
from api.event import addEvent
from api.location import addLocation
from api.pilots import addPilot, associatePilotBeacon
from api.location import addSessionToLocation
from api.event import addSessionToEvent
from api.session import addSession, addChronometerToSession, addPilotToSession
from api.pilots import deletePilots
from api.beacons import deleteBeacons, addBeacon
from api.chronometer import deleteChronometers
from api.event import deleteEvents
from api.category import deleteCategories
from api.session import deleteSessions
from api.location import deleteLocations

# Cleanup
deleteBeacons()
deletePilots()
deleteChronometers()
deleteSessions()
deleteEvents()
deleteCategories()
deleteLocations()

event = addEvent('Live tests')
chrono = addChronometer('Raspberry-0')
cleo = addPilot('Cleo', 'Pipart')

newbie = addCategory('Newbie')
addPilotToCategory(newbie['id'], cleo['id'])
garage = addLocation('Garage', True)
beacon = addBeacon(122)
associatePilotBeacon(cleo['id'], beacon['id'])

start = datetime(2018, 1, 1)
end = datetime(2019, 12, 31)
session = addSession('Test session', start, end, 'tt')
addSessionToLocation(garage['id'], session['id'])
addSessionToEvent(event['id'], session['id'])
addChronometerToSession(session['id'], chrono['id'])
addPilotToSession(session['id'], cleo['id'])
