package nl.tudelft.rdfgears.rgl.datamodel.value;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.ValueManager;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.RenewablyIterableBag;
import nl.tudelft.rdfgears.util.RenewableIterator;

public abstract class OrderedBagValue extends BagValue implements
		RenewablyIterableBag {
	
	//	na potrzeby debugu, docelowo do usunięcia
	static Map<Long, Integer> cartes = new HashMap<Long, Integer>();
	public static StringBuffer buffer = new StringBuffer(40000);

//	protected Map<Long, Integer> iteratorPosition = new HashMap<Long, Integer>();

	public final Map<Long, Integer> getIteratorMap() {
		return ValueManager.getIteratorPositionsMap();
	}

	@Override
	public final RenewableIterator<RGLValue> renewableIterator(long id) {
		int pointAt;
		if (!ValueManager.getIteratorPositionsMap().containsKey(id)) {
			ValueManager.getIteratorPositionsMap().put(id, 0);
			pointAt = 0;
		} else {
			pointAt = ValueManager.getIteratorPositionsMap().get(id);//Math.max(iteratorPosition.get(id) - 1 , 0);
			// this way, we gave the last element of the last iteration again,
			// if you don't want it - simply skip it.
		}

		return new OrderedBagIterator(id, pointAt);
	}
	
	protected Iterator<RGLValue> iteratorAt(int position) {
		Engine.getLogger().info("iteratorAt(" + position + ")");
		Iterator<RGLValue> iterator = iterator();
		for (int i = 0; i < position; ++i) {
			iterator.next();
		}
		return iterator;
	}

	protected class OrderedBagIterator implements RenewableIterator<RGLValue> {
		private Iterator<RGLValue> innerIterator;
		private int position;
		private long id;

		public OrderedBagIterator(long id, int position) {
			this.id = id;
			this.position = position;
			innerIterator = iteratorAt(position);
		}

		@Override
		public boolean hasNext() {
			return innerIterator.hasNext();
		}

		@Override
		public RGLValue next() {
			position++;
			{ //debug, do skasowania
//				OrderedBagValue.cartes.put(OrderedBagValue.this.myId, position);
////				List<Integer> list = Engine.diagnostic.get(cartes.get(807l) == null ? 0 : cartes.get(807l));
////				if (list == null) {
////					list = new ArrayList<Integer>(200);
////					Engine.diagnostic.put(cartes.get(807l), list);
////				}
////				list.add(cartes.get(1409l));
//				Iterator<Integer> it = OrderedBagValue.cartes.values().iterator();
//				buffer.append("\t");
//				for (Entry<Long, Integer> entry : cartes.entrySet()) {
//					buffer.append("<" + entry.getKey() + ":" + entry.getValue() + ":" + (entry.getKey() == myId ? id : "") + ">\t");
//				}
//				buffer.append("\n");
////				Engine.getLogger().warn(it.next() + "," + (it.hasNext() ? it.next() : 0));
			}
			ValueManager.getIteratorPositionsMap().put(id, position);
			return innerIterator.next();
		}

		@Override
		public void remove() {
			assert (false) : "Not implemented";
		}

	}

}