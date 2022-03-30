package de.skuld.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;

public class ApacheConfigurationCacher {

  private final Configuration config;
  private final ConcurrentMap<String, Object> cacheMap = new ConcurrentHashMap<>();

  ApacheConfigurationCacher(Configuration config) {
    this.config = config;
  }

  public boolean getBoolean(String key) {
    return (boolean) cacheMap.computeIfAbsent(key, config::getBoolean);
  }

  public <T> T[] getArray(Class<T> cls, String key) {
    return (T[]) cacheMap.computeIfAbsent(key, k -> config.getArray(cls, k));
  }

  public int getInt(String key) {
    return (int) cacheMap.computeIfAbsent(key, config::getInt);
  }

  public String getString(String key) {
    return (String) cacheMap.computeIfAbsent(key, config::getString);
  }

  public List<Object> getList(String key) {
    return (List<Object>) cacheMap.computeIfAbsent(key, config::getList);
  }

  public <T> List<T> getList(Class<T> cls, String key) {
    return (List<T>) cacheMap.computeIfAbsent(key, k -> config.getList(cls, k));
  }

  public long getLong(String key) {
    return (long) cacheMap.computeIfAbsent(key, config::getLong);
  }
}
