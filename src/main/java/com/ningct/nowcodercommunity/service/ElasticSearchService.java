package com.ningct.nowcodercommunity.service;

import com.ningct.nowcodercommunity.entity.DiscussPost;
import com.ningct.nowcodercommunity.util.CommunityUtil;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//全文搜索的处理类
@Service
public class ElasticSearchService {
    @Resource
    private RestHighLevelClient client;

    //向全文搜索添加帖子
    public void addPost(DiscussPost post){
        //获取JSON对象
        String jsonPost = CommunityUtil.getJsonFromPost(post);
        //构造请求
        IndexRequest request = new IndexRequest()
                .index("discusspost")
                .id(String.valueOf(post.getId()))
                .source(jsonPost,XContentType.JSON);
        //执行请求
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("elasticsearch添加数据失败！"+e.getMessage());
        }
    }

    //在全文搜索删除帖子
    public void deletePost(int id) {
        //构造请求
        DeleteRequest request = new DeleteRequest()
                .index("discusspost")
                .id(String.valueOf(id));
        //执行请求
        try {
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("elasticsearch删除失败！"+e.getMessage());
        }
    }

    //查找在全文搜索里的指定关键词的帖子数量
    public int findPostCount(String keyword){
        //查询字段
        MultiMatchQueryBuilder query = QueryBuilders.multiMatchQuery(keyword, "title", "content");

        //构建查询结构
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .query(query);
        //构建请求
        SearchRequest searchRequest = new SearchRequest()
                .indices("discusspost")
                .source(builder);
        //执行请求，获取响应
        SearchResponse response = null;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("elaeticsearch搜索失败！" +e.getMessage());
        }
        //处理响应
        return (int) response.getHits().getTotalHits().value;
    }

    //检索指定关键词的帖子
    public List<DiscussPost> searchPosts(String keyword, int offset, int limit){
        //查询字段
        MultiMatchQueryBuilder query = QueryBuilders.multiMatchQuery(keyword, "title", "content");

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("content")
                .field("title")
                .preTags("<em>")
                .postTags("</em>");
        //构建查询结构
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .query(query)
                .sort("type", SortOrder.DESC)
                .sort("score",SortOrder.DESC)
                .sort("createTime.keyword",SortOrder.DESC)//text不支持排序，使用其分词之后的keyword作为关键字
                .from(offset)
                .size(limit)
                .highlighter(highlightBuilder);
        //构建请求
        SearchRequest searchRequest = new SearchRequest()
                .indices("discusspost")
                .source(builder);

        //执行请求，获取响应
        SearchResponse response = null;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("elaeticsearch搜索失败！" +e.getMessage());
        }

        //处理响应
        SearchHits hits = response.getHits();
        List<DiscussPost> list = new ArrayList<>();

        for (SearchHit hit : hits) {
            DiscussPost post = CommunityUtil.getPostFromMap(hit.getSourceAsString());
            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                post.setTitle(titleField.getFragments()[0].toString());
            }

            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                post.setContent(contentField.getFragments()[0].toString());
            }

            list.add(post);
        }
        return list;
    }

}
