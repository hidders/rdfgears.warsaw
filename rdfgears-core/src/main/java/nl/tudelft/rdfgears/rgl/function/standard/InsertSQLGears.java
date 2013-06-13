package nl.tudelft.rdfgears.rgl.function.standard;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.ModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.ListBackedBagValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionConfigurationException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.util.row.FieldIndexMap;
import nl.tudelft.rdfgears.util.row.FieldIndexMapFactory;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;
import nl.tudelft.rdfgears.rgl.function.standard.SQLGears.SQLTypeSpec;

/**
 * 
 * @author jsroka
 */
public class InsertSQLGears extends AtomicRGLFunction {

	private PreparedStatement query;
	private Connection conn;
	private String databaseURL = null;
	private LinkedHashMap<String, SQLTypeSpec> outputType;
	private LinkedHashMap<String, SQLTypeSpec> inputType;

	LinkedHashMap<String, SQLTypeSpec> parseTypeString(String typeString) {
		LinkedHashMap<String, SQLTypeSpec> map = new LinkedHashMap<String, SQLTypeSpec>();
		if (typeString == null  || typeString.trim().isEmpty() ) {
			return map;
		}
		String[] typeStringsParts = typeString.split(",");
		for (String oneString : typeStringsParts) {
			String[] fields = oneString.split(":");
			SQLTypeSpec t = SQLTypeSpec.determineType(fields[1].charAt(0));
			if (t.equals(SQLTypeSpec.NOT_VALID)) {
				 throw new
				 FunctionConfigurationException("Unknown type specification: \n"
				 + fields[1]);
			}
			map.put(fields[0], t);
		}
		return map;
	}

	public void initialize(Map<String, String> config) {
		databaseURL = config.get("databaseURL");
		String userName = config.get("userName");
		String password = config.get("password");

		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(databaseURL, userName, password);
		} catch (Exception ex) {
			throw new FunctionConfigurationException(
					"Could not connect to databse: \n" + databaseURL + "\n"
							+ ex.getMessage());
		}

		String queryString = config.get("query");
		try {
			query = conn.prepareStatement(queryString);
		} catch (SQLException ex) {
			throw new FunctionConfigurationException(
					"Your SQL query is incorrect: \n" + queryString + "\n"
							+ ex.getMessage());
		}
		
//		String bindVarStr = config.get("bindVariables");
//		if (bindVarStr!=null){
//			String[] split = bindVarStr.split(";");
//			for (int i=0; i<split.length; i++){
//				if (split[i].length()>0)
//					requireInput(split[i]); // bound variables can be either graphs or URI/literals
//			}	
//		}
		
		requireInput("record");

		String inputSpec = config.get("inputSpec");
		// there can be no input, we allow it
		inputType = parseTypeString(inputSpec);

