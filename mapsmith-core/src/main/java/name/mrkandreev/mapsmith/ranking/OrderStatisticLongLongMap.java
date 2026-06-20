package name.mrkandreev.mapsmith.ranking;

import name.mrkandreev.mapsmith.LongLongMap;
import name.mrkandreev.mapsmith.openaddressing.LongLongOpenAddressMap;

/** A primitive map that ranks entries by value. */
public final class OrderStatisticLongLongMap implements LongLongRankingMap {
  private static final int MAX_BALANCE_DELTA = 1;

  private final LongLongMap scoreByKey;
  private Node root;

  /** Creates a map with default capacity. */
  public OrderStatisticLongLongMap() {
    this(LongLongOpenAddressMap.DEFAULT_EXPECTED_SIZE);
  }

  /**
   * Creates a map.
   *
   * @param expectedSize expected entry count
   */
  public OrderStatisticLongLongMap(int expectedSize) {
    scoreByKey = new LongLongOpenAddressMap(expectedSize);
  }

  @Override
  public int size() {
    return scoreByKey.size();
  }

  @Override
  public boolean containsKey(long key) {
    return scoreByKey.containsKey(key);
  }

  @Override
  public long get(long key) {
    return scoreByKey.get(key);
  }

  @Override
  public long getOrDefault(long key, long defaultValue) {
    return scoreByKey.getOrDefault(key, defaultValue);
  }

  @Override
  public long put(long key, long value) {
    boolean hadKey = scoreByKey.containsKey(key);
    long previousValue = scoreByKey.put(key, value);
    if (hadKey) {
      root = delete(root, key, previousValue);
    }
    root = insert(root, key, value);
    return previousValue;
  }

  @Override
  public long remove(long key) {
    if (!scoreByKey.containsKey(key)) {
      return 0L;
    }

    long previousValue = scoreByKey.remove(key);
    root = delete(root, key, previousValue);
    return previousValue;
  }

  @Override
  @SuppressWarnings("PMD.NullAssignment")
  public void clear() {
    scoreByKey.clear();
    root = null;
  }

  @Override
  public int rankOf(long key) {
    int countBefore = countBefore(key);
    return countBefore == MISSING_RANK ? MISSING_RANK : countBefore + 1;
  }

  @Override
  public int countBefore(long key) {
    if (!scoreByKey.containsKey(key)) {
      return MISSING_RANK;
    }
    return countBefore(root, key, scoreByKey.get(key));
  }

  @Override
  public int countAfter(long key) {
    int countBefore = countBefore(key);
    return countBefore == MISSING_RANK ? MISSING_RANK : size() - countBefore - 1;
  }

  private static Node insert(Node node, long key, long value) {
    if (node == null) {
      return new Node(key, value);
    }

    int comparison = compare(key, value, node);
    if (comparison < 0) {
      node.left = insert(node.left, key, value);
    } else if (comparison > 0) {
      node.right = insert(node.right, key, value);
    } else {
      node.value = value;
      return node;
    }

    return balance(update(node));
  }

  private static Node delete(Node node, long key, long value) {
    if (node == null) {
      return null;
    }

    int comparison = compare(key, value, node);
    if (comparison < 0) {
      node.left = delete(node.left, key, value);
      return balance(update(node));
    }
    if (comparison > 0) {
      node.right = delete(node.right, key, value);
      return balance(update(node));
    }

    if (node.left == null) {
      return node.right;
    }
    if (node.right == null) {
      return node.left;
    }

    Node successor = min(node.right);
    successor.right = deleteMin(node.right);
    successor.left = node.left;
    return balance(update(successor));
  }

  private static Node deleteMin(Node node) {
    if (node.left == null) {
      return node.right;
    }
    node.left = deleteMin(node.left);
    return balance(update(node));
  }

  private static Node min(Node node) {
    Node current = node;
    while (current.left != null) {
      current = current.left;
    }
    return current;
  }

  private static int countBefore(Node node, long key, long value) {
    if (node == null) {
      return 0;
    }

    int comparison = compare(key, value, node);
    if (comparison <= 0) {
      return countBefore(node.left, key, value);
    }
    return size(node.left) + 1 + countBefore(node.right, key, value);
  }

  private static Node balance(Node node) {
    int balance = height(node.left) - height(node.right);
    if (balance > MAX_BALANCE_DELTA) {
      if (height(node.left.left) < height(node.left.right)) {
        node.left = rotateLeft(node.left);
      }
      return rotateRight(node);
    }
    if (balance < -MAX_BALANCE_DELTA) {
      if (height(node.right.right) < height(node.right.left)) {
        node.right = rotateRight(node.right);
      }
      return rotateLeft(node);
    }
    return node;
  }

  private static Node rotateLeft(Node node) {
    Node right = node.right;
    node.right = right.left;
    right.left = update(node);
    return update(right);
  }

  private static Node rotateRight(Node node) {
    Node left = node.left;
    node.left = left.right;
    left.right = update(node);
    return update(left);
  }

  private static Node update(Node node) {
    node.height = Math.max(height(node.left), height(node.right)) + 1;
    node.size = size(node.left) + size(node.right) + 1;
    return node;
  }

  private static int compare(long key, long value, Node node) {
    if (value != node.value) {
      return value > node.value ? -1 : 1;
    }
    return Long.compare(key, node.key);
  }

  private static int height(Node node) {
    return node == null ? 0 : node.height;
  }

  private static int size(Node node) {
    return node == null ? 0 : node.size;
  }

  private static final class Node {
    private final long key;
    private long value;
    private Node left;
    private Node right;
    private int height = 1;
    private int size = 1;

    private Node(long key, long value) {
      this.key = key;
      this.value = value;
    }
  }
}
