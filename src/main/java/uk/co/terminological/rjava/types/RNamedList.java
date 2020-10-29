package uk.co.terminological.rjava.types;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import uk.co.terminological.rjava.RConverter;
import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;
import uk.co.terminological.rjava.UnconvertableTypeException;

/** The R named list is a flexible untyped map rather like a JSON document. Any kind of content can be included 
 * (as long as it is wrapped as an {@link RObject}). Using content from lists will require type checking.
 * 
 * @author terminological
 *
 */
@RDataType(
		JavaToR = { 
				"function(jObj) {",
				// get java to serialise code into an evaluatable bit of R code
				"	tmp = eval(parse(text=rJava::.jcall(jObj,'rCode', returnSig='Ljava/lang/String;')))",
				"	return(tmp)",
				"}"
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	if (!is.list(rObj) | is.null(names(rObj))) stop ('expecting a named list')",
				"	jout = rJava::.jnew('~RNAMEDLIST~')",
				"	lapply(names(rObj), function(name) {",
				"		x = rObj[[name]]",
				// If x is a dataframe dispatch it to ~TO_RDATAFRAME~
				"		if (is.null(x)) tmp = ~TO_RNULL~(x)",
				"		else if (is.data.frame(x)) tmp = ~TO_RDATAFRAME~(x)",
				"		else if (is.list(x) & !is.null(names(x))) tmp = ~TO_RNAMEDLIST~(x)",
				"		else if (is.list(x)) tmp = ~TO_RLIST~(x)",
				// TODO: add in matrix
				// Length one
				"		else if (length(x) == 1 & is.character(x)) tmp = ~TO_RCHARACTER~(x)",
				"		else if (length(x) == 1 & is.integer(x)) tmp = ~TO_RINTEGER~(x)",
				"		else if (length(x) == 1 & is.factor(x)) tmp = ~TO_RFACTOR~(x)",
				"		else if (length(x) == 1 & is.logical(x)) tmp = ~TO_RLOGICAL~(x)",
				"		else if (length(x) == 1 & is.numeric(x)) tmp = ~TO_RNUMERIC~(x)",
				"		else if (length(x) == 1 & inherits(x,c('Date','POSIXt'))) tmp = ~TO_RDATE~(x)",
				// Vectors
				"		else if (is.character(x)) tmp = ~TO_RCHARACTERVECTOR~(x)",
				"		else if (is.integer(x)) tmp = ~TO_RINTEGERVECTOR~(x)",
				"		else if (is.factor(x)) tmp = ~TO_RFACTORVECTOR~(x)",
				"		else if (is.logical(x)) tmp = ~TO_RLOGICALVECTOR~(x)",
				"		else if (is.numeric(x)) tmp = ~TO_RNUMERICVECTOR~(x)",
				"		else if (inherits(x,c('Date','POSIXt'))) tmp = ~TO_RDATEVECTOR~(x)",
				// If x is a dataframe dispatch it to ~TO_RDATAFRAME~
				// could issue a warning and put in a null?
				"		else stop ('unrecognised type: ',class(x),' with value ',x)",		
				// Add to list
				"		rJava::.jcall(jout,returnSig='L~ROBJECT~;',method='put',name,rJava::.jcast(tmp, new.class='~ROBJECT~'))",		
				"	})",
				"	return(jout)",
				"}"
		}//,
		//JNIType = "D"
	)
public class RNamedList extends LinkedHashMap<String, RObject> implements RCollection<RNamed<?>>  {

	private static final long serialVersionUID = RObject.datatypeVersion;
	
	public RObject put(String s,RObject o) {
		return super.put(s, o);
	}
	
	public RObject putRaw(String s,Object o) throws UnconvertableTypeException {
		return super.put(s, RConverter.convertObject(o));
	}
		
	@Override
	public Iterator<RNamed<?>> iterator() {
		return new Iterator<RNamed<?>>() {
			Iterator<java.util.Map.Entry<String, RObject>> it = RNamedList.this.entrySet().iterator();
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public RNamed<?> next() {
				return RNamed.from(it.next());
			}
		};
	}

	@Override
	public String rCode() {
		return "list("+this.entrySet().stream().map(kv -> kv.getKey()+"="+kv.getValue().rCode()).collect(Collectors.joining(", "))+")";
	}
	
	public RNamedList andRaw(String s, Object o) throws UnconvertableTypeException {
		this.putRaw(s, o);
		return this;
	}
	
	public static RNamedList withRaw(String s, Object o) throws UnconvertableTypeException {
		RNamedList out = new RNamedList();
		out.andRaw(s,o);
		return out;
	}

	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {
		X out = visitor.visit(this);
		this.iterator().forEachRemaining(c -> c.accept(visitor));
		return out;
	}

	public RNamedList and(String s, RObject o) throws UnconvertableTypeException {
		this.put(s, o);
		return this;
	}
	
	public static RNamedList with(String s, RObject o) throws UnconvertableTypeException {
		RNamedList out = new RNamedList();
		out.and(s,o);
		return out;
	}
}
