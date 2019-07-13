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
        //1.保存高亮显示数据
        map.putAll(searchList(searchMap));

        //2.保存根据关键字查询数据
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);
        //3.查询品牌和规格列表
        String category = (String) searchMap.get("category");
        if(!"".equals(category)){
            map.putAll(searchBrandAndSpecList(category));
        }else{
            if(categoryList.size()>0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
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
        //高亮设置初始化
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightoptions = new HighlightOptions().addField("item_title");//设置高亮的域
        highlightoptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightoptions.setSimplePostfix("</em>");//高亮后缀
        query.setHighlightOptions(highlightoptions);//设置高亮选项
        //1.1按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2 按照商品分类过滤查询
        if(!"".equals(searchMap.get("category"))){//如果用户选择了分类
            FilterQuery categoryQuery = new SimpleFacetQuery();
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            categoryQuery.addCriteria(categoryCriteria);
            query.addFilterQuery(categoryQuery);
        }

        //1.3 按照品牌分类过滤
        if(!"".equals(searchMap.get("brand"))){
            FilterQuery brandQuery = new SimpleFacetQuery();
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            brandQuery.addCriteria(brandCriteria);
            query.addFilterQuery(brandQuery);
        }

        //1.4 按照规格选项过滤
        if(!"".equals(searchMap.get("spec"))){
            Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String spec : specMap.keySet()) {

                FilterQuery specQuery = new SimpleFacetQuery();
                Criteria specCriteria = new Criteria("item_spec_"+spec).is(specMap.get(spec));
                specQuery.addCriteria(specCriteria);
                query.addFilterQuery(specQuery);
            }
        }

        //1.5 按照价格筛选
        if(!"".equals(searchMap.get("price"))){
            String[] price = ((String) searchMap.get("price")).split("-");
            if(!price[0].equals("0")){//如果区间起点不等于0
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if(!price[1].equals("*")){//如果区间终点不等于*
                Criteria filterCriteria=new  Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6 分页查找筛选

        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo==null){
            pageNo=1;//默认第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageSize==null){
            pageSize=20;//默认每页20条数据
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        //*********************** 获得结果集********************************************
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
        map.put("totalPages",page.getTotalPages());//返回总页数
        map.put("total",page.getTotalElements());//返回总记录数
        return map;
    }
}
