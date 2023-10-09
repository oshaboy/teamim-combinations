import java.io.*;
import kotlin.system.exitProcess;
val NIKKUD_BLOCK=0x5b0;
val NIKKUD_BLOCK_END=0x5bc;
enum class TAAM(val char:Char,val nikkud_name:String) {
	SHVA(0x5b0.toChar(),"שווא"),
	CHATAF_SEGOL(0x5b1.toChar(),"חטף סגל"),
	CHATAF_PATACH(0x5b2.toChar(),"חטף פתח"),
	CHATAF_KAMATZ(0x5b3.toChar(),"חטף קמץ"),
	CHIRIK(0x5b4.toChar(),"חיריק"),
	TSEREI(0x5b5.toChar(),"צרי"),
	SEGOL(0x5b6.toChar(), "סגל"),
	PATACH(0x5b7.toChar(),"פתח"),
	KAMATZ(0x5b8.toChar(),"קמץ"),   
	HOLAM(0x5b9.toChar(),"חולם"),
	HOLAM_MALEH(0x5ba.toChar(),"חולם מלא"),
	KUBUTZ(0x5bb.toChar(),"קבץ");
	companion object {
		fun byUnicodeValue(char: Char) = values().firstOrNull { it.char == char }
		fun byNikkudName(nikkud_name : String) = values().firstOrNull {it.nikkud_name==nikkud_name}
	}
}
fun Iterable<String>.toCSV() : String{
	return reduce{accumulator, s->accumulator+","+s}
}
fun error(msg : String) : Nothing {
   println(msg);
   exitProcess(1);
}
fun main(args: Array<String>){
	if (args.size < 1)
		error("לא התקבל קובץ");
    
    val input_file : File = File(args[0]);
	if (!input_file.exists())
		error("אין קובץ בשם "+args[0]);
    val wanted_combinations_file : File = File("../צירופים_רצויים.txt");

	if (!wanted_combinations_file.exists())
		error("אין קובץ צירופים_רצויים.txt");
    /* create a map of words from the combinations file.
     * with the key being a Set of Teamim and the value being an empty mutable set.  */
    val map_of_words : Map<Set<TAAM>, MutableSet<String>> = buildMap {
	    wanted_combinations_file.forEachLine{line->
		    put(line.split(",").map{nikkud_name->
			    TAAM.byNikkudName(nikkud_name)?:
			    error(" אין ניקוד בשם" + nikkud_name);
		    }.toSet(), mutableSetOf<String>());
	    }
    }
    if (map_of_words.isEmpty())
	   error("קובץ צירופים ריק");
	/* filter the nikkud from each word of the input file and put it
	 * into the corresponding MutableSet */
    input_file.forEachLine{line ->
	    val words=line.split("[\\p{Punct}\\p{Space}]".toRegex());
	    words.forEach{word : String ->
		    val nikkud : Set<TAAM> = word.filter{c ->
			    c.code >= NIKKUD_BLOCK
			    && c.code < NIKKUD_BLOCK_END
		    }.map{c->TAAM.byUnicodeValue(c)!!}.toSet();
		    map_of_words.get(nikkud)?.add(word);
	    }
    }

    val output_file = File(args[0]+".csv")
	/* Write the names of the nikkud combinations into the CSV */
    output_file.writeText(map_of_words.keys.
	    map{nikkud_set->"\""+
			nikkud_set.
			map{it.nikkud_name}.
			toCSV()+"\""}.
	    toCSV()+"\n"
    );

	/* Write the rest of the data
	 * one element from each set must be written
	 * on every row */
    val maximum_size = map_of_words.values.map{it.size}.maxOrNull()?:0;
    for (i in 0..maximum_size-1){
	    output_file.appendText(
		    map_of_words.keys.
		    map{nikkud_set->map_of_words[nikkud_set]!!.
				elementAtOrElse(i,{""})
			}.
		    toCSV()+"\n"
	    );
    }
}