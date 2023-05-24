package com.redis.om.skeleton.controllers

import com.redis.om.skeleton.models.Person
import com.redis.om.skeleton.services.PeopleService
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v2/people")
class PeopleControllerV2(val service: PeopleService) {

    @GetMapping("age_between")
    fun byAgeBetween(
        @RequestParam("min") min: Int,  //
        @RequestParam("max") max: Int
    ): Iterable<Person> {
        return service.findByAgeBetween(min, max)
    }

    @GetMapping("name")
    fun byFirstNameAndLastName(
        @RequestParam("first") firstName: String?,  //
        @RequestParam("last") lastName: String?
    ): Iterable<Person?>? {
        return service.findByFirstNameAndLastName(firstName, lastName)
    }

    @GetMapping("homeloc")
    fun byHomeLoc( //
        @RequestParam("lat") lat: Double,  //
        @RequestParam("lon") lon: Double,  //
        @RequestParam("d") distance: Double
    ): Iterable<Person?>? {
        return service.findByHomeLoc(Point(lon, lat), Distance(distance, Metrics.MILES))
    }

    @GetMapping("statement/{q}")
    fun byPersonalStatement(@PathVariable("q") q: String): Iterable<Person?>? {
        return service.searchByPersonalStatement(q)
    }
}
