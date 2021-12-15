package de.skuld.prng;

public interface PRNG {

  /**
   * Returns a byte array containing random bytes generated by this prng
   *
   * @param size amount of bytes to generate
   * @return random bytes
   */
  byte[] getRandomBytes(int size);

  /**
   * Returns the default seed that this PRNG uses in the original implementation. Might be Unix
   * time!
   *
   * @return seed
   */
  long getDefaultSeed();

  /**
   * @return whether the original implementation uses unix time as default seed
   */
  boolean usesUnixTimeAsDefault();
}