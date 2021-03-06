package com.ssm.demo.service.impl;

import com.ssm.demo.common.Constants;
import com.ssm.demo.dao.ArticleDao;
import com.ssm.demo.entity.Article;
import com.ssm.demo.entity.Picture;
import com.ssm.demo.redis.RedisUtil;
import com.ssm.demo.service.ArticleService;
import com.ssm.demo.utils.PageResult;
import com.ssm.demo.utils.PageUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Service("articleService")
public class ArticleServiceImpl implements ArticleService {
    final static Logger logger = Logger.getLogger(ArticleServiceImpl.class);
    @Resource
    private RedisUtil redisUtil;
    @Autowired
    private ArticleDao articleDao;

    @Override
    public PageResult getArticlePage(PageUtil pageUtil) {
        List<Article> articleList = articleDao.findArticles(pageUtil);
        int total = articleDao.getTotalArticles(pageUtil);
        PageResult pageResult = new PageResult(articleList, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public Article queryObject(Integer id) {
        logger.info("根据id获取文章数据:" + id);
        Article article = (Article) redisUtil.get(Constants.ARTICLE_CACHE_KEY + id, Article.class);
        if (article != null) {
            logger.info("文章数据已存在于redis中直接读取:" + Constants.ARTICLE_CACHE_KEY + id);
            return article;
        }
        Article articleFromMysql = articleDao.getArticleById(id);
        if (articleFromMysql != null) {
            logger.info("redis中无此文章的数据,从MySQL数据库中读取文章并存储至redis中:" + Constants.ARTICLE_CACHE_KEY + id);
            redisUtil.put(Constants.ARTICLE_CACHE_KEY + articleFromMysql.getId(), articleFromMysql);
            return articleFromMysql;
        }
        return null;
    }

    @Override
    public List<Article> queryList(Map<String, Object> map) {
        List<Article> articles = articleDao.findArticles(map);
        return articles;
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return articleDao.getTotalArticles(map);
    }

    @Override
    public int save(Article article) {
        try {
            return articleDao.insertArticle(article);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int update(Article article) {
        article.setUpdateTime(new Date());
        if (articleDao.updArticle(article) > 0) {
            logger.info("文章修改成功，更新redis中的文章数据:" + Constants.ARTICLE_CACHE_KEY + article.getId());
            redisUtil.del(Constants.ARTICLE_CACHE_KEY + article.getId());
            redisUtil.put(Constants.ARTICLE_CACHE_KEY + article.getId(), article);
            return 1;
        }
        return 0;
    }

    @Override
    public int delete(Integer id) {
        if (articleDao.delArticle(id) > 0) {
            redisUtil.del(Constants.ARTICLE_CACHE_KEY + id);
        }
        return 0;
    }

    @Override
    public int deleteBatch(Integer[] ids) {
        if (articleDao.deleteBatch(ids) > 0) {
            for (int i = 0; i < ids.length; i++) {
                redisUtil.del(Constants.ARTICLE_CACHE_KEY + ids[i]);
            }
        }
        return 0;
    }

    @Override
    public int test( ExecutorService exec) {
        Date now = new Date();
        //创建线程池
        for (int i = 0; i < 100; i++) {
            exec.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        Article article = new Article();
                        article.setAddName("13");
                        article.setArticleTitle("数据插入测试:" + i + "线程:" + Thread.currentThread().getId());
                        article.setArticleContent("数据插入测试" + i + "线程:" + Thread.currentThread().getName());
                        article.setCreateTime(now);
                        articleDao.insertArticle(article);
                    }
                }
            });
        }
        return 0;
    }
}
