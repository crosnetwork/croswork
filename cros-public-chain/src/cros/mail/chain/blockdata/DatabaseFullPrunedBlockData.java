/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package cros.mail.chain.blockdata;

import cros.mail.chain.core.*;
import cros.mail.chain.script.ChainScript;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

/**
 * 
 * @author CROS
 *
 */
public abstract class DatabaseFullPrunedBlockData implements FullPrunedBlockData {
	private static final Logger log = LoggerFactory.getLogger(DatabaseFullPrunedBlockData.class);

	private static final String CHAIN_HEAD_SETTING = "chainhead";
	private static final String VERIFIED_CHAIN_HEAD_SETTING = "verifiedchainhead";
	private static final String VERSION_SETTING = "version";

	private static final String DROP_SETTINGS_TABLE = "DROP TABLE settings";
	private static final String DROP_HEADERS_TABLE = "DROP TABLE headers";
	private static final String DROP_UNDOABLE_TABLE = "DROP TABLE undoableblocks";
	private static final String DROP_OPEN_OUTPUT_TABLE = "DROP TABLE openoutputs";

	private static final String SELECT_SETTINGS_SQL = "SELECT value FROM settings WHERE name = ?";
	private static final String INSERT_SETTINGS_SQL = "INSERT INTO settings(name, value) VALUES(?, ?)";
	private static final String UPDATE_SETTINGS_SQL = "UPDATE settings SET value = ? WHERE name = ?";

	private static final String SELECT_HEADERS_SQL = "SELECT chainwork, height, header, wasundoable FROM headers WHERE hash = ?";
	private static final String INSERT_HEADERS_SQL = "INSERT INTO headers(hash, chainwork, height, header, wasundoable) VALUES(?, ?, ?, ?, ?)";
	private static final String UPDATE_HEADERS_SQL = "UPDATE headers SET wasundoable=? WHERE hash=?";

	private static final String SELECT_UNDOABLEBLOCKS_SQL = "SELECT txoutchanges, transactions FROM undoableblocks WHERE hash = ?";
	private static final String INSERT_UNDOABLEBLOCKS_SQL = "INSERT INTO undoableblocks(hash, height, txoutchanges, transactions) VALUES(?, ?, ?, ?)";
	private static final String UPDATE_UNDOABLEBLOCKS_SQL = "UPDATE undoableblocks SET txoutchanges=?, transactions=? WHERE hash = ?";
	private static final String DELETE_UNDOABLEBLOCKS_SQL = "DELETE FROM undoableblocks WHERE height <= ?";

	private static final String SELECT_OPENOUTPUTS_SQL = "SELECT height, value, scriptbytes, coinbase, toaddress, addresstargetable FROM openoutputs WHERE hash = ? AND index = ?";
	private static final String SELECT_OPENOUTPUTS_COUNT_SQL = "SELECT COUNT(*) FROM openoutputs WHERE hash = ?";
	private static final String INSERT_OPENOUTPUTS_SQL = "INSERT INTO openoutputs (hash, index, height, value, scriptbytes, toaddress, addresstargetable, coinbase) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String DELETE_OPENOUTPUTS_SQL = "DELETE FROM openoutputs WHERE hash = ? AND index = ?";

	private static final String SELECT_DUMP_SETTINGS_SQL = "SELECT name, value FROM settings";
	private static final String SELECT_DUMP_HEADERS_SQL = "SELECT chainwork, header FROM headers";
	private static final String SELECT_DUMP_UNDOABLEBLOCKS_SQL = "SELECT txoutchanges, transactions FROM undoableblocks";
	private static final String SELECT_DUMP_OPENOUTPUTS_SQL = "SELECT value, scriptbytes FROM openoutputs";

	private static final String SELECT_TRANSACTION_OUTPUTS_SQL = "SELECT hash, value, scriptbytes, height, index, coinbase, toaddress, addresstargetable FROM openoutputs where toaddress = ?";

	private static final String SELECT_BALANCE_SQL = "select sum(value) from openoutputs where toaddress = ?";

	private static final String SELECT_CHECK_TABLES_EXIST_SQL = "SELECT * FROM settings WHERE 1 = 2";

