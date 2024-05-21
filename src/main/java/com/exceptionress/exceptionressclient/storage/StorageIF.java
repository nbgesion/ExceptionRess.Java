package com.exceptionress.exceptionressclient.storage;

import com.exceptionress.exceptionressclient.models.storage.StorageItem;

import java.util.List;

public interface StorageIF<X> {
  long save(X value);

  StorageItem<X> peek();

  List<StorageItem<X>> get(int limit);

  void remove(long timestamp);

  void clear();
}
