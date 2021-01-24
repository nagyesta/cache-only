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
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.github.nagyesta.cacheonly.example.replies.CommentContext.THREADS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CommentContext.class)
@SuppressWarnings("checkstyle:MagicNumber")
class CommentCacheServiceTemplateIntegrationTest {

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
        final ThreadRequest request = ThreadRequest.builder()
                .articleId(UUID.randomUUID())
                .threadIds(Collections.singletonList(1L))
                .build();

        //when
        assertThrows(BatchServiceException.class, () -> underTest.callCacheableBatchService(request));

        //then + excception
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldNotPutAnythingWhenNoResultsFound() throws BatchServiceException {
        //given
        // create the test request
        final List<Long> threadIds = Arrays.asList(1L, 2L, 3L, 4L);
        final ThreadRequest request = ThreadRequest.builder()
                .articleId(CommentService.NO_COMMENT)
                .threadIds(threadIds)
                .build();

        //when
        final CommentThreads actual = underTest.callCacheableBatchService(request);

        //then
        assertNotNull(actual);
        assertEquals(Collections.emptyMap(), actual.getThreads());
        // all items were tried from the cache and missed
        final Cache cache = cacheManager.getCache(THREADS);
        final InOrder inOrder = Mockito.inOrder(cache);
        threadIds.forEach(id -> inOrder.verify(cache).get(
                eq(CommentService.NO_COMMENT.toString() + "_thread_" + id),
                eq(List.class)));
        // nothing is put into the cache as all of them are missed
        inOrder.verify(cache, never()).put(anyString(), any());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldCacheWhenResultsAreFound() throws BatchServiceException, NotFoundException {
        //given
        // create the test request
        final List<Long> threadIds = Arrays.asList(1L, 2L, 3L, 4L);
        final ThreadRequest request = ThreadRequest.builder()
                .articleId(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT)
                .threadIds(threadIds)
                .build();
        final CommentThreads expected = commentService.threadsOf(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT, new HashSet<>(threadIds));

        //when
        final CommentThreads actual = underTest.callCacheableBatchService(request);

        //then
        assertNotNull(actual);
        assertEquals(2, actual.getThreads().size());
        assertEquals(expected, actual);
        // all items were tried from the cache and missed
        final Cache cache = cacheManager.getCache(THREADS);
        final InOrder inOrder = Mockito.inOrder(cache);
        threadIds.forEach(id -> inOrder.verify(cache).get(
                eq(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT.toString() + "_thread_" + id),
                eq(List.class)));
        // only 1 and 3 are put into the cache as others are nulls
        inOrder.verify(cache).put(eq(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT.toString() + "_thread_1"), any());
        inOrder.verify(cache).put(eq(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT.toString() + "_thread_3"), any());
        inOrder.verify(cache, never()).put(eq(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT.toString() + "_thread_2"), any());
        inOrder.verify(cache, never()).put(eq(CommentService.AINT_NOBODY_GOT_TIME_FOR_THAT.toString() + "_thread_4"), any());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldNotUseRealServiceWhenAllFoundInCache() throws BatchServiceException, NotFoundException {
        //given
        // create the test request
        final List<Long> threadIds = Collections.singletonList(1L);
        final ThreadRequest request = ThreadRequest.builder()
                .articleId(CommentService.ARE_YOU_OUT_OF_QUOTA)
                .threadIds(threadIds)
                .build();
        // fetch expected data
        final CommentThreads expected = underTest.callBatchServiceAndPutAllToCache(request);
        // verify it was just put into the cache
        final Cache cache = cacheManager.getCache(THREADS);
        verify(cache, never()).get(eq(CommentService.ARE_YOU_OUT_OF_QUOTA.toString() + "_thread_1"), eq(List.class));
        verify(cache).put(eq(CommentService.ARE_YOU_OUT_OF_QUOTA.toString() + "_thread_1"), any());
        reset(cache);
        // the real service is called this time
        final CommentService spyService = batchServiceCaller.getCommentService();
        verify(spyService).threadsOf(eq(CommentService.ARE_YOU_OUT_OF_QUOTA), eq(Collections.singleton(1L)));
        reset(spyService);

        //when
        final CommentThreads actual = underTest.callCacheableBatchService(request);

        //then
        assertNotNull(actual);
        assertEquals(1, actual.getThreads().size());
        assertEquals(expected, actual);
        // opportunistic processing will not call when all of the items are already cached
        verify(cache).get(eq(CommentService.ARE_YOU_OUT_OF_QUOTA.toString() + "_thread_1"), eq(List.class));
        verify(cache, never()).put(eq(CommentService.ARE_YOU_OUT_OF_QUOTA.toString() + "_thread_1"), any());
        // the real service is not called again
        verify(spyService, never()).threadsOf(eq(CommentService.ARE_YOU_OUT_OF_QUOTA), eq(Collections.singleton(1L)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldUseRealServiceOnlyWhenSomeNotFoundInCache() throws BatchServiceException, NotFoundException {
        //given
        // warm-up cache
        final ThreadRequest warmUpRequest = ThreadRequest.builder()
                .articleId(CommentService.CACHING_IS_NOT_ALWAYS_EASY).threadIds(Collections.singletonList(1L)).build();
        underTest.callBatchServiceAndPutAllToCache(warmUpRequest);
        // verify it was just put into the cache
        final Cache cache = cacheManager.getCache(THREADS);
        verify(cache, never()).get(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY.toString() + "_thread_1"), eq(List.class));
        verify(cache, atLeastOnce()).put(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY.toString() + "_thread_1"), any());
        reset(cache);
        // create the test request
        final List<Long> threadIds = Arrays.asList(1L, 5L);
        final ThreadRequest request = ThreadRequest.builder()
                .articleId(CommentService.CACHING_IS_NOT_ALWAYS_EASY).threadIds(threadIds).build();
        // fetch expected data
        final CommentThreads expected = commentService.threadsOf(CommentService.CACHING_IS_NOT_ALWAYS_EASY, new HashSet<>(threadIds));
        final CommentService spyService = batchServiceCaller.getCommentService();
        verify(spyService).threadsOf(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY), eq(Collections.singleton(1L)));
        reset(spyService);

        //when
        final CommentThreads actual = underTest.callCacheableBatchService(request);

        //then
        assertNotNull(actual);
        assertEquals(2, actual.getThreads().size());
        assertEquals(expected, actual);
        // opportunistic processing will refresh the already cached value as well
        verify(cache).get(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY.toString() + "_thread_1"), eq(List.class));
        verify(cache).put(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY.toString() + "_thread_1"), any());
        verify(cache).get(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY.toString() + "_thread_5"), eq(List.class));
        verify(cache).put(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY.toString() + "_thread_5"), any());
        // only a single call will be made
        verify(spyService, never()).threadsOf(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY), eq(Collections.singleton(1L)));
        verify(spyService, never()).threadsOf(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY), eq(Collections.singleton(5L)));
        verify(spyService).threadsOf(eq(CommentService.CACHING_IS_NOT_ALWAYS_EASY), eq(new HashSet<>(threadIds)));
    }
}
