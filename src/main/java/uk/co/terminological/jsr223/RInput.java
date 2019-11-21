package uk.co.terminological.jsr223;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * A set of helper functions to handle data coming from R.
 * @author terminological
 *
 */
public class RInput {

	/**
	 * Convert a colMajor dataframe to a rowMajor format
	 * @param colMajor
	 * @return
	 */
	public static ArrayList<LinkedHashMap<String, Object>> colMajorToRowMajor(LinkedHashMap<String, Object[]> colMajor) {
		ArrayList<LinkedHashMap<String, Object>> out = new ArrayList<LinkedHashMap<String, Object>>();
		String randomKey = colMajor.keySet().iterator().next();
		for(int i=0; i < colMajor.get(randomKey).length; i++) {
			LinkedHashMap<String, Object> tmp = new LinkedHashMap<String, Object>();
			for (String key: colMajor.keySet()) {
				tmp.put(key, colMajor.get(key)[i]);
			}
			out.add(tmp);
		}
		return out;
	}
	
	
}
