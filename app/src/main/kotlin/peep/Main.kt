package peep

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.FileReader

fun main(args: Array<String>) {
    println("hello, peep")

    val filename = args.firstOrNull()
        ?: "data.csv"
    println("loading data from file: \"${filename}\"")

    val mapper = CsvMapper().apply {
        registerKotlinModule()
    }
    val headerSchema = CsvSchema.emptySchema().withHeader()
    val reader = mapper.readerFor(SpeciesSchema::class.java).with(headerSchema)

    val entries = FileReader(filename).use {
        reader
            .readValues<SpeciesSchema>(it)
            .readAll()
    }
    val rawCount = entries.size.coerceAtLeast(0)
    println("read $rawCount rows (not including a header)")

    val normalisedRows = normalise(entries)
    val uniquesReducedBy = (rawCount - normalisedRows.keys.size).coerceAtLeast(0)
    println("normalised stats:")
    println("  ${normalisedRows.keys.size} uniques (reduced by $uniquesReducedBy)")

    val entryCount = normalisedRows.values.sum()
    println("  summing to $entryCount entries")

    println("top 10:")
    normalisedRows.entries
        .sortedByDescending { it.value }
        .take(10)
        .forEach {
            println("  ${it.key} - ${it.value}")
        }
}

private val whitespacePattern = """\s+""".toRegex()
private val punctuationPattern = """(\p{Punct})+""".toRegex()
private fun normalise(rawEntries: List<SpeciesSchema>): Map<String, Int> {
    val map = mutableMapOf<String, Int>()
    for (rawEntry in rawEntries) {
        val normalisedKey = rawEntry.input
            .trim()
            .replace(whitespacePattern, " ")
            .replace(punctuationPattern, "")
            .lowercase()
        if (normalisedKey.isBlank()) {
            continue
        }
        val count = map[normalisedKey] ?: 0
        map[normalisedKey] = count + rawEntry.count
    }
    return map
}
