import java.io.*;
import kotlin.system.exitProcess;
import javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JFileChooser;
val NIKKUD_BLOCK=0x5b0;
val NIKKUD_BLOCK_END=0x5bc;
enum class TAAM(val char:Char,val nikkud_name:String) {
	SHVA(0x5b0.toChar(),"שווא"),
	CHATAF_SEGOL(0x5b1.toChar(),"חטףסגל"),
	CHATAF_PATACH(0x5b2.toChar(),"חטףפתח"),
	CHATAF_KAMATZ(0x5b3.toChar(),"חטףקמץ"),
	CHIRIK(0x5b4.toChar(),"חיריק"),
	TSEREI(0x5b5.toChar(),"צרי"),
	SEGOL(0x5b6.toChar(), "סגל"),
	PATACH(0x5b7.toChar(),"פתח"),
	KAMATZ(0x5b8.toChar(),"קמץ"),   
	HOLAM(0x5b9.toChar(),"חולם"),
	HOLAM_MALEH(0x5ba.toChar(),"חולםמלא"),
	KUBUTZ(0x5bb.toChar(),"קבץ");
	companion object {
		fun byUnicodeValue(char: Char) = values().firstOrNull { it.char == char }
		fun byNikkudName(nikkud_name : String) = values().firstOrNull {it.nikkud_name==nikkud_name}
	}
}
fun Iterable<String>.toCSV() : String {
	return reduce{accumulator, s->accumulator+","+s}
}
fun exitWithError(msg : String) : Nothing {
   showMessageDialog(null,msg);
   exitProcess(1);
}
fun main() {
	val file_chooser = JFileChooser();
	file_chooser.showOpenDialog(null);
	val input_file : File? = file_chooser.getSelectedFile();
	if (input_file == null)
		exitWithError("לא התקבל קובץ");
	val filename : String = input_file.getAbsolutePath();
	if (!input_file.exists())
		exitWithError("אין קובץ בשם "+filename);
	val wanted_combinations_file : File = File("../צירופים_רצויים.txt");
	if (!wanted_combinations_file.exists())
		exitWithError("אין קובץ צירופים_רצויים.txt");
    /* create a map of words from the combinations file.
     * with the key being a Set of Teamim and the value being an empty mutable set.  */
    val map_of_words : Map<Set<TAAM>, MutableSet<String>> = buildMap {
	    wanted_combinations_file.forEachLine {line->
		    put(line.split(",").map {nikkud_name->
				val nikkud_name_sans_whitespace=nikkud_name.filterNot {it.isWhitespace()};
				if (nikkud_name_sans_whitespace.isEmpty()){
					exitWithError("רצף פסיקים בקובץ צירופים_רצויים או פסיק בסוף שורה");
				} else {
			    	TAAM.byNikkudName(nikkud_name_sans_whitespace)?:
			    	exitWithError(" אין ניקוד בשם" + nikkud_name);
				}
		    }.toSet(), mutableSetOf<String>());
	    }
    }
    if (map_of_words.isEmpty())
	   exitWithError("קובץ צירופים ריק");
	/* filter the nikkud from each word of the input file and put it
	 * into the corresponding MutableSet */
    input_file.forEachLine {line ->
	    val words=line.split("[\\p{Punct}\\p{Space}]".toRegex());
	    words.forEach {
			word : String ->
		    val nikkud : Set<TAAM> = word.filter{c ->
			    c.code >= NIKKUD_BLOCK
			    && c.code < NIKKUD_BLOCK_END
		    }.map {c->TAAM.byUnicodeValue(c)!!}.toSet();
		    map_of_words.get(nikkud)?.add(word);
	    }
    }

    val output_file = File(filename+".csv")
	/* Write the names of the nikkud combinations into the CSV */
    output_file.writeText("\ufeff"+map_of_words.keys.
	    map {nikkud_set->"\""+
			nikkud_set.
			map {it.nikkud_name}.
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
