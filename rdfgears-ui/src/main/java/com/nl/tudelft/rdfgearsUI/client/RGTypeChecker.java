package com.nl.tudelft.rdfgearsUI.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.allen_sauer.gwt.log.client.Log;

import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.nl.tudelft.rdfgearsUI.client.Dia.Node;
import com.nl.tudelft.rdfgearsUI.client.Dia.NodePort;
import com.nl.tudelft.rdfgearsUI.client.Dia.Path;
import com.nl.tudelft.rdfgearsUI.client.Dia.RGCanvas;
import com.nl.tudelft.rdfgearsUI.client.Dia.RGLogger;
import com.nl.tudelft.rdfgearsUI.client.Dia.RGWorkflow;

public class RGTypeChecker {
	private String varPrefix = "a";
	private int varIndex = 0;
	private RGCanvas canvas = null;
	private RGLogger logger;
	private Map <String, HashMap <String, String>> scope2vars = new HashMap <String, HashMap <String, String>>();
	//Map <String, String> newName2OldName = new HashMap <String, String>();
	
	
	/**
	 * with system tuple (v, M, E)
	 * v represented by the ports
	 */
	//variable name -> new type
	private Map <String, RGType> sTable = new HashMap <String, RGType>(); //M
	//unification table
	private Map <RGType, RGType> uTable = new HashMap <RGType, RGType>(); //E
	private Map <RGType, RGType> tempUTable = new HashMap <RGType, RGType>(); //E
	
	public RGTypeChecker(RGCanvas _canvas, RGLogger _logger) {
		canvas = _canvas;
		logger = _logger;
	}
	
	public boolean doTypeCheck(RGWorkflow wf){
		logger.clear();
		return rewriteNetworkType(wf.getNodeIds(), wf.getNodes());
	}
	/**
	 * apply all the unification and substitution rules based on the complete substitution table
	 */
	public boolean rewriteNetworkType(ArrayList <String> nodeIds, Map <String, Node> nodes){
		logger.clear();
		ArrayList <String> subsMessages = new ArrayList <String>();
		//revert all the port's type
//		Log.debug("------PHASE 1. REVERT TO INITIAL TYPE-------------");
		revertAllTypes(nodeIds, nodes);
//		Log.debug("------PHASE 2. BUILD SUBSTITUTION AND UNIFICATION TABLE-------------");
		
		if(!buildSubstitutionTable(nodeIds, nodes, subsMessages)){
			subsMessages.add(0, "[ERROR] Type's substitution process has failed");
			logger.display(subsMessages);
			
			return false;
		}
		
		/*uncomment to debug*/
//		Log.debug("------PHASE 3. DO UNIFICATION-------------");
////		Log.debug("final uTable content:");
//		Set <RGType> keySet1 = uTable.keySet();
//		Iterator <RGType> it1 = keySet1.iterator();
//		while(it1.hasNext()){
//			RGType t = it1.next();
////			Log.debug(t.getElement().toString() + " --> " + uTable.get(t).getElement().toString());
//		}
		ArrayList <String> uniMessages = new ArrayList <String>();
		if(!doUnifications(uniMessages)){
			canvas.displayErrorMessage("Types unification failed");
			logger.display(uniMessages);
			for(String nId: nodeIds){
				if(nodes.containsKey(nId)){
					Node n = nodes.get(nId);
					ArrayList <String> pNames = n.getPortNames();
					for(String pN : pNames){
						NodePort port = n.getPortByPortName(pN);
						Path p = port.getPath();
						if(p != null){
							p.setAsErrorPath(true);
						}
					}
				}
			}
			//Throw messages to logger 
			return false;
		}
//		Log.debug("------PHASE 4. REWRITE ALL PORTS' TYPE BY DEEP SUBSTITUTION-------------");
		/*uncomment to debug*/
////		Log.debug("final sTable content:");
//		Set <String> keySet = sTable.keySet();
//		Iterator <String> it = keySet.iterator();
//		while(it.hasNext()){
//			String v = it.next();
////			Log.debug(v + " --> " + sTable.get(v).getElement().toString());
//		}
		
		for(String nId: nodeIds){
			if(nodes.containsKey(nId)){
				Node n = nodes.get(nId);
				ArrayList <String> pNames = n.getPortNames();
//				Log.debug("Node: " + n.getId());
				for(String pN : pNames){
					NodePort port = n.getPortByPortName(pN);				
//					Log.debug("before:"+ port.getType().getElement().toString());
					deepSubstitute(port.getType()); //rewrite all types
//					Log.debug("after:"+ port.getType().getElement().toString());
				}
			}
		}
		return true;
	}
	
