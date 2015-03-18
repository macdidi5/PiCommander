package net.macdidi5.picommander;

import java.io.Serializable;

public class CommanderItem implements Serializable {

    private String gpioName;
    private boolean status;
    private String desc;
    private String highDesc;
    private String lowDesc;

    private static final String NA = "NA";

    public CommanderItem(String gpioName, boolean status, String desc,
                         String highDesc, String lowDesc) {
        this.gpioName = gpioName;
        this.status = status;
        this.desc = desc;
        this.setHighDesc(highDesc);
        this.setLowDesc(lowDesc);
    }

    public CommanderItem(String gpioName, boolean status, String desc) {
        this(gpioName, status, desc, NA, NA);
    }

    public CommanderItem(String gpioName, String desc,
                         String highDesc, String lowDesc) {
        this(gpioName, false, desc, NA, NA);
    }

    public CommanderItem(String gpioName, String desc) {
        this(gpioName, false, desc);
    }

    public CommanderItem() {
        this("", false, "", NA, NA);
    }

    public String getGpioName() {
        return gpioName;
    }

    public void setGpioName(String gpioName) {
        this.gpioName = gpioName;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public String getHighDesc() {
        return highDesc;
    }

    public void setHighDesc(String highDesc) {
        this.highDesc = highDesc;
    }

    public String getLowDesc() {
        return lowDesc;
    }

    public void setLowDesc(String lowDesc) {
        this.lowDesc = lowDesc;
    }
}
