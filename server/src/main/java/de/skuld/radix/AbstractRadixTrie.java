package de.skuld.radix;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.util.ConfigurationHelper;
import java.util.Arrays;
import java.util.Collection;
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
  protected RadixMetaData metaData;

  public AbstractRadixTrie() {
    this.metaData = new RadixMetaData(UUID.randomUUID(), new Date(), RadixTrieStatus.CREATED);
  }

  public AbstractRadixTrie(Date date) {
    this();
    this.metaData.setDate(date);
  }

  public AbstractRadixTrie(RadixTrieStatus status) {
    this();
    this.metaData.setStatus(status);
  }

  public AbstractRadixTrie(Date date, RadixTrieStatus status) {
    this(date);
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
      Collection<P> dataPoints = data.getDataPoints();
      if (dataPoints.size() > 0 && dataPoints.stream().findFirst()
          .get() instanceof RandomnessRadixTrieDataPoint) {
        Collection<RandomnessRadixTrieDataPoint> dataPointCollection = (Collection<RandomnessRadixTrieDataPoint>) dataPoints;

        dataPointCollection
            .forEach(dp -> dp.removePrefixFromRemainingIndexingData(coveredEdges.length));
      }

      // merge instead
      Optional<E> table = parent.getOutgoingEdges().stream().findFirst();
      if (table.isPresent()) {
        return this.add(table.get().getChild(), data, indexingData);
      }

      N leafNode = this.createNode(data, parent.getParentEdge());

      /*if (LOGGER.getLevel() == Level.DEBUG) {
        StringBuilder sb = new StringBuilder();
        sb.append("Created new node for data: {").append(Arrays.toString(edges))
            .append("} with path (from root): ");
        leafNode.getEdgesFromRoot()
            .forEach(e -> sb.append(Arrays.toString(e.getLabel())).append(", "));
        LOGGER.debug(sb.toString());
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
}
