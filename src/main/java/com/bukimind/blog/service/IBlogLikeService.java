package com.bukimind.blog.service;

import com.bukimind.blog.dto.BlogLikeDTO;

public interface IBlogLikeService {

    BlogLikeDTO likeBlog(Integer blogId);

    void unlikeBlog(Integer blogId);

    long getLikeCount(Integer blogId);

    boolean hasUserLiked(Integer blogId);
}

