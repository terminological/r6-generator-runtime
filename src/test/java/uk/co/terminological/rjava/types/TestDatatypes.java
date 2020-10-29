package uk.co.terminological.rjava.types;

import static org.junit.jupiter.api.Assertions.*;

// CS01
import uk.co.terminological.rjava.types.*;
import static uk.co.terminological.rjava.RConverter.*;
// CE01

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class TestDatatypes {

	@BeforeEach
	void setUp() throws Exception {
	}

	public static enum TestEnum {ONE,TWO,THREE}
	
	@Test
	final void testNaBehaviour() {
		
		{
			RInteger i1 = RInteger.from(RInteger.NA_VALUE);
			RInteger i2 = new RInteger();
			RInteger i3 = RInteger.NA;
			assertTrue(i1.equals(i2));
			assertTrue(i1.equals(i3));
			assertTrue(!i1.equals(RPrimitive.of(123)));
		}
		
		{
			RLogical i1 = RLogical.NA;
			RLogical i2 = new RLogical();
			RLogical i3 = RLogical.from(RLogical.NA_VALUE);
			assertTrue(i1.equals(i2));
			assertTrue(i1.equals(i3));
			assertTrue(!i1.equals(RPrimitive.of(false)));
		}
		
		{
			RNumeric i1 = RNumeric.NA;
			RNumeric i2 = new RNumeric();
			RNumeric i3 = RNumeric.from(RNumeric.NA_VALUE);
			assertTrue(i1.equals(i2));
			assertTrue(i1.equals(i3));
			assertTrue(!i1.equals(RPrimitive.of(2.0)));
			RNumeric nan = RPrimitive.of(Double.NaN);
			assertTrue(i1.isNa());
			assertTrue(!i1.equals(nan));
			
		}
		
		{
			RDate i1 = RDate.NA;
			RDate i2 = new RDate();
			RDate i3 = RDate.from(RDate.NA_VALUE);
			assertTrue(i1.equals(i2));
			assertTrue(i1.equals(i3));
			assertTrue(!i1.equals(RPrimitive.of(LocalDate.now())));
		}
		
		
		
		{
			RFactor i1 = RFactor.NA;
			RFactor i2 = new RFactor();
			RFactor i3 = RFactor.from(null);
			assertTrue(i1.equals(i2));
			assertTrue(i1.equals(i3));
			assertTrue(!i1.equals(RFactor.from(TestEnum.THREE)));
		}
		
		{
			RCharacter i1 = RCharacter.NA;
			RCharacter i2 = new RCharacter();
			RCharacter i3 = RCharacter.from(null);
			assertTrue(i1.equals(i2));
			assertTrue(i1.equals(i3));
			assertTrue(!i1.equals(RCharacter.from("Something")));
		}
		
	}
	
	@Test
	final void testIntegerVectors() {
		
		// CS02
		
		// Java boxed arrays can be directly converted to vector from RVector
		// Integer.MIN_VALUE inputs are converted to nulls silently
		Integer[] tmp = {1,3,5,7,null,Integer.MIN_VALUE, RInteger.NA_VALUE, 0};
		RIntegerVector col0 = RVector.with(tmp);
		RIntegerVector col1 = convert(tmp);
		
		assertTrue(col1.get(4).isNa());
		assertTrue(col1.get(5).isNa());
		assertTrue(col1.get(6).isNa());
		
		assertTrue(!col1.get(4).opt().isPresent());
		
		// Alternatively RConverter has a set of collectors for Streams of convertible types.
		RIntegerVector col2 = Stream.of(tmp).collect(integerCollector());
		assertTrue(col1.equals(col0));
		assertTrue(col1.equals(col2));
		
		// Conversion using a CollectionConverter to convert arbitrary collection types
		RIntegerVector col3 = using(integerCollector()).convert(tmp);
		RIntegerVector col4 = using(integerCollector()).convert(Arrays.asList(tmp));
		RIntegerVector col5 = using(integerCollector()).convert(Arrays.asList(tmp).iterator());
		RIntegerVector col6 = using(integerCollector()).convert(Stream.of(tmp));
		// These should all be the same		
		assertTrue(col1.equals(col3));
		assertTrue(col1.equals(col4));
		assertTrue(col1.equals(col5));
		assertTrue(col1.equals(col6));
				
		// But Converting collectors can also handle singleton conversions
		RIntegerVector i1 = using(integerCollector()).convert(tmp[0]);
		assertTrue(i1.get(0) instanceof RInteger);
		assertTrue(col1.get(0).equals(i1.get(0)));
						
		// Primitive arrays are slightly less flexible but RConverter can process them
		// nulls are not allowed but the "equivalent" to NA is Integer.MIN_VALUE = RInteger.NA_INT
		// If they are converted to a boxed form by an IntStream they can be collected as above
		int[] tmp2 = {1,3,5,7,RInteger.NA_VALUE, Integer.MIN_VALUE, RInteger.NA_VALUE, 0};
		RIntegerVector col7 = convert(tmp2);
		RIntegerVector col8 = IntStream.of(tmp2).boxed().collect(integerCollector());
		assertTrue(col1.equals(col7));
		assertTrue(col1.equals(col8));
		
		// CE02
	}

	@Test
	final void testOtherCollectors() {
		
		// CS03
		
		//Dates - difficulty is getting a stream of maybe null dates
		//Collect streams of other data types:
		RDateVector rdv = Stream
				.of("2020-03-01","2020-03-02","2020-03-03",null,"2020-03-05")
				.collect(dateFromStringCollector());
		RLogicalVector rlv1 = rdv.stream().map(d -> d.isNa()).collect(booleanCollector());
		//or use RConverter.convert on arrays
		boolean[] bools = {false,false,false,true,false};
		boolean[] bools2 = {false,false,false,true,true};
		assertTrue(rlv1.equals(convert(bools)));
		assertTrue(!rlv1.equals(convert(bools2)));
		
		// CE03
	}
	
	@Test
	final void testUnwrapRObject() {
		
		// CS04
		RDate date = RDate.from("2020-02-02");
		assertTrue(date.opt(LocalDate.class).isPresent());
		assertTrue(date.get().toString().equals("2020-02-02"));
		
		// RPrimitive.as(Class<?>) returns an optional. NA or incorrect types both come out as Optional.empty()
		RNumeric num = RNumeric.from(123.456);
		assertTrue(num.opt(BigDecimal.class).isPresent());
		assertTrue(num.opt(Float.class).isPresent());
		assertTrue(num.opt(Long.class).isPresent());
		
		// Will return an Optional.empty() as not a String
		assertTrue(!num.opt(String.class).isPresent());
		
		// RPrimitive.get() will return the main underlying implementation type (in this case a Double) or a null for a NA
		assertTrue(num.get().doubleValue() == 123.456);
		assertTrue(num.get(Float.class).equals(123.456F));
		
		// NA values are returned as Optional.empty() by RPrimitive.opt()
		assertTrue(!RInteger.NA.opt().isPresent());
		assertTrue(RInteger.from(234).opt().isPresent());
		
		// CE04
		
	}
}
