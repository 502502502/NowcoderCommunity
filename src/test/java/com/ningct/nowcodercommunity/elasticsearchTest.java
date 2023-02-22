package com.ningct.nowcodercommunity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ningct.nowcodercommunity.entity.DiscussPost;
import com.ningct.nowcodercommunity.mapper.DiscussPostMapper;
import com.ningct.nowcodercommunity.service.ElasticSearchService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@SpringBootTest
public class elasticsearchTest {
    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private RestHighLevelClient client;
    @Resource
    private ElasticSearchService searchService;

    @Test
    public void testDelete(){
        searchService.deletePost(224);
    }
    @Test
    public void testAdd(){
        for(int i = 109; i < 287; i++){
            DiscussPost post = discussPostMapper.selectDiscussPostById(i);
            if(post != null){
                searchService.addPost(post);
            }
        }
    }

    @Test
    public void testSearchService(){
        System.out.println(searchService.findPostCount("互联网"));
        List<DiscussPost> posts = searchService.searchPosts("互联网", 0, 10000);
        for (DiscussPost post : posts) {
            System.out.println(post.toString());
        }
        System.out.println(posts.size());
    }

    public void init(){
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.176.100", 9200, "http")));

    }
    public void close(){
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConnect(){
        init();
        close();
    }
    @Test
    public void testIndexCreat(){
        //创建索引

        //创建索引请求
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("discusspost");
        try {
            //es客户端执行请求，获取响应
            CreateIndexResponse createIndexResponse =
                    client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            //响应状态
            boolean acknowledged = createIndexResponse.isAcknowledged();
            System.out.println("索引操作：" +acknowledged);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testIndexSearch(){
        //查询索引

        //查询索引请求
        GetIndexRequest getIndexRequest = new GetIndexRequest("discusspost");
        try {
            //es客户端执行请求，获取响应
            GetIndexResponse getIndexResponse =
                    client.indices().get(getIndexRequest, RequestOptions.DEFAULT);
            //响应内容
            System.out.println(getIndexResponse.getAliases());
            System.out.println(getIndexResponse.getMappings());
            System.out.println(getIndexResponse.getSettings());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testIndexDelete(){
        //查询索引

        //查询索引请求
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("discusspost");
        try {
            //es客户端执行请求，获取响应
            AcknowledgedResponse response =
                    client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            //响应内容
            System.out.println("删除索引："+response.isAcknowledged());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDocumentInert() {
        init();

        //查询索引

        try {
            //封装对象为json字符串
            DiscussPost post = discussPostMapper.selectDiscussPostById(231);
            post.setContent("我是新人，我要灌水！");
            ObjectMapper objectMapper = new ObjectMapper();
            String userJson = objectMapper.writeValueAsString(post);

            //创建请求
            IndexRequest indexRequest = new IndexRequest()
                    .index("discusspost")
                    .id(String.valueOf(231))
                    .source(userJson, XContentType.JSON);


            //执行请求，获取响应
            IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);


            //响应处理
            System.out.println("操作："+response.getResult());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        close();
    }
    @Test
    public void testDocUpdata(){
        init();

        //查询索引

        try {
            //创建请求
            UpdateRequest updateRequest = new UpdateRequest()
                    .index("discusspost")
                    .id("231")
                    .doc(XContentType.JSON,"content","我今年一定要灌水");


            //执行请求，获取响应
            UpdateResponse response = client.update(updateRequest,RequestOptions.DEFAULT);


            //响应处理
            System.out.println("操作："+response.getResult());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        close();
    }

    @Test
    public void testDocSearch(){

        //查询索引

        try {
            //创建请求
            GetRequest getRequest = new GetRequest()
                    .index("discusspost")
                    .id("231");


            //执行请求，获取响应
            GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);


            //响应处理
            System.out.println(response.getSource());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testDocDelete(){
        init();

        //查询索引

        try {
            //创建请求
            DeleteRequest deleteRequest = new DeleteRequest()
                    .index("discusspost")
                            .id("231");

            //执行请求，获取响应
            DeleteResponse response = client.delete(deleteRequest, RequestOptions.DEFAULT);

            //响应处理
            System.out.println("操作："+response.status());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        close();
    }
    @Test
    public void testDocBulkinsert(){
        init();

        //查询索引

        try {
            //创建请求
            BulkRequest bulkRequest = new BulkRequest()
                    .add(new DeleteRequest().index("discusspost").id("110"))
                    .add(new DeleteRequest().index("discusspost").id("116"))
                    .add(new DeleteRequest().index("discusspost").id("119"));

            //执行请求，获取响应
            BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);


            //响应处理
            System.out.println(response.getTook());
            System.out.println(response.getItems());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        close();
    }
    @Test
    public  void testSearch(){

        try {
            //创建搜索条件
            SearchSourceBuilder builder = new SearchSourceBuilder()
                    .query(QueryBuilders.matchAllQuery());

            //创建请求
            SearchRequest searchRequest = new SearchRequest()
                    .indices("discusspost")
                    .source(builder);
            //执行请求，获取响应
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            //响应处理
            SearchHits hits = response.getHits();
            System.out.println(hits.getTotalHits());
            System.out.println(response.getTook());
            for (SearchHit hit : hits) {
                System.out.println(hit);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    @Test
    public void testSearchCondition(){

        try {
            //构建查询条件

                //等值
            TermsQueryBuilder termsQuery = QueryBuilders
                    .termsQuery("userId", new int[]{102, 103});
                //范围
            RangeQueryBuilder rangeQuery = QueryBuilders
                    .rangeQuery("id")
                    .lt(150)
                    .gt(100);
                //模糊
            FuzzyQueryBuilder fuzzyQuery = QueryBuilders
                    .fuzzyQuery("title", "互联万")
                    .fuzziness(Fuzziness.TWO);

                //组合
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .must(rangeQuery)
                    .must(termsQuery)
                    .must(fuzzyQuery);

            //过滤字段
            String[] includes = {"id","userId","content"};
            String[] excludes = {};

            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder()
                    .field("content")
                    .preTags("<em>")
                    .postTags("</em>");

            //分组
            AggregationBuilder aggregationBuilder = AggregationBuilders
                    .terms("userGroup")
                    .field("userId");

            //构建查询结构
            SearchSourceBuilder builder = new SearchSourceBuilder()
                    .fetchSource(includes,excludes)
                    .query(boolQuery)
                    .from(0)
                    .size(2)
                    .sort("id", SortOrder.DESC)
                    .highlighter(highlightBuilder)
                    .aggregation(aggregationBuilder);

            //构建请求
            SearchRequest searchRequest = new SearchRequest()
                    .indices("discusspost")
                    .source(builder);

            //执行请求，获取响应
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            //处理响应
            SearchHits hits = response.getHits();
            System.out.println(hits.getTotalHits());
            System.out.println(response.getTook());
            for (SearchHit hit : hits) {
                System.out.println(hit.getSourceAsString());
            }


        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}
