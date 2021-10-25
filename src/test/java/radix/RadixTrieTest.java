package radix;

import static org.junit.Assert.assertTrue;

import de.skuld.prng.ImplementedPRNGs;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.memory.MemoryRadixTrie;
import de.skuld.radix.memory.MemoryRadixTrieNode;
import org.junit.Test;

public class RadixTrieTest {

  @Test
  public void testInsertion() {
    MemoryRadixTrie trie = new MemoryRadixTrie();

    byte[] randomness = new byte[32];
    for (int i = 0; i < 32; i++) {
      randomness[i] = 0;
    }

    RandomnessRadixTrieData data = new RandomnessRadixTrieData(randomness, ImplementedPRNGs.JAVA_RANDOM, 0,
        42);

    byte[] randomness2 = new byte[32];
    for (int i = 0; i < 16; i++) {
      randomness2[i] = 0;
    }
    for (int i = 16; i < 32; i++) {
      randomness2[i] = 5;
    }

    RandomnessRadixTrieData data2 = new RandomnessRadixTrieData(randomness2, ImplementedPRNGs.JAVA_RANDOM, 1,
        421);

    trie.add(data);

    assertTrue(trie.contains(data));

    System.out.println(trie.getNode(data).get().getParentEdge().getLabel());

    trie.add(data2);

    assertTrue(trie.contains(data2));

    System.out.println(trie.getNode(data2).get().getParentEdge().getLabel());

    System.out.println(trie.getRoot().getOutgoingEdges());
  }

}
