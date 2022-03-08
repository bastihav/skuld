package de.skuld.radix;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.skuld.util.ConfigurationHelper;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractRadixTrie<D extends AbstractRadixTrieData<I, P>, P, I, N extends RadixTrieNode<D, E>, E extends AbstractRadixTrieEdge<D, N>> implements
    RadixTrie<D, P, I, N, E> {

  private static final Logger LOGGER = LogManager.getLogger();
  protected final BiMap<Integer, Long> seedMap = HashBiMap.create();
  protected N root = getDummyNode();
  protected RadixMetaData metaData = new RadixMetaData();

  public AbstractRadixTrie() {
    this.metaData.setDate(new Date());
    this.metaData.setId(UUID.randomUUID());
    this.metaData.setStatus(RadixTrieStatus.CREATED);
  }

  public AbstractRadixTrie(Date date) {
    this.metaData.setDate(date);
    this.metaData.setId(UUID.randomUUID());
    this.metaData.setStatus(RadixTrieStatus.CREATED);
  }

  public AbstractRadixTrie(RadixTrieStatus status) {
    this.metaData.setDate(new Date());
    this.metaData.setId(UUID.randomUUID());
    this.metaData.setStatus(status);
  }

  public AbstractRadixTrie(Date date, UUID uuid) {
    this.metaData.setDate(date);
    this.metaData.setId(uuid);
    this.metaData.setStatus(RadixTrieStatus.CREATED);
  }

  public AbstractRadixTrie(Date date, RadixTrieStatus status) {
    this.metaData.setDate(date);
    this.metaData.setId(UUID.randomUUID());
    this.metaData.setStatus(status);
  }

  @Override
  public RadixMetaData getMetaData() {
    return metaData;
  }

  public BiMap<Integer, Long> getSeedMap() {
    return seedMap;
  }

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

    if (parent.isLeafNode() && parent != root) {
      return parent.mergeNodes(data);
    }

    String[] remainingEdges = Arrays.copyOfRange(edges, coveredEdges.length, edges.length);

    Optional<E> edge = parent.getOutgoingEdge(remainingEdges[0]);

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

    int maxDepth = ConfigurationHelper.getConfig()
        .getInt("radix.height.max");

    if (parent.getEdgesFromRoot().size() < maxDepth) {
      // last case: we branch off of here
      boolean summarizeEdges = ConfigurationHelper.getConfig()
          .getBoolean("radix.summarize_edges");

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
      data.removePrefixFromRemainingIndexingData(coveredEdges.length);

      // merge instead
      Optional<E> table = parent.getOutgoingEdges().stream().findFirst();
      if (table.isPresent()) {
        return this.add(table.get().getChild(), data, indexingData);
      }

      this.createNode(data, parent.getParentEdge());
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

    while (!currentNode.isLeafNode() && advanced) {
      advanced = false;

      // match
      if (Arrays.equals(currentNode.getPathFromRoot(), edgeLabels)) {
        return Optional.of(currentNode);
      }

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

  @Override
  public Optional<P> search(@NotNull I indexingData) {

    int partitionSize = ConfigurationHelper.getConfig().getInt("radix.partition.size");

    // TODO if we shift too often, we have to many partial matches which tanks our performance
    for (int i = 0; i < partitionSize-26; i++) {
      // shift
      I shiftedIndexingData = shiftIndexingData(indexingData, i);
      I discardedIndexingData = getDiscardedIndexingData(indexingData, i);

      // search
      Optional<N> node =  getNode(shiftedIndexingData);
      Collection<P> dataPoints = node.isPresent() && node.get().isLeafNode() ?
          node.get().getData().getDataPoints(shiftedIndexingData) : Collections.emptyList();

      // check
      for (P dataPoint : dataPoints) {
        if (checkDiscardedIndexingData(dataPoint, discardedIndexingData)) {
          return Optional.of(dataPoint);
        }
      }
    }
    return Optional.empty();
  }
}
