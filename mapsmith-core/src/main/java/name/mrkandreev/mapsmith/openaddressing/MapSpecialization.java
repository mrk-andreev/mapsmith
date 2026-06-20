package name.mrkandreev.mapsmith.openaddressing;

/** Supported open-addressing implementations. */
public enum MapSpecialization {
  /** Linear probing. */
  LINEAR_PROBING {
    @Override
    public LongLongOpenAddressingStrategy strategy() {
      return LongLongOpenAddressingStrategy.LINEAR_PROBING;
    }

    @Override
    public <T> LongObjectOpenAddressingStrategy<T> objectStrategy() {
      return castObjectStrategy(LongObjectOpenAddressingStrategy.LINEAR_PROBING);
    }
  },
  /** Robin Hood hashing. */
  ROBIN_HOOD {
    @Override
    public LongLongOpenAddressingStrategy strategy() {
      return LongLongOpenAddressingStrategy.ROBIN_HOOD;
    }

    @Override
    public <T> LongObjectOpenAddressingStrategy<T> objectStrategy() {
      return castObjectStrategy(LongObjectOpenAddressingStrategy.ROBIN_HOOD);
    }
  },
  /** Swiss table hashing. */
  SWISS_TABLE {
    @Override
    public LongLongOpenAddressingStrategy strategy() {
      return LongLongOpenAddressingStrategy.SWISS_TABLE;
    }

    @Override
    public <T> LongObjectOpenAddressingStrategy<T> objectStrategy() {
      return castObjectStrategy(LongObjectOpenAddressingStrategy.SWISS_TABLE);
    }
  };

  /**
   * Returns the primitive map strategy.
   *
   * @return primitive map strategy
   */
  public abstract LongLongOpenAddressingStrategy strategy();

  /**
   * Returns the object map strategy.
   *
   * @return object map strategy
   */
  public abstract <T> LongObjectOpenAddressingStrategy<T> objectStrategy();

  @SuppressWarnings("unchecked")
  private static <T> LongObjectOpenAddressingStrategy<T> castObjectStrategy(
      LongObjectOpenAddressingStrategy<?> strategy) {
    return (LongObjectOpenAddressingStrategy<T>) strategy;
  }
}