	private static final String SELECT_COMPATIBILITY_COINBASE_SQL = "SELECT coinbase FROM openoutputs WHERE 1 = 2";

	protected Sha256Hash chainHeadHash;
	protected StoredDataBlock chainHeadBlock;
	protected Sha256Hash verifiedChainHeadHash;
	protected StoredDataBlock verifiedChainHeadBlock;
	protected NetworkParams params;
	protected ThreadLocal<Connection> conn;
	protected List<Connection> allConnections;
	protected String connectionURL;
	protected int fullStoreDepth;
	protected String username;
	protected String password;
	protected String schemaName;

	public DatabaseFullPrunedBlockData(NetworkParams params, String connectionURL, int fullStoreDepth,
			@Nullable String username, @Nullable String password, @Nullable String schemaName)
			throws BlockDataException {
		this.params = params;
		this.fullStoreDepth = fullStoreDepth;
		this.connectionURL = connectionURL;
		this.schemaName = schemaName;
		this.username = username;
		this.password = password;
		this.conn = new ThreadLocal<Connection>();
		this.allConnections = new LinkedList<Connection>();

		try {
			Class.forName(getDatabaseDriverClass());
			log.info(getDatabaseDriverClass() + " loaded. ");
		} catch (ClassNotFoundException e) {
			log.error("check CLASSPATH for database driver jar ", e);
		}

		maybeConnect();

		try {

			if (!tablesExists()) {
				createTables();
			} else {
				checkCompatibility();
			}
			initFromDatabase();
		} catch (SQLException e) {
			throw new BlockDataException(e);
		}
	}

	protected abstract String getDatabaseDriverClass();

	protected abstract List<String> getCreateSchemeSQL();

	protected abstract List<String> getCreateTablesSQL();

	protected abstract List<String> getCreateIndexesSQL();

	protected abstract String getDuplicateKeyErrorCode();

	protected String getBalanceSelectSQL() {
		return SELECT_BALANCE_SQL;
	}

	protected String getTablesExistSQL() {
		return SELECT_CHECK_TABLES_EXIST_SQL;
	}

	protected List<String> getCompatibilitySQL() {
		List<String> sqlStatements = new ArrayList<String>();
		sqlStatements.add(SELECT_COMPATIBILITY_COINBASE_SQL);
		return sqlStatements;
	}

	protected String getTrasactionOutputSelectSQL() {
		return SELECT_TRANSACTION_OUTPUTS_SQL;
	}

	protected List<String> getDropTablesSQL() {
		List<String> sqlStatements = new ArrayList<String>();
		sqlStatements.add(DROP_SETTINGS_TABLE);
		sqlStatements.add(DROP_HEADERS_TABLE);
		sqlStatements.add(DROP_UNDOABLE_TABLE);
		sqlStatements.add(DROP_OPEN_OUTPUT_TABLE);
		return sqlStatements;
	}

	protected String getSelectSettingsSQL() {
		return SELECT_SETTINGS_SQL;
	}

	protected String getInsertSettingsSQL() {
		return INSERT_SETTINGS_SQL;
	}

	protected String getUpdateSettingsSLQ() {
		return UPDATE_SETTINGS_SQL;
	}

	protected String getSelectHeadersSQL() {
		return SELECT_HEADERS_SQL;
	}

	protected String getInsertHeadersSQL() {
		return INSERT_HEADERS_SQL;
	}

	protected String getUpdateHeadersSQL() {
		return UPDATE_HEADERS_SQL;
	}

	protected String getSelectUndoableBlocksSQL() {
		return SELECT_UNDOABLEBLOCKS_SQL;
	}

	protected String getInsertUndoableBlocksSQL() {
		return INSERT_UNDOABLEBLOCKS_SQL;
	}

	protected String getUpdateUndoableBlocksSQL() {
		return UPDATE_UNDOABLEBLOCKS_SQL;
	}

	protected String getDeleteUndoableBlocksSQL() {
		return DELETE_UNDOABLEBLOCKS_SQL;
	}

	protected String getSelectOpenoutputsSQL() {
		return SELECT_OPENOUTPUTS_SQL;
	}

	protected String getSelectOpenoutputsCountSQL() {
		return SELECT_OPENOUTPUTS_COUNT_SQL;
	}

