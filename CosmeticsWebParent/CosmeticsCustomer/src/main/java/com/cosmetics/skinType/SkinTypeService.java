package com.cosmetics.skinType;

import com.cosmetics.common.entity.SkinResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class SkinTypeService {

    private final RestTemplate restTemplate;

    @Value("${skin.api.url:http://localhost:8000/analyze}")
    private String skinApiUrl;

    public SkinTypeService() {
        this.restTemplate = new RestTemplate();
    }

    public SkinResult analyze(MultipartFile file) throws IOException {

        // Header multipart
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Body form-data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<SkinResult> response =
                restTemplate.postForEntity(skinApiUrl, requestEntity, SkinResult.class);

        return response.getBody();
    }
}
