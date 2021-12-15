package de.skuld.radix;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.BaseEncoding;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.radix.disk.DiskBasedRadixTrieNode;
import de.skuld.radix.disk.DiskBasedRandomnessRadixTrieData;
import de.skuld.radix.memory.MemoryRadixTrieNode;
import de.skuld.radix.memory.StringRadixTrieEdge;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Optional;
import java.util.Stack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractRadixTrie<D extends AbstractRadixTrieData<I, P>, P, I, N extends RadixTrieNode<D, E>, E extends AbstractRadixTrieEdge<D, N>> implements RadixTrie<D, P, I, N, E> {
  protected N root = getDummyNode();
  public BiMap<Integer, Long> getSeedMap() {
    return seedMap;
  }
  protected final BiMap<Integer, Long> seedMap = HashBiMap.create();
  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public @NotNull N getRoot() {
    return root;
  }

  @Override
  public boolean add(@NotNull D data, @NotNull I indexingData) {
    return add(root, data, indexingData);
  }

  @Override
  public boolean add(@NotNull N parent, @NotNull D data, @NotNull I indexingData) {
    String[] edges = data.toLabels(indexingData);
    String[] coveredEdges = parent.getPathFromRoot();

    //System.out.println("trying to add " + Arrays.toString(edges) + " under " + parent);
    //System.out.println("covered: " + Arrays.toString(coveredEdges));

    if (parent.isLeafNode() && parent != root) {
      //System.out.println("merging ths now");

      return parent.mergeNodes(data);
    }



    String[] remainingEdges = Arrays.copyOfRange(edges, coveredEdges.length, edges.length);

    Optional<E> edge = parent.getOutgoingEdge(remainingEdges[0]);

    //System.out.println(edge);
    if (edge.isPresent()) {
      return this.add(edge.get().getChild(), data, indexingData);
    }

      for (E e : parent.getOutgoingEdges()) {
        if (e.isSummary()) {
          // walk this edge
          if (e.queryIncludesEdge(remainingEdges)) {
            return this.add(e.getChild(), data, indexingData);
          }

          int size = 0;
          for (int i = 1; i <= remainingEdges.length; i++) {
            if (e.edgeIncludesQuery(Arrays.copyOfRange(remainingEdges, 0, i))) {
              size = i;
            } else {
              break;
            }
          }

          if (size > 0) {
            // merge nodes
            if (size == remainingEdges.length) {
              return this.add(e.getChild(), data, indexingData);
            } else {
              // split edge
              //System.out.println("in split case!");
              E firstEdge = this.createEdge(Arrays.copyOfRange(remainingEdges, 0, size), parent);
              firstEdge.setSummary(size > 1);
              firstEdge.setAmountOfSummarizedElements(size);

              N middleNode = createNode(null, firstEdge);

              E secondEdge = this
                  .createEdge(Arrays.copyOfRange(e.getLabel(), size, e.getLabel().length),
                      middleNode);
              secondEdge.setSummary(e.amountOfSummarizedElements() - size > 1);
              secondEdge.setAmountOfSummarizedElements(e.amountOfSummarizedElements() - size);

              N oldSubtree = e.getChild();

              moveSubtree(oldSubtree, secondEdge, middleNode);

              return this.add(middleNode, data, indexingData);
            }
          }

        }
      }

    // TODO config max depth
    if (parent.getEdgesFromRoot().size() < 3) {
      // last case: we branch off of here

      // todo: config
      boolean summarizeEdges = false;

      E newEdge;
      if (summarizeEdges) {
        newEdge = this.createEdge(remainingEdges, parent);
        newEdge.setSummary(remainingEdges.length > 1);
        newEdge.setAmountOfSummarizedElements(remainingEdges.length);
      } else {
        newEdge = this.createEdge(new String[]{remainingEdges[0]}, parent);
        newEdge.setSummary(false);
        newEdge.setAmountOfSummarizedElements(1);
      }

      N newNode = this.createNode(null, newEdge);

      return this.add(newNode, data, indexingData);
    } else {
      //System.out.println("apparently adding " + data + " with " + Arrays.toString((byte[]) indexingData) + " under " + parent);
      //System.out.println("covered: " + Arrays.toString(coveredEdges));
      Collection<P> dataPoints = data.getDataPoints();
      if (dataPoints.size() > 0 && dataPoints.stream().findFirst()
          .get() instanceof RandomnessRadixTrieDataPoint) {
        //System.out.println("adding the remaining edges ");
        Collection<RandomnessRadixTrieDataPoint> dataPointCollection = (Collection<RandomnessRadixTrieDataPoint>) dataPoints;

        dataPointCollection.forEach(dp -> dp.removePrefixFromRemainingIndexingData(coveredEdges.length));
      }

      // merge instead
      Optional<E> table = parent.getOutgoingEdges().stream().findFirst();
      if (table.isPresent()) {
        //System.out.println("adding under first child! ");
        return this.add(table.get().getChild(), data, indexingData);
      }

      // todo: config
      boolean summarizeEdges = false;

      N leafNode = this.createNode(data, parent.getParentEdge());

      /*if (LOGGER.getLevel() == Level.DEBUG) {
        StringBuilder sb = new StringBuilder();
        sb.append("Created new node for data: {").append(Arrays.toString(edges))
            .append("} with path (from root): ");
        leafNode.getEdgesFromRoot()
            .forEach(e -> sb.append(Arrays.toString(e.getLabel())).append(", "));
        LOGGER.debug(sb.toString());
        // todo:
        // do not summarize edges?
        // after 3 edges, go to table bin
      }*/
    }
    return true;
  }

  @Override
  public boolean moveSubtree(N src, E edge, N dest) {
    src.getParentEdge().getParent().removeEdge(src.getParentEdge());
    edge.setParent(dest);
    edge.setChild(src);
    src.setParentEdge(edge);
    dest.addOutgoingEdge(edge);
    return true;
  }

  @Override
  public boolean contains(@NotNull I indexingData) {
    return this.getNode(indexingData).isPresent();
  }

  @Override
  public Optional<N> getNode(@NotNull I indexingData) {
    String[] edgeLabels = this.getLabels(indexingData);

    N currentNode = getRoot();

    int i = 0;
    boolean advanced = true;

    while(!currentNode.isLeafNode() && advanced) {
      advanced = false;

      // match
      if (Arrays.equals(currentNode.getPathFromRoot(), edgeLabels))
        return Optional.of(currentNode);

      // single edge
      Optional<E> target = currentNode.getOutgoingEdge(edgeLabels[i]);
      if (target.isPresent()) {
        currentNode = target.get().getChild();
        i++;
        advanced = true;
        continue;
      }

      // summary edge
      String[] partial = Arrays.copyOfRange(edgeLabels, i, edgeLabels.length);
      for (E edge : currentNode.getOutgoingEdges()) {
        if (edge.queryIncludesEdge(partial)) {
          currentNode = edge.getChild();
          i += edge.amountOfSummarizedElements();
          advanced = true;
          break;
        }
      }
    }

    String[] pathFromRoot = currentNode.getPathFromRoot();

    //System.out.println("I went all the way to " + Arrays.toString(pathFromRoot));
    //System.out.println("Looking for " + Arrays.toString(edgeLabels));

    if (!currentNode.isLeafNode()) {
      return Optional.empty();
    }

    for (int j = 0; j < Math.min(edgeLabels.length, pathFromRoot.length); j++) {
      if (!pathFromRoot[j].equals(edgeLabels[j])) {
        return Optional.empty();
      }
    }
     return Optional.of(currentNode);
  }
}
