/**
 * 
 */
package br.com.psystems.crud.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.psystems.crud.exception.DAOException;
import br.com.psystems.crud.exception.SystemException;
import br.com.psystems.crud.infra.TransactionCallback;
import br.com.psystems.crud.model.Vendor;

/**
 * @author rafael.saldanha
 *
 */
public class VendorDAO extends AbstractDAO<Vendor> {


	public VendorDAO() throws DAOException {
		super();
	}

	public static final String TABLE_NAME = "tb_vendor";
	protected static final String SQL_FIND_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
	private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (name, description) VALUES (?,?)";
	private static final String SQL_UPDATE = "UPDATE " + TABLE_NAME + " SET name = ?, description = ? WHERE id = ?";
	private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
	private static final String SQL_FIND_ALL = "SELECT * FROM " + TABLE_NAME + "";
	private static final String SQL_FIND_BY_NOME = "SELECT * FROM " + TABLE_NAME + " WHERE UPPER(name) like UPPER(?)";
	
	private static Logger logger = Logger.getLogger(VendorDAO.class);
	
	@Override
	public void save(Vendor entity) throws DAOException, SystemException {

		connectionManager.doInTransaction(new TransactionCallback() {
			
			@Override
			public void execute(Connection connection) throws SQLException, DAOException, SystemException {
				
				PreparedStatement ps = null;
				ps = getPreparedStatement(connection, SQL_INSERT);
				ps.setString(1, entity.getName());
				ps.setString(2, entity.getDescription());

				ps.executeUpdate();

				logger.info("Fornecedor inserido com sucesso!\n ".concat(entity.toString()));
			}
		});
	}

	@Override
	public Vendor update(Vendor entity) throws DAOException, SystemException {

		connectionManager.doInTransaction(new TransactionCallback() {
			
			@Override
			public void execute(Connection connection) throws SQLException, DAOException, SystemException {
				
				PreparedStatement ps = null;
				ps = getPreparedStatement(connection, SQL_UPDATE);
				ps.setString(1, entity.getName());
				ps.setString(2, entity.getDescription());
				ps.setLong(3, entity.getId());

				int qtdLinhas = ps.executeUpdate();

				if (0 >= qtdLinhas) {
					logger.info("Nenhum registro alterado.");
				}

				logger.info("Fornecedor atualizado com sucesso!\n ".concat(entity.toString()));
			}
		});
		
		return findById(entity.getId());
	}

	@Override
	public void delete(Long id) throws DAOException, SystemException {

		connectionManager.doInTransaction(new TransactionCallback() {
			
			@Override
			public void execute(Connection connection) throws SQLException, DAOException, SystemException {
				
				PreparedStatement ps = null;
				ps = getPreparedStatement(connection, SQL_DELETE);
				ps.setLong(1, id);

				int qtdLinhas = ps.executeUpdate();

				if (0 >= qtdLinhas) {
					throw new DAOException("Nenhum registro apagado.");
				}

				logger.info("Fornecedor apagado com sucesso!");
			}
		});
	}

	@Override
	public Vendor findById(Long id) throws DAOException, SystemException {

		Connection con = null;
		
		try {
			con = connectionManager.getConnection();
			PreparedStatement ps = getPreparedStatement(con, SQL_FIND_BY_ID);
			ps.setLong(1, id);

			return getVendor(ps.executeQuery());
			
		} catch (Exception e) {
			set(e);
			return null;
		} finally {
			connectionManager.close(con);
		}

	}

	@Override
	public List<Vendor> findByName(String nome) throws DAOException, SystemException {

		Connection con = null;
		
		try {
			con = connectionManager.getConnection();
			PreparedStatement ps = getPreparedStatement(con, SQL_FIND_BY_NOME);
			ps.setString(1, "%" + nome + "%");
			
			return getVendors(ps.executeQuery());
			
		} catch (Exception e) {
			set(e);
			return null;
		} finally {
			connectionManager.close(con);
		}
	}

	@Override
	public List<Vendor> getAll() throws DAOException, SystemException {
		
		Connection con = null;
		
		try {
			con = connectionManager.getConnection();
			PreparedStatement ps = getPreparedStatement(con, SQL_FIND_ALL);
			
			return getVendors(ps.executeQuery());
			
		} catch (Exception e) {
			set(e);
			return null;
		} finally {
			connectionManager.close(con);
		}
	}
	
	private List<Vendor> getVendors(ResultSet rs) throws SQLException {
		List<Vendor> vendors = new ArrayList<>();
		
		while (rs.next())
			vendors.add(createVendor(rs));
		
		return vendors;
	}

	private Vendor getVendor(ResultSet rs) throws SQLException {
		rs.next();
		return createVendor(rs);
	}

	private Vendor createVendor(ResultSet rs) throws SQLException {
		Vendor fornecedor = new Vendor();
		fornecedor.setId(rs.getLong("id"));
		fornecedor.setName(rs.getString("name"));
		fornecedor.setDescription(rs.getString("description"));
		return fornecedor;
	}

}