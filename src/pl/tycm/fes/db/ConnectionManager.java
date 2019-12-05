package pl.tycm.fes.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

@Stateless
@LocalBean
public class ConnectionManager {
	static Logger logger = Logger.getLogger(ConnectionManager.class);
	
	public static Connection getConnection() throws SQLException {
		try {
			Context initContext = new InitialContext();
			DataSource ds = (DataSource) initContext.lookup("java:jboss/datasources/OracleDS");
			
			return ds.getConnection();
		} catch (NamingException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static boolean commit(Connection c) {
		if (c != null) {
			try {
				if (!c.getAutoCommit()) {
					c.commit();
					return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				rollback(c);
			}
		}
		return false;
	}

	public static void rollback(Connection c) {
		if (c != null) {
			try {
				if (!c.getAutoCommit()) {
					c.rollback();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static void close(Connection c) {
		if (c != null) {
			try {
				if (!c.getAutoCommit()) {
					// c.setAutoCommit(true);
				}
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static void close(PreparedStatement ps) {
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static void close(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
			}
		}
	}

}
