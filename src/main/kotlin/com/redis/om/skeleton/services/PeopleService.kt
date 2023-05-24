package com.redis.om.skeleton.services

import com.redis.om.skeleton.models.Person
import com.redis.om.skeleton.models.`Person$`
import com.redis.om.spring.search.stream.EntityStream
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import org.springframework.stereotype.Service
import redis.clients.jedis.search.aggr.SortedField
import java.util.stream.Collectors

@Service
class PeopleService(val entityStream: EntityStream) {

    // Find people by age range
    fun findByAgeBetween(minAge: Int, maxAge: Int): Iterable<Person> {
        return entityStream //
            .of(Person::class.java) //
            .filter(`Person$`.AGE.between(minAge, maxAge)) //
            .sorted(`Person$`.AGE, SortedField.SortOrder.ASC) //
            .collect(Collectors.toList())
    }

    // Find people by their first and last name
    fun findByFirstNameAndLastName(firstName: String?, lastName: String?): Iterable<Person> {
        return entityStream //
            .of(Person::class.java) //
            .filter(`Person$`.FIRST_NAME.eq(firstName)) //
            .filter(`Person$`.LAST_NAME.eq(lastName)) //
            .collect(Collectors.toList())
    }

    fun findByHomeLoc( //
        point: Point, distance: Distance
    ): Iterable<Person> {
        return entityStream //
            .of(Person::class.java) //
            .filter(`Person$`.HOME_LOC.near(point, distance)) //
            .collect(Collectors.toList())
    }

    // Performs full text search on a person's personal Statement
    fun searchByPersonalStatement(text: String): Iterable<Person> {
        return entityStream //
            .of(Person::class.java) //
            .filter(`Person$`.PERSONAL_STATEMENT.eq(text)) //
            .collect(Collectors.toList())
    }
}
