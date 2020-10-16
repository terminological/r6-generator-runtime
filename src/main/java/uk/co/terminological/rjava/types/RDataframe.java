package uk.co.terminological.rjava.types;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import uk.co.terminological.rjava.RConverter;
import uk.co.terminological.rjava.IncompatibleTypeException;
import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;


/**
 * A java equivalent of the R Dataframe organised in column format. This method has various accessors for iterating
 * over or streaming the contents
 * @author terminological
 *
 */
@RDataType(
		JavaToR = { 
				"function(jObj) {",
				// dynamically construct a local conversion function based on structure of dataframe
				"	convDf = eval(parse(text=rJava::.jcall(jObj,'rConversion', returnSig='Ljava/lang/String;')))",
				"	return(convDf(jObj))",
				"}"
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	jout = rJava::.jnew('~RDATAFRAME~')",
				"	lapply(colnames(rObj), function(x) {",
				"		rcol = rObj[[x]]",
				// select correct conversion function using naming convention
				"		if(is.character(rcol)) jvec = ~TO_RCHARACTERVECTOR~(rcol)",
				"		else if(is.integer(rcol)) jvec = ~TO_RINTEGERVECTOR~(rcol)",
				"		else if(is.factor(rcol)) jvec = ~TO_RFACTORVECTOR~(rcol)",
				"		else if(is.logical(rcol)) jvec = ~TO_RLOGICALVECTOR~(rcol)",
				"		else if(is.numeric(rcol)) jvec = ~TO_RNUMERICVECTOR~(rcol)",
				"		else if(inherits(rcol,c('Date','POSIXt'))) jvec = ~TO_RDATEVECTOR~(rcol)",
				"		else stop('unsupported data type in column: ',x)",
				// do the call to add columns by name and rvector wrapper
				"		rJava::.jcall(jout,returnSig='V',method='addCol',x,rJava::.jcast(jvec,new.class='~RVECTOR~'))",		
				"	})",
				"	return(jout)",
				"}"
		}
		//JNIType = "Luk/co/terminological/rjava/types/RDataframe;"
	)
public class RDataframe extends LinkedHashMap<String, RVector<? extends RPrimitive>> implements RCollection<RNamedList> {
	
	public RDataframe() {
		super();
	}

	public String toString() {
		return this.entrySet().stream()
			.map(
				kv -> kv.getKey() +": "+ kv.getValue().toString()
			).collect(Collectors.joining("\n"));
	}

	public Iterator<LinkedHashMap<String,RObject>> rowIterator() {
		return new Iterator<LinkedHashMap<String,RObject>>() {
			int row = 0;
			@Override
			public boolean hasNext() {
				return row < RDataframe.this.size();
			}

			@Override
			public LinkedHashMap<String, RObject> next() {
				LinkedHashMap<String,RObject> tmp = new LinkedHashMap<>();
				RDataframe.this.keySet().forEach(k -> tmp.put(k, RDataframe.this.get(k).get(row)));
				row += 1;
				return tmp;
			}
			
		};
	}
	
	public int nrow() {
		return this.values().stream().findAny().map(i -> i.size()).orElse(0);
	}
	
	public int ncol() {
		return this.keySet().size();
	}
	
	public Class<? extends RPrimitive> getTypeOfColumn(String name) {
		return this.get(name).getType();
	}
	
	public Class<? extends RVector<?>> getVectorTypeOfColumn(String name) {
		if (this.get(name).getType().equals(RCharacter.class)) return RCharacterVector.class;
		if (this.get(name).getType().equals(RInteger.class)) return RIntegerVector.class;
		if (this.get(name).getType().equals(RNumeric.class)) return RNumericVector.class;
		if (this.get(name).getType().equals(RFactor.class)) return RFactorVector.class;
		if (this.get(name).getType().equals(RLogical.class)) return RLogicalVector.class;
		if (this.get(name).getType().equals(RDate.class)) return RDateVector.class;
		throw new IncompatibleTypeException("Unsupported type in column: "+name);
		
	}
	
	private <X extends RPrimitive> void ensureColumnExists(String name) {
		if (!this.containsKey(name)) {
			this.put(name, RVector.padded(nrow(), this.getTypeOfColumn(name)));
		}
	}
	
	public synchronized void addRow(Map<String,Object> row) {
		row.forEach((k,v) -> {
			if(this.containsKey(k)) {
				this.get(k).add(RConverter.convertObjectToPrimitive(v));
			} else {
				RPrimitive prim = RConverter.convertObjectToPrimitive(v); 
				this.put(k, RVector.padded(nrow(), prim));
			}
		});
	}
	
