package com.redis.om.skeleton.controllers

import com.redis.om.skeleton.models.Person
import com.redis.om.skeleton.repositories.PeopleRepository
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/people")
class PeopleControllerV1(val repo: PeopleRepository) {

    @GetMapping("age_between")
    fun byAgeBetween( //
        @RequestParam("min") min: Int,  //
        @RequestParam("max") max: Int
    ): Iterable<Person?>? {
        return repo.findByAgeBetween(min, max)
    }

    @GetMapping("homeloc")
    fun byHomeLoc( //
        @RequestParam("lat") lat: Double,  //
        @RequestParam("lon") lon: Double,  //
        @RequestParam("d") distance: Double
    ): Iterable<Person> {
        return repo.findByHomeLoc(Point(lon, lat), Distance(distance, Metrics.MILES))
    }

    @GetMapping("name")
    fun byFirstNameAndLastName(
        @RequestParam("first") firstName: String,  //
        @RequestParam("last") lastName: String
    ): Iterable<Person> {
        return repo.findByFirstNameAndLastName(firstName, lastName)
    }

    @GetMapping("statement")
    fun byPersonalStatement(@RequestParam("q") q: String): Iterable<Person> {
        return repo.searchByPersonalStatement(q)
    }

    @PostMapping("new")
    fun create(@RequestBody newPerson: Person): Person {
        return repo.save(newPerson)
    }

    @GetMapping("{id}")
    fun byId(@PathVariable id: String): Optional<Person> {
        return repo.findById(id)
    }

    @PutMapping("{id}")
    fun update(@RequestBody newPerson: Person, @PathVariable id: String): Person {
        return repo.findById(id).map { person ->
            person.firstName = newPerson.firstName
            person.lastName = newPerson.lastName
            person.age = newPerson.age
            person.address = newPerson.address
            person.homeLoc = newPerson.homeLoc
            person.personalStatement = newPerson.personalStatement
            repo.save(person)
        }.orElseGet { repo.save(newPerson) }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: String) {
        repo.deleteById(id)
    }

    @GetMapping("city")
    fun byCity(@RequestParam("city") city: String): Iterable<Person?>? {
        return repo.findByAddress_City(city)
    }

    @GetMapping("city_state")
    fun byCityAndState(
        @RequestParam("city") city: String,  //
        @RequestParam("state") state: String
    ): Iterable<Person?>? {
        return repo.findByAddress_CityAndAddress_State(city, state)
    }

    @GetMapping("skills")
    fun byAnySkills(@RequestParam("skills") skills: Set<String>): Iterable<Person?>? {
        return repo.findBySkills(skills)
    }

    @GetMapping("skills/all")
    fun byAllSkills(@RequestParam("skills") skills: Set<String>): Iterable<Person?>? {
        return repo.findBySkillsContainingAll(skills)
    }

    @GetMapping("search/{q}")
    fun fullTextSearch(@PathVariable("q") q: String?): Iterable<Person?>? {
        return repo.search(q)
    }
}
