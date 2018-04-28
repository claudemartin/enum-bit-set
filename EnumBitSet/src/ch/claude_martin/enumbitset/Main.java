package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

  enum Bla implements EnumBitSetHelper<Bla> {
    A(-1), B(42), C(108);
    final int i;

    private Bla(int i) {
      this.i = i;
    }

    public int toInt() {
      return this.i;
    }
  }

  public static void main(String[] args) {
    var set = GeneralDomainBitSet.of(EnumDomain.of(Bla.class), asList(Bla.A));
    set.add(Bla.C);
    var list = set.stream().map(Bla::toInt).collect(Collectors.toList());
    System.out.println(list);
  }
}
