package com.udream.es.serivce;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.analysis.Tokenizer;
import co.elastic.clients.elasticsearch._types.analysis.TokenizerDefinition;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.IndexSettingsAnalysis;
import co.elastic.clients.json.JsonData;
import com.udream.es.dto.QaEsDataDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 描述: 智能QA系统es服务
 *
 * @author kun.zhu
 * @date 2022/11/18 17:53
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QaEsManager {

    final static String INDEX_NAME = "udream_qa";

    @NonNull
    private ElasticsearchClient client;

    public Boolean createIndex() throws IOException {
        String tokenJson = "{\"type\":\"pinyin\",\"keep_first_letter\":true,\"keep_separate_first_letter\":true," +
                "\"keep_full_pinyin\":true,\"keep_original\":false,\"limit_first_letter_length\":16,\"lowercase\":true}";
        Map<String, Tokenizer> tokenizer = new HashMap<>();
        tokenizer.put("my_pinyin", new Tokenizer.Builder().definition(new TokenizerDefinition(
                TokenizerDefinition.Kind._Custom.jsonValue(), JsonData.fromJson(tokenJson))).build());
        IndexSettingsAnalysis analysis = new IndexSettingsAnalysis.Builder()
                .analyzer("pinyin_analyzer", a -> a.custom(e -> e.tokenizer("my_pinyin")))
                .tokenizer(tokenizer).build();

        CreateIndexResponse response = client.indices().create(builder -> builder.index(INDEX_NAME)
                .settings(sb -> sb.numberOfReplicas("1").numberOfShards("1").analysis(analysis))
                .mappings(typeMappingBuilder -> typeMappingBuilder
                        .properties("id", e -> e.long_(b -> b))
                        .properties("keyword", e -> e.text(b -> b.store(false)))
                        .properties("content", e -> e.text(b -> b.analyzer("ik_max_word").searchAnalyzer("ik_max_word")))
                        .properties("sourceType", e -> e.integer(b -> b))
                        .properties("viewCount", e -> e.integer(b -> b))
                        .properties("time", e -> e.date(b -> b.format("yyyy-MM-dd HH:mm:ss").index(false).docValues(false)))
                        .properties("title", e -> e.text(t -> t.analyzer("standard")
                                .fields("keyword", b -> b.keyword(k -> k))
                                .fields("search", b -> b.searchAsYouType(k -> k))
                                .fields("pinyin", b -> b.text(v -> v.analyzer("pinyin_analyzer"))))))
        );
        log.info("acknowledged: {}", response.acknowledged());
        return response.acknowledged();
    }

    public boolean deleteIndex() throws IOException {
        DeleteIndexResponse response = client.indices().delete(e -> e.index(INDEX_NAME));
        return response.acknowledged();
    }

    public Result addDoc(@RequestBody QaEsDataDTO dto) throws IOException {
        CreateResponse createResponse = client.create(e -> e.index(INDEX_NAME)
                .id(dto.getId().toString()).document(dto));
        return createResponse.result();
    }

    public boolean bulkAddDoc(@RequestBody List<QaEsDataDTO> dtos) throws IOException {
        List<BulkOperation> list = new ArrayList<>();

        for (QaEsDataDTO dataCollect : dtos) {
            list.add(new BulkOperation.Builder().create(builder -> builder.index(INDEX_NAME)
                    .id(dataCollect.getId().toString()).document(dataCollect)).build());
        }
        BulkResponse response = client.bulk(builder -> builder.index(INDEX_NAME).operations(list));
        return !response.errors();
    }

    public QaEsDataDTO getDocById(@RequestParam String id) throws IOException {
        GetResponse<QaEsDataDTO> response = client.get(e -> e.index(INDEX_NAME).id(id), QaEsDataDTO.class);
        return response.source();
    }

    public Result deleteDocById(@RequestParam String id) throws IOException {
        DeleteResponse deleteResponse = client.delete(e -> e.index(INDEX_NAME).id(id));
        return deleteResponse.result();
    }

    /**
     * 描述: Match，对输入内容先分词再查询
     *
     * @author kun.zhu
     * @date 2022/11/21 16:42
     */
    public List<QaEsDataDTO> search(String keyword) throws IOException {
        SearchResponse<QaEsDataDTO> response = client.search(builder -> builder
                        .index(INDEX_NAME)
                        .query(queryBuilder -> queryBuilder
                                .match(matchQueryBuilder -> matchQueryBuilder
                                        .field("content").query(keyword)))
                , QaEsDataDTO.class);
        List<Hit<QaEsDataDTO>> hits = response.hits().hits();
        return hits.stream().map(Hit::source).collect(Collectors.toList());
    }

    /**
     * 描述: Multi Match，对输入内容先分词再查询
     *
     * @author kun.zhu
     * @date 2022/11/21 16:53
     */
    public List<QaEsDataDTO> searchMultiMatch(String keyword) throws IOException {
        SearchResponse<QaEsDataDTO> response = client.search(searchRequestBuilder -> searchRequestBuilder
                .index(INDEX_NAME)
                .query(queryBuilder -> queryBuilder
                        .multiMatch(multiMatchQueryBuilder -> multiMatchQueryBuilder
                                .fields("title", "content")
                                .operator(Operator.And)
                                .query(keyword))), QaEsDataDTO.class);
        List<Hit<QaEsDataDTO>> hits = response.hits().hits();
        return hits.stream().map(Hit::source).collect(Collectors.toList());
    }

    /**
     * 描述: Highlight Multi Match，对输入内容先分词再查询
     *
     * @author kun.zhu
     * @date 2022/11/21 19:52
     */
    public List<QaEsDataDTO> highlightSearchMultiMatch(String keyword) throws IOException {
        // 高亮查询
        SearchResponse<QaEsDataDTO> response = client.search(searchRequestBuilder -> searchRequestBuilder
                        .index(INDEX_NAME)
                        .query(queryBuilder -> queryBuilder.multiMatch(
                                mmb -> mmb.fields("title", "content").operator(Operator.And).query(keyword)))
                        .highlight(hl -> hl.preTags("<font color='red'>").postTags("</font>")
                                .fields("title", hb -> hb).fields("content", hb -> hb))
                , QaEsDataDTO.class);

        List<QaEsDataDTO> ret = new ArrayList<>();
        List<Hit<QaEsDataDTO>> hits = response.hits().hits();
        for (Hit<QaEsDataDTO> hit : hits) {
            QaEsDataDTO source = hit.source();
            Map<String, List<String>> highlight = hit.highlight();
            if (highlight != null) {
                List<String> defaultTitles = Collections.singletonList(source.getTitle());
                source.setTitle(highlight.getOrDefault("title", defaultTitles).get(0));
            }
            ret.add(source);
        }
        return ret;
    }
}
