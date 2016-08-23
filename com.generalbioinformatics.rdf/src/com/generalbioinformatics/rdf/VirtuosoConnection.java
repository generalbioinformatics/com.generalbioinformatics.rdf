/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.generalbioinformatics.rdf.stream.NtWriter;

import nl.helixsoft.recordstream.Record;
import nl.helixsoft.recordstream.RecordStream;
import nl.helixsoft.recordstream.ResultSetRecordStream;
import nl.helixsoft.recordstream.Stream;
import nl.helixsoft.recordstream.StreamException;
import nl.helixsoft.util.HFileUtils;
import nl.helixsoft.util.StringUtils;
import virtuoso.jdbc3.VirtuosoConnectionPoolDataSource;
import virtuoso.jdbc3.VirtuosoException;

/**
 * This class is a wrapper for a JDBC connection to virtuoso.
 * <p>
 * In addition to executing sparql queries, this class has
 * some helpful utility functions for storing namespace prefixes, 
 * loading rdf data, renaming graphs, and more
 * <p>
 * In addition, queries can be cached (@see setCacheDir)
 */
public class VirtuosoConnection extends AbstractTripleStore
{
	Logger log = LoggerFactory.getLogger("com.generalbioinformatics.rdf.VirtuosoConnection");

	/** wrapper to execute a JDBC statement with tracing */ 
	private ResultSet executeQuery(Statement st, String sql) throws SQLException
	{
		log.trace(sql);
		return st.executeQuery(sql);
	}

	/** wrapper to execute a JDBC statement with tracing */
	private boolean execute(Statement st, String sql) throws SQLException
	{
		log.trace(sql);
		return st.execute(sql);
	}

	/** constructor using the default host value (localhost). No connection is created until {@link init} is called. */
	public VirtuosoConnection() throws ClassNotFoundException
	{
		this(null);
	}
	
	/** constructor for the given virtuoso host. No connection is created until {@link init} is called. */
	public VirtuosoConnection(String host) throws ClassNotFoundException
	{
		Class.forName("virtuoso.jdbc3.Driver");
//		Class.forName("virtuoso.jdbc4.Driver");

		this.host = host;
	}
	
	// JDBC connection
	private Connection _con = null;
	
	private Connection getConnection() throws SQLException, IOException
	{
		if (ds == null)
		{		
			if (_con == null)
			{			
				// read Properties
				Properties props = new Properties();
				
				InputStream stream = VirtuosoConnection.class.getResourceAsStream("connection.properties");
				if (stream != null) props.load(stream);
	
				if (user == null) user = props.getProperty("username", "dba");
				if (pass == null) pass = props.getProperty("password", "dba");
				if (host == null) host = props.getProperty("host", "localhost");
				if (port == null) port = props.getProperty("port", "1111");
			
				String url = "jdbc:virtuoso://" + host + ":" + port + "/charset=UTF-8";
	
				try
				{
					_con = DriverManager.getConnection(url, user, pass);
				}
				catch (VirtuosoException ex)
				{
					rethrowWithTips(ex);
				}
				
				if (tempDir == null) tempDir = new File (props.getProperty("vload.directory", "/local/tmp/virtuoso-tmp"));			
	
			}
			return _con;
		}
		else
		{
			assert _con == null : "Programming error: Connection and DataSource both in use at the same time";
			assert user == null && pass == null && host == null && port == null : "Set either DataSource or user/pass/host/port, but not both";
			return ds.getConnection();
		}
	}
	
	// tempdir used by vload, can be configured in connection.properties
	private File tempDir;
	
	private String user;
	private String host;
	private String port;
	private String pass;
	private DataSource ds;

	/** set a DataSource in a pooled Connection environment. Use either this or user/pass/host/port, but not both */
	public void setDataSource(DataSource value)
	{
		assert _con == null : "Can't set DataSource when connection is already initialised";
		assert user == null && pass == null && host == null && port == null : "Set either DataSource or user/pass/host/port, but not both";
		ds = value;
	}
	
	/** set the host of the virtuoso server, for example 'localhost' (default)*/
	public void setHost (String val) { host = val; }
	/** set the port of the virtuoso server, for example: 1111 (default) */
	public void setPort (String val) { port = val; }
	/** set the user for the connection with virtuoso, for example 'dba' (default) */
	public void setUser (String val) { user = val; }
	/** set the password for the virtuoso server connection */
	public void setPass (String val) { pass = val; }
	
