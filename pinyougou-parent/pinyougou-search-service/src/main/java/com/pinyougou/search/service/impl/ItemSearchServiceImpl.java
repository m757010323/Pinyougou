package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

//        Query query = new SimpleQuery("*:*");
//        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        query.addCriteria(criteria);
//        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//        map.put("rows",page.getContent());
        Map map = new HashMap();
        map.putAll(searchList(searchMap));
        return map;
    }

    private Map searchList(Map searchMap){
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
            if(highlights.size()>0 && highlights.get(0).getSnipplets().size() >0){

                TbItem item = entry.getEntity();
                //设置高亮的值
                item.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows",page.getContent());
        return map;
    }
}
