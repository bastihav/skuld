package de.skuld.solvers;

public class XorShift128PlusSolverTest extends AbstractSolverTest {

  @Override
  protected byte[] getSolverInput(long seed) {
    return new byte[]{59, 24, 13, -101, 10, 39, -77, -91, -92, 66, -34, -84, 90, -9, -18, 51, 8, -69, -126, 51, -123, -101, -75, 68, -39, -102, 65, 83, 2, 30, -83, 35};
  }

  @Override
  protected Solver getSolver() {
    return new XorShift128PlusSolver();
  }
}
