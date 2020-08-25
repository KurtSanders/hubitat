/* lgk larry kahn kahn-st@lgk.com custom ct100 .. changes to stock device type

1. add some icons and colors heat color, cool color,
2. add operating mode tile ie idle heating etc
3. add color for humididity as well
4. get rid of sliders that worked like crap and added up down arrows for both heating and cooling,
and color based on temp for both.
5. add color for battery status.

v 2. some returns between indicators to move things down a line as recommended
v 3 reintegrate changes smarthigns made in setheatingsetpoint and setcoolingsetpoint functions apparently to get around bugs.
    these changes were made in the stock ct100 device type.
v 5 something they did in they new setheat and cool setpoints even in stock delays changing each temp 
to 30 secs before it registers changed this to 500 ms.
v 6 lgk. added status tile with last upddate time for current temp so you can see at a glance it is updating without checking logs.
note: there is a new input preferernce toffset which defaults to -5 and you must set yourself.
v 7 forgot to add the preference section added now.
v8 turn on extra debugging to figure out why direct sets are failing from routines
v9 fixed issue with routines not setting temps.. it was silently timing out with no error.. needed to up delay times
v 10 figured out how to do time without user input timezone and handles daylight saving correctly.
v 11 fix for change in how labels are handled.

even the stock one with a delay time would only set the heat setpoint from a routine (first call) and would timeout on the cool
one.. I upped the delays to 45 seconds and it seems to set both. Then i passed in a smaller delay from the toggle buttons so you
still can go up or down a temp in 1/2 sec.. otherwise it would only update every 30 or 45 secs etc.




*/

metadata {
	// Automatically generated. Make future change here.
	definition (name: "My CT100 Thermostat", namespace: "smartthings", author: "LGK Customized") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"
		command "heatLevelUp"
        command "heatLevelDown"
        command "coolLevelUp"
        command "coolLevelDown"
		attribute "thermostatFanState", "string"
        attribute "lastUpdate", "string"

       
		command "switchMode"
		command "switchFanMode"
		command "quickSetCool"
		command "quickSetHeat"

		fingerprint deviceId: "0x08", inClusters: "0x43,0x40,0x44,0x31,0x80,0x85,0x60"
	}