	public void setTempDir (File val) { tempDir = val; }
	
	/** get the current virtuoso server host */
	public String getHost() { return host; }
	/** get the current virtuoso server port */
	public String getPort() { return port; }

	/**
	 * Set up the connection.
	 * If the database parameters haven't been provided using setHost, setPort, etc,
	 * it will depend on the database parameters provided in connection.properties
	 * @throws IOException if the connection properties couldn't be loaded
	 */
	public void init() throws SQLException, IOException
	{
		getConnection();
	}

	/**
	 * Execute a SPARQL query. The query will be based directly to Virtuoso, so all the
	 * virtuoso sparql syntax quirks can be used.
	 * <p>
	 * If you set any prefixes with setPrefixes, they will be included in this query.
	 * <p>
	 * If you set any cache dir, the query will be saved to File on the first run, 
	 * and retrieved on the second run. 
	 */
	public RecordStream _sparqlSelectDirect(String query) throws StreamException
	{
		try
		{
			Statement st = getConnection().createStatement();
			ResultSet result = executeQuery(st, "SPARQL " + prefixes + " " + query);
			RecordStream rs = new VirtuosoRecordStream(result);
			return rs;
		}
		catch (SQLException ex)
		{
			throw new StreamException(ex);
		} catch (IOException ex) {
			throw new StreamException(ex);
		}
	}
	
	public Stream<com.generalbioinformatics.rdf.stream.Statement> sparqlSelectAsStatementStream(String query) throws StreamException
	{
		try
		{
			Statement st = getConnection().createStatement();
			ResultSet result = executeQuery(st, "SPARQL " + prefixes + " " + query);
			Stream<com.generalbioinformatics.rdf.stream.Statement> ss = new VirtuosoStatementStream(result);
			return ss;
		}
		catch (SQLException ex)
		{
			throw new StreamException(ex);
		} catch (IOException ex) {
			throw new StreamException(ex);
		}
	}

	@Override
	public void sparqlConstruct(String query, OutputStream os) throws StreamException
	{
		try
		{
			Statement st = getConnection().createStatement();
			
			//TODO: evaluate
			logEnable(st, true, false); 
			
			RecordStream rs = new ResultSetRecordStream(executeQuery(st, "SPARQL " + prefixes + " " + query));
			
			NtWriter nt = new NtWriter (os);
			
			Record r;
			while ((r = rs.getNext()) != null)
			{
				String s = "" + r.get("S");
				String p = "" + r.get("P");
				String o = "" + r.get("O");
				nt.writeStatement(s, p, o);
			}
			st.close();
		}
		catch (SQLException ex)
		{
			throw new StreamException(ex);
		} catch (IOException ex) {
			throw new StreamException(ex);
		}
		
	}
	
	/**
	 * Wrapper for Virtuoso Exception, which sometimes can give additional clues to what is wrong. 
	 */
	public static class VirtuosoExceptionWrapper extends SQLException
	{
		public VirtuosoExceptionWrapper(String msg, Throwable t)
		{
			super (msg, t);
		}
		
	}
	
	/**
	 * Add some extra information to Virtuoso exceptions so they are a bit more user-friendly.
	 * @throws VirtuosoExceptionWrapper same as original, but with extra helpful tips to solve the problem
	 * @throws VirtuosoException unmodified original, in case there was no additional info to add.
	 */
	private void rethrowWithTips (VirtuosoException ex) throws SQLException
	{
		String tip = null;
		if (ex.getMessage().startsWith("SR325:"))
		{
			tip = "Try to increase TransactionAfterImageLimit in virtuoso.ini";
		}
		if (ex.getMessage().startsWith("Connection failed: Connection refused"))
		{
			tip = "Check if Virtuoso is running on host: " + host + ", port: " + port;
			if (!port.equals ("1111"))
			{
				tip += "\nAlso, make sure you specified the JDBC port (normally 1111), not the web interface (normally 8890)";
			}							
		}
		if (ex.getMessage().startsWith("Bad login"))
		{
			tip = "Check that the username and password are correct";
		}
		if (ex.getMessage().startsWith("FA026:"))
		{
			tip = "Check if enough space is left on device";
		}

		if (tip != null)
		{
			throw new VirtuosoExceptionWrapper (ex.getMessage() + "\nTip: " + tip, ex);			
		}
		else
			throw ex;
	}
	
