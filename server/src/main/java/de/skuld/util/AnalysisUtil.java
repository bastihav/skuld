package de.skuld.util;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import de.skuld.prng.ImplementedPRNGs;
import de.skuld.prng.PRNG;
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
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnalysisUtil {

  private static final Logger LOGGER = LogManager.getLogger();

  public static void analyzeWithPrecomputations(DiskBasedRadixTrie radixTrie,
      List<byte[]> randomness, Result result, TypeEnum type,
      ThreadPoolExecutor threadPoolExecutor) {
    if (randomness.size() == 0) {
      return;
    }

    for (byte[] arr : randomness) {
      threadPoolExecutor.execute(() -> {
        Optional<RandomnessRadixTrieDataPoint> optionalDataPoint = radixTrie.search(arr);

        if (optionalDataPoint.isPresent()) {
          // verify quickly
          RandomnessRadixTrieDataPoint dataPoint = optionalDataPoint.get();
          PRNG instance = ImplementedPRNGs.getPRNG(dataPoint.getRng(),
              radixTrie.getSeedMap().get(dataPoint.getSeedIndex()));
          if (verifyPrecomputationsMatch(randomness, dataPoint.getByteIndexInRandomness(),
              instance)) {
            ResultPairs pair = new ResultPairs();
            pair.setSeeds(new ArrayList<>());
            pair.addSeedsItem(
                Longs.toByteArray(radixTrie.getSeedMap().get(dataPoint.getSeedIndex())));
            pair.setPrng(dataPoint.getRng().toString());
            pair.setType(type);
            result.addPairsItem(pair);
            threadPoolExecutor.shutdownNow();
            LOGGER.info("precomp done");
            return;
          }
        }
        LOGGER.info("precomp done");
      });
    }
  }

  private static boolean verifyPrecomputationsMatch(List<byte[]> randomness, int byteIndex,
      PRNG instance) {
    int verifySize = ConfigurationHelper.getConfig().getInt("radix.solver.verify_size");

    int startIndex = Math.max(0, byteIndex - verifySize);
    int endIndex = byteIndex + verifySize;
    int length = endIndex - startIndex;

    byte[] bytes = instance.getBytes(startIndex, length);

    return ArrayUtil.checkSubArraysInArraySequential(bytes, randomness);
  }

  public static void analyzeWithSolvers(List<byte[]> randomness, Result result,
      TypeEnum type, ThreadPoolExecutor threadPoolExecutor) {
    Solver[] solvers = new Solver[]{new JavaRandomSolver(), new XoShiRo128StarStarSolver()};

    if (randomness.size() == 0) {
      return;
    }

    for (Solver solver : solvers) {
      if (!solver.solveable(randomness.get(0))) {
        continue;
      }

      threadPoolExecutor.execute(() -> {
        ResultPairs pair = new ResultPairs();
        pair.setSeeds(new ArrayList<>());
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
          threadPoolExecutor.shutdownNow();
        }
      });
    }
    LOGGER.info("solver done");
  }

}
