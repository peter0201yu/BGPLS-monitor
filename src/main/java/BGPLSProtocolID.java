public enum BGPLSProtocolID {
    ISIS_LEVEL_1(1),
    ISIS_LEVEL_2(2),
    OSPFv2(3),
    Direct(4),
    Static_Configuration(5),
    OSPFv3(60);

    int numVal;

    BGPLSProtocolID(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }

    public static BGPLSProtocolID valueOf(int val) {
        for (BGPLSProtocolID protocol : values()) {
            if (protocol.getNumVal() == val) {
                return protocol;
            }
        }
        throw new IllegalArgumentException(String.valueOf(val));
    }


}
