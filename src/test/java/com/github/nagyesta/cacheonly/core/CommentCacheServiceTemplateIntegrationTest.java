package com.github.nagyesta.cacheonly.core;

import com.github.nagyesta.cacheonly.example.replies.CommentCacheServiceTemplate;
import com.github.nagyesta.cacheonly.example.replies.CommentContext;
import com.github.nagyesta.cacheonly.example.replies.raw.CommentRepliesBatchServiceCaller;
import com.github.nagyesta.cacheonly.example.replies.raw.CommentService;
import com.github.nagyesta.cacheonly.example.replies.raw.NotFoundException;
import com.github.nagyesta.cacheonly.example.replies.request.ThreadRequest;
import com.github.nagyesta.cacheonly.example.replies.response.CommentThreads;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static com.github.nagyesta.cacheonly.example.replies.CommentContext.THREADS;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CommentContext.class)
@SuppressWarnings("checkstyle:MagicNumber")
class CommentCacheServiceTemplateIntegrationTest {

    private static final String THREAD = "_thread_";
    private static final String THREAD_1 = THREAD + 1;
    private static final String THREAD_2 = THREAD + 2;
    private static final String THREAD_3 = THREAD + 3;
    private static final String THREAD_4 = THREAD + 4;
    @Autowired
    private CommentCacheServiceTemplate underTest;
    @Autowired
    private CommentRepliesBatchServiceCaller batchServiceCaller;
    @Autowired
    private CommentService commentService;
    @Autowired
    private CacheManager cacheManager;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldWrapExceptionWhenExceptionCaught() {
        //given
        // create the test request
        final var request = ThreadRequest.builder()
                .articleId(UUID.randomUUID())
                .threadIds(Collections.singletonList(1L))
                .build();

        //when
        assertThrows(BatchServiceException.class, () -> underTest.callCacheableBatchService(request));

        //then + excception
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldNotPutAnythingWhenNoResultsFound()
            throws BatchServiceException {
        //given
        // create the test request
        final var threadIds = Arrays.asList(1L, 2L, 3L, 4L);
        final var request = ThreadRequest.builder()
                .articleId(CommentService.NO_COMMENT)
                .threadIds(threadIds)
                .build();

        //when
        final var actual = underTest.callCacheableBatchService(request);

        //then
        assertNull(actual);
        // all items were tried from the cache and missed
        final var cache = cacheManager.getCache(THREADS);
        final var inOrder = Mockito.inOrder(cache);
        threadIds.forEach(id -> inOrder.verify(cache)
                .get(CommentService.NO_COMMENT + THREAD + id, CommentThreads.class));
        // nothing is put into the cache as all of them are missed
        inOrder.verify(cache, never()).put(anyString(), any());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldCacheWhenResultsAreFound()
            throws BatchServiceException, NotFoundException {
        //given
        // create the test request
        final var threadIds = Arrays.asList(1L, 2L, 3L, 4L);
        final var request = ThreadRequest.builder()
                .articleId(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT)
                .threadIds(threadIds)
                .build();
        final var expected = commentService.threadsOf(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT, new HashSet<>(threadIds));

        //when
        final var actual = underTest.callCacheableBatchService(request);

        //then
        assertNotNull(actual);
        assertEquals(2, actual.getThreads().size());
        assertEquals(expected, actual);
        // all items were tried from the cache and missed
        final var cache = cacheManager.getCache(THREADS);
        final var inOrder = Mockito.inOrder(cache);
        threadIds.forEach(id -> inOrder.verify(cache)
                .get(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT + THREAD + id, CommentThreads.class));
        // only 1 and 3 are put into the cache as others are nulls
        inOrder.verify(cache).put(eq(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT + THREAD_1), any());
        inOrder.verify(cache).put(eq(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT + THREAD_3), any());
        inOrder.verify(cache, never()).put(eq(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT + THREAD_2), any());
        inOrder.verify(cache, never()).put(eq(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT + THREAD_4), any());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldNotUseRealServiceWhenAllFoundInCache()
            throws BatchServiceException, NotFoundException {
        //given
        // create the test request
        final var threadIds = Collections.singletonList(1L);
        final var request = ThreadRequest.builder()
                .articleId(CommentService.ARE_YOU_OUT_OF_QUOTA)
                .threadIds(threadIds)
                .build();
        // fetch expected data
        final var expected = underTest.callBatchServiceAndPutAllToCache(request);
        // verify it was just put into the cache
        final var cache = cacheManager.getCache(THREADS);
        verify(cache, never()).get(CommentService.ARE_YOU_OUT_OF_QUOTA + THREAD_1, List.class);
        verify(cache).put(eq(CommentService.ARE_YOU_OUT_OF_QUOTA + THREAD_1), any());
        reset(cache);
        // the real service is called this time
        final var spyService = batchServiceCaller.getCommentService();
        verify(spyService).threadsOf(CommentService.ARE_YOU_OUT_OF_QUOTA, singleton(1L));
        reset(spyService);

        //when
        final var actual = underTest.callCacheableBatchService(request);

        //then
        assertNotNull(actual);
        assertEquals(1, actual.getThreads().size());
        assertEquals(expected, actual);
        // opportunistic processing will not call when all the items are already cached
        verify(cache).get(CommentService.ARE_YOU_OUT_OF_QUOTA + THREAD_1, CommentThreads.class);
        verify(cache, never()).put(eq(CommentService.ARE_YOU_OUT_OF_QUOTA + THREAD_1), any());
        // the real service is not called again
        verify(spyService, never()).threadsOf(CommentService.ARE_YOU_OUT_OF_QUOTA, singleton(1L));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldUseRealServiceOnlyWhenSomeNotFoundInCache()
            throws BatchServiceException, NotFoundException {
        //given
        // warm-up cache
        final var warmUpRequest = ThreadRequest.builder()
                .articleId(CommentService.CACHING_IS_NOT_ALWAYS_EASY).threadIds(Collections.singletonList(1L)).build();
        underTest.callBatchServiceAndPutAllToCache(warmUpRequest);
        // verify it was just put into the cache
        final var cache = cacheManager.getCache(THREADS);
        verify(cache, never()).get(CommentService.CACHING_IS_NOT_ALWAYS_EASY + THREAD_1, CommentThreads.class);
        verify(cache, atLeastOnce()).put(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY + THREAD_1), any());
        reset(cache);
        // create the test request
        final var threadIds = Arrays.asList(1L, 5L);
        final var request = ThreadRequest.builder()
                .articleId(CommentService.CACHING_IS_NOT_ALWAYS_EASY).threadIds(threadIds).build();
        // fetch expected data
        final var expected = commentService.threadsOf(CommentService.CACHING_IS_NOT_ALWAYS_EASY, new HashSet<>(threadIds));
        final var spyService = batchServiceCaller.getCommentService();
        verify(spyService).threadsOf(CommentService.CACHING_IS_NOT_ALWAYS_EASY, singleton(1L));
        reset(spyService);

        //when
        final var actual = underTest.callCacheableBatchService(request);

        //then
        assertNotNull(actual);
        assertEquals(2, actual.getThreads().size());
        assertEquals(expected, actual);
        // opportunistic processing will refresh the already cached value as well
        verify(cache).get(CommentService.CACHING_IS_NOT_ALWAYS_EASY + THREAD_1, CommentThreads.class);
        verify(cache).put(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY + THREAD_1), any());
        verify(cache).get(CommentService.CACHING_IS_NOT_ALWAYS_EASY + "_thread_5", CommentThreads.class);
        verify(cache).put(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY + "_thread_5"), any());
        // only a single call will be made
        verify(spyService, never()).threadsOf(CommentService.CACHING_IS_NOT_ALWAYS_EASY, singleton(1L));
        verify(spyService, never()).threadsOf(CommentService.CACHING_IS_NOT_ALWAYS_EASY, singleton(5L));
        verify(spyService).threadsOf(CommentService.CACHING_IS_NOT_ALWAYS_EASY, new HashSet<>(threadIds));
    }
}
