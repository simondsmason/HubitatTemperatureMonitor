# Temperature Monitor for Hubitat

## Overview
This app monitors selected temperature sensors and sends notifications when temperatures go outside of specified ranges for a minimum duration. It is designed for the Hubitat platform.

## Features
- Monitors multiple temperature sensors
- Configurable minimum and maximum temperature thresholds
- Configurable delay before first out-of-range notification
- Configurable interval for repeat notifications
- Option to notify when temperature returns to normal
- Debug logging for troubleshooting

## Version History
- **v1.20** (July 8, 2025): Ensure `initialDelay` and `notifyInterval` are always initialized to defaults in `initialize()` to prevent null errors and missed notifications.
- **v1.19** (July 6, 2025): Added configurable delay before first out-of-range notification (default 30 minutes)
- See in-app code for full change history.

## Installation
1. Copy the contents of `TemperatureMonitor.groovy` to a new app in the Hubitat web interface.
2. Save and install the app.
3. Configure your sensors, thresholds, notification devices, and options.

## Usage
- The app will send notifications to your selected devices if any sensor goes out of range for the configured delay period.
- Repeat notifications will be sent at the configured interval if the temperature remains out of range.
- If enabled, a notification will be sent when the temperature returns to normal.

## Notes
- The README will be updated automatically for future changes.
- For more details, see the comments and change history in the Groovy source file.

## License
Copyright 2025 Simon Mason. All rights reserved. 