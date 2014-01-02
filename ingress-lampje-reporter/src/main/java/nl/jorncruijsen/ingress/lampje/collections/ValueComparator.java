package nl.jorncruijsen.ingress.lampje.collections;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<Object> {
  Map<?, Integer> base;
  public ValueComparator(final Map<?, Integer> players) {
    this.base = players;
  }

  @Override
  public int compare(final Object a, final Object b) {
    if (base.get(a) >= base.get(b)) {
      return -1;
    } else {
      return 1;
    }
  }
}
