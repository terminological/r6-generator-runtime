package uk.co.terminological.rjava.types;

import java.util.Arrays;

import uk.co.terminological.rjava.RDataType;

/**
 * The vector of numerics is needed to ensure that NA values are correctly handled and
 * allow flexibility of a java List structure for easy manipulation. Factory methods are in {@link RVector}. 
 * @author terminological
 *
 */
@RDataType(
		JavaToR = { 
				"function(jObj) {",
				"	tmpVec = as.numeric(rJava::.jcall(jObj,returnSig='[D',method='rPrimitive'))",
				"	tmpDim = as.integer(rJava::.jcall(jObj,returnSig='[I',method='rDim'))",
				"   if (length(tmpDim)==2) return(matrix(tmpVec,tmpDim))",
				"	return(array(tmpVec,tmpDim))",
				"}"
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	if (is.null(rObj)) return(rJava::.jnew('~RNUMERICARRAY~'))",
				"	if (!is.numeric(rObj)) stop('expected a numeric')",
				"	if (!is.array(rObj)) stop('expected an array')",
				"	tmpVec = as.vector(as.numeric(rObj))",
				"	tmpDim = dim(rObj)",
				"	return(rJava::.jnew('~RNUMERICARRAY~',rJava::.jarray(tmpVec),rJava::.jarray(tmpDim)))", 
				"}"
		}//,
		//JNIType = "[D"
	)
public class RNumericArray extends RArray<RNumeric> implements JNIPrimitive {
	
	private static final long serialVersionUID = RObject.datatypeVersion;
	private RNumericVector vector = new RNumericVector();
	
	public RNumericArray(double[] primitives, int[] dimensions) {
		for (int i=0; i<primitives.length; i++) this.vector.add(new RNumeric(primitives[i]));
		this.dimensions = dimensions;
	}
	
	public RNumericVector getVector() {return vector;}
	
	public RNumericArray() {super();}
	public RNumericArray(int length) {
		vector = new RNumericVector(length);}
	
	public double[] rPrimitive() {
		return this.vector.stream().mapToDouble(ri -> ri.rPrimitive()).toArray();
	}
	
	public int[] rDim() {
		return this.dimensions;
	}
	
	@Override
	public Class<RNumeric> getType() {
		return RNumeric.class;
	}
	
//	@SuppressWarnings("unchecked")
//	public Stream<Double> get() {
//		return this.stream().map(r -> r.get());
//	}
//	
//	@SuppressWarnings("unchecked")
//	public Stream<Optional<Double>> opt() {
//		return this.stream().map(s -> s.opt());
//	}
	
	public Double get(int... indices) {
		if (indices.length != this.dimensions.length) throw new IndexOutOfBoundsException("Dimensionality mismatch: given "+indices.length+" dimensions expected "+this.dimensions.length);
		int multiplier = 1;
		int index=0;
		for (int dimension=0; dimension<this.dimensions.length; dimension+=1) {
			if (indices[dimension] <0 || indices[dimension] >= this.dimensions[dimension]) throw new IndexOutOfBoundsException("Index out of bounds: "+indices[dimension]+" should be >=0 and <"+this.dimensions.length);
			index += indices[dimension]*multiplier;
			multiplier *= this.dimensions[dimension];
		}
		return vector.get(index).get();
	}
	
	public RNumeric getR(int... rIndices) {
		if (rIndices.length != this.dimensions.length) throw new IndexOutOfBoundsException("Dimensionality mismatch: given "+rIndices.length+" dimensions expected "+this.dimensions.length);
		int multiplier = 1;
		int index=0;
		for (int dimension=0; dimension<this.dimensions.length; dimension+=1) {
			if (rIndices[dimension] < 1 || rIndices[dimension] > this.dimensions[dimension]) throw new IndexOutOfBoundsException("Index out of bounds: "+rIndices[dimension]+" should be >0 and <="+this.dimensions.length);
			index += (rIndices[dimension]-1)*multiplier;
			multiplier *= this.dimensions[dimension];
		}
		return vector.get(index);
	}
	
	public String toString() {
		return "Array:\n\tdimensions: "+Arrays.toString(this.dimensions)+"\n"+
				"\tvalues:"+this.getVector().toString();
	}
	
//	public RNumericArray get(int i) {
//		
//	}
	
	public static RNumericArray empty() {
		return new RNumericArray();
	}
	
}