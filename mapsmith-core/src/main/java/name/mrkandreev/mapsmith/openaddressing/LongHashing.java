package name.mrkandreev.mapsmith.openaddressing;

public enum LongHashing {
  MURMUR3_FINALIZER {
    @Override
    public long hash(long value) {
      long result = value;
      result ^= result >>> 33;
      result *= 0xff51afd7ed558ccdL;
      result ^= result >>> 33;
      result *= 0xc4ceb9fe1a85ec53L;
      result ^= result >>> 33;
      return result;
    }
  },
  FIBONACCI {
    @Override
    public long hash(long value) {
      return value * 0x9e3779b97f4a7c15L;
    }
  },
  XOR_SHIFT {
    @Override
    public long hash(long value) {
      long result = value;
      result ^= result >>> 32;
      result *= 0xd6e8feb86659fd93L;
      result ^= result >>> 32;
      return result;
    }
  },
  IDENTITY {
    @Override
    public long hash(long value) {
      return value;
    }
  };

  public abstract long hash(long value);
}
