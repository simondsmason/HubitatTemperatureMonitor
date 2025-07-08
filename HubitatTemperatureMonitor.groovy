/**
 *  Temperature Monitor with Notifications
 *
 *  Description:
 *  This app monitors selected temperature sensors and sends notifications
 *  when temperatures go outside of specified ranges for a minimum duration.
 *
 *  Copyright 2025
 *
 *  Change History:
 *  v1.00 - Initial release
 *  v1.01 - Fixed issue with null device status in temperatureHandler
 *  v1.02 - Fixed duplicate notifications by correctly handling state updates
 *  v1.03 - Fixed notification timing to respect minimum notification interval
 *          regardless of temperature status (April 29, 2025)
 *  v1.04 - Fixed issue with notifications being sent with every device update
 *          by strictly enforcing minimum notification intervals (April 30, 2025)
 *  v1.05 - Simplified notification logic to ensure minimum interval is always 
 *          enforced regardless of temperature changes or device state (May 3, 2025)
 *  v1.06 - Changed approach to notify only after temperature stays out of range
 *          for a minimum duration, replacing immediate notification with delayed
 *          notification to reduce false alarms (May 4, 2025)
 *  v1.07 - Changed to notify immediately when temperature goes out of range,
 *          then use specified interval for subsequent notifications (May 5, 2025)
 *  v1.08 - Added enhanced debug logging to troubleshoot repeat notification timing issues (May 5, 2025)
 *  v1.09 - Added additional state debugging to identify state persistence issues (May 5, 2025)
 *  v1.10 - Added forced state persistence and enhanced state debugging (May 5, 2025)
 *  v1.11 - Fixed state handling to prevent duplicate entries and ensure proper state persistence (May 5, 2025)
 *  v1.12 - Simplified state handling and removed direct state manipulation to prevent errors (May 5, 2025)
 *  v1.13 - Added state format migration to handle old state entries with different structure (May 5, 2025)
 *  v1.14 - Added enhanced debugging to troubleshoot state migration issues (May 5, 2025)
 *  v1.15 - Added device ID type debugging and string fallback lookup to handle type mismatches (May 5, 2025)
 *  v1.16 - Added 5-minute minimum interval for restore notifications to prevent duplicates (May 5, 2025)
 *  v1.17 - Fixed restore notification logic to use separate timestamp from out-of-range notifications (May 5, 2025)
 *  v1.18 - Added state cleanup to remove duplicate entries and fixed device ID consistency (May 5, 2025)
 *  v1.19 - Added configurable delay before first out-of-range notification (default 30 minutes) (July 6, 2025)
 *  v1.20 - Ensure initialDelay and notifyInterval are always initialized to defaults in initialize() (July 8, 2025)
 *
 */

definition(
    name: "Temperature Monitor",
    namespace: "simonmason",
    author: "Simon Mason",
    description: "Monitors temperature sensors and sends notifications when outside of range for a minimum duration",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Temperature Monitor Settings", install: true, uninstall: true) {
        section("Select Temperature Sensors") {
            input "tempSensors", "capability.temperatureMeasurement", title: "Temperature Sensors", required: true, multiple: true
        }
        
        section("Temperature Range") {
            input "minTemp", "decimal", title: "Minimum Temperature (°F)", required: true
            input "maxTemp", "decimal", title: "Maximum Temperature (°F)", required: true
        }
        
        section("Notification Devices") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", required: true, multiple: true
        }
        
        section("Notification Options") {
            input "initialDelay", "number", title: "Delay before first notification (minutes)", defaultValue: 30, description: "How long temperature must be out of range before first notification"
            input "notifyInterval", "number", title: "Time between repeat notifications (minutes)", defaultValue: 60, description: "How often to send repeat notifications"
            input "notifyOnRestore", "bool", title: "Notify when temperature returns to normal range?", defaultValue: true
        }
        
        section("Name and Logging") {
            label title: "Assign a name", required: false
            input "logEnable", "bool", title: "Enable debug logging", defaultValue: false
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    if (logEnable) log.debug "Initializing Temperature Monitor"
    
    // Ensure initialDelay and notifyInterval are set to defaults if not provided
    if (initialDelay == null) {
        initialDelay = 30
        if (logEnable) log.debug "initialDelay not set, defaulting to 30 minutes"
    }
    if (notifyInterval == null) {
        notifyInterval = 60
        if (logEnable) log.debug "notifyInterval not set, defaulting to 60 minutes"
    }
    
    // Subscribe to temperature events from each sensor
    tempSensors.each { sensor ->
        subscribe(sensor, "temperature", temperatureHandler)
    }
    
    // Create state map to track temperature status
    if (!state.sensorStatus) {
        state.sensorStatus = [:]
    }
    
    // Clean up any duplicate entries in state
    def cleanedState = [:]
    state.sensorStatus.each { deviceId, status ->
        if (cleanedState.containsKey(deviceId)) {
            if (logEnable) log.debug "Removing duplicate entry for device ID: ${deviceId}"
        } else {
            cleanedState[deviceId] = status
        }
    }
    state.sensorStatus = cleanedState
    
    // Initialize sensor status
    tempSensors.each { sensor ->
        if (!state.sensorStatus.containsKey(sensor.id)) {
            state.sensorStatus[sensor.id] = [
                lastTemp: sensor.currentTemperature,
                inRange: isInRange(sensor.currentTemperature),
                outOfRangeStart: 0,
                lastNotification: 0,
                notified: false,
                lastRestoreNotification: 0
            ]
            if (logEnable) {
                log.debug "Initialized new sensor status for ${sensor.displayName}: ${state.sensorStatus[sensor.id]}"
            }
        } else {
            if (logEnable) {
                log.debug "Existing sensor status for ${sensor.displayName}: ${state.sensorStatus[sensor.id]}"
            }
        }
    }
    
    if (logEnable) log.debug "Initialization complete"
}