preferences {
  }

	// simulator metadata
	simulator {
		status "off"            : "command: 4003, payload: 00"
		status "heat"           : "command: 4003, payload: 01"
		status "cool"           : "command: 4003, payload: 02"
		status "auto"           : "command: 4003, payload: 03"
		status "emergencyHeat"  : "command: 4003, payload: 04"

		status "fanAuto"        : "command: 4403, payload: 00"
		status "fanOn"          : "command: 4403, payload: 01"
		status "fanCirculate"   : "command: 4403, payload: 06"

		status "heat 60"        : "command: 4303, payload: 01 09 3C"
		status "heat 72"        : "command: 4303, payload: 01 09 48"

		status "cool 76"        : "command: 4303, payload: 02 09 4C"
		status "cool 80"        : "command: 4303, payload: 02 09 50"

		status "temp 58"        : "command: 3105, payload: 01 2A 02 44"
		status "temp 62"        : "command: 3105, payload: 01 2A 02 6C"
		status "temp 78"        : "command: 3105, payload: 01 2A 03 0C"
		status "temp 86"        : "command: 3105, payload: 01 2A 03 34"

		status "idle"           : "command: 4203, payload: 00"
		status "heating"        : "command: 4203, payload: 01"
		status "cooling"        : "command: 4203, payload: 02"

		// reply messages
		reply "2502": "command: 2503, payload: FF"
	}

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}�',
                icon: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@2x.png",
				backgroundColors:[
					[value: 32, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
				]
			)
		}
        
		standardTile("mode", "device.thermostatMode", inactiveLabel: false) {
			state "off", label:'${name}', action:"switchMode", icon: "st.Outdoor.outdoor19" ,nextState:"to_heat"
			state "heat", label:'${name}', action:"switchMode", icon: "st.Weather.weather14", backgroundColor: '#E14902', nextState:"to_cool"
			state "cool", label:'${name}', action:"switchMode", icon: "st.Weather.weather7", backgroundColor: '#1e9cbb', nextState:"..."
			state "auto", label:'${name}', action:"switchMode", icon: "st.Weather.weather3", backgroundColor: '#44b621', nextState:"..."
			state "emergency heat", label:'${name}', action:"switchMode", nextState:"..."
			state "to_heat", label: "heat", action:"switchMode", nextState:"to_cool"
			state "to_cool", label: "cool", action:"switchMode", nextState:"..."
			state "...", label: "...", action:"off", nextState:"off"
		}
        
		standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false) {
			state "fanAuto", label:'${name}', action:"switchFanMode", icon: "st.Appliances.appliances11", backgroundColor: '#44b621'
			state "fanOn", label:'${name}', action:"switchFanMode", icon: "st.Appliances.appliances11", backgroundColor: '#44b621'
			state "fanCirculate", label:'${name}', action:"switchFanMode",  icon: "st.Appliances.appliances11", backgroundColor: '#44b621'
		}

   valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false) 
    	  {
          state "default", label:'Cool\n ${currentValue}�F', unit:"F",
           backgroundColors:
           [
            [value: 31, color: "#153591"],
            [value: 44, color: "#1e9cbb"],
            [value: 59, color: "#90d2a7"],
            [value: 74, color: "#44b621"],
            [value: 84, color: "#f1d801"],
            [value: 95, color: "#d04e00"],
            [value: 96, color: "#bc2323"]
          ]   
        }

     valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false) 
    	{
      state "default", label:'Heat\n ${currentValue}�F', unit:"F",
       backgroundColors:
       [
        [value: 31, color: "#153591"],
        [value: 44, color: "#1e9cbb"],
        [value: 59, color: "#90d2a7"],
        [value: 74, color: "#44b621"],
        [value: 84, color: "#f1d801"],
        [value: 95, color: "#d04e00"],
        [value: 96, color: "#bc2323"]
      ]   
    }
    
 
        standardTile("heatLevelUp", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "heatLevelUp", label:'  ', action:"heatLevelUp", icon:"st.thermostat.thermostat-up"
        }
        standardTile("heatLevelDown", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "heatLevelDown", label:'  ', action:"heatLevelDown", icon:"st.thermostat.thermostat-down"
        }
        standardTile("coolLevelUp", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "coolLevelUp", label:'  ', action:"coolLevelUp", icon:"st.thermostat.thermostat-up"
        }
        standardTile("coolLevelDown", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false) {
                        state "coolLevelDown", label:'  ', action:"coolLevelDown", icon:"st.thermostat.thermostat-down"
        }
        
        /*
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "setHeatingSetpoint", action:"quickSetHeat", backgroundColor:"#d04e00"
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}� heat', backgroundColor:"#ffffff"
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "setCoolingSetpoint", action:"quickSetCool", backgroundColor: "#1e9cbb"
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue}� cool', backgroundColor:"#ffffff"
		}
        */
        
		valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "humidity", label:'Humidity\n ${currentValue}%', unit:"",
              icon: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png",
               backgroundColors : [
                    [value: 01, color: "#724529"],
                    [value: 11, color: "#724529"],
                    [value: 21, color: "#724529"],
                    [value: 35, color: "#44b621"],
                    [value: 49, color: "#44b621"],
                    [value: 50, color: "#1e9cbb"]
         ]        
		}
        
		valueTile("battery", "device.battery", inactiveLabel: false) {
			state "battery", label:'Battery\n ${currentValue}%', unit:"",
             backgroundColors : [
                    [value: 20, color: "#720000"],
                    [value: 40, color: "#724529"],
                    [value: 60, color: "#00cccc"],
                    [value: 80, color: "#00b621"],
                    [value: 90, color: "#009c00"],
                    [value: 100, color: "#00ff00"]
             ]
		}
        
        valueTile("thermostatOperatingState", "device.thermostatOperatingState", inactiveLabel: false, decoration: "flat") {
			state "thermostatOperatingState", label:'${currentValue}', unit:""
		}
        
		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        valueTile("status", "device.lastUpdate", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Last Update: ${currentValue}'
		}
       main "temperature"
        details(["temperature", "mode", "fanMode",   
        "heatLevelUp", "heatingSetpoint" , "heatLevelDown", "coolLevelUp",
        "coolingSetpoint", "coolLevelDown",
        "humidity", "battery","thermostatOperatingState","status","refresh"])
        
		/*main "temperature"
		details(["temperature", "mode", "fanMode",
        "heatSliderControl", "heatingSetpoint", "coolSliderControl", "coolingSetpoint",
         "humidity", "battery","thermostatOperatingState","refresh"])*/
	}
}

