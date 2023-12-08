class PrefixTrie {
    private final TrieNode root;

    public PrefixTrie() {
        root = new TrieNode(0);
    }

    public void insert(String prefix) {
        String[] parts = prefix.split("/");
        String ipAddress = parts[0];
        int length = Integer.parseInt(parts[1]);
        String[] octets = ipAddress.split("\\.");

        TrieNode node = root;
        for (int i = 0; i < length; i++) {
            // get ith bit
            int octetIndex = i / 8;
            int bitIndex = i % 8;
            int octet = Integer.parseInt(octets[octetIndex]);
            int bit = (octet >> (7 - bitIndex)) & 1;
            if (!node.containsKey(bit)) {
                node.put(bit, new TrieNode(i+1));
            }
            node = node.get(bit);
        }
        node.setEnd();
    }

    public TrieNode longestMatch(String prefix) {
        String[] parts = prefix.split("/");
        String ipAddress = parts[0];
        int length = Integer.parseInt(parts[1]);
        String[] octets = ipAddress.split("\\.");

        TrieNode node = root;
        TrieNode longestMatch = null;
        for (int i = 0; i < length; i++) {
            int octetIndex = i / 8;
            int bitIndex = i % 8;
            int octet = Integer.parseInt(octets[octetIndex]);
            int bit = (octet >> (7 - bitIndex)) & 1;
            if (node.containsKey(bit)) {
                node = node.get(bit);
                longestMatch = node; // Update the last matching node
            } else {
                break;
            }
        }
        return longestMatch;
    }
}

