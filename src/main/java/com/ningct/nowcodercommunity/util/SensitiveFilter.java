package com.ningct.nowcodercommunity.util;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger= LoggerFactory.getLogger(SensitiveFilter.class);

    //替换词
    private static final String REPLACEEMENT = "***";

    //根节点
    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init(){
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-word.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while((keyword = reader.readLine()) != null){
                this.addKeyword(keyword);
            }

        }catch (IOException e){
            logger.error("加载敏感词文件失败！" +e.getMessage());
        }
    }

    //将一个敏感词加入字典树
    public void addKeyword(String keyword){
        TrieNode cur = root;
        for (char c : keyword.toCharArray()) {
            TrieNode subNode = cur.getSubNode(c);
            if(subNode == null){
                subNode = new TrieNode();
                cur.subNodes.put(c,subNode);
            }
            cur = subNode;
        }
        cur.setKeywordEnd(true);
    }

    //过滤敏感词
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        char[] s = text.toCharArray();
        //初始化指针
        TrieNode cur = root;
        int st = 0;
        int end = 0;
        //暂存过滤后的字符串
        StringBuilder sb = new StringBuilder();
        while(end < s.length){
            char c = s[end];
            //跳过字符
            if(isSymbol(c)){
                if(cur == root){
                    sb.append(c);
                    st++;
                }
                end++;
                continue;
            }
            //根据字符获取子节点
            cur = cur.getSubNode(c);
            //不是敏感词，第一个字符暂存
            if(cur == null){
                sb.append(s[st]);
                st++;
                end = st;
                cur = root;
            }
            //是敏感词，跳过这一段
            else if(cur.isKeywordEnd()){
                sb.append(REPLACEEMENT);
                st = end +1;
                end = st;
                cur = root;
            }
            //是敏感字符，但是不是敏感词，检查下一个字符
            else{
                end++;
            }
        }
        //处理末尾字符
        sb.append(text.substring(st));
        return sb.toString();
    }

    //判断是不是字符
    public boolean isSymbol(Character c){
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //树结点定义
    private class TrieNode{
        //是否是关键词
        private boolean isKeywordEnd = false;
        //子节点
        Map<Character, TrieNode> subNodes = new HashMap<>();


        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c,node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }

}