def parse(String description)
{
 //log.debug "in parse desc = $description"
 
	def result = []
	if (description == "updated") {
	} else {
		def zwcmd = zwave.parse(description, [0x42:2, 0x43:2, 0x31: 2, 0x60: 3])
		if (zwcmd) {
			result += zwaveEvent(zwcmd)
		} else {
			log.debug "$device.displayName couldn't parse $description"
		}
	}
	if (!result) {
		return null
	}
	if (result.size() == 1 && (!state.lastbatt || now() - state.lastbatt > 48*60*60*1000)) {
		result << response(zwave.batteryV1.batteryGet().format())
	}
	//log.debug "$device.displayName parsed '$description' to $result"
	result
}

def zwaveEvent(hubitat.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def result = null
	def encapsulatedCommand = cmd.encapsulatedCommand([0x42:2, 0x43:2, 0x31: 2])
	log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")
	if (encapsulatedCommand) {
		result = zwaveEvent(encapsulatedCommand)
		if (cmd.sourceEndPoint == 1) {    // indicates a response to refresh() vs an unrequested update
			def event = ([] + result)[0]  // in case zwaveEvent returns a list
			def resp = nextRefreshQuery(event?.name)
			if (resp) {
				log.debug("sending next refresh query: $resp")
				result = [] + result + response(["delay 200", resp])
			}
		}
	}
	result
}

def zwaveEvent(hubitat.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd)
{
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def temp = convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision)
	def unit = getTemperatureScale()
	def map1 = [ value: temp, unit: unit, displayed: false ]
	switch (cmd.setpointType) {
		case 1:
			map1.name = "heatingSetpoint"
			break;
		case 2:
			map1.name = "coolingSetpoint"
			break;
		default:
			log.debug "unknown setpointType $cmd.setpointType"
			return
	}

	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision

	def mode = device.latestValue("thermostatMode")
	if (mode && map1.name.startsWith(mode) || (mode == "emergency heat" && map1.name == "heatingSetpoint")) {
		def map2 = [ name: "thermostatSetpoint", value: temp, unit: unit ]
		[ createEvent(map1), createEvent(map2) ]
      
	} else {
		createEvent(map1)
    
	}
}

def zwaveEvent(hubitat.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
log.debug "in sensor multilevel v2 cmd type = $cmd.sensorType";
	def map = [:]
	if (cmd.sensorType == 1) {
		map.name = "temperature"
		map.unit = getTemperatureScale()
		map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
        
        def now = new Date().format('MM/dd/yyyy h:mm a',location.timeZone)
        sendEvent(name: "lastUpdate", value: now, descriptionText: "Last Update: $now")

	} else if (cmd.sensorType == 5) {
		map.name = "humidity"
		map.unit = "%"
		map.value = cmd.scaledSensorValue
	}
	createEvent(map)
}

