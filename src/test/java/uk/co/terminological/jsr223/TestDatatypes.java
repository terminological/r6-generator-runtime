package uk.co.terminological.jsr223;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.co.terminological.rjava.types.RIntegerVector;

class TestDatatypes {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	final void test() {
		int[] tmp = {1,3,5,7,8, Integer.MIN_VALUE, 0};
		RIntegerVector col = new RIntegerVector(tmp);
		col.getClass();
	}

}
