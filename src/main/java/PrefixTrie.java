import java.util.*;
class PrefixTrie {
    private final TrieNode root;

    public PrefixTrie() {
        root = new TrieNode("", 0);
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
                node.put(bit, new TrieNode(node.getBitString() + Integer.toString(bit), i+1));
            }
            node = node.get(bit);
        }
        node.setPrefix(prefix, true);
    }

    public void delete(String prefix) {
        String[] parts = prefix.split("/");
        String ipAddress = parts[0];
        int length = Integer.parseInt(parts[1]);
        String[] octets = ipAddress.split("\\.");

        TrieNode node = root;
        List<TrieNode> nodesVisited = new ArrayList<>(); // To track visited nodes
        for (int i = 0; i < length; i++) {
            int octetIndex = i / 8;
            int bitIndex = i % 8;
            int octet = Integer.parseInt(octets[octetIndex]);
            int bit = (octet >> (7 - bitIndex)) & 1;
            if (node.containsKey(bit)) {
                if (node.isPrefix()) {
                    nodesVisited.add(node);
                }
                node = node.get(bit);
            } else {
                // Prefix not found in the Trie
                return;
            }
        }

        if (node.isPrefix()){
            node.setPrefix("", false); // Mark the node as not a prefix
            if ((!node.containsKey(0)) && (!node.containsKey(1)) && !nodesVisited.isEmpty()) {
                // If the node has no children and is not the root, remove the node
                TrieNode parentNode = nodesVisited.get(nodesVisited.size() - 1);
                char nextBit = node.getBitString().charAt(parentNode.getBitString().length());
                parentNode.remove(nextBit-'0');
            }
        }
    }

    public String longestMatch(String prefix) {
        String[] parts = prefix.split("/");
        String ipAddress = parts[0];
        int length = Integer.parseInt(parts[1]);
        String[] octets = ipAddress.split("\\.");

        TrieNode node = root;
        TrieNode longestMatch = node;
        for (int i = 0; i < length; i++) {
            int octetIndex = i / 8;
            int bitIndex = i % 8;
            int octet = Integer.parseInt(octets[octetIndex]);
            int bit = (octet >> (7 - bitIndex)) & 1;
            if (node.containsKey(bit)) {
                node = node.get(bit);
                if (node.isPrefix()) {
                    // Update the longest matching node only when the node is an existing prefix
                    longestMatch = node;
                }
            } else {
                break;
            }
        }
        return longestMatch.getPrefix();
    }
}

