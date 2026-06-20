package name.mrkandreev.mapsmith.openaddressing;

public enum MapSpecialization {
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

  public abstract LongLongOpenAddressingStrategy strategy();

  public abstract <T> LongObjectOpenAddressingStrategy<T> objectStrategy();

  @SuppressWarnings("unchecked")
  private static <T> LongObjectOpenAddressingStrategy<T> castObjectStrategy(
      LongObjectOpenAddressingStrategy<?> strategy) {
    return (LongObjectOpenAddressingStrategy<T>) strategy;
  }
}
