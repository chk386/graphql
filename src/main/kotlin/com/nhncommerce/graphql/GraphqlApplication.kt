package com.nhncommerce.graphql

import com.netflix.graphql.dgs.*
import com.nhncommerce.graphql.client.FindMemberByIdProjectionRoot
import com.nhncommerce.graphql.types.Company
import com.nhncommerce.graphql.types.Member
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

@SpringBootApplication
class GraphqlApplication

fun main(args: Array<String>) {
	runApplication<GraphqlApplication>(*args)
}

@DgsComponent
class MembersDataFetcher {

//	@DgsData(parentType = DgsConstants.QUERY_TYPE, field = DgsConstants.QUERY.FindMemberById)
	@DgsQuery
	// @RequestHeader
	// @RequestParam
	// @InputArgument(collectionType = Member.class) members: List<Member>
	fun findMemberById(@InputArgument id: Int?): Member {
		return Member(id.toString(), "this is graphQL", Company.COMMERCE, listOf(), OffsetDateTime.now())
	}

	@DgsSubscription
	fun createMember() {

	}
}
