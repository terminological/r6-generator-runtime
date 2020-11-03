package uk.co.terminological.rjava.types;

import java.util.Iterator;

import uk.co.terminological.rjava.RObjectVisitor;

public class RDataframeRow extends RNamedPrimitives implements RCollection<RNamed<RPrimitive>> {

	
	private int row;
	private RDataframe dataframe;
		
	public RDataframeRow(RDataframe rDataframe, int i) {
		super();
		this.row = i;
		this.dataframe = rDataframe;
		rDataframe.keySet().forEach(k -> {
			this.put(k, rDataframe.get(k).get(i));
		});
	}
	public int getRowNumber() {return row;}
	
	public RDataframeRow lag(int before) {return dataframe.getRow(row-before);}
	public RDataframeRow lead(int after) {return dataframe.getRow(row+after);}
	
	public RDataframeRow lag() {return lag(1);}
	public RDataframeRow lead() {return lead(1);}
	
	
	
	@Override
	public String rCode() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {
		X out = visitor.visit(this);
		this.iterator().forEachRemaining(c -> c.accept(visitor));
		return out;
	}
	@Override
	public Iterator<RNamed<RPrimitive>> iterator() {
		return new Iterator<RNamed<RPrimitive>>() {
			Iterator<Entry<String, RPrimitive>> it = RDataframeRow.this.entrySet().iterator();
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public RNamed<RPrimitive> next() {
				return new RNamed<RPrimitive>(it.next());
			}
		
		};
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	public RNamedPrimitives rowGroup() {
		RNamedPrimitives out = new RNamedPrimitives();
		this.stream().filter(s -> dataframe.groupSet().contains(s.getKey())).forEach(np -> out.put(np.getKey(), np.getValue()));
		return out;
	}
	
}