	protected String getInsertOpenoutputsSQL() {
		return INSERT_OPENOUTPUTS_SQL;
	}

	protected String getDeleteOpenoutputsSQL() {
		return DELETE_OPENOUTPUTS_SQL;
	}

	protected String getSelectSettingsDumpSQL() {
		return SELECT_DUMP_SETTINGS_SQL;
	}

	protected String getSelectHeadersDumpSQL() {
		return SELECT_DUMP_HEADERS_SQL;
	}

	protected String getSelectUndoableblocksDumpSQL() {
		return SELECT_DUMP_UNDOABLEBLOCKS_SQL;
	}

	protected String getSelectopenoutputsDumpSQL() {
		return SELECT_DUMP_OPENOUTPUTS_SQL;
	}

	protected synchronized void maybeConnect() throws BlockDataException {
		try {
			if (conn.get() != null && !conn.get().isClosed())
				return;

			if (username == null || password == null) {
				conn.set(DriverManager.getConnection(connectionURL));
			} else {
				Properties props = new Properties();
				props.setProperty("user", this.username);
				props.setProperty("password", this.password);
				conn.set(DriverManager.getConnection(connectionURL, props));
			}
			allConnections.add(conn.get());
			Connection connection = conn.get();

			if (schemaName != null) {
				Statement s = connection.createStatement();
				for (String sql : getCreateSchemeSQL()) {
					s.execute(sql);
				}
			}
			log.info("Made a new connection to database " + connectionURL);
		} catch (SQLException ex) {
			throw new BlockDataException(ex);
		}
	}