def zwaveEvent(hubitat.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport cmd)
{

	def map = [name: "thermostatOperatingState" ]
	switch (cmd.operatingState) {
		case hubitat.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
			map.value = "idle"
			break
		case hubitat.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
			map.value = "heating"
			break
		case hubitat.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_COOLING:
			map.value = "cooling"
			break
		case hubitat.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_FAN_ONLY:
			map.value = "fan only"
			break
		case hubitat.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_HEAT:
			map.value = "pending heat"
			break
		case hubitat.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_COOL:
			map.value = "pending cool"
			break
		case hubitat.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_VENT_ECONOMIZER:
			map.value = "vent economizer"
			break
	}
    
	def result = createEvent(map)
	if (result.isStateChange && device.latestValue("thermostatMode") == "auto" && (result.value == "heating" || result.value == "cooling")) {
		def thermostatSetpoint = device.latestValue("${result.value}Setpoint")
		result = [result, createEvent(name: "thermostatSetpoint", value: thermostatSetpoint, unit: getTemperatureScale())]
	}
	result
}

def zwaveEvent(hubitat.zwave.commands.thermostatfanstatev1.ThermostatFanStateReport cmd) {
	def map = [name: "thermostatFanState", unit: ""]
	switch (cmd.fanOperatingState) {
		case 0:
			map.value = "idle"
			break
		case 1:
			map.value = "running"
			break
		case 2:
			map.value = "running high"
			break
	}
	createEvent(map)
}

def zwaveEvent(hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def map = [name: "thermostatMode"]
	def thermostatSetpoint = null
	switch (cmd.mode) {
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
			map.value = "off"
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			map.value = "heat"
			thermostatSetpoint = device.latestValue("heatingSetpoint")
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUXILIARY_HEAT:
			map.value = "emergency heat"
			thermostatSetpoint = device.latestValue("heatingSetpoint")
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_COOL:
			map.value = "cool"
			thermostatSetpoint = device.latestValue("coolingSetpoint")
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUTO:
			map.value = "auto"
			def temp = device.latestValue("temperature")
			def heatingSetpoint = device.latestValue("heatingSetpoint")
			def coolingSetpoint = device.latestValue("coolingSetpoint")
			if (temp && heatingSetpoint && coolingSetpoint) {
				if (temp < (heatingSetpoint + coolingSetpoint) / 2.0) {
					thermostatSetpoint = heatingSetpoint
				} else {
					thermostatSetpoint = coolingSetpoint
				}
			}
			break
	}
	state.lastTriedMode = map.value
	if (thermostatSetpoint) {
		[ createEvent(map), createEvent(name: "thermostatSetpoint", value: thermostatSetpoint, unit: getTemperatureScale()) ]
	} else {
		createEvent(map)
	}
}

def zwaveEvent(hubitat.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport cmd) {
	def map = [name: "thermostatFanMode", displayed: false]
	switch (cmd.fanMode) {
		case hubitat.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_AUTO_LOW:
			map.value = "fanAuto"
			break
		case hubitat.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_LOW:
			map.value = "fanOn"
			break
		case hubitat.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_CIRCULATION:
			map.value = "fanCirculate"
			break
	}
	state.lastTriedFanMode = map.value
	createEvent(map)
}

def zwaveEvent(hubitat.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	def supportedModes = ""
	if(cmd.off) { supportedModes += "off " }
	if(cmd.heat) { supportedModes += "heat " }
	if(cmd.auxiliaryemergencyHeat) { supportedModes += "emergency heat " }
	if(cmd.cool) { supportedModes += "cool " }
	if(cmd.auto) { supportedModes += "auto " }

	state.supportedModes = supportedModes
	[ createEvent(name:"supportedModes", value: supportedModes, displayed: false),
	  response(zwave.thermostatFanModeV3.thermostatFanModeSupportedGet()) ]
}

