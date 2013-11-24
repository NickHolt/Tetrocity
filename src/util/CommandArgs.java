/* Standard package for processing command-line options. */

package util;

import java.util.*;
import java.util.regex.*;

/** A CommandArgs object is a mapping from option keys to values that
 *  interprets the command-line options to a main program.  It
 *  expects such arguments to conform to Sun's standard guidelines, 
 *  according to which, a command (issued to a shell) has the 
 *  following general form:
 *  <pre>
 *     COMMAND [ OPTION ... ] [ -- ] [ OTHER_ARGUMENT ... ]
 *  </pre>
 *  ([]'s indicate optional parts; ... indicates one or more).  Each
 *  OPTION has one of the following forms (x, y, etc. denote 
 *  non-blank characters):
 *  <pre>
 *  Single parameterless short option:
 *     -x 
 *
 *  Several parameterless short options:
 *     -xyz...
 *
 *  Single short option with parameter:
 *     -x OPTARG        
     or
 *     -xOPTARG
 * 
 *  Long parameterless option:
 *     --opt
 *
 *  Long option with parameter:
 *     --opt=foo
 *  </pre>
 *  If a short option takes an additional argument, that argument is 
 *  always required to follow; it cannot be omitted.  When a long argument
 *  takes an argument, it is optional.
 *      <p>
 *  The '--' before the first OTHER_ARGUMENT is optional unless that
 *  OTHER_ARGUMENT starts with '-'.  
 *      <p>
 *  One creates a CommandArgs object by supplying a String describing 
 *  the legal options, and an array  of command-line argument strings (as 
 *  sent to the main function).  
 *     <p>
 *  The CommandArgs object then parses the command-line arguments according
 *  to the specification, and presents the options and other arguments
 *  by as a standard Java Map<String, List<String>>.  That is, it maps
 *  Strings that indicate option keys (like "--opt" or "-x") to a list of
 *  argument values supplied for that option in the command-line arguments
 *  (it is a list because in general, an option can appear several times).
 *  Options that take no arguments get the argument value "".  Trailing
 *  arguments correspond to the option key "--".  The CommandArgs class 
 *  extends the normal Map methods with a few convenience methods to
 *  assist common use.
 *     <p>
 *  Any short option is considered equivalent to a one-character long
 *  option, and vice-versa.
 *     <p>
 *  For example, suppose that we have a program whose usage is
 *  <pre>
 *     foo [ -c ] [ -h ] [ -o FILE ] ARG1
 *  </pre>
 *  where []'s indicate optional arguments, and there may be at most
 *  one -o argument.  It's main program would begin
 *  <pre>
 *    import ucb.util.CommandArgs;
 *    class foo {
 *      public static void main (String[] args0) {
 *         boolean cOptionSpecified;
 *         boolean hOptionSpecified;
 *         String oOptionValue;
 *         List<String> arg1;
 *         CommandArgs args = 
 *            new CommandArgs ("-c -h -o={0,1} --={1}", args0);
 *         if (! args.ok ())
 *            ERROR ();
 *         cOptionSpecified = args.containsKey ("-c");
 *         hOptionSpecified = args.containsKey ("-h");
 *         oOptionValue = args.getLast ("-o"); // null if absent.
 *         arg1 = args.get ("--").get (0);
 *         ...
 *   </pre>
 *     <p>
 *   For a program whose usage is
 *   <pre>
 *      bar [ -c ] [ -k COUNT ] [ --dry-run ] [ --form=NAME ] [ ARG ... ]
 *   </pre> 
 *   where there may be at most one -k option (which must be an integer), 
 *   any number of --form options, and zero or more trailing arguments, we
 *   could write:
 *   <pre>
 *    import ucb.util.CommandArgs;
 *    class foo {
 *      public static void main (String[] args0) {
 *         ...
 *         String options = "-c -k=(\\d+){0,1} --dry-run --form="
 *                          + "--={0,}";
;
 *         CommandArgs args = new CommandArgs (options, args0);
 *         ...
 *         
 *         int count;
 *         if (args.containsKey ("-k"))
 *            count = args.getInt ("-k");
 *         List<String> forms = args.get ("--form");
 *         List<String> otherArgs = args.get ("--");
 *    </pre>
 *    <p>
 *    Here is an example in which there must be exactly one 
 *    occurrence of either the option -i, -q, or -l, an optional occurrence 
 *    of either of the mutually-exclusive options -n or -N, up to
 *    3 occurrences of options -a and -b in any combination, and no 
 *    trailing arguments:
 *
 *    <pre>
 *    import ucb.util.CommandArgs;
 *    class foo {
 *      public static void main (String[] args0) {
 *         ...
 *         String options = "-c{1}:1 -q:1 -l:1 -n{0,1}:2 -N:2 -a={0,3}:3 -b=:3";
 *         CommandArgs args = new CommandArgs (options, args0);
 *         ...
 *    </pre>
 *  
 *    <p>
 *    By default, when an option has a value (indicated by = after the option
 *    key), that value may be any string.  You may also describe argument 
 *    values with general patterns in parentheses, using the 
 *    regular-expression patterns provided by the 
 *    {@link java.util.regex.Pattern} class.  For example, writing
 *
 *    <pre>
 *    CommandArgs args = 
 *      new CommandArgs ("--flavor=(van(illa)?|choc(olate)?)", args0)
 *    </pre>
 *
 *    specifies any number of --flavor parameters, each of which may be
 *    either 'vanilla' ('van' for short) or 'chocolate' ('choc' for short).
 * 
 * <h4>Option descriptors</h4>
 *    The option string that describes possible options consists of a sequence 
 *    of option descriptors, separated by whitespace.  The syntax of an
 *    option string is as follows:
 *
 *    <pre>
 *       &lt;option string&gt; ::= &lt;options&gt; &lt;trailing&gt; | &lt;options&gt; | &lt;trailing&gt;
 *       &lt;options&gt; ::= &lt;option&gt; | &lt;options&gt; option&gt;
 *       &lt;option&gt; ::= &lt;option pattern&gt; | &lt;option pattern&gt;&lt;repeat&gt;
 *       &lt;option pattern&gt; ::= &lt;simple pattern&gt; | (&lt;simple patterns&gt;)
 *       &lt;simple pattern&gt; ::=
 *               &lt;option key&gt;
 *             | &lt;option key&gt;=&lt;pattern&gt;
 *       &lt;option key&gt; ::= 
 *                -&lt;single graphic character other than -&gt;
 *             | --&lt;graphic characters other than = not starting with -&gt;
 *       &lt;simple patterns&gt; ::= 
 *               &lt;simple pattern&gt; | &lt;simple patterns&gt; `|' &lt;simple pattern&gt;
 *       &lt;repeat&gt; ::= 
 *               &lt;count&gt; | &lt;count&gt; &lt;label&gt; | &lt;label&gt;
 *       &lt;count&gt; ::=
 *               {&lt;integer&gt;}
 *             | {&lt;integer&gt;,&lt;integer&gt;} 
 *             | {&lt;integer&gt;,} 
 *       &lt;label&gt; ::= : &lt;positive integer&gt;
 *       &lt;trailing&gt; ::= --=&lt;pattern&gt; | --=&lt;pattern&gt;&lt;repeat&gt;
 *       &lt;pattern&gt; ::= &lt;empty&gt; | (&lt;regular expression&gt;)
 *     </pre>
 *
 *   &lt;regular expression&gt; is as described in the documentation 
 *   for {@link java.util.regex.Pattern}.  The default is `.+' (any
 *   non-empty string).
 *       
 *   <p>
 *   A &lt;repeat&gt; clause limits the number of instances of a given
 *   option or trailing argument.  When unspecified, it is "zero or more" 
 *   ({0,}).  A trailing &lt;label&gt; indicates a group of options that
 *   are mutually exclusive.  The count that appears on the first option
 *   specification of the group applies to all (subsequent options should
 *   specify just the label part, not the { } part).  At most one of the keys
 *   in any group may appear.  The count applies to whichever one does.  
 *   <p>
 *   No &lt;option&gt; may contain whitespace.  Also, be careful of the
 *   usual escaping problems with representing regular expressions as 
 *   java Strings.  The regular expression \d, for example, is written as
 *   the String literal "\\d".
 */

