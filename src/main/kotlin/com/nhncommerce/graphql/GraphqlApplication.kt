package com.nhncommerce.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.*
import com.nhncommerce.graphql.DgsConstants.MEMBER
import com.nhncommerce.graphql.DgsConstants.MEMBER.Teams
import com.nhncommerce.graphql.DgsConstants.QUERY
import com.nhncommerce.graphql.DgsConstants.QUERY.FindMembers
import com.nhncommerce.graphql.DgsConstants.QUERY_TYPE
import com.nhncommerce.graphql.client.FindMemberByIdProjectionRoot
import com.nhncommerce.graphql.types.Company
import com.nhncommerce.graphql.types.Member
import com.nhncommerce.graphql.types.Team
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RouterFunctions
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.util.function.Consumer

@SpringBootApplication
class GraphqlApplication


fun main(args: Array<String>) {
    runApplication<GraphqlApplication>(*args)
}

@DgsComponent
class MembersDataFetcher {

    val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @DgsQuery
    fun fetchOne(@InputArgument id: Int?): Member {
        val member = Member(id.toString(), "this is graphQL", Company.COMMERCE, listOf(), OffsetDateTime.now())

        log.debug(member.toString())

        return member
    }

    @DgsData(parentType = QUERY_TYPE, field = FindMembers)
    fun fetchAll(): List<Member> {
        return listOf(Member("1", "graphQL", Company.COMMERCE, listOf(), OffsetDateTime.now()),
            Member("2", "restful", Company.ACCOMMATE, listOf(), OffsetDateTime.now()),
            Member("3", "websocket", Company.WETOO, listOf(), OffsetDateTime.now()))
    }

    @DgsData(parentType = MEMBER.TYPE_NAME, field = Teams)
    fun findMembers(dfe: DgsDataFetchingEnvironment): List<Team> {
        val member = dfe.getSource<Member>()

        log.debug(member.id)

        return listOf(
            Team("1", "backoffice", OffsetDateTime.now()),
            Team("2", "BE", OffsetDateTime.now()),
            Team("3", "FE", OffsetDateTime.now())
        )
    }

    @DgsData.List(
        DgsData(parentType = QUERY_TYPE, field = FindMembers),
        DgsData(parentType = QUERY_TYPE, field = FindMembers)
    )
    fun aa() {

    }

//	@DgsSubscription
//	fun createMember() {
//
//	}
}
