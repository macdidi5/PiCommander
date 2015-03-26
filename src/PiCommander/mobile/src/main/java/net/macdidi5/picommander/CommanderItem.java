package net.macdidi5.picommander;

import java.io.Serializable;

public class CommanderItem implements Serializable {

    private String gpioName;
    private boolean status;
    private String desc;
    private String highDesc;
    private String lowDesc;
    private String commandType;
    private boolean highNotify;
    private boolean lowNotify;

    private static final String NA = "NA";

    public CommanderItem(String gpioName, boolean status, String desc,
                         String highDesc, String lowDesc,
                         String commandType, boolean highNotify, boolean lowNotify) {
        this.gpioName = gpioName;
        this.status = status;
        this.desc = desc;
        this.setHighDesc(highDesc);
        this.setLowDesc(lowDesc);
        this.setCommandType(commandType);
        this.setHighNotify(highNotify);
        this.setLowNotify(lowNotify);
    }

    public CommanderItem(String gpioName, String desc,
                         String commandType) {
        this(gpioName, false, desc, NA, NA, commandType, false, false);
    }

    public CommanderItem(String gpioName, String desc,
                         String highDesc, String lowDesc,
                         String commandType, boolean highNotify,
                         boolean lowNotify) {
        this(gpioName, false, desc, highDesc, lowDesc, commandType,
                highNotify, lowNotify);
    }

    public CommanderItem(String gpioName, boolean status, String desc,
                         String highDesc, String lowDesc) {
        this(gpioName, status, desc, highDesc, lowDesc, NA, false, false);
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

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }


    public boolean isHighNotify() {
        return highNotify;
    }

    public void setHighNotify(boolean highNotify) {
        this.highNotify = highNotify;
    }

    public boolean isLowNotify() {
        return lowNotify;
    }

    public void setLowNotify(boolean lowNotify) {
        this.lowNotify = lowNotify;
    }
}
