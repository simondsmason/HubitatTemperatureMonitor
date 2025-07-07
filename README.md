# Hubitat Temperature Monitor

A smart home automation app for Hubitat that monitors temperature sensors and sends intelligent notifications when temperatures go outside specified ranges.

## Features

- **Multi-sensor monitoring**: Monitor multiple temperature sensors simultaneously
- **Customizable ranges**: Set minimum and maximum temperature thresholds
- **Smart notifications**: Immediate alerts when temperatures go out of range, with configurable repeat intervals
- **Restore notifications**: Optional notifications when temperatures return to normal
- **Flexible notification options**: Send alerts to any Hubitat notification device (SMS, push notifications, etc.)
- **Debug logging**: Comprehensive logging for troubleshooting and monitoring

## Perfect for monitoring:
- Freezers and refrigerators
- Wine cellars and storage areas
- Server rooms and equipment
- Greenhouses and grow rooms
- Vacation homes and remote properties
- Any environment where temperature monitoring is critical

## Installation

1. Install the app in your Hubitat hub
2. Select your temperature sensors
3. Configure temperature ranges and notification preferences
4. Choose your notification devices
5. Save and start monitoring!

## Configuration Options

- **Temperature Sensors**: Select one or more temperature sensors to monitor
- **Temperature Range**: Set minimum and maximum temperature thresholds
- **Notification Devices**: Choose where to send alerts (SMS, push notifications, etc.)
- **Repeat Interval**: Configure how often to send repeat notifications (default: 60 minutes)
- **Restore Notifications**: Enable/disable notifications when temperature returns to normal
- **Debug Logging**: Enable detailed logging for troubleshooting

## Version History

- v1.18 - Added state cleanup to remove duplicate entries and fixed device ID consistency
- v1.17 - Fixed restore notification logic to use separate timestamp from out-of-range notifications
- v1.16 - Added 5-minute minimum interval for restore notifications to prevent duplicates
- v1.15 - Added device ID type debugging and string fallback lookup to handle type mismatches
- v1.14 - Added enhanced debugging to troubleshoot state migration issues
- v1.13 - Added state format migration to handle old state entries with different structure
- v1.12 - Simplified state handling and removed direct state manipulation to prevent errors
- v1.11 - Fixed state handling to prevent duplicate entries and ensure proper state persistence
- v1.10 - Added forced state persistence and enhanced state debugging
- v1.09 - Added additional state debugging to identify state persistence issues
- v1.08 - Added enhanced debug logging to troubleshoot repeat notification timing issues
- v1.07 - Changed to notify immediately when temperature goes out of range, then use specified interval for subsequent notifications
- v1.06 - Changed approach to notify only after temperature stays out of range for a minimum duration
- v1.05 - Simplified notification logic to ensure minimum interval is always enforced
- v1.04 - Fixed issue with notifications being sent with every device update
- v1.03 - Fixed notification timing to respect minimum notification interval
- v1.02 - Fixed duplicate notifications by correctly handling state updates
- v1.01 - Fixed issue with null device status in temperatureHandler
- v1.00 - Initial release

## Requirements

- Hubitat Elevation hub
- Temperature sensors with temperature measurement capability
- Notification devices (SMS, push notifications, etc.)

## License

Copyright 2025 Simon Mason

Built for Hubitat Elevation hub users who need reliable, customizable temperature monitoring with smart notification management. 