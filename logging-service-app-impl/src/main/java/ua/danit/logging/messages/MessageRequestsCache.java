package ua.danit.logging.messages;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Repository;
import ua.danit.logging.model.ResponseMessage;

/**
 * Cache for message requests to provide asynchronous response
 * when response came.
 *
 * @author Andrey Minov
 */
@Repository
public class MessageRequestsCache {
  private static final int ENTRY_TIMEOUT_MIN = 1;
  private Cache<String, CompletableFuture<ResponseMessage>> cache;

  public MessageRequestsCache() {
    this.cache =
        CacheBuilder.newBuilder().expireAfterAccess(ENTRY_TIMEOUT_MIN, TimeUnit.MINUTES).build();
  }

  public void store(String messageId, CompletableFuture<ResponseMessage> future) {
    cache.put(messageId, future);
  }

  Optional<CompletableFuture<ResponseMessage>> get(String messageId) {
    return Optional.ofNullable(cache.getIfPresent(messageId));
  }
}