	/**
	 * return types to its original value/pattern (reset all types)
	 * @param nodeIds
	 * @param nodes
	 */
	private void revertAllTypes(ArrayList <String> nodeIds, Map <String, Node> nodes){
		for(String nId: nodeIds){
			if(nodes.containsKey(nId)){
				Node n = nodes.get(nId);
				ArrayList <String> pNames = n.getPortNames();
//				Log.debug("Node: " + n.getId());
				for(String pN : pNames){
					NodePort port = n.getPortByPortName(pN);
					port.getType().revert();
					Path p = port.getPath();
					if(p != null){
						p.setAsErrorPath(false);
					}
//					Log.debug("reverted to: " + port.getType().getElement().toString());
				}
			}
		}
	}
	
	/**
	 * For all non-variable type, rewrite type to variable (A) and add entry into substitution table A -> original_type
	 * @param t
	 */
	public void decomposeType(RGType t){
		if(t.getTypeName() != RGTypeName.VARIABLE){
			String newName = createUniqueTypeName();
			sTable.put(newName, new RGType(t));
			t.substitute(new RGType(RGTypeUtils.getSimpleVarType(newName)));
		}
	}
	
	public void decomposeTuple(RGType r){
		if(r.getTypeName() == RGTypeName.TUPLE){
			ArrayList <String> fieldNames = r.getTupleFieldNames();
			for(String fieldName: fieldNames){
				decomposeType(r.getTupleFieldType(fieldName));
			}
		}
	}
	/**
	 * Lazy type's rewriting
	 */
	
