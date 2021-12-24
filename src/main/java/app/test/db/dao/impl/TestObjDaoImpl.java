package app.test.db.dao.impl;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Component;

import app.test.db.dao.Dao;

@Component("testObjDao")
public class TestObjDaoImpl extends SqlSessionDaoSupport implements Dao {

	public Integer selectDual() {
		SqlSession sqlSession = getSqlSession();
		return sqlSession.selectOne("TEST_OBJ.selectDual");
	}
}