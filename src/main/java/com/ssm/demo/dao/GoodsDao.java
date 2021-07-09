package com.ssm.demo.dao;

import com.ssm.demo.entity.AdminUser;
import com.ssm.demo.entity.Goods;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 13
 * @date 2018-08-15
 */
public interface GoodsDao {
    /**
     * 返回相应的数据集合
     *
     * @param map
     * @return
     */
    List<Goods> findGoodsByPages(Map<String, Object> map);

    /**
     * 数据数目
     * @param storeId
     * @return
             */
    List<Goods> getAliveGoodsByStore(int storeId);

    /**
     * 添加商品
     *
     * @return
     */
    int insertGoods(Goods goods);

    /**
     * 获取商品总数
     *
     * @return
     */
    int getTotalGoods(Map<String, Object> map);

    /**
     * 根据店铺和商品ID搜索商品
     *
     * @return
     */
    Goods selectByGoodIdAndStore(Goods goods);

    /**
     * 批量新增商品记录
     *
     * @return
     */
    int insertGoodsBatch(@Param("goodsList") List<Goods> goodsList);

    /**
     * 更新商品修改记录
     *
     * @return
     */
    int updateGoodsUpdateTimes(Goods goods);

    /**
     * 下架所有商品
     *
     * @return
     */
    int downAllGoods();

    /**
     * 上架商品
     *
     * @return
     */
    int upGoodsByGoodsIds(@Param("goodsIdArray")ArrayList<String> goodsIdArray, @Param("storeId")int storeId);


    /**
     * 修改商品价格
     *
     * @return
     */
    int updateGoodsPrice(@Param("goodsId")String goodsId, @Param("price")double price);

}
