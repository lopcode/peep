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

    val values = FileReader(filename).use {
        reader
            .readValues<SpeciesSchema>(it)
            .readAll()
    }
    println("read ${values.size} rows")
}