def zwaveEvent(hubitat.zwave.commands.thermostatfanmodev3.ThermostatFanModeSupportedReport cmd) {
	def supportedFanModes = ""
	if(cmd.auto) { supportedFanModes += "fanAuto " }
	if(cmd.low) { supportedFanModes += "fanOn " }
	if(cmd.circulation) { supportedFanModes += "fanCirculate " }

	state.supportedFanModes = supportedFanModes
	[ createEvent(name:"supportedFanModes", value: supportedModes, displayed: false),
	  response(refresh()) ]
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "Zwave event received: $cmd"
}

def zwaveEvent(hubitat.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = now()
	createEvent(map)
}

def zwaveEvent(hubitat.zwave.Command cmd) {
	log.warn "Unexpected zwave command $cmd"
}

def refresh() {
	// Use encapsulation to differentiate refresh cmds from what the thermostat sends proactively on change
	def cmd = zwave.sensorMultilevelV2.sensorMultilevelGet()
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:1).encapsulate(cmd).format()
}

def nextRefreshQuery(name) {
	def cmd = null
	switch (name) {
		case "temperature":
			cmd = zwave.thermostatModeV2.thermostatModeGet()
			break
		case "thermostatMode":
			cmd = zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1)
			break
		case "heatingSetpoint":
			cmd = zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2)
			break
		case "coolingSetpoint":
			cmd = zwave.thermostatFanModeV3.thermostatFanModeGet()
			break
		case "thermostatFanMode":
			cmd = zwave.thermostatOperatingStateV2.thermostatOperatingStateGet()
			break
		case "thermostatOperatingState":
			// get humidity, multilevel sensor get to endpoint 2
			cmd = zwave.sensorMultilevelV2.sensorMultilevelGet()
			return zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:2).encapsulate(cmd).format()
		default: return null
	}
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:1).encapsulate(cmd).format()
}


def quickSetHeat(degrees) {
	setHeatingSetpoint(degrees, 500)
}

def setHeatingSetpoint(degrees, delay = 40000) {
	setHeatingSetpoint(degrees.toDouble(), delay)
}

def setHeatingSetpoint(Double degrees, Integer delay = 40000) {
	log.debug "setHeatingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
	def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

	def convertedDegrees
	if (locationScale == "C" && deviceScaleString == "F") {
		convertedDegrees = celsiusToFahrenheit(degrees)
	} else if (locationScale == "F" && deviceScaleString == "C") {
		convertedDegrees = fahrenheitToCelsius(degrees)
	} else {
		convertedDegrees = degrees
	}

  log.debug "calling thermostatSetpointV1.thermostatSetpointSet (heat type = 1) with degrees = $convertedDegrees delay = $delay"
	delayBetween([
		zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()
	], delay)
}

def quickSetCool(degrees) {
	setCoolingSetpoint(degrees, 500)
}

def setCoolingSetpoint(degrees, delay = 40000) {
	setCoolingSetpoint(degrees.toDouble(), delay)
}

def setCoolingSetpoint(Double degrees, Integer delay = 40000) {
	log.debug "setCoolingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
	def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

	def convertedDegrees
	if (locationScale == "C" && deviceScaleString == "F") {
		convertedDegrees = celsiusToFahrenheit(degrees)
	} else if (locationScale == "F" && deviceScaleString == "C") {
		convertedDegrees = fahrenheitToCelsius(degrees)
	} else {
		convertedDegrees = degrees
	}

   log.debug "calling thermostatSetpointV1.thermostatSetpointSet (cool type=2) with degrees = $convertedDegrees delay = $delay"
	delayBetween([
		zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 2, scale: deviceScale, precision: p,	 scaledValue: convertedDegrees).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format()
	], delay)
}

def configure() {


	delayBetween([
		zwave.thermostatModeV2.thermostatModeSupportedGet().format(),
	], 2300)
}

def modes() {
	["off", "heat", "cool", "auto", "emergency heat"]
}

