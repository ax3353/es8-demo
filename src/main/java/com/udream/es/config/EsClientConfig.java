package com.udream.es.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kun.zhu
 * @date 2022/11/18 17:36
 */
@Configuration
public class EsClientConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 创建低级客户端
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();
        // 使用Jackson映射器创建传输层
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        // 创建API客户端
        return new ElasticsearchClient(transport);
    }
}