		String outputSpec = config.get("outputSpec");
		if (outputSpec == null || outputSpec.equals("")) {
			// throw new
			// FunctionConfigurationException("Please define the output values names and types");
		}
		outputType = parseTypeString(outputSpec);
	}

	protected PreparedStatement getQuery() {
		return this.query;
	}

	// public RGLType getOutputType() {
	// return behavior.getOutputType();
	// }
	// public RGLValue simpleExecute(ValueRow inputRow) {
	// String computedQueryString = queryString;
	// for (String varName : inputRow.getRange()) {
	// if (!inputRow.get(varName).isGraph()) {
	// computedQueryString =
	// computedQueryString.replace("?" + varName,
	// inputRow.get(varName).toString());
	// }
	// }
	//
	// System.err.println(queryString);
	//
	// setQuery(computedQueryString);
	// configureBehavior();
	//
	// return behavior.simpleExecute(inputRow);
	// }

	// only the order of input/outpu matters, it has to be the same as
	// in the query, the names are just for displying to the user
	private List<LinkedHashMap<String, RGLValue>> exec(List<RGLValue> input) {
		List<LinkedHashMap<String, RGLValue>> l = new ArrayList<LinkedHashMap<String, RGLValue>>();
		try {
			query.clearParameters();

			// setting input parameters for the query
			Iterator<Entry<String, SQLTypeSpec>> iSpecIter = inputType
					.entrySet().iterator();
			Iterator<RGLValue> iValuesIter = input.iterator();
			for (int i = 1; i <= input.size(); i++) {
				Entry<String, SQLTypeSpec> e = iSpecIter.next();
				RGLValue value = iValuesIter.next();
				switch (e.getValue()) {
				case BOOLEAN:
					query.setBoolean(i, value.asBoolean().isTrue());
					break;
				case DOUBLE:
					query.setDouble(i, value.asLiteral().getValueDouble());
					break;
				case FLOAT:
					query.setFloat(i,
							Float.valueOf(value.asLiteral().getValueString()));
					break;
				case INT:
					query.setInt(i,
							Integer.valueOf(value.asLiteral().getValueString()));
					break;
				case LONG:
					query.setLong(i,
							Long.valueOf(value.asLiteral().getValueString()));
					break;
				case STRING:
					query.setString(i, value.asURI().toString());
					break;
				case NOT_VALID:
					throw new RuntimeException(
							"Unknown type specification in output. This should not have had happened!");
				}
			}

			// reading result from the query and filling the output map
			query.execute();
//			ResultSet rs = query.executeQuery();
//			while (rs.next()) {
//				LinkedHashMap<String, RGLValue> m = new LinkedHashMap<String, RGLValue>();
//				int i = 1;
//				for (Entry<String, SQLTypeSpec> e : outputType.entrySet()) {
//					switch (e.getValue()) {
//					case BOOLEAN:
//						m.put(e.getKey(),
//								rs.getBoolean(i) ? ValueFactoryIface.trueValue
//										: ValueFactoryIface.falseValue);
//						break;
//					case DOUBLE:
//						m.put(e.getKey(), ValueFactory.createLiteralDouble(rs
//								.getDouble(i)));
//						break;
//					case FLOAT:
//						m.put(e.getKey(), ValueFactory.createLiteralTyped(
//								rs.getFloat(i), XSDDatatype.XSDfloat));
//						break;
//					case INT:
//						m.put(e.getKey(), ValueFactory.createLiteralTyped(
//								rs.getInt(i), XSDDatatype.XSDinteger));
//						break;
//					case LONG:
//						m.put(e.getKey(), ValueFactory.createLiteralTyped(
//								rs.getLong(i), XSDDatatype.XSDlong));
//						break;
//					case STRING:
//						m.put(e.getKey(), ValueFactory.createLiteralPlain(
//								rs.getString(i), ""));
//						break;
//					case NOT_VALID:
//						throw new RuntimeException(
//								"Unknown type specification in output. This should not have had happened!");
//					}
//					i++;
//				}
//				l.add(m);
//			}
		} catch (SQLException ex) {
			throw new FunctionConfigurationException(
					"Could not execute quer: \n" + ex.getMessage());
		}
		return l;
	}

	public static void main(String[] args) {
		// // TODO code application logic here
		LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
		// // m.put("userName", "yzhang12");
		// // m.put("databaseURL",
		// "jdbc:oracle:thin:@orca.csc.ncsu.edu:1521:ORCL");
		// // m.put("password", "guest");
		// // m.put("query", "SELECT * FROM yzhang12.student");
		m.put("userName", "tomek");
		m.put("databaseURL", "jdbc:postgresql://localhost/tomek");
		// m.put("password", "app");
		m.put("query",
				"select name, credit_limit, customer_id from customer where discount_code = ? and state = ?");
		m.put("outputSpec", "NAME:S,CREDIT_LIMIT:L,CUSTOMER_ID:I");
		m.put("inputSpec", "DISCOUNT_CODE:S,STATE:S");
		InsertSQLGears sq = new InsertSQLGears();
		sq.initialize(m);
		//
		// List<RGLValue> input = new ArrayList<RGLValue>();
		// input.add("N");
		// input.add("FL");
		// List<LinkedHashMap<String, RGLValue>> output = sq.exec(input);
		// for (LinkedHashMap<String, RGLValue> oMap : output) {
		// for (Entry<String, RGLValue> e : oMap.entrySet()) {
		// System.out.print(e.getKey()+":"+e.getValue()+"  ");
		// }
		// System.out.println();
		// }
	}

	@Override
	protected RGLValue executeImpl(ValueRow inputRow) {
		List<RGLValue> inputList = new ArrayList<RGLValue>();

		for (String key : inputType.keySet()) {
			inputList.add(inputRow.get("record").asRecord().get(key));
		}

		List<RGLValue> backingList = ValueFactory.createBagBackingList();

		List<LinkedHashMap<String, RGLValue>> inner = exec(inputList);

		String[] fieldsAr = new String[outputType.size()];
		int i = 0;
		for (String fieldName : outputType.keySet()) {
			fieldsAr[i++] = fieldName;
		}

		FieldIndexMap fiMap = FieldIndexMapFactory.create(fieldsAr);

		for (LinkedHashMap<String, RGLValue> m : inner) {
			AbstractModifiableRecord rec = new ModifiableRecord(fiMap);
			for (Entry<String, RGLValue> e : m.entrySet()) {
				rec.put(e.getKey(), (RGLValue) e.getValue());
			}
			backingList.add(rec);
		}

		return new ListBackedBagValue(backingList);
	}

	@Override
	public RGLType getOutputType(TypeRow inputTypes)
			throws WorkflowCheckingException {
		TypeRow recordTypeRow = new TypeRow();
		RDFType rdfType = RDFType.getInstance();
		for (String name : outputType.keySet()) {
			recordTypeRow.put(name, rdfType);
		}
		return BagType.getInstance(RecordType.getInstance(recordTypeRow));
	}
}