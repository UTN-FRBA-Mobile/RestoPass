package com.example.restopass.domain

data class Restaurant(
    val restaurantId: String,
    val name: String,
    val img: String,
    val address: String,
    val location: Location,
    val tags: List<String>,
    val timeTable: List<TimeTable>,
    val dishes: List<Dish>,
    val stars: Double
)

data class TimeTable(val openingDays: List<String>, val pairHours: List<PairHour>?)

data class Dish(val name: String, val description: String, val topMembership: MembershipType, val stars: Double)

data class PairHour(val  openingHour: Int, val  openingMinute: Int, val  closingHour: Int, val  closingMinute: Int)

data class Tags(val memberships: List<String>, val tags: Map<String, List<String>>) //tags are title to tags

data class Location(val x: Double, val y: Double)