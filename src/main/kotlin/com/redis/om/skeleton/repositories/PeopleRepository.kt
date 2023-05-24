package com.redis.om.skeleton.repositories

import com.redis.om.skeleton.models.Person
import com.redis.om.spring.repository.RedisDocumentRepository
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point

interface PeopleRepository : RedisDocumentRepository<Person, String> {
    // Find people by age range
    fun findByAgeBetween(minAge: Int, maxAge: Int): Iterable<Person>

    // Draws a circular geo-filter around a spot and returns all people in that radius
    fun findByHomeLoc(point: Point, distance: Distance): Iterable<Person>

    // Find people by their first and last name
    fun findByFirstNameAndLastName(firstName: String, lastName: String): Iterable<Person>

    // Performs full text search on a person's personal Statement
    fun searchByPersonalStatement(text: String): Iterable<Person>

    // Performing a tag search on city
    fun findByAddress_City(city: String): Iterable<Person>

    // Performing a full-search on street
    fun findByAddress_CityAndAddress_State(city: String, state: String): Iterable<Person>

    // Search Persons that have one of multiple skills (OR condition)
    fun findBySkills(skills: Set<String>): Iterable<Person>

    // Search Persons that have all the skills (AND condition):
    fun findBySkillsContainingAll(skills: Set<String>): Iterable<Person>

    // Performing a text search on all text fields:
    fun search(text: String?): Iterable<Person>
}