	/** 
	 * rename a graph from one graph URI to the other. 
	 **/
	public void renameGraph (String oldName, String newName) throws SQLException, IOException
	{
		if (oldName.contains("'") || newName.contains("'")) 
			throw new IllegalArgumentException ("Graph Uri and file name may not contain quote characters (')!");
		
		try
		{
			Statement st = getConnection().createStatement();
			execute(st, 
					"UPDATE DB.DBA.RDF_QUAD TABLE OPTION (index RDF_QUAD_GS) " + 
						"  SET g = iri_to_id ('" + newName + "') " +
						"WHERE g = iri_to_id ('" + oldName + "', 0)"
					);
		}
		catch (VirtuosoException ex)
		{
			rethrowWithTips(ex);
		}
	}
	
	public int getVirtuosoMajorVersion() throws SQLException, IOException
	{
		String s = getVirtuosoVersionString();
		if (s.length() < 2) return -1;
		Integer result = StringUtils.safeParseInt(s.substring(0, 2));
		if (result == null) return -1;
		return result;
	}
	
	public String getVirtuosoVersionString() throws SQLException, IOException
	{
		String result = "Could not obtain version";
		Statement st = getConnection().createStatement();
		try
		{
			//TODO: better, but not working in JDBC - why?
			/*
			ResultSet rs = executeQuery(st, "select sys_stat('st_dbms_ver');");
			rs.next();
			result = rs.getString(1);
			*/
						
			ResultSet rs = executeQuery(st, "status()");
			String versionLine = "Version ";
			while (rs.next())
			{
				String s = rs.getString(1);
				
				if (s.startsWith (versionLine))
				{
					result = s.substring(versionLine.length());
				}
			}
		}
		finally
		{
			st.close();
		}
		return result;
	}

	
	Statement batchStatement = null;
	String batchGraphUri = null;
	
	/**
	 * Prepare to load one or more files in a single commit batch.
	 */
	public void vloadBatchStart(String graphUri) throws SQLException, IOException
	{
		assert (batchStatement == null);
		assert (batchGraphUri == null);
		
		batchGraphUri = graphUri;
		try
		{	
			Connection con = getConnection();
			batchStatement = con.createStatement();
			logEnable (batchStatement, true, false);
			
			// the following two lines are copied from rdf_loader, but I removed the clustering aspect.
			execute (batchStatement, "checkpoint_interval (0)"); // this disables checkpointing. See: http://docs.openlinksw.com/virtuoso/fn_checkpoint_interval.html
			
			try {
				// TODO virtusoso 6 only...
				execute (batchStatement, "__dbf_set ('cl_non_logged_write_mode', 1)");
			} catch (VirtuosoException ex) {}

		}
		catch (VirtuosoException ex)
		{
			rethrowWithTips(ex);
		}
	

	}
	
	public void vloadBatchAdd(File f) throws FileNotFoundException, IOException, SQLException
	{
		assert (batchStatement != null);
		assert (batchGraphUri != null);
		
		try
		{
			vloadSingleFileHelper(f, batchGraphUri, batchStatement);
		}
		catch (VirtuosoException ex)
		{
			rethrowWithTips(ex);
		}

	}
	
	public void vloadBatchEnd() throws SQLException
	{
		assert (batchStatement != null);
		assert (batchGraphUri != null);
		
		try
		{
			execute (batchStatement, "checkpoint"); // do explicit checkpoint. This is a good idea in case of a crash, because we don't have the log to back us up.					
		}
		catch (VirtuosoException ex)
		{
			rethrowWithTips(ex);
		}
		finally
		{
			if (batchStatement != null)
				batchStatement.close();
		}				

		batchGraphUri = null;
		batchStatement = null;
	}
	
	/**
	 * Load in RDF data from file.
	 * @param f file to load. Type is recognized based on file extension
	 * @param graphUri name of graph to load into.
	 */
	public void vload(File f, String graphUri) throws SQLException, IOException
	{
		vloadBatchStart(graphUri);
		vloadBatchAdd(f);
		vloadBatchEnd();
	}

