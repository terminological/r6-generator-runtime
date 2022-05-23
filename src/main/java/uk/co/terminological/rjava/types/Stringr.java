package uk.co.terminological.rjava.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Stringr {

	List<String> input;
	
	private Stringr(List<String> input) {
		this.input = input;
	}
	
	// FLUENT WRAPPERS
	
	public static Stringr of(String s) {
		Stringr out = new Stringr(Collections.singletonList(s));
		return out;
	}

	public static Stringr of(String... s) {
		Stringr out = new Stringr(Arrays.asList(s));
		return out;
	}
	
	public static Stringr of(Collection<String> s) {
		Stringr out = new Stringr(new ArrayList<String>(s));
		return out;
	}
	
	public static Stringr of(Stream<String> s) {
		Stringr out = new Stringr(s.collect(Collectors.toList()));
		return out;
	}
	
	public static Stringr of(RCharacter s) {
		Stringr out = new Stringr(Collections.singletonList(s.get()));
		return out;
	}
	
	public static Stringr of(RCharacterVector s) {
		Stringr out = new Stringr(s.get().collect(Collectors.toList()));
		return out;
	}
	
	// FUNCTIONS
	
	// str_count() Count the number of matches in a string
	// str_detect()	Detect the presence or absence of a pattern in a string
	// str_extract() str_extract_all() Extract matching patterns from a string
	// str_locate() str_locate_all() Locate the position of patterns in a string
	// str_match() str_match_all()
	// Extract matched groups from a string
	// str_remove() str_remove_all()
	// Remove matched patterns in a string
	// str_replace() str_replace_all()
	// Replace matched patterns in a string
	// str_starts() str_ends()
	// Detect the presence or absence of a pattern at the beginning or end of a string.
	// str_split() str_split_fixed() str_split_n()
	// Split up a string into pieces
	// str_subset() str_which()
	// Keep strings matching a pattern, or find positions
	// fixed() coll() regex() boundary()
	// Control matching behaviour with modifier functions
	
	// Combining strings
	// str_c()

	//	Join multiple strings into a single string
	//
	//	str_flatten()
	//
	//	Flatten a string
	//
	//	str_glue() str_glue_data()
	//
	//	Format and interpolate a string with glue
	//
	//	Whitespace
	//	str_pad()
	//
	//	Pad a string
	//
	//	str_trim() str_squish()
	//
	//	Trim whitespace from a string
	//
	//	str_wrap()
	//
	//	Wrap strings into nicely formatted paragraphs
	//
	//	Locale aware
	//	str_order() str_sort()
	//
	//	Order or sort a character vector
	//
	//	str_equal()
	//
	//	Determine if two strings are equivalent
	//
	//	str_to_upper() str_to_lower() str_to_title() str_to_sentence()
	//
	//	Convert case of a string
	//
	//	Other helpers
	//	invert_match()
	//
	//	Switch location of matches to location of non-matches.
	//
	//	str_conv()
	//
	//	Specify the encoding of a string
	//
	//	str_dup()
	//
	//	Duplicate and concatenate strings within a character vector
	//
	//	str_length()
	//
	//	The length of a string
	//
	//	str_like()
	//
	//	Detect the presence of a pattern in the string using SQL LIKE convention.
	//
	//	str_replace_na()
	//
	//	Turn NA into "NA"
	//
	//	str_trunc()
	//
	//	Truncate a character string
	//
	//	str_unique()
	//
	//	Keep unique strings only
	//
	//	str_view() str_view_all()
	//
	//	View HTML rendering of regular expression match
	//
	//	str_sub() `str_sub<-`()
	//
	//	Extract and replace substrings from a character vector
	//
	//	word()
	//
	//	Extract words from a sentence


	
	// FUNCTIONAL INTERFACE
	
//	public static String captureGroup(String s, String regExp) {
//		return captureGroup(s, regExp, 0);
//	}
//	
	public static String capture(String s, String regExp, int group) {
		// Compile and use regular expression
		Pattern pattern = Pattern.compile(regExp);
		Matcher matcher = pattern.matcher(s);
		if (!matcher.find()) return null;
		
		if (matcher.groupCount() > group+1) {
		    return matcher.group(group+1);
		} else {
			return null;
		}
	}
	
	public static String extract(String s, String regExp) {
		Pattern pattern = Pattern.compile(regExp);
		Matcher matcher = pattern.matcher(s);
		if (!matcher.find()) return null;
		return s.substring(matcher.start(), matcher.end());
	}
	
//	public static Comparator<String> ignoreCaseSorter() {
//		 return new Comparator<String> () {
//			@Override
//			public int compare(String arg0, String arg1) {
//				return arg0.compareToIgnoreCase(arg1);
//			}
//		};
//	}
	
}
