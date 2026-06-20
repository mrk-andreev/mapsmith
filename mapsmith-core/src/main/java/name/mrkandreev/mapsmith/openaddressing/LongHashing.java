package name.mrkandreev.mapsmith.openaddressing;

/** Hash functions for primitive {@code long} values. */
public enum LongHashing {
  /** MurmurHash3 finalizer. */
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
  /** Fibonacci hashing. */
  FIBONACCI {
    @Override
    public long hash(long value) {
      return value * 0x9e3779b97f4a7c15L;
    }
  },
  /** Xor-shift hashing. */
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
  /** Identity hashing. */
  IDENTITY {
    @Override
    public long hash(long value) {
      return value;
    }
  };

  /**
   * Hashes {@code value}.
   *
   * @param value value to hash
   * @return hash value
   */
  public abstract long hash(long value);
}