def switchMode() {
	def currentMode = device.currentState("thermostatMode")?.value
	def lastTriedMode = state.lastTriedMode ?: currentMode ?: "off"
	def supportedModes = getDataByName("supportedModes")
	def modeOrder = modes()
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	if (supportedModes?.contains(currentMode)) {
		while (!supportedModes.contains(nextMode) && nextMode != "off") {
			nextMode = next(nextMode)
		}
	}
	state.lastTriedMode = nextMode
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[nextMode]).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], 1000)
}

def switchToMode(nextMode) {
	def supportedModes = getDataByName("supportedModes")
	if(supportedModes && !supportedModes.contains(nextMode)) log.warn "thermostat mode '$nextMode' is not supported"
	if (nextMode in modes()) {
		state.lastTriedMode = nextMode
		"$nextMode"()
	} else {
		log.debug("no mode method '$nextMode'")
	}
}

def switchFanMode() {
	def currentMode = device.currentState("thermostatFanMode")?.value
	def lastTriedMode = state.lastTriedFanMode ?: currentMode ?: "off"
	def supportedModes = getDataByName("supportedFanModes") ?: "fanAuto fanOn"
	def modeOrder = ["fanAuto", "fanCirculate", "fanOn"]
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	while (!supportedModes?.contains(nextMode) && nextMode != "fanAuto") {
		nextMode = next(nextMode)
	}
	switchToFanMode(nextMode)
}

def switchToFanMode(nextMode) {
	def supportedFanModes = getDataByName("supportedFanModes")
	if(supportedFanModes && !supportedFanModes.contains(nextMode)) log.warn "thermostat mode '$nextMode' is not supported"

	def returnCommand
	if (nextMode == "fanAuto") {
		returnCommand = fanAuto()
	} else if (nextMode == "fanOn") {
		returnCommand = fanOn()
	} else if (nextMode == "fanCirculate") {
		returnCommand = fanCirculate()
	} else {
		log.debug("no fan mode '$nextMode'")
	}
	if(returnCommand) state.lastTriedFanMode = nextMode
	returnCommand
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def getModeMap() { [
	"off": 0,
	"heat": 1,
	"cool": 2,
	"auto": 3,
	"emergency heat": 4
]}

def setThermostatMode(String value) {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[value]).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def getFanModeMap() { [
	"auto": 0,
	"on": 1,
	"circulate": 6
]}

def setThermostatFanMode(String value) {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: fanModeMap[value]).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

def off() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 0).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def heat() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 1).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def emergencyHeat() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 4).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def cool() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 2).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def auto() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 3).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def fanOn() {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 1).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

def fanAuto() {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 0).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

def fanCirculate() {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 6).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

private getStandardDelay() {
	1000
}


def coolLevelUp(){
    int nextLevel = device.currentValue("coolingSetpoint") + 1
    
    if( nextLevel > 99){
      nextLevel = 99
    }
    log.debug "Setting cool set point up to: ${nextLevel}"
    setCoolingSetpoint(nextLevel,500)
}

def coolLevelDown(){
    int nextLevel = device.currentValue("coolingSetpoint") - 1
    
    if( nextLevel < 50){
      nextLevel = 50
    }
    log.debug "Setting cool set point down to: ${nextLevel}"
    setCoolingSetpoint(nextLevel,500)
}

def heatLevelUp(){
    int nextLevel = device.currentValue("heatingSetpoint") + 1
    
    if( nextLevel > 90){
      nextLevel = 90
    }
    log.debug "Setting heat set point up to: ${nextLevel}"
    setHeatingSetpoint(nextLevel,500)
}

def heatLevelDown(){
    int nextLevel = device.currentValue("heatingSetpoint") - 1
    
    if( nextLevel < 40){
      nextLevel = 40
    }
    log.debug "Setting heat set point down to: ${nextLevel}"
    setHeatingSetpoint(nextLevel,500)
}

def input()
{
 
}


