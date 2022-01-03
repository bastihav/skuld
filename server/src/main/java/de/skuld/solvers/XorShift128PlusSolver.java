package de.skuld.solvers;

import com.microsoft.z3.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.lang3.tuple.Triple;

public class XorShift128PlusSolver implements Solver {

  Random r = new Random();

  @Override
  public int getConsecutiveBitsNeeded() {
    return 0;
  }

  public Triple<BitVecExpr, BitVecExpr, BoolExpr> sym_xs128p(Context context, com.microsoft.z3.Solver solver, BitVecExpr sym_state0, BitVecExpr sym_state1, long generated, Browser browser) {
    BitVecExpr s1 = sym_state0;
    BitVecExpr s0 = sym_state1;

    s1 = context.mkBVXOR(s1, context.mkBVSHL(s1, context.mkBV(23, 64)));
    s1 = context.mkBVXOR(s1, context.mkBVLSHR(s1, context.mkBV(17, 64)));
    s1 = context.mkBVXOR(s1, s0);
    s1 = context.mkBVXOR(s1, context.mkBVLSHR(s0, context.mkBV(26, 64)));

    sym_state0 = sym_state1;
    sym_state1 = s1;

    BitVecExpr calc = context.mkBVASHR(context.mkBVAdd(sym_state0, sym_state1), context.mkBV(56, 64));

    BoolExpr condition = context.mkBoolConst("c" + (generated * r.nextDouble()));
    //BitVecExpr shift = context.mkBVLSHR(calc, context.mkBV(12, 64));

    BoolExpr implication = context.mkImplies(condition, context.mkEq(calc, context.mkBV(generated, 64)));

    solver.add(implication);

    return Triple.of(sym_state0, sym_state1, condition);
  }

  @Override
  public long[] solve(byte[] input) {
    Context context = new Context();

    BitVecExpr sym_state0 = context.mkBVConst("ostate0", 64);
    BitVecExpr sym_state1 = context.mkBVConst("ostate1", 64);

    com.microsoft.z3.Solver solver = context.mkSolver();

    long[] generated = new long[input.length];
    for (int i = 0; i < input.length; i++) {
      generated[i] = input[i];
    }

    ArrayList<BoolExpr> conditions = new ArrayList<>();

    for (long gen : generated) {
      Triple<BitVecExpr, BitVecExpr, BoolExpr> ret = sym_xs128p(context, solver, sym_state0, sym_state1, gen, Browser.CHROME);
      conditions.add(ret.getRight());
    }

    for (BoolExpr condition : conditions) {
      //System.out.println(condition.get);
    }

    if (solver.check(conditions.toArray(BoolExpr[]::new)).equals(Status.SATISFIABLE)) {
      System.out.println("sat");

      Model model = solver.getModel();
      System.out.println(model.getConstInterp(sym_state0));

    } else {
      System.out.println(solver.check(conditions.toArray(BoolExpr[]::new)));
    }

    return new long[0];
  }

  private enum Browser {
    CHROME, FIREFOX, SAFARI;
  }
}
