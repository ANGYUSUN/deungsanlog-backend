package com.example.test1.batch.client;

import com.example.test1.batch.dto.ApiMountainDto;
import com.example.test1.batch.dto.ApiMountainResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Data
@Component
@RequiredArgsConstructor
public class MountainApiClient {

    private final RestTemplate restTemplate;

    public MountainApiClient() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(0,
                new org.springframework.http.converter.StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    @Value("${publicdata.api.key}")
    private String serviceKey;

    @PostConstruct
    public void checkKey() {
        System.out.println("✅ Loaded API Key: " + serviceKey);
    }

    private static final String BASE_URL = "http://api.forest.go.kr/openapi/service/trailInfoService/getforeststoryservice";

    public List<ApiMountainDto> searchByName(String mountainName) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("serviceKey", serviceKey)
                .queryParam("mntnnm", URLEncoder.encode(mountainName, StandardCharsets.UTF_8))
                .queryParam("numOfRows", 10)
                .queryParam("pageNo", 1)
                .queryParam("_type", "xml");

        URI uri = builder.build(false).toUri();

        try {
            String xml = restTemplate.getForObject(uri, String.class);
            System.out.println("🔍 API Response for " + mountainName + ":\n" + xml);
            XmlMapper xmlMapper = new XmlMapper();
            ApiMountainResponse response = xmlMapper.readValue(xml, ApiMountainResponse.class);

            if (response == null || response.getBody() == null || response.getBody().getItems() == null) {
                System.out.println("❌ No data found for: " + mountainName);
                return Collections.emptyList();
            }

            return response.getBody().getItems().getItem();
        } catch (Exception e) {
            System.out.println("❌ API 요청 실패: " + mountainName);
            e.printStackTrace();
            return Collections.emptyList();
        }

    }
}
