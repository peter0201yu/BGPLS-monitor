import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PrefixTrieTest {

    @Test
    public void testEmptyTrie() {
        PrefixTrie trie = new PrefixTrie();
        assertEquals(trie.longestMatch("192.168.0.0/16"), "");
    }

    @Test
    public void testInsertAndMatch() {
        PrefixTrie trie = new PrefixTrie();
        trie.insert("192.168.0.1/16");
        trie.insert("192.168.0.2/24");
        trie.insert("192.168.0.3/32");
        assertEquals("192.168.0.1/16", trie.longestMatch("192.168.0.0/16"));
        assertEquals("192.168.0.1/16", trie.longestMatch("192.168.0.2/20"));
        assertEquals("192.168.0.2/24", trie.longestMatch("192.168.0.3/24"));
        assertEquals("192.168.0.2/24", trie.longestMatch("192.168.0.3/30"));
        assertEquals("192.168.0.3/32", trie.longestMatch("192.168.0.3/32"));
        assertEquals("192.168.0.2/24", trie.longestMatch("192.168.0.4/32"));
    }

    @Test
    public void testDeleteAndMatch() {
        PrefixTrie trie = new PrefixTrie();
        trie.insert("192.168.0.1/16");
        trie.insert("192.168.0.2/24");
        trie.insert("192.168.0.3/32");
        assertEquals("192.168.0.2/24", trie.longestMatch("192.168.0.3/24"));
        trie.delete("192.168.0.2/24");
        assertEquals("192.168.0.1/16", trie.longestMatch("192.168.0.3/24"));
        assertEquals("192.168.0.3/32", trie.longestMatch("192.168.0.3/32"));
        assertEquals("192.168.0.1/16", trie.longestMatch("192.168.0.4/32"));
    }
}

