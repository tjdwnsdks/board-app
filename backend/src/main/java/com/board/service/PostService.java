package com.board.service;

import com.board.dto.CommentDto;
import com.board.dto.PostDto;
import com.board.entity.Comment;
import com.board.entity.Post;
import com.board.entity.User;
import com.board.repository.CommentRepository;
import com.board.repository.PostRepository;
import com.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public Page<PostDto.ListResponse> getPosts(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = (keyword != null && !keyword.isBlank())
                ? postRepository.searchByKeyword(keyword, pageable)
                : postRepository.findAllWithUser(pageable);

        return posts.map(p -> PostDto.ListResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .authorNickname(p.getUser().getNickname())
                .viewCount(p.getViewCount())
                .commentCount(p.getComments() != null ? p.getComments().size() : 0)
                .createdAt(p.getCreatedAt())
                .build());
    }

    @Transactional
    public PostDto.DetailResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        post.setViewCount(post.getViewCount() + 1);

        List<CommentDto.Response> comments = commentRepository.findByPostIdWithUser(id).stream()
                .map(c -> CommentDto.Response.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .authorNickname(c.getUser().getNickname())
                        .authorUsername(c.getUser().getUsername())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PostDto.DetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getUser().getNickname())
                .authorUsername(post.getUser().getUsername())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(comments)
                .build();
    }

    @Transactional
    public PostDto.DetailResponse createPost(PostDto.Request request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        Post saved = postRepository.save(post);
        return getPost(saved.getId());
    }

    @Transactional
    public PostDto.DetailResponse updatePost(Long id, PostDto.Request request, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        return getPost(id);
    }

    @Transactional
    public void deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username).orElseThrow();
        if (!post.getUser().getUsername().equals(username) && !user.getRole().equals("ADMIN")) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }
        postRepository.delete(post);
    }

    @Transactional
    public CommentDto.Response addComment(Long postId, CommentDto.Request request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(user)
                .post(post)
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentDto.Response.builder()
                .id(saved.getId())
                .content(saved.getContent())
                .authorNickname(user.getNickname())
                .authorUsername(user.getUsername())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!comment.getUser().getUsername().equals(username) && !user.getRole().equals("ADMIN")) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }
}
