import java.io.*;
import kotlin.system.exitProcess;
import javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JFileChooser;

enum class TAAM(val nikkud_regex:Regex,val nikkud_name_match:String, val nikkud_name_display : String) {
	SHVA(Regex.fromLiteral("\u05b0"),"שווא","שווא"),
	CHATAF_SEGOL(Regex.fromLiteral("\u05b1"),"חטףסגל","חטף סגל"),
	CHATAF_PATACH(Regex.fromLiteral("\u05b2"),"חטףפתח","חטף פתח"),
	CHATAF_KAMATZ(Regex.fromLiteral("\u05b3"),"חטףקמץ","חטף קמץ"),
	CHIRIK(Regex.fromLiteral("\u05b4"),"חיריק","חיריק"),
	TSEREI(Regex.fromLiteral("\u05b5"),"צרי","צרי"),
	SEGOL(Regex.fromLiteral("\u05b6"), "סגל", "סגל"),
	PATACH(Regex.fromLiteral("\u05b7"),"פתח","פתח"),
	KAMATZ(Regex.fromLiteral("\u05b8"),"קמץ","קמץ"),   
	HOLAM(Regex.fromLiteral("\u05b9|\u05ba"),"חולם","חולם"),
	KUBUTZ(Regex.fromLiteral("\u05bb"),"קבץ","קבץ"),
	SHURUK(Regex.fromLiteral("\u05d5\u05bc"), "שורוק", "שורוק");
	companion object {
		//fun byUnicodeValue(nikkud_regex: String) = values().firstOrNull { it.nikkud_regex == nikkud_regex.toRegex() }
		fun byNikkudName(nikkud_name : String) = values().firstOrNull {it.nikkud_name_match==nikkud_name}
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
			if (!line.isEmpty()){
				put(line.split(",").map {nikkud_name->
					val nikkud_name_sans_whitespace=nikkud_name.filterNot {it.isWhitespace()};
					//println(nikkud_name_sans_whitespace);
					if (nikkud_name_sans_whitespace.isEmpty()){
						exitWithError("רצף פסיקים בקובץ צירופים_רצויים או פסיק בסוף שורה");
					} else {
						TAAM.byNikkudName(nikkud_name_sans_whitespace)?:
						exitWithError(" אין ניקוד בשם" + nikkud_name);
					}
				}.toSet(), mutableSetOf<String>());
			}
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
			val nikkud : Set<TAAM> = TAAM.values().filter {taam->
				taam.nikkud_regex.find(word)!=null
			}.toSet();
		    map_of_words.get(nikkud)?.add(word);
	    }
    }

    val output_file = File(filename+".csv")
	/* Write the names of the nikkud combinations into the CSV */
    output_file.writeText("\ufeff"+map_of_words.keys.
	    map {nikkud_set->"\""+
			nikkud_set.
			map {it.nikkud_name_display}.
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
