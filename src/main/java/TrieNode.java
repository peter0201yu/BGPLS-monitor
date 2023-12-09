class TrieNode {
    private final String bitString;
    private final int level;
    private final TrieNode[] children;
    private boolean isPrefix;
    private String prefix;

    public TrieNode(String _bitString, int _level) {
        bitString = _bitString;
        level = _level;
        children = new TrieNode[2]; // 0 and 1 bits for binary representation
        isPrefix = false;
        prefix = "";
    }

    public boolean containsKey(int bit) {
        return children[bit] != null;
    }

    public TrieNode get(int bit) {
        return children[bit];
    }

    public String getBitString(){
        return bitString;
    }

    public void put(int bit, TrieNode node) {
        children[bit] = node;
    }

    // recursively remove child and sub-children
    public void remove(int bit) {
        if (children[bit].containsKey(0)){
            children[bit].remove(0);
        }
        if (children[bit].containsKey(1)){
            children[bit].remove(1);
        }
        children[bit] = null;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isPrefix() {
        return isPrefix;
    }
    public void setPrefix(String _prefix, boolean _isPrefix) {
        prefix = _prefix;
        isPrefix = _isPrefix;
    }

    // For debugging: compute the prefix string for any node
    public String computePrefixString(){
        StringBuilder prefix = new StringBuilder();
        String fullBitString = bitString + "0".repeat(32 - level);
        for (int i = 0; i + 7 < 32; i += 8){
            int decimalValue = Integer.parseInt(fullBitString.substring(i, i+8), 2);
            prefix.append(Integer.toString(decimalValue)).append(".");
        }
        prefix.deleteCharAt(prefix.length()-1); // remove last dot
        prefix.append("/").append(Integer.toString(level));
        return prefix.toString();
    }
}