	/**
	 * Vload a single file.
	 * Statement is already prepared with logging enabled and checkpointing disabled.
	 * Does NOT do a commit at the end.
	 * <p>
	 * This function is useful for batching several files in a single commit.
	 */
	private void vloadSingleFileHelper(File f, String graphUri, Statement st)
								throws FileNotFoundException, IOException, SQLException 
	{
		File tempFile = null;
		try
		{
			String fabs = f.getAbsolutePath();
			if (!f.exists()) throw new FileNotFoundException("File " + fabs + " not found.");
			
			String fname = f.toString().toLowerCase();
			if (fabs.contains("'") || graphUri.contains("'")) 
					throw new IllegalArgumentException ("Graph Uri and file name may not contain quote characters (')!");
			
			File tempDir = getTempDir();

			// check if f is below virtuoso load dir
			// if not, copy it there.
			File dest;
			tempFile = null;
			if (!HFileUtils.isBelowDirectory(tempDir, f))
			{
				log.debug ("Copying file to virtuoso temp directory " + tempDir);
				tempFile = File.createTempFile("vload-", f.getName().replaceAll(" ", "_"), tempDir);
				FileUtils.copyFile(f, tempFile);
				dest = tempFile;
			}
			else
			{
				dest = f;
				tempFile = null;
			}		
			
			assert (dest.exists());

			String func = composeVloadSql(graphUri, fname, dest);
			if (func == null) throw new IllegalArgumentException ("File type not supported: " + fabs);
			
			execute(st, func);
		}
		finally
		{
			if (tempFile != null)
				tempFile.delete();
		}
	}

	/**
	 * Compose the Virtuoso/SQL statement to call to vload the given file into the given graph
	 */
	private String composeVloadSql(String graphUri, String fname, File dest) 
	{
		String func = null;
		if (fname.endsWith (".rdf") || fname.endsWith(".owl")) // rdf-xml
			func = "DB.DBA.RDF_LOAD_RDFXML_MT(file_to_string_output('" + dest + "'), '', '" + graphUri + "')";
		if (fname.endsWith (".rdf.gz") || fname.endsWith(".owl.gz")) // gzipped rdf-xml
			func = "DB.DBA.RDF_LOAD_RDFXML_MT(gz_file_open('" + dest + "'), '', '" + graphUri + "')";
		else if (fname.endsWith(".ttl") || fname.endsWith(".n3")) // Turtle / N3
			func = "DB.DBA.TTLP_MT(file_to_string_output('" + dest + "'),'','" + graphUri + "', 255)";
		else if (fname.endsWith(".nt")) // n-triple
			func = "DB.DBA.TTLP_MT(file_to_string_output('" + dest + "'),'','" + graphUri + "', 255)";
		else if (fname.endsWith(".n3")) // n3
			func = "DB.DBA.TTLP_MT(file_to_string_output('" + dest + "'),'','" + graphUri + "', 255)";
		else if (fname.endsWith(".nt.gz")) // gzipped n-triple
			func = "DB.DBA.TTLP_MT(gz_file_open('" + dest + "'),'','" + graphUri + "', 255)";
		else if (fname.endsWith(".ttl.gz") || fname.endsWith(".n3.gz")) // gzipped ttl
			func = "DB.DBA.TTLP_MT(gz_file_open('" + dest + "'),'','" + graphUri + "', 255)";
		else if (fname.endsWith(".nq")) // n-quad
			func = "DB.DBA.TTLP_MT(file_to_string_output('" + dest + "'),'','" + graphUri + "', 512)";
		return func;
	}
	
	public File getTempDir() throws FileNotFoundException {
		File result = tempDir;
		if (!result.exists())
			if (!result.mkdirs())
				throw new FileNotFoundException("Temporary directory " + result + " doesn't exist, and I couldn't create it");
		return result;
	}
	
