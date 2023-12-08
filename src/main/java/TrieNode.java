class TrieNode {
    private final int level;
    private final TrieNode[] children;
    private boolean isEnd;

    public TrieNode(int _level) {
        level = _level;
        children = new TrieNode[2]; // 0 and 1 bits for binary representation
        isEnd = false;
    }

    public boolean containsKey(int bit) {
        return children[bit] != null;
    }

    public TrieNode get(int bit) {
        return children[bit];
    }

    public void put(int bit, TrieNode node) {
        children[bit] = node;
    }

    public void setEnd() {
        isEnd = true;
    }

    public boolean isEnd() {
        return isEnd;
    }
}
