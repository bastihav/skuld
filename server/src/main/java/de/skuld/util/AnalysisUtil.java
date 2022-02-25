package de.skuld.util;

import com.google.common.primitives.Longs;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import de.skuld.solvers.JavaRandomSolver;
import de.skuld.solvers.Solver;
import de.skuld.solvers.XoShiRo128StarStarSolver;
import de.skuld.web.model.RandomnessQueryInner.TypeEnum;
import de.skuld.web.model.Result;
import de.skuld.web.model.ResultPairs;
import java.util.ArrayList;
import java.util.List;

public class AnalysisUtil {
  public static void analyzeWithPrecomputations(DiskBasedRadixTrie radixTrie,
      List<byte[]> randomness, Result result, TypeEnum type) {
    // TODO do this in different thread
    List<RandomnessRadixTrieDataPoint> dp = new ArrayList<>(randomness.size());

    for (byte[] arr : randomness) {
      radixTrie.search(arr).ifPresent(dp::add);
    }

    boolean sameSeeds = dp.stream().map(RandomnessRadixTrieDataPoint::getSeedIndex).distinct().count() == 1;
    boolean sameRNG =  dp.stream().map(RandomnessRadixTrieDataPoint::getRng).distinct().count() == 1;

    // we need to find datapoints for all randomness
    if (sameSeeds && sameRNG && dp.size() == randomness.size()) {
      long current = -1;

      for (RandomnessRadixTrieDataPoint p : dp) {
        long seedIndex = p.getSeedIndex();
        if (seedIndex > current) {
          current = seedIndex;
        } else {
          current = Long.MAX_VALUE;
          break;
        }
      }

      if (current != Long.MAX_VALUE) {
        // increasing seed indices! This is a match!
        ResultPairs pair = new ResultPairs();
        pair.addSeedsItem(Longs.toByteArray(radixTrie.getSeedMap().get(dp.get(0).getSeedIndex())));
        pair.setPrng(dp.get(0).getRng().toString());
        pair.setType(type);

        result.addPairsItem(pair);
      }
    }
  }

  public static void analyzeWithSolvers(List<byte[]> randomness, Result result,
      TypeEnum type) {
    Solver[] solvers = new Solver[]{new JavaRandomSolver(), new XoShiRo128StarStarSolver()};

    // TODO do this in different threads
    for (Solver solver : solvers) {
      ResultPairs pair = new ResultPairs();
      pair.setPrng(solver.getPrng().toString());
      pair.setType(type);

      List<byte[]> possibleSeeds = solver.solve(randomness.get(0));

      for (byte[] possibleSeed : possibleSeeds) {
        if (solver.verify(randomness, possibleSeed)) {
          pair.addSeedsItem(possibleSeed);
        }
      }
      if (pair.getSeeds().size() > 0) {
        result.addPairsItem(pair);
      }
    }
  }

}