def isInRange(temperature) {
    return (temperature >= minTemp && temperature <= maxTemp)
}

def temperatureHandler(evt) {
    def deviceId = evt.deviceId
    def deviceName = evt.displayName
    def currentTemp = evt.value.toBigDecimal()
    def inRange = isInRange(currentTemp)
    def now = now()
    
    // Note: Sensor status initialization is now handled above
    
    // Get existing sensor status or create new one
    def sensorStatus = state.sensorStatus[deviceId]
    def wasInRange = false
    
    if (logEnable) {
        log.debug "Looking up device ID: ${deviceId} (type: ${deviceId.class.name})"
        log.debug "Available device IDs in state: ${state.sensorStatus.keySet()}"
    }
    
    if (logEnable) {
        log.debug "Temperature event: ${deviceName} (ID: ${deviceId}) is ${currentTemp}°F (Range: ${minTemp}°F to ${maxTemp}°F)"
        log.debug "Full state at start: ${state.sensorStatus}"
    }
    
    if (sensorStatus) {
        if (logEnable) {
            log.debug "Found existing sensor status for ${deviceName}: ${sensorStatus}"
        }
    } else {
        // Try looking up with string version of device ID
        def stringDeviceId = deviceId.toString()
        sensorStatus = state.sensorStatus[stringDeviceId]
        if (sensorStatus) {
            if (logEnable) {
                log.debug "Found existing sensor status using string device ID for ${deviceName}: ${sensorStatus}"
            }
        } else {
            if (logEnable) {
                log.debug "No existing sensor status found for ${deviceName} (tried both ${deviceId} and ${stringDeviceId})"
            }
        }
    }
    
    if (sensorStatus) {
        
        // Check if this is an old state format that needs migration
        if (sensorStatus.lastStatus != null) {
            if (logEnable) {
                log.debug "Found old state format for ${deviceName}, migrating to new format"
            }
            // Migrate old format to new format
            sensorStatus = [
                lastTemp: sensorStatus.lastTemp ?: currentTemp,
                inRange: inRange,
                outOfRangeStart: inRange ? 0 : now,
                lastNotification: sensorStatus.lastNotification ?: 0,
                notified: false,
                lastRestoreNotification: 0
            ]
            if (logEnable) {
                log.debug "Migrated to new format: ${sensorStatus}"
            }
        } else {
            // Use existing sensor status
            wasInRange = sensorStatus.inRange ?: false
            if (logEnable) {
                log.debug "Using existing sensor status for ${deviceName}: ${sensorStatus}"
            }
            // Update existing sensor status
            sensorStatus.lastTemp = currentTemp
            sensorStatus.inRange = inRange
        }
    } else {
        if (logEnable) {
            log.debug "No existing sensor status found for ${deviceName}, creating new one"
        }
        // Create new sensor status
        sensorStatus = [
            lastTemp: currentTemp,
            inRange: inRange,
            outOfRangeStart: inRange ? 0 : now,
            lastNotification: 0,
            notified: false,
            lastRestoreNotification: 0
        ]
        if (logEnable) {
            log.debug "Created new sensor status for ${deviceName}: ${sensorStatus}"
        }
    }
    
    if (logEnable) {
        log.debug "In range: ${inRange}, Was in range: ${wasInRange}"
    }
    
    // Handle temperature going out of range
    if (wasInRange && !inRange) {
        // Temperature just went out of range - start tracking but don't notify immediately
        sensorStatus.outOfRangeStart = now
        sensorStatus.notified = false
        
        if (logEnable) {
            log.debug "Temperature went out of range. Starting ${initialDelay}-minute delay timer."
        }
    }
    // Handle temperature returning to range
    else if (!wasInRange && inRange) {
        // Temperature returned to normal
        sensorStatus.outOfRangeStart = 0
        sensorStatus.notified = false
        
        if (notifyOnRestore) {
            // Check if we've already sent a restore notification recently (within 5 minutes)
            def timeSinceLastRestore = now - (sensorStatus.lastRestoreNotification ?: 0)
            def minRestoreInterval = 5 * 60 * 1000 // 5 minutes in milliseconds
            
            if (timeSinceLastRestore >= minRestoreInterval) {
                def message = "Temperature Restored: ${deviceName} has returned to normal range at ${currentTemp}°F"
                sendNotification(message)
                sensorStatus.lastRestoreNotification = now
                if (logEnable) log.debug "Sent temperature restored notification"
            } else {
                if (logEnable) log.debug "Skipped restore notification (sent recently: ${timeSinceLastRestore / 60000} minutes ago)"
            }
        }
        if (logEnable) log.debug "Temperature returned to normal range"
    }
    // Handle temperature staying in range (no action needed)
    else if (wasInRange && inRange) {
        if (logEnable) log.debug "Temperature staying in normal range"
    }
    // Handle temperature still out of range
    else if (!inRange && sensorStatus.outOfRangeStart > 0) {
        def initialDelayMillis = initialDelay * 60 * 1000
        def intervalMillis = notifyInterval * 60 * 1000
        def timeSinceOutOfRange = now - sensorStatus.outOfRangeStart
        def timeSinceLastNotification = now - sensorStatus.lastNotification
        
        if (logEnable) {
            log.debug "Temperature still out of range. Checking for notifications."
            log.debug "Time since out of range: ${timeSinceOutOfRange / 60000} minutes"
            log.debug "Initial delay required: ${initialDelay} minutes"
            log.debug "Time since last notification: ${timeSinceLastNotification / 60000} minutes"
            log.debug "Repeat interval: ${notifyInterval} minutes"
        }
        
        // Check if we should send first notification (after initial delay)
        if (!sensorStatus.notified && timeSinceOutOfRange >= initialDelayMillis) {
            def message = ""
            if (currentTemp < minTemp) {
                message = "Temperature Alert: ${deviceName} is too cold at ${currentTemp}°F (minimum: ${minTemp}°F)"
            } else {
                message = "Temperature Alert: ${deviceName} is too hot at ${currentTemp}°F (maximum: ${maxTemp}°F)"
            }
            
            sendNotification(message)
            sensorStatus.lastNotification = now
            sensorStatus.notified = true
            
            if (logEnable) {
                log.debug "Sent first notification after ${initialDelay}-minute delay: ${message}"
            }
        }
        // Check if we should send repeat notification
        else if (sensorStatus.notified && timeSinceLastNotification >= intervalMillis) {
            def message = ""
            if (currentTemp < minTemp) {
                message = "Temperature Alert: ${deviceName} is still too cold at ${currentTemp}°F (minimum: ${minTemp}°F)"
            } else {
                message = "Temperature Alert: ${deviceName} is still too hot at ${currentTemp}°F (maximum: ${maxTemp}°F)"
            }
            
            sendNotification(message)
            sensorStatus.lastNotification = now
            
            if (logEnable) {
                log.debug "Sent repeat notification: ${message}"
            }
        } else {
            if (logEnable) {
                if (!sensorStatus.notified) {
                    log.debug "Waiting for initial delay to complete (${(initialDelayMillis - timeSinceOutOfRange) / 60000} minutes remaining)"
                } else {
                    log.debug "Not enough time has passed for repeat notification"
                }
            }
        }
    }
    
    // Save updated sensor status - ensure we use the correct device ID
    def finalDeviceId = deviceId
    if (sensorStatus) {
        // If we found the sensor status using string lookup, use string device ID for saving
        if (state.sensorStatus.containsKey(deviceId.toString()) && !state.sensorStatus.containsKey(deviceId)) {
            finalDeviceId = deviceId.toString()
            if (logEnable) log.debug "Using string device ID for saving: ${finalDeviceId}"
        }
    }
    
    state.sensorStatus[finalDeviceId] = sensorStatus
    
    if (logEnable) {
        log.debug "Saved sensor status to state: ${state.sensorStatus[finalDeviceId]}"
        log.debug "Full state after save: ${state.sensorStatus}"
    }
}

def sendNotification(message) {
    // Send notification to all configured notification devices
    notificationDevices.each { device ->
        device.deviceNotification(message)
        
        if (logEnable) {
            log.debug "Sent notification to ${device.displayName}: ${message}"
        }
    }
} 