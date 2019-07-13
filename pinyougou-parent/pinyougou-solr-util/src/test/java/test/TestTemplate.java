package test;


import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:spring/applicationContext-solr.xml")
public class TestTemplate {
    @Autowired
    private SolrTemplate solrTemplate;

    @Test
    public void test1(){
        TbItem item=new TbItem();
        item.setId(1L);
        item.setBrand("华为");
        item.setCategory("手机");
        item.setGoodsId(1L);
        item.setSeller("华为2号专卖店");
        item.setTitle("华为Mate9");
        item.setPrice(new BigDecimal(2000));
        solrTemplate.saveBean(item);
        solrTemplate.commit();
    }

    @Test
    public void getId(){
        TbItem byId = solrTemplate.getById(1, TbItem.class);
        System.out.println(byId);
    }

    @Test
    public void dele(){
        solrTemplate.deleteById("1");
        solrTemplate.commit();
    }

    @Test
    public void insertmany(){
        List<TbItem> list = new ArrayList<TbItem>();
        for (int i = 0; i <100 ; i++) {
            TbItem item = new TbItem();
            item.setId(i+1L);
            item.setBrand("一加");
            item.setCategory("手机");
            item.setGoodsId(1L);
            item.setSeller("一加专卖店");
            item.setTitle("一加"+i+"pro");
            item.setPrice(new BigDecimal(4999));
            list.add(item);
        }

        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Test
    public void testPage(){
        Query query = new SimpleQuery("*:*");
        query.setOffset(20);//开始索引
        query.setRows(20);//每页记录数
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query,TbItem.class);
        System.out.println(page.getTotalElements());
        List<TbItem> content = page.getContent();
        for (TbItem tbItem : content) {
            System.out.println(tbItem.getTitle());
            System.out.println(tbItem.getPrice());
        }
    }

    @Test
    public void deleteAll(){
        Query query=new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    @Test
    public void findOne(){
        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_title");
        criteria = criteria.and("item_title").contains("2");
        query.addCriteria(criteria);
        query.setOffset(0);
        query.setRows(100);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        System.out.println("总记录数"+page.getTotalElements());
        List<TbItem> content = page.getContent();
        for (TbItem tbItem : content) {
            System.out.println(tbItem.getTitle());
            System.out.println(tbItem.getPrice());
        }
    }
}
