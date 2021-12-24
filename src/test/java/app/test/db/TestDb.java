package app.test.db;

import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import app.test.db.dao.Dao;

public class TestDb {

	private Logger logger = Logger.getLogger(TestDb.class);
	private ApplicationContext applicationContext;
	private Dao dao = null;
	private DataSourceTransactionManager txnMgr = null;
	private static final Integer LOOP_COUNT = 100000;
	private static Integer INTERVAL = 10000;
	private TransactionStatus status = null;
	
	@Before
	public void init() {
		applicationContext = new ClassPathXmlApplicationContext("**/applicationContext.xml");
		txnMgr = applicationContext.getBean(DataSourceTransactionManager.class);
		dao = (Dao)applicationContext.getBean("testObjDao");
		assertNotNull(dao);
		assertNotNull(txnMgr);
	}

	@After
	public void cleanup() {
		if (applicationContext != null) {
			((ConfigurableApplicationContext) applicationContext).close();
		}
	}

	private void performDAOQuery(Boolean willPerformIntervalCommit) throws Exception {

		logger.info("perform query " + (LOOP_COUNT) + " times, willPerformIntervalCommit? "+willPerformIntervalCommit);
		Long now = System.nanoTime();
		for (int i = 0; i < LOOP_COUNT; i++) {
			dao.selectDual();
			if (i != 0 && i % INTERVAL == 0) {
				logger.info("processDaoQuery @ " + (i) + " records\t" + (System.nanoTime() - now)+"(ns)");
				if (willPerformIntervalCommit) {
					getTransation();
				}
				now = System.nanoTime();
			}
		}
		logger.info("processDaoQuery @ " + (LOOP_COUNT) + " records\t" + (System.nanoTime() - now)+"(ns)");
		now = System.nanoTime();
	}
	
	private DefaultTransactionDefinition prpareTransationDef() {

		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("transaction");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		
		return def;
	}
	
	private void getTransation() {
		if (status != null) {
			txnMgr.commit(status);
		}
		status = txnMgr.getTransaction(prpareTransationDef());
	}

	@Test
	public void processWithTxnMgr() throws Exception {
		logger.info("START "+ new Object(){}.getClass().getEnclosingMethod().getName());

		getTransation();
		try {
			performDAOQuery(true);
		} catch (Exception e) {
			txnMgr.rollback(status);
			logger.error(e.getMessage(), e);
			throw e;
		}
		txnMgr.commit(status);
		logger.info("END "+ new Object(){}.getClass().getEnclosingMethod().getName());
	}

	@Test
	public void processWithoutTxnMgr() throws Exception {
		logger.info("START "+ new Object(){}.getClass().getEnclosingMethod().getName());

		getTransation();
		try {
			performDAOQuery(false);
		} catch (Exception e) {
			txnMgr.rollback(status);
			logger.error(e.getMessage(), e);
			throw e;
		}
		txnMgr.commit(status);
		logger.info("END "+ new Object(){}.getClass().getEnclosingMethod().getName());
	}

}
