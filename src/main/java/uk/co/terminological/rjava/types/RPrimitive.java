package uk.co.terminological.rjava.types;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import uk.co.terminological.rjava.RConverter;

public interface RPrimitive extends RObject {
	
	
	
	public <X extends Object> X get(Class<X> type) throws ClassCastException;
	public <X extends Object> X get();
	
	public default <X extends Object> Optional<X> opt(Class<X> type) {
		try {
			return Optional.ofNullable(get(type));
		} catch (ClassCastException e) {
			return Optional.empty();
		}
	};
	
	public default <X extends Object> Optional<X> opt() {
		return Optional.ofNullable(get());
	};
	
	public boolean isNa();
	
	public static RCharacter of(String o) {return (RCharacter) RConverter.convert(o);}
	public static RNumeric of(Double o) {return RConverter.convert(o);}
	public static RNumeric of(Long o) {return RConverter.convert(o);}
	public static RNumeric of(BigDecimal o) {return RConverter.convert(o);}
	public static RNumeric of(Float o) {return RConverter.convert(o);}
	
	public static RInteger of(Integer o) {return RConverter.convert(o);}
	public static RLogical of(Boolean o) {return RConverter.convert(o);}
	public static RDate of(LocalDate o) {return RConverter.convert(o);}
	
	public static RFactor of(Enum<?> o) {return RConverter.convert(o);}

}
