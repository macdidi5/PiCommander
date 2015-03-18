package picommanderservice;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.RaspiPin;
import java.util.HashMap;
import java.util.Map;

public class GpioPi {
    
    private final GpioController gpio;
    private final Map<String, GpioPinDigitalMultipurpose> pins;
    
    public GpioPi(GpioController gpio) {
        this.gpio = gpio;
        pins = new HashMap<>();
    }
    
    public GpioPinDigitalMultipurpose getPin(String pinName) {    
        GpioPinDigitalMultipurpose result = pins.get(pinName);
        
        if (result == null) {
            Pin pin = RaspiPin.getPinByName(pinName);
            
            if (pin != null) {
                result = gpio.provisionDigitalMultipurposePin(
                        pin, PinMode.DIGITAL_OUTPUT);
                pins.put(result.getName(), result);
            }
        }
                
        return result;
    }
    
}
