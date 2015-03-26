package net.macdidi5.picommander;

public enum McpGpioExpander {

    MCP23008("MCP23008"), MCP23017("MCP23017");

    private final String name;

    private McpGpioExpander(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Get McpGpioExpander member by name
     *
     * @param name McpGpioExpander member name
     * @return McpGpioExpander member
     */
    public static McpGpioExpander fromString(String name) {
        McpGpioExpander result = null;

        if (name != null) {
            for (McpGpioExpander m : McpGpioExpander.values()) {
                if (name.equalsIgnoreCase(m.name)) {
                    result = m;
                }
            }
        }

        return result;
    }
    
}
