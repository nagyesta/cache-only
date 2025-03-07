package com.github.nagyesta.cacheonly.example.replies.raw;

import com.github.nagyesta.cacheonly.example.replies.response.Comment;
import com.github.nagyesta.cacheonly.example.replies.response.CommentThreads;
import org.jetbrains.annotations.NotNull;
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
        this.database = Map.of(NO_COMMENT, Collections.emptyList(), CACHING_IS_NOT_ALWAYS_EASY, Collections.unmodifiableList(Arrays.asList(
                Comment.builder()
                        .articleId(CACHING_IS_NOT_ALWAYS_EASY)
                        .commentId(1L)
                        .author(J_KIDDING)
                        .message("First!")
                        .build(),
                Comment.builder()
                        .articleId(CACHING_IS_NOT_ALWAYS_EASY)
                        .commentId(2L)
                        .author(S_SERIOUS)
                        .message("Great article, thank you!")
                        .build(),
                Comment.builder()
                        .articleId(CACHING_IS_NOT_ALWAYS_EASY)
                        .commentId(3)
                        .threadId(1L)
                        .author(K_BYE)
                        .message("Second!")
                        .build(),
                Comment.builder()
                        .articleId(CACHING_IS_NOT_ALWAYS_EASY)
                        .commentId(4)
                        .threadId(1L)
                        .author(J_KIDDING)
                        .message("Almost!")
                        .build(),
                Comment.builder()
                        .articleId(CACHING_IS_NOT_ALWAYS_EASY)
                        .commentId(5)
                        .author(O_PEN)
                        .message("Do you have an example project?")
                        .build(),
                Comment.builder()
                        .articleId(CACHING_IS_NOT_ALWAYS_EASY)
                        .commentId(6)
                        .threadId(5L)
                        .author(O_PEN)
                        .message("Please ignore me, just found it on my own.")
                        .build(),
                Comment.builder()
                        .articleId(CACHING_IS_NOT_ALWAYS_EASY)
                        .commentId(7)
                        .threadId(5L)
                        .author(S_SERIOUS)
                        .message("I am looking for one too, could you send a link?")
                        .build()
        )), ARE_YOU_OUT_OF_QUOTA, Collections.unmodifiableList(Arrays.asList(
                Comment.builder()
                        .articleId(ARE_YOU_OUT_OF_QUOTA)
                        .commentId(1L)
                        .author(S_SERIOUS)
                        .message("I think you have a typo in the last paragraph.")
                        .build(),
                Comment.builder()
                        .articleId(ARE_YOU_OUT_OF_QUOTA)
                        .commentId(2L)
                        .threadId(1L)
                        .author(S_SERIOUS)
                        .message("'Qutoa' should be 'Quota'.")
                        .build(),
                Comment.builder()
                        .articleId(ARE_YOU_OUT_OF_QUOTA)
                        .commentId(3)
                        .threadId(1L)
                        .author(O_PEN)
                        .message("It happens.")
                        .build()
        )), AINT_NOBODY_GOT_TIME_FOR_THAT, Collections.unmodifiableList(Arrays.asList(
                Comment.builder()
                        .articleId(AINT_NOBODY_GOT_TIME_FOR_THAT)
                        .commentId(1L)
                        .author(J_KIDDING)
                        .message("I want my 10 minutes back! :)")
                        .build(),
                Comment.builder()
                        .articleId(AINT_NOBODY_GOT_TIME_FOR_THAT)
                        .commentId(2L)
                        .threadId(1L)
                        .author(S_SERIOUS)
                        .message("It wasn't that bad...")
                        .build(),
                Comment.builder()
                        .articleId(AINT_NOBODY_GOT_TIME_FOR_THAT)
                        .commentId(3L)
                        .author(K_BYE)
                        .message("First!")
                        .build(),
                Comment.builder()
                        .articleId(AINT_NOBODY_GOT_TIME_FOR_THAT)
                        .commentId(4L)
                        .threadId(3L)
                        .author(J_KIDDING)
                        .message("LOL, more like third.")
                        .build()
        )));
    }

    @NotNull
    public CommentThreads threadsOf(
            final @NotNull UUID article,
            final @NotNull Set<Long> threadIds)
            throws NotFoundException {
        Assert.isTrue(threadIds.size() <= 5, "Batch size is too large.");
        if (!database.containsKey(article)) {
            throw new NotFoundException();
        }
        final var threadStarters = this.database.get(article).stream()
                .filter(comment -> comment.getThreadId() == null)
                .map(Comment::getCommentId)
                .collect(Collectors.toSet());
        final var threads = this.database.get(article).stream()
                .filter(comment -> comment.getThreadId() != null)
                .filter(comment -> threadIds.contains(comment.getThreadId()))
                .collect(Collectors.groupingBy(Comment::getThreadId));
        final Map<Long, List<Comment>> result = new HashMap<>();
        threadIds.forEach(id -> {
            if (!threadStarters.contains(id)) {
                return;
            }
            result.put(id, threads.getOrDefault(id, Collections.emptyList()));
        });
        return CommentThreads.builder()
                .threads(result)
                .build();
    }

}
