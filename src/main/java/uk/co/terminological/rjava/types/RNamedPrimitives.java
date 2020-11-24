package uk.co.terminological.rjava.types;

import java.util.Map.Entry;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class RNamedPrimitives extends LinkedHashMap<String,RPrimitive> {

	public RNamedPrimitives(RNamedPrimitives tmp) {
		super(tmp);
	}
	
	public RNamedPrimitives() {
		super();
	}

	public int hashCode() {
		return super.hashCode();
	}
	
	public boolean equals(Object another) {
		return super.equals(another);
	}
	
	public Iterator<Entry<String,RPrimitive>> iterator() {
		return this.entrySet().iterator();
//		return new Iterator<RNamed<RPrimitive>>() {
//			Iterator<Entry<String, RPrimitive>> it = RNamedPrimitives.this.entrySet().iterator();
//			
//			@Override
//			public boolean hasNext() {
//				return it.hasNext();
//			}
//
//			@Override
//			public RNamed<RPrimitive> next() {
//				return RNamed.from(it.next());
//			}
//		
//		};
	}
}
