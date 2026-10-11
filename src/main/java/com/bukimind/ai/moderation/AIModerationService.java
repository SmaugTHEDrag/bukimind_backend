package com.bukimind.ai.moderation;

import com.bukimind.common.exception.ToxicContentException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIModerationService implements IAIModerationService{
    private final RestTemplate restTemplate;

    @Value("${ai.moderation.url}")
    private String AI_MODERATION_URL;

    // check toxic content
    @Override
    public void checkComment(String comment, String errorMessage){
        try {
            restTemplate.postForEntity(AI_MODERATION_URL, Map.of("text", comment), Void.class);
        } catch(HttpClientErrorException.BadRequest e){
            throw new ToxicContentException(HttpStatus.BAD_REQUEST,errorMessage);
        }
    }
}