	public void vstore(File f, String graphUri) throws SQLException, IOException
	{
		File tempDir = getTempDir();
		
		// adapted from http://docs.openlinksw.com/virtuoso/rdfperformancetuning.html#rdfperfdumpandreloadgraphs
		String proc = 
				"CREATE PROCEDURE dump_one_graph ( IN  srcgraph VARCHAR  , IN  out_file VARCHAR  , IN  file_length_limit  INTEGER  := 1000000000   ) " +
				"{     " +
				"  DECLARE  file_name     varchar;     " +
				"  DECLARE  env, ses      any;     " +
				"  DECLARE  ses_len,               " +
				"           max_ses_len,               " +
				"           file_len,               " +
				"           file_idx      integer;     " +
				"  SET ISOLATION = 'uncommitted';     max_ses_len := 10000000;     file_len := 0;     file_idx := 1;     " +
				"  file_name := sprintf ('%s%06d.ttl', out_file, file_idx);     " +
				"  string_to_file ( file_name, sprintf ( '# Dump of graph <%s>, as of %s\n', srcgraph, CAST (NOW() AS VARCHAR) ), -2 ); " +
				"  env := vector (dict_new (16000), 0, '', '', '', 0, 0, 0, 0);     " +
				"  ses := string_output ();     " +
				"  FOR (SELECT * FROM ( SPARQL DEFINE input:storage \"\"                           " +
				"      SELECT ?s ?p ?o { GRAPH `iri(?:srcgraph)` { ?s ?p ?o } }                         ) " +
				"      AS sub OPTION (LOOP)) " +
				"  DO       " +
				"  {         " +
				"    http_ttl_triple (env, \"s\", \"p\", \"o\", ses);         " +
				"    ses_len := length (ses);         " +
				"    IF (ses_len > max_ses_len)           " +
				"    {            " +
				"       file_len := file_len + ses_len;             " +
				"       IF (file_len > file_length_limit)               " +
				"       {                 " +
				"         http (' .\n', ses);                 " +
				"         string_to_file (file_name, ses, -1);                 file_len := 0;                 file_idx := file_idx + 1;                 " +
				"         file_name := sprintf ('%s%06d.ttl', out_file, file_idx);                 " +
				"         string_to_file ( file_name,                                   " +
				"         sprintf ( '# Dump of graph <%s>, as of %s (part %d)\n', srcgraph, CAST (NOW() AS VARCHAR), file_idx), -2 ); " +
				"         env := vector (dict_new (16000), 0, '', '', '', 0, 0, 0, 0);               " +
				"       }             " +
				"       ELSE               " +
				"         string_to_file (file_name, ses, -1);             " +
				"         ses := string_output ();           " +
				"     }" +
				"   }     " +
				"   IF (LENGTH (ses))       " +
				"   {         " +
				"     http (' .\n', ses);         " +
				"     string_to_file (file_name, ses, -1);       " +
				"   }   " +
				"}";

		File temp = new File (tempDir, f.getName());
		
		String func = "dump_one_graph ('" + graphUri + "', '" + temp.getAbsolutePath() + "')";
		Statement st = getConnection().createStatement();
		try
		{
			execute(st, proc);
			execute(st, func);
		}
		catch (VirtuosoException ex)
		{
			rethrowWithTips(ex);
		}
		
		for (File g : temp.getParentFile().listFiles())
		{
			if (g.getName().startsWith (f.getName()))
			{
				log.info ("output: " + g.getName());
				FileUtils. moveFile(g, new File (f.getParentFile(), g.getName()));
			}
		}
		

		// TODO: following functions may be useful as well
		// DB.DBA.RDF_TRIPLES_TO_RDF_XML_TEXT
		// DB.DBA.RDF_TRIPLES_TO_TTL
	}
	
	private String prefixes = "";
	
