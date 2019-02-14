package net.ylophones.kotlin

import java.util.*

// don't need semi-colons

fun main(args: Array<String>) {
    println(max(1, 2))
    mapLoop()
}

// fun with expression body
fun max(a: Int, b: Int): Int = if (a > b) a else b

// fun with statement body
fun max2(a: Int, b: Int): Int {
    return if (a > b) a else b
}

// var and val as per scala
fun variable() {
    val x = 1
    var y = 2

    val z: String = ""
}

// string templates
fun string() {
    val num = if (1 > 2) "one" else "woot"
    println("Hello $num")
}

// classes - like case classes
class Person(val name: String)

// class with mutability & default
class Banana(val colour: String, var size: Int = 1)

// custom accessors
class Rectangle(val height: Int = 1, val width: Int = 1) {
    val isSquare: Boolean
        get() = width == height
}

// top level function
fun test() {
    println("ok")
}

// top level property
val VALUE: Int = 0

// enums are nice
enum class Planet(val size: Int, val niceName: String) {
    MERCURY(1, "Mercury"),
    VENUS(2, "Venus"),
    EARTH(3, "Earth");

    fun nameAndSize() = "name " + this.size
}

// when statement
fun handleEnum(planet: Planet) =
        when (planet) {
            Planet.MERCURY -> "m"
            Planet.VENUS, Planet.EARTH -> "v"
        }

// is
interface Animal

open class Mammal : Animal
class Dog : Mammal()
class Cat : Mammal()

val x: Animal = Dog()
fun isExample() {
    if (x is Dog) {
        println("")
    }
}

fun isWhenExample() {
    when (x) {
        is Dog -> "dog"
        else -> "something else "
    }
}

// ranges
val oneToTwenty = 1..20

// maps
fun mapLoop() {
    val myMap = TreeMap<String, Int>()
    myMap["one"] = 1
    myMap["two"] = 2

    for ((key, value) in myMap) {
        println("$key - $value")
    }
}

/*
Collections
 */

val mySet = hashSetOf(1, 2, 3)
val myMap = hashMapOf(1 to "one", 2 to "two")

// extension functions

fun String.lastChar(): Char = this.get(this.length - 1)

fun useExtFunction() {
    println("1234".lastChar())
}

// varargs
fun useVars(vararg strings: String) {
    println(strings[0])
}

// explicit constructor
class Secret private constructor() {
    val X = 1
}

// nullable
fun checkNUl() {
    var nullString: String? = null
    val size = nullString?.length ?: 0

    val notNull = nullString!!

    var str: String = "wibble"
    //str = null

    nullString?.let { println(nullString) }
}

