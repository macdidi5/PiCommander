package picommanderservice;

import com.pi4j.gpio.extension.mcp.MCP23008GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23008Pin;
import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.i2c.I2CBus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GpioExpander {

    private final GpioController gpio;
    private final Map<String, GpioProvider> mcpProviders;
    private final Map<String, GpioPinDigitalMultipurpose> pins;
    
    public GpioExpander(GpioController gpio) {
        this.gpio = gpio;
        mcpProviders = new HashMap<>();
        pins = new HashMap<>();
    }

    public GpioPinDigitalMultipurpose getPin(int address, 
            McpGpioExpander type, String pinName) {    
        GpioPinDigitalMultipurpose result = null;
        
        String key = address + "," + type.toString();
        GpioProvider provider = mcpProviders.get(key);
        
        if (provider == null) {
            provider = getMcpProvier(address, type);
            mcpProviders.put(key, provider);
        }
        
        if (provider != null) {
            String keyPin = key + "," + pinName;
            result = pins.get(keyPin);

            if (result == null) {
                Pin pin = null;

                if (type == McpGpioExpander.MCP23008) {
                    pin = getMCP23008PinByName(pinName);
                }
                else if (type == McpGpioExpander.MCP23017) {
                    pin = getMCP23017PinByName(pinName);
                }

                if (pin != null) {
                    result = gpio.provisionDigitalMultipurposePin(
                            provider, pin, PinMode.DIGITAL_OUTPUT);
                    pins.put(keyPin, result);
                }
            }
        }
        
        return result;
    }
    
    private GpioProvider getMcpProvier(int address, McpGpioExpander type) {
        GpioProvider result = null;
        
        if (type == McpGpioExpander.MCP23008) {
            result = getMCP23008(I2CBus.BUS_1, address);
        }
        else if (type == McpGpioExpander.MCP23017) {
            result = getMCP23017(I2CBus.BUS_1, address);
        }
        
        return result;
    }
    
    private Pin getMCP23008PinByName(String pinName) {
        Pin result = null;
        
        Pin[] mcp23008pins = MCP23008Pin.ALL;
        
        for (Pin pin : mcp23008pins) {
            if (pin.getName().equals(pinName)) {
                result = pin;
            }
        }
        
        return result;
    }
    
    
    private Pin getMCP23017PinByName(String pinName) {
        Pin result = null;
        
        Pin[] mcp23017pins = MCP23017Pin.ALL;
        
        for (Pin pin : mcp23017pins) {
            if (pin.getName().equals(pinName)) {
                result = pin;
            }
        }
        
        return result;
    }    
    
    private MCP23008GpioProvider getMCP23008(int bus, int address) {
        MCP23008GpioProvider result = null;
        
        try {
            result = new MCP23008GpioProvider(bus, address);
        }
        catch (IOException e) {
            System.out.println("============ " + e.toString());
        }
        
        return result;
    }
    
    private MCP23017GpioProvider getMCP23017(int bus, int address) {
        MCP23017GpioProvider result = null;
        
        try {
            result = new MCP23017GpioProvider(bus, address);
        }
        catch (IOException e) {
            System.out.println("============ " + e.toString());
        }
        
        return result;
    }

}
