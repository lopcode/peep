package peep

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.FileReader
import java.text.DecimalFormat

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

    val topCount = 10
    println("top $topCount uncategorised:")
    normalisedRows.entries
        .sortedByDescending { it.value }
        .take(topCount)
        .forEach {
            println("  ${it.key} - ${it.value}")
        }

    val normalisedRowsToKnownCategories = normalisedRows
        .mapValues { (key, _) ->
            findKnownCategories(key)
        }
    val categoryCounts = mutableMapOf<String, Int>()
    for ((normalisedRow, knownCategories) in normalisedRowsToKnownCategories) {
        val normalisedRowEntryCount = normalisedRows[normalisedRow] ?: 0
        if (knownCategories.isEmpty()) {
            categoryCounts[normalisedRow] = (categoryCounts[normalisedRow] ?: 0) + normalisedRowEntryCount
            continue
        }
        for (category in knownCategories) {
            categoryCounts[category] = (categoryCounts[category] ?: 0) + normalisedRowEntryCount
        }
    }

    println("top $topCount, accounting for categories and their aliases:")
    categoryCounts.entries
        .sortedByDescending { it.value }
        .take(topCount)
        .forEach {
            println("  ${it.key} - ${it.value}")
        }

    val uniquesReducedBy = (rawCount - normalisedRows.keys.size).coerceAtLeast(0)
    val entryCount = normalisedRows.values.sum()
    val entryCategoryCount = categoryCounts.values.sum()
    val averageEntryCategoryCount = entryCategoryCount.toDouble() / entryCount.toDouble()
    val formattedAverageEntryCategoryCount = DecimalFormat("#.00").format(averageEntryCategoryCount)
    println("normalised stats:")
    println("  ${normalisedRows.keys.size} uniques (reduced by $uniquesReducedBy)")
    println("  summing to $entryCount entries")
    println("  with $entryCategoryCount category tags ($formattedAverageEntryCategoryCount average categories per entry)")
}

private val whitespacePattern = """\s+""".toRegex()
private val punctuationPattern = """(\p{Punct})+""".toRegex()
private fun normalise(rawEntries: List<SpeciesSchema>): Map<String, Int> {
    val map = mutableMapOf<String, Int>()
    for (rawEntry in rawEntries) {
        val normalisedKey = rawEntry.input
            .replace(punctuationPattern, " ")
            .replace(whitespacePattern, " ")
            .trim()
            .lowercase()
        if (normalisedKey.isBlank()) {
            continue
        }
        val count = map[normalisedKey] ?: 0
        map[normalisedKey] = count + rawEntry.count
    }
    return map
}

val aliasMap = mapOf(
    "rabbit" to listOf("rabbit", "bunny", "buny", "bun", "hare"),
    "fox" to listOf("fox", "red fox", "arctic fox", "snowfox", "fonx", "fomx", "kitsune", "fennec", "fennec fox", "folf", "fusky", "catfox"),
    "coyote" to listOf("coyote", "yote"),
    "hyena" to listOf("hyena", "yena", "yeen", "yeena"),
    "cat" to listOf("cat", "kitten", "housecat", "tabby", "catfox", "maine coon", "ocelot", "catboy", "catgirl", "mew"),
    "wolf" to listOf("wolf", "folf", "wusky", "werewolf"),
    "dog" to listOf("dog", "husky", "border collie", "collie", "shep", "gshep", "shepherd", "shiba", "doberman", "dobe", "skulldog", "wusky", "corgi", "pup", "puppy", "doggo", "mutt", "malamute", "lab", "labrador", "akita inu", "pomeranian", "cocker spaniel"),
    "deer" to listOf("deer", "bleater"),
    "raccoon" to listOf("raccoon", "trashpanda"),
    "panda" to listOf("panda", "red panda"),
    "snow leopard" to listOf("snow leopard", "snep"),
    "horse" to listOf("horse", "pegasus pony"),
    "possum" to listOf("possum", "opossum"),
    "cheetah" to listOf("cheetah", "chee"),
    "mouse" to listOf("mouse", "squeaker"),
    "otter" to listOf("otter", "ott", "river otter", "awtter", "eurasian river otter"),
    "bear" to listOf("bear", "polar bear", "brown bear", "black bear"),
    "bird" to listOf("bird", "crow", "avian", "cockatiel", "vulture", "falcon", "owl", "cockatoo"),
    "pokémon" to listOf("pokémon", "pokemon", "umbreon", "lucario", "arcanine", "vaporeon", "riolu", "flareon", "buizel"),
    "human" to listOf("human", "catboy", "catgirl"),
    // single-entries are also more leniently searched for
    "dragon" to listOf("dragon"),
    "shark" to listOf("shark"),
    "goat" to listOf("goat"),
    "tiger" to listOf("tiger"),
    "kobold" to listOf("kobold"),
    "bull" to listOf("bull"),
    "bat" to listOf("bat")
)

private fun findKnownCategories(normalisedRow: String): List<String> {
    val occurrences = mutableListOf<String>()
    for ((category, matchers) in aliasMap) {
        for (matcher in matchers) {
            if (normalisedRow.startsWith(matcher) || normalisedRow.contains(" $matcher")) {
                if (!occurrences.contains(category)) {
                    occurrences.add(category)
                }
            }
        }
    }
    return occurrences
}
