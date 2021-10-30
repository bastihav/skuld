package radix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.skuld.prng.ImplementedPRNGs;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.memory.MemoryRadixTrie;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class RadixTrieTest {

  @Test
  public void testInsertion() {
    MemoryRadixTrie trie = new MemoryRadixTrie();

    byte[] randomness = new byte[32];
    for (int i = 0; i < 32; i++) {
      randomness[i] = 0;
    }

    RandomnessRadixTrieData data = new RandomnessRadixTrieData(ImplementedPRNGs.JAVA_RANDOM, 0,
        42);

    byte[] randomness2 = new byte[32];
    for (int i = 0; i < 16; i++) {
      randomness2[i] = 0;
    }
    for (int i = 16; i < 32; i++) {
      randomness2[i] = 5;
    }

    RandomnessRadixTrieData data2 = new RandomnessRadixTrieData(ImplementedPRNGs.JAVA_RANDOM, 1,
        421);

    trie.add(data, randomness);

    assertTrue(trie.contains(randomness));

    System.out.println(Arrays.toString(trie.getNode(randomness).get().getParentEdge().getLabel()));

    trie.add(data2, randomness2);

    System.out.println("outgoing edges " + trie.getRoot().getOutgoingEdges());

    assertTrue(trie.contains(randomness));
    assertTrue(trie.contains(randomness2));

    assertEquals(1, trie.getRoot().getOutgoingEdges().size());
    assertEquals(2, trie.getRoot().getOutgoingEdges().stream().findFirst().get().getChild().getOutgoingEdges().size());

    System.out.println(Arrays.toString(trie.getNode(randomness2).get().getParentEdge().getLabel()));
  }

}