	public synchronized void addCol(String s, RVector<?> col) {
		if (this.ncol() > 0 && col.size() != this.nrow()) throw new RuntimeException("Array is the incorrect length for column:" + s);
		this.put(s, col);
	}
	
	public RDataframe withCol(String s, RVector<?> col) {
		this.addCol(s, col);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void append(RDataframe rows) {
		rows.keySet().forEach(this::ensureColumnExists);
		int appendNrow = rows.values().stream().findAny().map(l -> l.size()).orElse(0);
		this.keySet().forEach(k -> {
			
			if (rows.containsKey(k)) {
				@SuppressWarnings("rawtypes")
				RVector toAdd = rows.get(k);
				if (toAdd.getType().equals(this.getTypeOfColumn(k))) {
					this.get(k).addAll(toAdd);
				} else {
					throw new IncompatibleTypeException("Tried to append dataframe with different type columns: "+k);
				}
			} else {
				@SuppressWarnings("rawtypes")
				RVector toPad = RVector.padded(appendNrow, this.getTypeOfColumn(k));
				this.get(k).addAll(toPad);
			}
			
		});
	}
	
	public Stream<LinkedHashMap<String,Object>> streamJava() {
		return IntStream.range(0, RDataframe.this.size()).mapToObj(row -> {
			LinkedHashMap<String,Object> tmp = new LinkedHashMap<>();
			RDataframe.this.keySet().forEach(k -> tmp.put(k, RConverter.unconvert(RDataframe.this.get(k).get(row))));
			return tmp;
		});
	}
	
	public Iterator<RNamed<RVector<?>>> columnIterator() {
		return new Iterator<RNamed<RVector<?>>>() {
			Iterator<Map.Entry<String, RVector<? extends RPrimitive>>> it = RDataframe.this.entrySet().iterator();
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public RNamed<RVector<?>> next() {
				return RNamed.from(it.next());
			}
			
		};
		
	}
	
	public String[] rKeys() {
		return this.keySet().toArray(new String[] {});
	}
	
	public RVector<?> rColumn(String key) {
		return this.get(key);
	}
	
	public String rConversion() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream sb = new PrintStream(baos);
		sb.println("function(jObj) {");
		// create a function for translating each of the component columns
		List<String> keyList = new ArrayList<String>(this.keySet());
		for(int i = 0; i<this.ncol(); i++) {
			String k = keyList.get(i);
			String colFunction = Stream.of(this.getVectorTypeOfColumn(k).getAnnotation(RDataType.class).JavaToR()).collect(Collectors.joining("\n"));
			String fnName = "convert_"+i;
			sb.append(fnName+" = "+colFunction+"\n");
		}
		//get jobj references for each of the vector columns
		for(int i = 0; i<this.ncol(); i++) {
			String k = keyList.get(i);
			String returnSig = "L"+/*this.getTypeOfColumn(k)*/ RVector.class.getCanonicalName().replace(".", "/")+";";
			String actualSig = "L"+this.getVectorTypeOfColumn(k).getCanonicalName().replace(".", "/")+";";
			sb.println("tmp_"+i+" = rJava::.jcall(obj=jObj,returnSig='"+returnSig+"',method='rColumn','"+k+"')");
			//.jcast(obj, new.class = "java/lang/Object"
			sb.println("tmp_"+i+" = rJava::.jcast(tmp_"+i+",new.class='"+actualSig+"')");
		}
		//construct the tibble
		sb.println("\nreturn(tibble::tibble(");
		for(int i = 0; i<this.ncol(); i++) {
			String k = keyList.get(i);
			sb.println("`"+k+"` = convert_"+i+"(tmp_"+i+")"+(i==this.ncol()-1?"":","));
		}
		sb.println("))");
		sb.println("}");
		return baos.toString();
	}

	@Override
	public Iterator<RNamedList> iterator() {
		return new Iterator<RNamedList>() {
			int i =0;

			@Override
			public boolean hasNext() {
				return i<nrow();
			}

			@Override
			public RNamedList next() {
				RNamedList out = new RNamedList();
				RDataframe.this.keySet().forEach(k -> {
					out.put(k, RDataframe.this.get(k).get(i));
				});
				i=i+1;
				return out;
			}
			
		};
	}
	
	public String rCode() {
		return "tibble::tibble("+
				this.entrySet().stream()
				.map(kv -> kv.getKey()+"="+kv.getValue().rCode())
				.collect(Collectors.joining(", "))+")";
		
	}

	public static RDataframe create() {
		return new RDataframe();
	}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {
		X out = visitor.visit(this);
		this.columnIterator().forEachRemaining(c -> c.accept(visitor));
		return out;
	}
}