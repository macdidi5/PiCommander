package net.macdidi5.picommander;

public class ExpanderCommanderItem extends CommanderItem {

    private int address;
    private McpGpioExpander type;

    public ExpanderCommanderItem(String gpioName, boolean status,
                                 String desc, String highDesc, String lowDesc,
                                 int address, McpGpioExpander type) {
        super(gpioName, status, desc, highDesc, lowDesc);
        this.setAddress(address);
        this.setType(type);
    }

    public ExpanderCommanderItem(String gpioName, String desc,
                                 String highDesc, String lowDesc,
                                 int address, McpGpioExpander type) {
        super(gpioName, desc, highDesc, lowDesc);
        this.setAddress(address);
        this.setType(type);
    }

    public ExpanderCommanderItem(String gpioName, String desc,
                                 int address, McpGpioExpander type) {
        super(gpioName, desc);
        this.setAddress(address);
        this.setType(type);
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public McpGpioExpander getType() {
        return type;
    }

    public void setType(McpGpioExpander type) {
        this.type = type;
    }

}
