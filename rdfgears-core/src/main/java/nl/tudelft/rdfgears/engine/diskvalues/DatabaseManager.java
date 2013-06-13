package nl.tudelft.rdfgears.engine.diskvalues;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.rdfgears.engine.Config;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;

public class DatabaseManager {
	private static Environment dbEnvironment;
	private static DatabaseConfig dbConfig;

	private static Map<String, Database> databases;
	private static Database complexStore;
	
	private static Database myClassDb;
	
	private static StoredClassCatalog storedClassCatalog;

	public static void initialize() {
		databases = new HashMap<String, Database>();
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);

		dbEnvironment = new Environment(new File(Config.DEFAULT_DB_PATH),
				envConfig);
		dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setTemporary(true);
		complexStore = (dbEnvironment.openDatabase(null, "complexStore",
				dbConfig));
		dbConfig.setSortedDuplicates(false);
		myClassDb = dbEnvironment.openDatabase(null, "classDb", 
	            dbConfig); 
	}

	public static Database openListDatabase(String name) {
		Database ret = databases.get(name);

		if (ret == null) {
			ret = dbEnvironment.openDatabase(null, name, dbConfig);
			databases.put(name, ret);
		}

		return ret;
	}

	public static Database getComplexStore() {
		return complexStore;
	}

	public static void cleanUp() {
		complexStore.close();
		myClassDb.close();
		for (Database db : databases.values())
			if (db != null)
				db.close();

		if (dbEnvironment != null)
			dbEnvironment.close();
	}

	public static DatabaseEntry int2entry(int i) {
		DatabaseEntry key = new DatabaseEntry();
		EntryBinding<Integer> myBinding = TupleBinding
				.getPrimitiveBinding(Integer.class);
		myBinding.objectToEntry(i, key);
		return key;
	}

	public static DatabaseEntry long2entry(long i) {
		DatabaseEntry key = new DatabaseEntry();
		EntryBinding<Long> myBinding = TupleBinding
				.getPrimitiveBinding(Long.class);
		myBinding.objectToEntry(i, key);
		return key;
	}

	public static DatabaseEntry getComplexEntry(long id) {
		DatabaseEntry key = DatabaseManager.long2entry(id);
		DatabaseEntry data = new DatabaseEntry();
		complexStore.get(null, key, data, LockMode.DEFAULT);
		return data;
	}
	
	public static StoredClassCatalog getClassCatalog() {
		return new StoredClassCatalog(myClassDb);
	}
	
}
