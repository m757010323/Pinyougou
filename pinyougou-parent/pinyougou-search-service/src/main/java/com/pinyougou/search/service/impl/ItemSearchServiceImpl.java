package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        Map map = new HashMap();
        //保存高亮显示数据
        map.putAll(searchList(searchMap));

        //保存根据关键字查询数据
        List<String> categoryList = searchCategoryList(searchMap);

        map.put("categoryList",categoryList);
        if(categoryList.size()>0){
            System.out.println("11111111111111111111111111");
            map.putAll(searchBrandAndSpecList(categoryList.get(0)));
        }
        return map;
    }

    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if(templateId!=null){
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("brandList",brandList);
            map.put("specList",specList);

        }

        return map;
    }

    private List<String> searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList();
        Query query = new SimpleQuery("*:*");
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));//where....
        query.addCriteria(criteria);
        //按照关键字分组
        GroupOptions groupoptions = new GroupOptions().addGroupByField("item_category");//group...by
        query.setGroupOptions(groupoptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据分组列得到分组结果集
        GroupResult<TbItem> result = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> entries = result.getGroupEntries();
        //得到分组结果入口集合
        for (GroupEntry<TbItem> entry : entries) {
            list.add(entry.getGroupValue());//将分组结果封装到数据集中
        }
        return list;
    }

    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightoptions = new HighlightOptions().addField("item_title");//设置高亮的域
        highlightoptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightoptions.setSimplePostfix("</em>");//高亮后缀
        query.setHighlightOptions(highlightoptions);//设置高亮选项
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        for (HighlightEntry<TbItem> entry : page.getHighlighted()) {//每条记录的高亮入口
            //获取高亮列表,根据上文设置的高亮域有多少
            List<HighlightEntry.Highlight> highlights = entry.getHighlights();
            //并且每个域可能存储多值
            if (highlights.size() > 0 && highlights.get(0).getSnipplets().size() > 0) {

                TbItem item = entry.getEntity();
                //设置高亮的值
                item.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", page.getContent());
        return map;
    }
}