	@Override
	public synchronized void close() {
		for (Connection conn : allConnections) {
			try {
				if (!conn.getAutoCommit()) {
					conn.rollback();
				}
				conn.close();
				if (conn == this.conn.get()) {
					this.conn.set(null);
				}
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
		}
		allConnections.clear();
	}

	private boolean tablesExists() throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.get().prepareStatement(getTablesExistSQL());
			ResultSet results = ps.executeQuery();
			results.close();
			return true;
		} catch (SQLException ex) {
			return false;
		} finally {
			if (ps != null && !ps.isClosed()) {
				ps.close();
			}
		}
	}

	private void checkCompatibility() throws SQLException, BlockDataException {
		for (String sql : getCompatibilitySQL()) {
			PreparedStatement ps = null;
			try {
				ps = conn.get().prepareStatement(sql);
				ResultSet results = ps.executeQuery();
				results.close();
			} catch (SQLException ex) {
				throw new BlockDataException("Database block store is not compatible with the current release.  "
						+ "See bitcoinj release notes for further information: " + ex.getMessage());
			} finally {
				if (ps != null && !ps.isClosed()) {
					ps.close();
				}
			}
		}
	}

	private void createTables() throws SQLException, BlockDataException {
		Statement s = conn.get().createStatement();

		for (String sql : getCreateTablesSQL()) {
			if (log.isDebugEnabled()) {
				log.debug("DatabaseFullPrunedBlockData : CREATE table [SQL= {0}]", sql);
			}
			s.executeUpdate(sql);
		}

		for (String sql : getCreateIndexesSQL()) {
			if (log.isDebugEnabled()) {
				log.debug("DatabaseFullPrunedBlockData : CREATE index [SQL= {0}]", sql);
			}
			s.executeUpdate(sql);
		}
		s.close();

		PreparedStatement ps = conn.get().prepareStatement(getInsertSettingsSQL());
		ps.setString(1, CHAIN_HEAD_SETTING);
		ps.setNull(2, Types.BINARY);
		ps.execute();
		ps.setString(1, VERIFIED_CHAIN_HEAD_SETTING);
		ps.setNull(2, Types.BINARY);
		ps.execute();
		ps.setString(1, VERSION_SETTING);
		ps.setBytes(2, "03".getBytes());
		ps.execute();
		ps.close();
		createNewStore(params);
	}

	private void createNewStore(NetworkParams params) throws BlockDataException {
		try {

			StoredDataBlock storedGenesisHeader = new StoredDataBlock(params.getGenesisBlock().cloneAsHeader(),
					params.getGenesisBlock().getWork(), 0);

			List<Transaction> genesisTransactions = Lists.newLinkedList();
			StoredInvalidDataBlock storedGenesis = new StoredInvalidDataBlock(params.getGenesisBlock().getHash(),
					genesisTransactions);
			put(storedGenesisHeader, storedGenesis);
			setChainHead(storedGenesisHeader);
			setVerifiedChainHead(storedGenesisHeader);
		} catch (VeriException e) {
			throw new RuntimeException(e);
		}
	}

	private void initFromDatabase() throws SQLException, BlockDataException {
		PreparedStatement ps = conn.get().prepareStatement(getSelectSettingsSQL());
		ResultSet rs;
		ps.setString(1, CHAIN_HEAD_SETTING);
		rs = ps.executeQuery();
		if (!rs.next()) {
			throw new BlockDataException("corrupt database block store - no chain head pointer");
		}
		Sha256Hash hash = Sha256Hash.wrap(rs.getBytes(1));
		rs.close();
		this.chainHeadBlock = get(hash);
		this.chainHeadHash = hash;
		if (this.chainHeadBlock == null) {
			throw new BlockDataException("corrupt database block store - head block not found");
		}
		ps.setString(1, VERIFIED_CHAIN_HEAD_SETTING);
		rs = ps.executeQuery();
		if (!rs.next()) {
			throw new BlockDataException("corrupt database block store - no verified chain head pointer");
		}
		hash = Sha256Hash.wrap(rs.getBytes(1));
		rs.close();
		ps.close();
		this.verifiedChainHeadBlock = get(hash);
		this.verifiedChainHeadHash = hash;
		if (this.verifiedChainHeadBlock == null) {
			throw new BlockDataException("corrupt databse block store - verified head block not found");
		}
	}

	protected void putUpdateStoredBlock(StoredDataBlock storedDataBlock, boolean wasUndoable) throws SQLException {
		try {
			PreparedStatement s = conn.get().prepareStatement(getInsertHeadersSQL());

			byte[] hashBytes = new byte[28];
			System.arraycopy(storedDataBlock.getHeader().getHash().getBytes(), 4, hashBytes, 0, 28);
			s.setBytes(1, hashBytes);
			s.setBytes(2, storedDataBlock.getChainWork().toByteArray());
			s.setInt(3, storedDataBlock.getHeight());
			s.setBytes(4, storedDataBlock.getHeader().cloneAsHeader().unsafeBitcoinSerialize());
			s.setBoolean(5, wasUndoable);
			s.executeUpdate();
			s.close();
		} catch (SQLException e) {

			if (!(e.getSQLState().equals(getDuplicateKeyErrorCode())) || !wasUndoable)
				throw e;

			PreparedStatement s = conn.get().prepareStatement(getUpdateHeadersSQL());
			s.setBoolean(1, true);

			byte[] hashBytes = new byte[28];
			System.arraycopy(storedDataBlock.getHeader().getHash().getBytes(), 4, hashBytes, 0, 28);
			s.setBytes(2, hashBytes);
			s.executeUpdate();
			s.close();
		}
	}

	@Override
	public void put(StoredDataBlock storedDataBlock) throws BlockDataException {
		maybeConnect();
		try {
			putUpdateStoredBlock(storedDataBlock, false);
		} catch (SQLException e) {
			throw new BlockDataException(e);
		}
	}

	@Override
	public void put(StoredDataBlock storedDataBlock, StoredInvalidDataBlock undoableBlock) throws BlockDataException {
		maybeConnect();

		byte[] hashBytes = new byte[28];
		System.arraycopy(storedDataBlock.getHeader().getHash().getBytes(), 4, hashBytes, 0, 28);
		int height = storedDataBlock.getHeight();
		byte[] transactions = null;
		byte[] txOutChanges = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			if (undoableBlock.getTxOutChanges() != null) {
				undoableBlock.getTxOutChanges().serializeToStream(bos);
				txOutChanges = bos.toByteArray();
			} else {
				int numTxn = undoableBlock.getTransactions().size();
				bos.write(0xFF & numTxn);
				bos.write(0xFF & (numTxn >> 8));
				bos.write(0xFF & (numTxn >> 16));
				bos.write(0xFF & (numTxn >> 24));
				for (Transaction tx : undoableBlock.getTransactions())
					tx.bitcoinSerialize(bos);
				transactions = bos.toByteArray();
			}
			bos.close();
		} catch (IOException e) {
			throw new BlockDataException(e);
		}

		try {
			try {
				PreparedStatement s = conn.get().prepareStatement(getInsertUndoableBlocksSQL());
				s.setBytes(1, hashBytes);
				s.setInt(2, height);
				if (transactions == null) {
					s.setBytes(3, txOutChanges);
					s.setNull(4, Types.BINARY);
				} else {
					s.setNull(3, Types.BINARY);
					s.setBytes(4, transactions);
				}
				s.executeUpdate();
				s.close();
				try {
					putUpdateStoredBlock(storedDataBlock, true);
				} catch (SQLException e) {
					throw new BlockDataException(e);
				}
			} catch (SQLException e) {
				if (!e.getSQLState().equals(getDuplicateKeyErrorCode()))
					throw new BlockDataException(e);

				PreparedStatement s = conn.get().prepareStatement(getUpdateUndoableBlocksSQL());
				s.setBytes(3, hashBytes);
				if (transactions == null) {
					s.setBytes(1, txOutChanges);
					s.setNull(2, Types.BINARY);
				} else {
					s.setNull(1, Types.BINARY);
					s.setBytes(2, transactions);
				}
				s.executeUpdate();
				s.close();
			}
		} catch (SQLException ex) {
			throw new BlockDataException(ex);
		}
	}

	public StoredDataBlock get(Sha256Hash hash, boolean wasUndoableOnly) throws BlockDataException {

		if (chainHeadHash != null && chainHeadHash.equals(hash))
			return chainHeadBlock;
		if (verifiedChainHeadHash != null && verifiedChainHeadHash.equals(hash))
			return verifiedChainHeadBlock;
		maybeConnect();
		PreparedStatement s = null;
		try {
			s = conn.get().prepareStatement(getSelectHeadersSQL());

			byte[] hashBytes = new byte[28];
			System.arraycopy(hash.getBytes(), 4, hashBytes, 0, 28);
			s.setBytes(1, hashBytes);
			ResultSet results = s.executeQuery();
			if (!results.next()) {
				return null;
			}

			if (wasUndoableOnly && !results.getBoolean(4))
				return null;

			BigInteger chainWork = new BigInteger(results.getBytes(1));
			int height = results.getInt(2);
			Block b = new Block(params, results.getBytes(3));
			b.verifyHeader();
			StoredDataBlock stored = new StoredDataBlock(b, chainWork, height);
			return stored;
		} catch (SQLException ex) {
			throw new BlockDataException(ex);
		} catch (ProtocolException e) {

			throw new BlockDataException(e);
		} catch (VeriException e) {

			throw new BlockDataException(e);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (SQLException e) {
					throw new BlockDataException("Failed to close PreparedStatement");
				}
			}
		}
	}

	@Override
	public StoredDataBlock get(Sha256Hash hash) throws BlockDataException {
		return get(hash, false);
	}

	@Override
	public StoredDataBlock getOnceUndoableStoredBlock(Sha256Hash hash) throws BlockDataException {
		return get(hash, true);
	}

	@Override
	public StoredInvalidDataBlock getUndoBlock(Sha256Hash hash) throws BlockDataException {
		maybeConnect();
		PreparedStatement s = null;
		try {
			s = conn.get().prepareStatement(getSelectUndoableBlocksSQL());

			byte[] hashBytes = new byte[28];
			System.arraycopy(hash.getBytes(), 4, hashBytes, 0, 28);
			s.setBytes(1, hashBytes);
			ResultSet results = s.executeQuery();
			if (!results.next()) {
				return null;
			}

			byte[] txOutChanges = results.getBytes(1);
			byte[] transactions = results.getBytes(2);
			StoredInvalidDataBlock block;
			if (txOutChanges == null) {
				int offset = 0;
				int numTxn = ((transactions[offset++] & 0xFF)) | ((transactions[offset++] & 0xFF) << 8)
						| ((transactions[offset++] & 0xFF) << 16) | ((transactions[offset++] & 0xFF) << 24);
				List<Transaction> transactionList = new LinkedList<Transaction>();
				for (int i = 0; i < numTxn; i++) {
					Transaction tx = new Transaction(params, transactions, offset);
					transactionList.add(tx);
					offset += tx.getMessageSize();
				}
				block = new StoredInvalidDataBlock(hash, transactionList);
			} else {
				TxOutputChanges outChangesObject = new TxOutputChanges(new ByteArrayInputStream(txOutChanges));
				block = new StoredInvalidDataBlock(hash, outChangesObject);
			}
			return block;
		} catch (SQLException ex) {
			throw new BlockDataException(ex);
		} catch (NullPointerException e) {

			throw new BlockDataException(e);
		} catch (ClassCastException e) {

			throw new BlockDataException(e);
		} catch (ProtocolException e) {

			throw new BlockDataException(e);
		} catch (IOException e) {

			throw new BlockDataException(e);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (SQLException e) {
					throw new BlockDataException("Failed to close PreparedStatement");
				}
			}
		}
	}

	@Override
	public StoredDataBlock getChainHead() throws BlockDataException {
		return chainHeadBlock;
	}

	@Override
	public void setChainHead(StoredDataBlock chainHead) throws BlockDataException {
		Sha256Hash hash = chainHead.getHeader().getHash();
		this.chainHeadHash = hash;
		this.chainHeadBlock = chainHead;
		maybeConnect();
		try {
			PreparedStatement s = conn.get().prepareStatement(getUpdateSettingsSLQ());
			s.setString(2, CHAIN_HEAD_SETTING);
			s.setBytes(1, hash.getBytes());
			s.executeUpdate();
			s.close();
		} catch (SQLException ex) {
			throw new BlockDataException(ex);
		}
	}

	@Override
	public StoredDataBlock getVerifiedChainHead() throws BlockDataException {
		return verifiedChainHeadBlock;
	}

	@Override
	public void setVerifiedChainHead(StoredDataBlock chainHead) throws BlockDataException {
		Sha256Hash hash = chainHead.getHeader().getHash();
		this.verifiedChainHeadHash = hash;
		this.verifiedChainHeadBlock = chainHead;
		maybeConnect();
		try {
			PreparedStatement s = conn.get().prepareStatement(getUpdateSettingsSLQ());
			s.setString(2, VERIFIED_CHAIN_HEAD_SETTING);
			s.setBytes(1, hash.getBytes());
			s.executeUpdate();
			s.close();
		} catch (SQLException ex) {
			throw new BlockDataException(ex);
		}
		if (this.chainHeadBlock.getHeight() < chainHead.getHeight())
			setChainHead(chainHead);
		removeUndoableBlocksWhereHeightIsLessThan(chainHead.getHeight() - fullStoreDepth);
	}

	private void removeUndoableBlocksWhereHeightIsLessThan(int height) throws BlockDataException {
		try {
			PreparedStatement s = conn.get().prepareStatement(getDeleteUndoableBlocksSQL());
			s.setInt(1, height);
			if (log.isDebugEnabled())
				log.debug("Deleting undoable undoable block with height <= " + height);
			s.executeUpdate();
			s.close();
		} catch (SQLException ex) {
			throw new BlockDataException(ex);
		}
	}

	@Override
	public Unspent getTransactionOutput(Sha256Hash hash, long index) throws BlockDataException {
		maybeConnect();
		PreparedStatement s = null;
		try {
			s = conn.get().prepareStatement(getSelectOpenoutputsSQL());
			s.setBytes(1, hash.getBytes());

			s.setInt(2, (int) index);
			ResultSet results = s.executeQuery();
			if (!results.next()) {
				return null;
			}

			int height = results.getInt(1);
			Coin value = Coin.valueOf(results.getLong(2));
			byte[] scriptBytes = results.getBytes(3);
			boolean coinbase = results.getBoolean(4);
			String address = results.getString(5);
			Unspent txout = new Unspent(hash, index, value, height, coinbase, new ChainScript(scriptBytes), address);
			return txout;
		} catch (SQLException ex) {
			throw new BlockDataException(ex);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (SQLException e) {
					throw new BlockDataException("Failed to close PreparedStatement");
				}
			}
		}
	}

	@Override
	public void addUnspentTransactionOutput(Unspent out) throws BlockDataException {
		maybeConnect();
		PreparedStatement s = null;
		try {
			s = conn.get().prepareStatement(getInsertOpenoutputsSQL());
			s.setBytes(1, out.getHash().getBytes());

			s.setInt(2, (int) out.getIndex());
			s.setInt(3, out.getHeight());
			s.setLong(4, out.getValue().value);
			s.setBytes(5, out.getScript().getProgram());
			s.setString(6, out.getAddress());
			s.setInt(7, out.getScript().getScriptType().ordinal());
			s.setBoolean(8, out.isCoinbase());
			s.executeUpdate();
			s.close();
		} catch (SQLException e) {
			if (!(e.getSQLState().equals(getDuplicateKeyErrorCode())))
				throw new BlockDataException(e);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (SQLException e) {
					throw new BlockDataException(e);
				}
			}
		}
	}

	@Override
	public void removeUnspentTransactionOutput(Unspent out) throws BlockDataException {
		maybeConnect();

		if (getTransactionOutput(out.getHash(), out.getIndex()) == null)
			throw new BlockDataException(
					"Tried to remove a Unspent from DatabaseFullPrunedBlockData that it didn't have!");
		try {
			PreparedStatement s = conn.get().prepareStatement(getDeleteOpenoutputsSQL());
			s.setBytes(1, out.getHash().getBytes());

			s.setInt(2, (int) out.getIndex());
			s.executeUpdate();
			s.close();
		} catch (SQLException e) {
			throw new BlockDataException(e);
		}
	}

	@Override
	public void beginDatabaseBatchWrite() throws BlockDataException {
		maybeConnect();
		if (log.isDebugEnabled())
			log.debug("Starting database batch write with connection: " + conn.get().toString());
		try {
			conn.get().setAutoCommit(false);
		} catch (SQLException e) {
			throw new BlockDataException(e);
		}
	}

	@Override
	public void commitDatabaseBatchWrite() throws BlockDataException {
		maybeConnect();
		if (log.isDebugEnabled())
			log.debug("Committing database batch write with connection: " + conn.get().toString());
		try {
			conn.get().commit();
			conn.get().setAutoCommit(true);
		} catch (SQLException e) {
			throw new BlockDataException(e);
		}
	}

	@Override
	public void abortDatabaseBatchWrite() throws BlockDataException {
		maybeConnect();
		if (log.isDebugEnabled())
			log.debug("Rollback database batch write with connection: " + conn.get().toString());
		try {
			if (!conn.get().getAutoCommit()) {
				conn.get().rollback();
				conn.get().setAutoCommit(true);
			} else {
				log.warn("Warning: Rollback attempt without transaction");
			}
		} catch (SQLException e) {
			throw new BlockDataException(e);
		}
	}

	@Override
	public boolean hasUnspentOutputs(Sha256Hash hash, int numOutputs) throws BlockDataException {
		maybeConnect();
		PreparedStatement s = null;
		try {
			s = conn.get().prepareStatement(getSelectOpenoutputsCountSQL());
			s.setBytes(1, hash.getBytes());
			ResultSet results = s.executeQuery();
			if (!results.next()) {
				throw new BlockDataException("Got no results from a COUNT(*) query");
			}
			int count = results.getInt(1);
			return count != 0;
		} catch (SQLException ex) {
			throw new BlockDataException(ex);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (SQLException e) {
					throw new BlockDataException("Failed to close PreparedStatement");
				}
			}
		}
	}

	@Override
	public NetworkParams getParams() {
		return params;
	}

	@Override
	public int getChainHeadHeight() throws UnspentException {
		try {
			return getVerifiedChainHead().getHeight();
		} catch (BlockDataException e) {
			throw new UnspentException(e);
		}
	}

	public void resetStore() throws BlockDataException {
		maybeConnect();
		try {
			deleteStore();
			createTables();
			initFromDatabase();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void deleteStore() throws BlockDataException {
		maybeConnect();
		try {
			Statement s = conn.get().createStatement();
			for (String sql : getDropTablesSQL()) {
				s.execute(sql);
			}
			s.close();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	public BigInteger calculateBalanceForAddress(Address address) throws BlockDataException {
		maybeConnect();
		PreparedStatement s = null;
		try {
			s = conn.get().prepareStatement(getBalanceSelectSQL());
			s.setString(1, address.toString());
			ResultSet rs = s.executeQuery();
			BigInteger balance = BigInteger.ZERO;
			if (rs.next()) {
				return BigInteger.valueOf(rs.getLong(1));
			}
			return balance;
		} catch (SQLException ex) {
			throw new BlockDataException(ex);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (SQLException e) {
					throw new BlockDataException("Could not close statement");
				}
			}
		}
	}

	@Override
	public List<Unspent> getOpenTransactionOutputs(List<Address> addresses) throws UnspentException {
		PreparedStatement s = null;
		List<Unspent> outputs = new ArrayList<Unspent>();
		try {
			maybeConnect();
			s = conn.get().prepareStatement(getTrasactionOutputSelectSQL());
			for (Address address : addresses) {
				s.setString(1, address.toString());
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					Sha256Hash hash = Sha256Hash.wrap(rs.getBytes(1));
					Coin amount = Coin.valueOf(rs.getLong(2));
					byte[] scriptBytes = rs.getBytes(3);
					int height = rs.getInt(4);
					int index = rs.getInt(5);
					boolean coinbase = rs.getBoolean(6);
					String toAddress = rs.getString(7);
					Unspent output = new Unspent(hash, index, amount, height, coinbase, new ChainScript(scriptBytes),
							toAddress);
					outputs.add(output);
				}
			}
			return outputs;
		} catch (SQLException ex) {
			throw new UnspentException(ex);
		} catch (BlockDataException bse) {
			throw new UnspentException(bse);
		} finally {
			if (s != null)
				try {
					s.close();
				} catch (SQLException e) {
					throw new UnspentException("Could not close statement", e);
				}
		}
	}

	public void dumpSizes() throws SQLException, BlockDataException {
		maybeConnect();
		Statement s = conn.get().createStatement();
		long size = 0;
		long totalSize = 0;
		int count = 0;
		ResultSet rs = s.executeQuery(getSelectSettingsDumpSQL());
		while (rs.next()) {
			size += rs.getString(1).length();
			size += rs.getBytes(2).length;
			count++;
		}
		rs.close();
		System.out.printf(Locale.US, "Settings size: %d, count: %d, average size: %f%n", size, count,
				(double) size / count);

		totalSize += size;
		size = 0;
		count = 0;
		rs = s.executeQuery(getSelectHeadersDumpSQL());
		while (rs.next()) {
			size += 28;
			size += rs.getBytes(1).length;
			size += 4;
			size += rs.getBytes(2).length;
			count++;
		}
		rs.close();
		System.out.printf(Locale.US, "Headers size: %d, count: %d, average size: %f%n", size, count,
				(double) size / count);

		totalSize += size;
		size = 0;
		count = 0;
		rs = s.executeQuery(getSelectUndoableblocksDumpSQL());
		while (rs.next()) {
			size += 28;
			size += 4;
			byte[] txOutChanges = rs.getBytes(1);
			byte[] transactions = rs.getBytes(2);
			if (txOutChanges == null)
				size += transactions.length;
			else
				size += txOutChanges.length;

			count++;
		}
		rs.close();
		System.out.printf(Locale.US, "Undoable Blocks size: %d, count: %d, average size: %f%n", size, count,
				(double) size / count);

		totalSize += size;
		size = 0;
		count = 0;
		long scriptSize = 0;
		rs = s.executeQuery(getSelectopenoutputsDumpSQL());
		while (rs.next()) {
			size += 32;
			size += 4;
			size += 4;
			size += rs.getBytes(1).length;
			size += rs.getBytes(2).length;
			scriptSize += rs.getBytes(2).length;
			count++;
		}
		rs.close();
		System.out.printf(Locale.US,
				"Open Outputs size: %d, count: %d, average size: %f, average script size: %f (%d in id indexes)%n",
				size, count, (double) size / count, (double) scriptSize / count, count * 8);

		totalSize += size;
		System.out.println("Total Size: " + totalSize);

		s.close();
	}
}