	/**
	 * build substitution table from node's input ports
	 * @param nodeIds
	 * @param nodes
	 */
	public boolean buildSubstitutionTable(ArrayList <String> nodeIds, Map <String, Node> nodes, ArrayList <String> messages){
		sTable.clear();
		uTable.clear();
		tempUTable.clear();
		
		for(String nId: nodeIds){
			if(nodes.containsKey(nId)){
				Node n = nodes.get(nId);
				if(!nId.equals(RGCanvas.WORKFLOW_INPUT_NODE_ID)){
					ArrayList <String> pNames = n.getPortNames();
					for(String pN : pNames){
						NodePort endPort = n.getPortByPortName(pN);
						NodePort startPort = endPort.getConnectedPort(); //output port will return null
						if(startPort != null){
							decomposeType(endPort.getType());
							decomposeType(startPort.getType());
							if(startPort.getParentNode().getId() == RGCanvas.WORKFLOW_INPUT_NODE_ID){
								if(!updateSubstitutionTable(endPort.getType(), startPort.getType(), messages)){
									endPort.getPath().setAsErrorPath(true);
									messages.add(0, endPort.getParentNode().getId()+"("+ endPort.getParentNode().getName()+") --> " + startPort.getParentNode().getId()+"("+ startPort.getParentNode().getName()+")");
									return false;
								}else{
									endPort.getPath().setAsErrorPath(false);
								}
							}else{
								if(!updateSubstitutionTable(startPort.getType(), endPort.getType(), messages)){
									endPort.getPath().setAsErrorPath(true);
									messages.add(0, startPort.getParentNode().getId()+"("+ startPort.getParentNode().getName()+") --> " + endPort.getParentNode().getId()+"("+ endPort.getParentNode().getName()+")");
									return false;
								}else{
									endPort.getPath().setAsErrorPath(false);
								}
							}
						}
						
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * When 2 ports connected; A --> B
	 * Port A will have a bigger priority than B. A will overwrite B.
	 * @param startPortType = A.getType()
	 * @param endPortType = B.getType()
	 */
	private boolean updateSubstitutionTable(RGType startPortType, RGType endPortType, ArrayList <String> messages){
		
		if(startPortType.getTypeName() != RGTypeName.VARIABLE || endPortType.getTypeName() != RGTypeName.VARIABLE){
			return false; //have to be a variable, all the port type should be decomposed first.
		}
		RGType et1 = getSubstitution(endPortType); //M(alpha)
		RGType st1 = getSubstitution(startPortType); //M(beta)
		
//		Log.debug("----------sub for: " + st1.getElement().toString() + " -- "+ et1.getElement().toString());
		
		if(et1.getTypeName() == RGTypeName.VARIABLE){
			if(sTable.containsKey(et1.getVarName())){
				if(startPortType != sTable.get(et1.getVarName())){
					messages.add("Worfklow is not well typed: Contain type ambiguous error");
				}
			}else if(isSubstitutionRecursive(et1, st1)){
					messages.add("[ERROR] Recursive type error");
					messages.add(et1.getElement().toString() + " --> " + st1.getElement().toString());
					return false;
			}else{
					sTable.put(et1.getVarName(), st1);
			}
		}else if(st1.getTypeName() == RGTypeName.VARIABLE){
			if(sTable.containsKey(st1.getVarName())){
				if(endPortType != sTable.get(st1.getVarName())){
					messages.add("Worfklow is not well typed: Contain type ambiguous error");
				}
			}else if(isSubstitutionRecursive(st1, et1)){
				messages.add("[ERROR] Recursive type error");
				messages.add(st1.getElement().toString() + " --> " + et1.getElement().toString());
				return false;
			}else{
				sTable.put(st1.getVarName(), et1);
			}
		}else if(st1.getTypeName() == RGTypeName.TUPLE){
			if(et1.getTypeName() == RGTypeName.TUPLE || et1.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE){ //compatible record
				sTable.put(endPortType.getVarName(), startPortType); // (alpha --> beta) //or to startPortType
				decomposeTuple(st1);
				decomposeTuple(et1);
				return updateTupleUnificationTable(uTable, st1, et1, messages); //E` = E U (TR1i -- TR2i) i= 0..n
			}else{
				messages.add("Type Error: Tuple and non tuple type");
				messages.add(st1.getElement().toString() + " -- " + et1.getElement().toString());
				return false;
			}
		}else if(st1.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE){
			if(et1.getTypeName() == RGTypeName.TUPLE){
				sTable.put(startPortType.getVarName(), endPortType);
				decomposeTuple(st1);
				decomposeTuple(et1);
				return updateTupleUnificationTable(uTable, et1, st1, messages);
			}else if(et1.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE) {
				sTable.put(startPortType.getVarName(), endPortType); 
				decomposeTuple(st1);
				decomposeTuple(et1);
				return updateTupleUnificationTable(uTable, st1, et1, messages); //E` = E U (TR1i -- TR2i) i= 0..n
			}else{
				messages.add("Type Error: Tuple and non tuple type");
				messages.add(st1.getElement().toString() + " -- " + et1.getElement().toString());
				return false;
			}
		}else if(st1.getTypeName() == RGTypeName.BAG && et1.getTypeName() == RGTypeName.BAG){
			sTable.put(startPortType.getVarName(), endPortType);
			uTable.put(et1.getSubType(), st1.getSubType());
		}else{
			if(st1.getTypeName() != et1.getTypeName()){ //unmatch substitution attempt
				messages.add("Unmatch type error");
				messages.add(st1.getElement().toString() + " --> " + et1.getElement().toString());
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * update unification table
	 * - r1 unified with r2 which put r1 on higher priority
	 * 
	 * PREREQUISITE: r2 have to be overwritten by r1 by updating the substitution table
	 * before:
	 * A -> <a:T...>
	 * B -> <a:T...>
	 * after (PREREQUISITE)
	 * A -> <a:T...>
	 * B -> A
	 * add entry to unification table
	 * @param r1 
	 * @param r2
	 */
	private boolean updateTupleUnificationTable(Map <RGType, RGType> table, RGType r1, RGType r2, ArrayList <String> messages){
		ArrayList <String> r1FieldNames = r1.getTupleFieldNames();
		ArrayList <String> r2FieldNames = r2.getTupleFieldNames();
		
		if(r1.getTypeName() == RGTypeName.TUPLE && r2.getTypeName() == RGTypeName.TUPLE){
			if(r1FieldNames.size() != r2FieldNames.size()){
				messages.add("Type error: Tuple fields do not match");
				deepSubstitute(r1);
				deepSubstitute(r2);
				messages.add(r1.getElement().toString() + " --> " + r2.getElement().toString());
				return false;
			}
		
			for(int i = 0; i < r1FieldNames.size(); i++){
				String fn = r1FieldNames.get(i);
				if(r2FieldNames.contains(fn)){					
					table.put(r1.getTupleFieldType(fn), r2.getTupleFieldType(fn));
				}else{
					messages.add("Type error: Tuple fields do not match");
					deepSubstitute(r1);
					deepSubstitute(r2);
					messages.add(r1.getElement().toString() + " --> " + r2.getElement().toString());
					return false;
				}
			}
		}else if(r1.getTypeName() == RGTypeName.TUPLE && r2.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE){
			if(r1FieldNames.size() < r2FieldNames.size()){
				messages.add("Type error: Tuple fields do not match");
				deepSubstitute(r1);
				deepSubstitute(r2);
				messages.add(r1.getElement().toString() + " --> " + r2.getElement().toString());
				return false;
			}
		
			for(int i = 0; i < r2FieldNames.size(); i++){
				String fn = r2FieldNames.get(i);
				if(r1FieldNames.contains(fn)){
					table.put(r1.getTupleFieldType(fn), r2.getTupleFieldType(fn));
				}else{
					messages.add("Type error: Tuple fields do not match");
					deepSubstitute(r1);
					deepSubstitute(r2);
					messages.add(r1.getElement().toString() + " --> " + r2.getElement().toString());
					return false;
				}
			}
		}else if(r1.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE && r2.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE){
			for(int i = 0; i < r2FieldNames.size(); i++){
				String fn = r2FieldNames.get(i);
				if(r1.getTupleFieldNames().contains(fn)){
					table.put(r1.getTupleFieldType(fn), r2.getTupleFieldType(fn));
				}else{
					r1.addTupleField(fn, r2.getTupleFieldType(fn));
				}
			}
		}else if(r1.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE && r2.getTypeName() == RGTypeName.TUPLE){
			messages.add("Type error: Tuple unification cannot be done.");
//			Log.error("Try to unify TUPLE into EXTENDIBLE_TUPLE. The operation must be the other way around");
			return false;
		}
		
		return true;
	}
	
	/*
	 * unify all the entry in unification table
	 * return true if all the unification success and unification table will be empty as the result
	 * return false otherwise
	 */
	private boolean doUnifications(ArrayList <String> messages){
		while(uTable.keySet().iterator().hasNext()){
			RGType t = uTable.keySet().iterator().next();
			if(!unifyTypes(t, uTable.get(t), messages)){
				return false;
			}else{
				uTable.remove(t);
			}
			if(tempUTable.keySet().size() > 0){
				while(tempUTable.keySet().iterator().hasNext()){
					RGType tT = tempUTable.keySet().iterator().next();
					if(!uTable.containsKey(tT)){
						uTable.put(tT, tempUTable.get(tT));
						tempUTable.remove(tT);
					}
				}
			}
		}
		return true;
	}
	
	//the result of the unification is by updating the substitution table and the possibility 
	//to add entry to unification table (tempUnificationTable to avoid concurrent object modification exception)
	private boolean unifyTypes(RGType t1, RGType t2, ArrayList <String> messages){
		RGType st1 = getSubstitution(t1);
		RGType st2 = getSubstitution(t2);
		
//		Log.debug("uni for: " + st1.getElement().toString() + " -- "+ st2.getElement().toString());
		
		if(st1.getTypeName() == RGTypeName.VARIABLE){
			if(isSubstitutionRecursive(st1, st2)){
				messages.add("[ERROR] Recursive type error");
				return false;
			}else
				sTable.put(st1.getVarName(), st2);
			
		}else if(st2.getTypeName() == RGTypeName.VARIABLE){
			if(isSubstitutionRecursive(st2, st1)){
				messages.add("[ERROR] Recursive type error");
				return false;
			}else
				sTable.put(st2.getVarName(), st1);
			
		}else if(st1.getTypeName() == st2.getTypeName()){
			//tuple - tuple; etuple - etuple; B - B | B element {bool, int}
			if(st1.getTypeName() == RGTypeName.TUPLE || st1.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE){
				decomposeTuple(st1);
				decomposeTuple(st2);
				return updateTupleUnificationTable(tempUTable, st1, st2, messages);
			}else if(st1.getTypeName() == RGTypeName.BAG){
				return unifyTypes(st1.getSubType(), st2.getSubType(), messages);
			}
		}else if(st1.getTypeName() == RGTypeName.TUPLE && st2.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE){
			decomposeTuple(st1);
			decomposeTuple(st2);
			return updateTupleUnificationTable(tempUTable, st1, st2, messages);
		}else if(st1.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE && st2.getTypeName() == RGTypeName.TUPLE){
			decomposeTuple(st1);
			decomposeTuple(st2);
			RGType temp = st1;
			st1 = st2;
			st2 = temp;
			return updateTupleUnificationTable(tempUTable, st1, st2, messages);
		}else{
			messages.add("Types cannot be unified");
			deepSubstitute(t1);
			deepSubstitute(t2);
			messages.add(t1.getElement().toString() + " -- " + t2.getElement().toString());
			return false; //types cannot be unified
		}
		return true;
	}
	
	/**
	 * Apply exhaustive substitution operation on a variable type, based on temporary substitution table
	 * @param t
	 * @return if t -> t` element Substitution Table and t` == variable, return shallowSubsitution(t`) else return t
	 */
	private RGType getSubstitution(RGType t){
	//	Log.debug("shallow s:" + t.getTypeElement().toString());
		if(t.getTypeName() == RGTypeName.VARIABLE){
			if(sTable.containsKey(t.getVarName())){
				return getSubstitution(sTable.get(t.getVarName()));
			}
		}
		return t;
	}
	
	/**
	 * Apply exhaustive substitution operation on a variable type, based on complete substitution table
	 * @param t, type to be rewritten
	 */
	private void deepSubstitute(RGType t){		
		if(t.getTypeName() == RGTypeName.VARIABLE){
			if(sTable.containsKey(t.getVarName())){
				t.substitute(sTable.get(t.getVarName()));
				deepSubstitute(t);
			}
		}else if(t.getTypeName() == RGTypeName.BAG){
			deepSubstitute(t.getSubType());
		}else if(t.getTypeName() == RGTypeName.TUPLE || t.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE){
			ArrayList <String> fNames = t.getTupleFieldNames();
			for(String fname: fNames){
				deepSubstitute(t.getTupleFieldType(fname));
			}
		}
	}
	
	/**
	 * rewrite all port's types based on substitution table
	 * @return true if well typed, false otherwise
	 */
	public boolean doNetworkExhaustiveSubstition(){
		
		return true;
	}
	
	public boolean isTypeMatch(RGType t1, RGType t2){
		if(t1.getTypeName() == t2.getTypeName()){
			if(t1.getTypeName() == RGTypeName.TUPLE || t1.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE){
				return isTupleFieldsMatch(t1, t2);
			}else if(t1.getTypeName() == RGTypeName.VARIABLE){
				return !t1.getVarName().equals(t2.getVarName());
			}else if(t1.getSubType().getTypeName() != RGTypeName.NULL)
				return isTypeMatch(t1.getSubType(), t2.getSubType());
			else
				return true;
		}else if(t1.getTypeName() == RGTypeName.VARIABLE || t2.getTypeName() == RGTypeName.VARIABLE){
			if(t1.getTypeName() == RGTypeName.VARIABLE){
				return !isSubstitutionRecursive(t1, t2);
			}else{
				return !isSubstitutionRecursive(t2, t1);
			}
		}else if( ((t1.getTypeName() == RGTypeName.TUPLE) && (t2.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE)) || 
				  ((t1.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE) && (t2.getTypeName() == RGTypeName.TUPLE))){
			return isTupleFieldsMatch(t1, t2);
		}
		
		return false;
	}
	
	public boolean isTupleFieldsMatch(RGType r1, RGType r2){
		ArrayList <String> r1FieldNames = r1.getTupleFieldNames();
		ArrayList <String> r2FieldNames = r2.getTupleFieldNames();
		
		if(r1.getTypeName() == RGTypeName.TUPLE && r2.getTypeName() == RGTypeName.TUPLE){
			if(r1FieldNames.size() != r2FieldNames.size()){
				return false;
			}else{
				for(int i = 0; i < r1FieldNames.size(); i++){
					String fn = r1FieldNames.get(i);
					if(!r2FieldNames.contains(fn)){
						return false;
					}else if(!isTypeMatch(r1.getTupleFieldType(fn),r2.getTupleFieldType(fn))){
						return false;
					}
				}
			}
		}else if(r1.getTypeName() == RGTypeName.TUPLE && r2.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE){
			for(String r1fn: r1FieldNames){
				if(r2FieldNames.contains(r1fn)){
					if(!isTypeMatch(r2.getTupleFieldType(r1fn), r1.getTupleFieldType(r1fn))){
						return false;
					}
				}
			}
		}else if((r1.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE && r2.getTypeName() == RGTypeName.TUPLE) || 
				 (r1.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE && r2.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE)){
			for(String r2fn: r2FieldNames){
				if(r1FieldNames.contains(r2fn)){
					if(!isTypeMatch(r1.getTupleFieldType(r2fn), r2.getTupleFieldType(r2fn))){
						return false;
					}
				}
			}
		}
		return true;
	}
	/**
	 * check/test if the substitution of var --> t will lead to a recursive substitution
	 * only happens with tuple type, with field type of tuple, variable, or bag
	 * ex.  A --> <a:A, a2:B>, 
	 * 		A --> <a:<aa:A>, a2:B>, 
	 * 		A --> <a:<aa:<aaa:A>>, a2:B>, 
	 * 		and so on
	 * @param var
	 * @param t
	 * @return
	 */
	public boolean isSubstitutionRecursive(RGType var, RGType t ){
		if(var.getTypeName() == RGTypeName.VARIABLE && (t.getTypeName() == RGTypeName.TUPLE || 
														t.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE || 
														t.getTypeName() == RGTypeName.VARIABLE ||
														t.getTypeName() == RGTypeName.BAG)){
			
			if((t.getTypeName() == RGTypeName.TUPLE) || (t.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE)){
				ArrayList <String> tFieldNames = t.getTupleFieldNames();
				for(String f: tFieldNames){
					if(isSubstitutionRecursive(var, t.getTupleFieldType(f))){
						Log.debug("cyclic detected:" + var.getElement().toString() + " --> " + t.getElement().toString());
						return true;
					}
				}
			}else if(t.getTypeName() == RGTypeName.VARIABLE){
				RGType ts = getSubstitution(t);
				if(var.getVarName().equals(ts.getVarName())){
					Log.debug("cyclic detected:" + var.getElement().toString() + " --> " + t.getElement().toString());
					return true;
				}
			}else{ //bag
				return isSubstitutionRecursive(var, t.getSubType());
			}
			
		}
		return false;
	}
	/**
	 * assign unique name to all the variables in the type
	 * @param type
	 * @return
	 */
	public Element rename(Element type, String scope){
	//	Log.debug("renaming:" + type.toString());
		String newName = "";
		if(!scope2vars.containsKey(scope)){
			scope2vars.put(scope, new HashMap<String, String>());
		}
		HashMap <String, String> varPool = scope2vars.get(scope);
		if(type.getTagName().equalsIgnoreCase("var")){
			if(varPool.containsKey(type.getAttribute("name"))){
				newName = varPool.get(type.getAttribute("name"));
			}else{
				newName = varPrefix + varIndex;
				varIndex += 1;
			}
			
			varPool.put(type.getAttribute("name"), newName);
			type.setAttribute("name", newName);
		}else{
			NodeList vars = type.getElementsByTagName("var");
			for(int i = 0; i < vars.getLength(); i++){
				Element v = (Element) vars.item(i);
				if(varPool.containsKey(v.getAttribute("name"))){
					newName = varPool.get(v.getAttribute("name"));
				}else{
					newName = varPrefix + varIndex;
					varIndex += 1;
					varPool.put(v.getAttribute("name"), newName);
				}
				v.setAttribute("name", newName);
			}
		}
		scope2vars.put(scope, varPool);
		return type;
	}
	
	public String createUniqueTypeName(){
		String n = varPrefix + varIndex;
		varIndex += 1;
		
		return n;
	}
}