	public void setPrefixes(String prefixes)
	{
		this.prefixes = prefixes;
	}
	
//	public ResultSet execSelect(String query) 
//	{
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, graph);
//		ResultSet results = vqe.execSelect();
//		return results;
//	}
//	
//	public Model execConstruct(Query sparql)
//	{
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, graph);
//		Model result = vqe.execConstruct();
//		return result;
//	}
	

	@Override
	public int hashCode()
	{
		// hashCode that is somewhat stable between instantiations
		return
				2 * ((user != null) ? user.hashCode() : 0) +  
				3 * ((port != null) ? port.hashCode() : 0) +
				7 * ((host != null) ? host.hashCode() : 0);
	}	
	
	@Override
	public String toString()
	{
		return "VirtuosoConnection[" + user + "@" + host + ":" + port + "]";
	}

	File iniFile = null;
	
	/** get the virtuoso ini file location */
	public File getIni() throws SQLException, IOException 
	{
		// SELECT server_root (), virtuoso_ini_path ();
		// to get location of virtuoso.ini

		if (iniFile == null)
		{
			Statement st = getConnection().createStatement();
			ResultSet rs = executeQuery(st, "SELECT server_root (), virtuoso_ini_path ();");
			rs.next();
			iniFile = new File (rs.getString(0), rs.getString(1));
		}
		return null;
	}

	/**
	 * Virtuoso can process raw sql queries
	 */
	public boolean rawSql(String string) throws SQLException, IOException 
	{
		Statement st = getConnection().createStatement();
		try
		{
			return execute(st, string);
		}
		catch (VirtuosoException ex)
		{
			rethrowWithTips(ex);
		}
		return false; // will never be executed in practice
	}

	public RecordStream execSelect(String string) throws SQLException, IOException, StreamException 
	{
		Statement st = getConnection().createStatement();
		try
		{
			return new ResultSetRecordStream(executeQuery(st, string));
		}
		catch (VirtuosoException ex)
		{
			rethrowWithTips(ex);
		}
		return null; // will never be executed in practice		
	}

	private void logEnable(Statement st, boolean autoCommitEnabled, boolean loggingEnabled) throws SQLException
	{
		/**
		 * Arguments to function log_enable:
		 * param 1: bit 1 is for enabling logging, bit 2 is for enabling autocommit. NULL leaves unchanged. (can be used to read current value) 
		 * param 2: non-zero to ignore any errors
		 * returns the previous value of the bits.
		 */
		int newValue = (loggingEnabled ? 1 : 0) + (autoCommitEnabled ? 2 : 0);
		
		executeQuery(st, "log_enable(" + newValue + ",1)"); // enable log
	}
	
	private int readLogEnable(Statement st) throws SQLException
	{
		ResultSet rs = executeQuery(st, "SELECT log_enable(NULL)"); // enable log
		rs.next();
		return rs.getInt(1);
	}
	
				
	public void vdelete(String graphUri) throws SQLException, IOException 
	{
		Statement st = getConnection().createStatement();
		ResultSet rs = null;
		try
		{
			// autocommit on, logging off -> log file doesn't blow up, but deleting is slow (factor 3)
			// autocommit off, logging on -> deleting is fast. Log isn't used???
			
			/*
			 * Documentation is unclear about which log_enable settings might work well with vdelete.
			 * So here I record the results of each settings empirically, and hope we stumble upon a permutation that works.
			 * 
			 * Before 18/4/2014, code was:
			 * 		logEnable(st, false, true); 
			 * 
			 * 18/4/2014: maru01:db02 crashed in attempt to delete CTD.
			 * 
			 * 19/4/2014: code changed to:
			 * 		logEnable(st, true, false);
			 * 		-> Deletion of CTD on maru01:db02 now successful.
			 */
			logEnable(st, true, false);

			//TODO: input sanitization
			String q = "SPARQL CLEAR GRAPH <" + graphUri + ">";
			rs = executeQuery(st, q);
		}
		finally
		{
			if (rs != null) rs.close();
			st.close();
		}
	}
	
	public void setNamespacePrefix (String prefix, String uri) throws SQLException, IOException
	{
		String error;
		if ((error = StringUtils.checkForIllegalCharacter(prefix, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-")) != null) throw new IllegalArgumentException ("Prefix '" + prefix + "' " + error);
		if ((error = StringUtils.checkForIllegalCharacter(uri, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-:/%~#.")) != null) throw new IllegalArgumentException ("Url '" + uri + "' " + error);
		
		Statement st = getConnection().createStatement();
		
		String sql = 
		"DB.DBA.XML_SET_NS_DECL ('" + prefix + "', '" + uri + "', 2)";
		ResultSet rs = executeQuery(st, sql);
		rs.next();
	}
	
	public Map<String, String> getNamespacePrefixes() throws SQLException, IOException
	{
		Map<String, String> result = new HashMap<String, String>();
		Statement st = getConnection().createStatement();
		String sql = "SELECT NS_PREFIX, NS_URL FROM DB.DBA.SYS_XML_PERSISTENT_NS_DECL";
		ResultSet rs = executeQuery(st, sql);
		while (rs.next())
		{
			String prefix = rs.getString("NS_PREFIX");
			String namespace = rs.getString("NS_URL");
			result.put (prefix, namespace);
		}
		return result;
	}

	@Override
	public Stream<com.generalbioinformatics.rdf.stream.Statement> sparqlConstruct(String query) throws StreamException 
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
