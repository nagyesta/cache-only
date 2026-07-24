package com.github.nagyesta.cacheonly.example.replies.raw;

import com.github.nagyesta.cacheonly.example.replies.response.Comment;
import com.github.nagyesta.cacheonly.example.replies.response.CommentThreads;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:JavadocVariable", "checkstyle:DesignForExtension"})
public class CommentService {

    public static final UUID CACHING_IS_NOT_ALWAYS_EASY = UUID.fromString("76ddc1c8-1816-42fc-96f2-aebb9aa4def1");
    public static final UUID ARE_YOU_OUT_OF_QUOTA = UUID.fromString("bbfab199-8517-467f-977e-598d015e3641");
    public static final UUID AINT_NOBODY_GOT_TIME_FOR_THAT = UUID.fromString("0b9dee71-89aa-400f-a631-dddaf467ffde");
    public static final UUID NO_COMMENT = UUID.fromString("6c5f9515-38c4-4725-b74d-dbdb4e2ccc32");
    private static final String J_KIDDING = "J. Kidding";
    private static final String S_SERIOUS = "S. Serious";
    private static final String K_BYE = "K. Bye";
    private static final String O_PEN = "O. Pen";
    private final Map<UUID, List<Comment>> database;

    CommentService() {
        this.database = Map.of(NO_COMMENT, Collections.emptyList(), CACHING_IS_NOT_ALWAYS_EASY, List.of(
                        new Comment(CACHING_IS_NOT_ALWAYS_EASY,
                                1L, null, J_KIDDING, "First!"),
                        new Comment(CACHING_IS_NOT_ALWAYS_EASY,
                                2L, null, S_SERIOUS, "Great article, thank you!"),
                        new Comment(CACHING_IS_NOT_ALWAYS_EASY,
                                3, 1L, K_BYE, "Second!"),
                        new Comment(CACHING_IS_NOT_ALWAYS_EASY,
                                4, 1L, J_KIDDING, "Almost!"),
                        new Comment(CACHING_IS_NOT_ALWAYS_EASY,
                                5, null, O_PEN, "Do you have an example project?"),
                        new Comment(CACHING_IS_NOT_ALWAYS_EASY,
                                6, 5L, O_PEN, "Please ignore me, just found it on my own."),
                        new Comment(CACHING_IS_NOT_ALWAYS_EASY,
                                7, 5L, S_SERIOUS, "I am looking for one too, could you send a link?")),
                ARE_YOU_OUT_OF_QUOTA, List.of(
                        new Comment(ARE_YOU_OUT_OF_QUOTA,
                                1L, null, S_SERIOUS, "I think you have a typo in the last paragraph."),
                        new Comment(ARE_YOU_OUT_OF_QUOTA,
                                2L, 1L, S_SERIOUS, "'Quota' should be 'Quota'."),
                        new Comment(ARE_YOU_OUT_OF_QUOTA,
                                3, 1L, O_PEN, "It happens.")),
                AINT_NOBODY_GOT_TIME_FOR_THAT, List.of(
                        new Comment(AINT_NOBODY_GOT_TIME_FOR_THAT,
                                1L, null, J_KIDDING, "I want my 10 minutes back! :)"),
                        new Comment(AINT_NOBODY_GOT_TIME_FOR_THAT,
                                2L, 1L, S_SERIOUS, "It wasn't that bad..."),
                        new Comment(AINT_NOBODY_GOT_TIME_FOR_THAT,
                                3L, null, K_BYE, "First!"),
                        new Comment(AINT_NOBODY_GOT_TIME_FOR_THAT,
                                4L, 3L, J_KIDDING, "LOL, more like third.")));
    }

    public CommentThreads threadsOf(
            final UUID article,
            final Set<Long> threadIds)
            throws NotFoundException {
        Assert.isTrue(threadIds.size() <= 5, "Batch size is too large.");
        if (!database.containsKey(article)) {
            throw new NotFoundException();
        }
        final var threadStarters = this.database.get(article).stream()
                .filter(comment -> comment.threadId() == null)
                .map(Comment::commentId)
                .collect(Collectors.toSet());
        final var threads = this.database.get(article).stream()
                .filter(comment -> comment.threadId() != null)
                .filter(comment -> threadIds.contains(comment.threadId()))
                .collect(Collectors.groupingBy(Comment::threadId));
        final Map<Long, List<Comment>> result = new HashMap<>();
        threadIds.forEach(id -> {
            if (!threadStarters.contains(id)) {
                return;
            }
            result.put(id, threads.getOrDefault(id, Collections.emptyList()));
        });
        return new CommentThreads(result);
    }

}
