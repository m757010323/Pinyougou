package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;

public interface BrandService {

    List<TbBrand> findAll();

    /**
     * 品牌分页
     * @param pageNum 当前页码
     * @param pageSize  每页记录数
     * @return
     */
    PageResult findPage(int pageNum,int pageSize);

    /**
     * 添加方法
     * @param tbBrand
     */
    public void add(TbBrand tbBrand);

    public void update(TbBrand brand);

    public TbBrand findOne(Long id);

    public void delete(Long[] ids);

    /**
     * 分页
     * @param brand
     * @param pageNum 当前页码
     * @param pageSize  一页的记录数
     * @return
     */
    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);
}