public class CommandArgs extends AbstractMap<String,List<String>>
  implements Map<String,List<String>> {

  /** A set of argument values extracted from RAWARGS according to 
   *  the description given in OPTIONSTRING.  OPTIONSTRING is defined
   *  in the class documentation for this class (see above).  RAWARGS
   *  is typically the array of arguments passed to the main procedure.
   *  @throws IllegalArgumentException if OPTIONSTRING does not conform
   *  to the syntax above.
   *  @throws PatternSyntaxException if a regular expression in OPTIONSTRING
   *  has invalid format.
 */
  public CommandArgs (String optionString, String[] rawArgs) {
    this.optionString = optionString;
    this.arguments = rawArgs;
    ok = true;
    List<OptionSpec> specs = OptionSpec.parse (optionString);
    createRawOptionLists (rawArgs, specs);
    gatherOptions (specs);
  }

  /** The option string with which THIS was created. */
  public String getOptionString () {
    return optionString;
  }

  /** The argument array (not a copy) with which THIS was created. */
  public String[] getArguments () {
    return arguments;
  }

  /** The number of occurrences of option key KEY. */
  public int number (String key) {
    List<String> opts = get (key);
    return opts == null ? 0 : opts.size ();
  }

  /** The argument value of the last occurrence of option KEY, or null
   *  if there is no occurrence, or it has the wrong format. */
  public String getLast (String key) {
    List<String> val = get (key);
    if (val == null || val.size () == 0)
      return null;
    else
      return val.get (val.size () - 1);
  }

  /** The value of the last occurrence of option KEY, as a decimal integer.
   *  Exception if there is no such option, or it does not have the format
   *  of an integer. 
   *  @throws NumberFormatException if the value of option KEY 
   *  is not a decimal numeral.
*/
  public int getInt (String key) {
    return getInt (key, 10);
  }

  /** The value of the last occurrence of option KEY, as an integer of
   *  given RADIX. Exception if there is no such option, or it does not 
   *  have the format of an integer.  Hexadecimal integers may have 
   *  a leading '0x', which is ignored.
   *  @throws NumberFormatException if the value of option KEY is not 
   *  a valid numeral of radix RADIX,  or RADIX is not a valid RADIX. */
  public int getInt (String key, int radix) {
    String val = getLast (key);
    if (radix == 16 && val != null && val.startsWith ("0x"))
      return Integer.parseInt (val.substring (2), 16);
    else if (radix == 16 && val != null && val.startsWith ("-0x"))
      return Integer.parseInt ("-" + val.substring (3), 16);
    else
      return Integer.parseInt (val, radix);
  }

  /** The value of the last occurrence of option KEY, as a decimal integer.
   *  Exception if there is no such option, or it does not have the format
   *  of an integer. 
   *  @throws NumberFormatException if the value of option KEY 
   *  is not a decimal numeral. */
  public long getLong (String key) {
    return getLong (key, 10);
  }

  /** The value of the last occurrence of option KEY, as an integer of
   *  given RADIX. Exception if there is no such option, or it does not 
   *  have the format of an integer. Hexadecimal integers may have 
   *  a leading '0x', which is ignored.
   *  @throws NumberFormatException if the value of option KEY is not 
   *  a valid numeral of radix RADIX,  or RADIX is not a valid RADIX. */
  public long getLong (String key, int radix) {
    String val = getLast (key);
    if (radix == 16 && val != null && val.startsWith ("0x"))
      return Long.parseLong (val.substring (2), 16);
    else if (radix == 16 && val != null && val.startsWith ("-0x"))
      return Long.parseLong ("-" + val.substring (3), 16);
    else
      return Long.parseLong (val, radix);
  }

  /** The value of the last occurrence of option KEY, as a floating-point
   *  value.  Exception if there is no such option, or it does not have 
   *  the proper format. 
   *  @throws NumberFormatException if the value of option KEY 
   *  is not a proper floating-point numeral. */
  public double getDouble (String key) {
    return Double.parseDouble (getLast (key));
  }

  /** True iff all arguments were correct. */
  public boolean ok () {
    return ok;
  }

  /** A list of all keys that appeared in the arguments, in order of
   *  appearance.  Trailing arguments are marked with the key "--". 
   *  Invalid keys are not represented. */
  public List<String> optionKeys () {
    return keyList;
  }

  /** A list of all option values that appeared in the arguments, in order
   *  of appearance.  Trailing arguments appear at the end.  Options
   *  that don't take values or are given a value of "", as are some
   *  options that are supplied incorrectly.  The order and number of the 
   *  elements corresponds to the result of optionKeys (). */
  public List<String> optionValues () {
    return valueList;
  }

  /* Implementations of abstract methods of AbstractMap. */

  /** The set of all pairs (KEY, VALUES) represented by THIS.  Each
   *  key is an option key (such as "--output" or "-v"), and each
   *  value is a list of values that were supplied for that key (as
   *  by "--output=results.txt").  For options that don't accept
   *  an argument, the associated value is the empty string. */
  public Set<Map.Entry<String,List<String>>> entrySet () {
    return entrySet;
  }
  
  /* Private section */

  /* Saved constructor arguments */
  private final String optionString;
  private final String[] arguments;

  /** Flag indicating whether any error has been found. */
  private boolean ok;

  /** The underlying map representation. */
  private ListSet entrySet = new ListSet ();
  /** Value of optionKeys (). */
  private List<String> keyList = new ArrayList<String> ();
  /** Value of optionValues (). */
  private List<String> valueList = new ArrayList<String> ();

  /** A map entry as used in entrySet: a (String, List<String>) pair. */
  private static class Entry implements Map.Entry<String,List<String>> {
    Entry (String key, List<String> val) {
      this.key = key;
      this.value = val;
    }

    public String getKey() {
      return key;
    }

    public List<String> getValue() {
      return value;
    }

    public List<String> setValue(List<String> value) {
      throw new UnsupportedOperationException ();
    }

    public boolean equals(Object o) {
      return (o instanceof Entry) 
	&& ((Entry) o).key.equals (key)
	&& ((Entry) o).value.equals (value);
    }

    public int hashCode() {
      return getKey ().hashCode ();
    }

    private final String key;
    private final List<String> value;
  }
    
  /** A set of String=>List<String> mappings.  Unmodifiable outside the
   *  CommandArgs class. */
  private static class ListSet 
    extends AbstractSet<Map.Entry<String,List<String>>> {

    public Iterator<Map.Entry<String,List<String>>> iterator () {
      return contents.iterator ();
    }

    public int size () {
      return contents.size ();
    }

    /** As for add (X), but unavailable outside CommandArgs class. */
    void _add (Map.Entry<String,List<String>> x) {
      contents.add (x);
    }

    LinkedList<Map.Entry<String,List<String>>> contents =
      new LinkedList<Map.Entry<String,List<String>>> ();
  }

  final private static String 
    SHORT_OPTION = "-[a-zA-Z0-9_#@+%]",
    LONG_OPTION = "--[-_a-zA-Z0-9]*",
    VALUE = "(?:=(\\(.*\\)|))?",
    REPEAT = "(?:\\{(\\d+)(,)?(\\d*)\\}(?::([1-9]\\d*))?|:([1-9]\\d*))?",
    OPTION = "(" + SHORT_OPTION + "|" + LONG_OPTION + ")" + VALUE + REPEAT;
  final private static Pattern
    /* Group 
             1 = <option key>
	     2 = <value>
	     3,4,5 = <repeat spec>  6 = <label definition>  7 = <label use>
    */
    OPTION_PATTERN = Pattern.compile (OPTION);

  /** Encapsulates a single option specification (e.g., "-o=", "--foo{0,1}",
   *  etc.), plus a list of values supplied for that option.  */
  private static class OptionSpec {
    /** Create an OptionSpec that parses the option described by OPT. */
    static List<OptionSpec> parse (String opt) {
      List<OptionSpec> result = new ArrayList<OptionSpec> ();
      Map<String,OptionSpec> labels = new HashMap<String,OptionSpec> ();
      String[] opts = opt.trim ().split ("\\s+");
      for (int i = 0; i < opts.length; i += 1) {
	if (opts[i].equals (""))
	  continue;
	Matcher m = OPTION_PATTERN.matcher (opts[i]);
	if (m.matches ()) {
	  OptionSpec spec = new OptionSpec ();
	  spec.key = m.group (1);
	  spec.valuePattern = ("").equals (m.group (2)) ? ".+" : m.group (2);
	  spec.primary = spec;
	  if (m.group (7) != null) {
	    spec.primary = labels.get (m.group (7));
	    if (spec.primary == null)
	      throw new IllegalArgumentException ("undefined label: " 
						  + m.group (7));
	  } else if (m.group (6) != null) {
	    if (labels.containsKey (m.group (6)))
	      throw new IllegalArgumentException ("multiply defined label: "
						  + m.group (6));
	    else
	      labels.put (m.group (6), spec);
	  } 
	  if (m.group (3) != null) {
	    spec.min = Integer.parseInt (m.group (3));
	    if (m.group (4) == null) 
	      spec.max = spec.min;
	    else if (m.group (5).equals (""))
	      spec.max = Integer.MAX_VALUE;
	    else 
	      spec.max = Integer.parseInt (m.group (5));
	  } else {
	    spec.min = 0;
	    spec.max = Integer.MAX_VALUE;
	  }
	  spec.count = 0; spec.error = false;
	  result.add (spec);
	  if (spec.key.equals ("--") && i != opts.length - 1)
	    throw new IllegalArgumentException ("junk at end of option string");
	} else 
	  throw new IllegalArgumentException ("bad option specifier: " + 
					      opts[i]);
      }
      return result;
    }

    /** True iff erroneous uses have been made of this option. */
    boolean error () { return error || primary.count < primary.min; }

    /** True iff this OptionSpec handles options whose key is KEY. */
    boolean matches (String key) { 
      return key.equals (this.key);
    }

    /** True iff at least one instance of the option described by THIS 
     *  has been supplied. */
    boolean present () { 
      return value != null;
    }

    /** True iff the option described by THIS takes an argument. */
    boolean hasArgument () { 
      return valuePattern != null;
    }

    /** Record an instance of the option corresponding to THIS whose value is
     *  VAL (which should be "" for an option that does not take a value). */
    void add (String val) {
      if (val == null) 
	return;
      if (value == null)
	value = new ArrayList<String> (1);
      primary.count += 1;
      if (primary.count > primary.max)
	error = true;
      else if (valuePattern == null && !val.equals ("")) {
	error = true;
	value.add (null);
      } else if (valuePattern == null)
	value.add ("");
      else if (Pattern.matches (valuePattern, val))
	value.add (val);
      else {
	error = true;
	value.add (null);
      }
    }

    /** The option key for THIS. */
    String key () { return key; }

    /** The list of option values accumulated for THIS. */
    List<String> value () { return value; }

    private String key;
    private String valuePattern;
    private List<String> value;
    private OptionSpec primary;
    private int count, min, max;
    private boolean error;
  }


  @SuppressWarnings("unused")
final private static Pattern 
    ARGUMENT = Pattern.compile ("(--)|(--\\S+?)(?:=(.*))?|(-[^-].*)"),
    WS = Pattern.compile ("\\s+");

    
  /** Fill keyList and valueList from ARGS, where OPTIONSPECS is option 
   *  string supplied to the constructor, broken into individual 
   *  specifications. */
  private void createRawOptionLists (String[] args, 
				     List<OptionSpec> optionSpecs) {
    keyList.clear ();
    valueList.clear ();

    int i;
    i = 0;
    while (i < args.length) {
      Matcher m = ARGUMENT.matcher (args[i]);
      if (m.matches ()) {
	if (m.group (1) != null) {
	  i += 1;
	  break;
	} else if (m.group (2) != null) {
	  keyList.add (m.group (2));
	  if (m.group (3) == null)
	    valueList.add ("");
	  else
	    valueList.add (m.group (3));
	} else if (shortOptionWithArg (m.group (4), optionSpecs)) {
	  keyList.add (m.group (4).substring (0, 2));
	  if (m.group (4).length () > 2)
	    valueList.add (m.group (4).substring (2));
	  else {
	    i += 1;
	    if (i == args.length) {
	      ok = false;
	      valueList.add ("");
	    } else
	      valueList.add (args[i]);
	  }
	} else {
	  for (int k = 1; k < m.group (4).length (); k += 1) {
	    String key = "-" + m.group (4).charAt (k);
	    keyList.add (key);
	    if (shortOptionWithArg (key, optionSpecs))
	      ok = false;
	    valueList.add ("");
	  }
	}
	i += 1;
      }	else
	break;
    }
    while (i < args.length) {
      keyList.add ("--");
      valueList.add (args[i]);
      i += 1;
    }
  }

  /** Initialize the option map represented by THIS from the data accumulated 
   *  in optionList and valueList, given that SPECS is the list of parsed
   *  OptionSpecs. */
  private void gatherOptions (List<OptionSpec> specs) {
    FindSpec:
    for (int i = 0; i < keyList.size (); i += 1) {
      for (OptionSpec spec : specs) {
	if (spec.matches (keyList.get (i))) {
	  spec.add (valueList.get (i));
	  continue FindSpec;
	}
      }
      ok = false;
    }

    entrySet.clear ();
    for (OptionSpec spec : specs) {
      if (spec.present ())
	entrySet._add (new Entry (spec.key (), spec.value ()));
      if (spec.error ())
	ok = false;
    }
  }

  /** True iff, according to SPECS, OPT is short option that takes an
   *  argument. */
  private static boolean shortOptionWithArg (String opt, 
					     List<OptionSpec> specs) {
    if (opt.startsWith ("--") || ! opt.startsWith ("-") 
	|| opt.length () < 2)
      return false;
    opt = opt.substring (0, 2);
    for (int i = 0; i < specs.size (); i += 1) {
      if (specs.get(i).matches (opt))
	return specs.get(i).hasArgument ();
    }
    return false;
  }

  

}

